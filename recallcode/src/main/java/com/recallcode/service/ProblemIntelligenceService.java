
package com.recallcode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recallcode.model.*;
import com.recallcode.repository.*;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

// Identifies algorithmic techniques in problems and caches reusable AI analysis.
@Service
public class ProblemIntelligenceService {

    private static final String PROMPT_VERSION = "1.1";
    private static final String MODEL_NAME = "openrouter/free";

    private final ChatClient chatClient;
    private final ProblemRepository problemRepository;
    private final ProblemAnalysisRepository analysisRepository;
    private final DsaConceptRepository conceptRepository;
    private final AnalysisConceptRepository mappingRepository;

    public ProblemIntelligenceService(
        ChatClient.Builder chatClientBuilder,
        ProblemRepository problemRepository,
        ProblemAnalysisRepository analysisRepository,
        DsaConceptRepository conceptRepository,
        AnalysisConceptRepository mappingRepository
) {
    this.chatClient = chatClientBuilder.build();
    this.problemRepository = problemRepository;
    this.analysisRepository = analysisRepository;
    this.conceptRepository = conceptRepository;
    this.mappingRepository = mappingRepository;
}

    @Transactional
    public ProblemAnalysis analyzeProblem(Long problemId) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Problem not found: " + problemId
                ));

        String problemContent = buildProblemContent(problem);
        String contentHash = generateHash(problemContent);

      var cachedAnalysis =
        analysisRepository
                .findFirstByProblemIdAndContentHashAndPromptVersionOrderByAnalyzedAtDesc(
                        problemId,
                        contentHash,
                        PROMPT_VERSION
                );

        if (cachedAnalysis.isPresent()) {
            return cachedAnalysis.get();
        }

        String prompt = buildPrompt(problemContent);

        ProblemAnalysisResult result = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(ProblemAnalysisResult.class);

        validateResult(result);

        ProblemAnalysis analysis = new ProblemAnalysis(
                problem,
                MODEL_NAME,
                PROMPT_VERSION,
                contentHash,
                result.getApproachSummary(),
                result.getAlternativeApproaches(),
                result.getConfidence()
        );

        analysis = analysisRepository.save(analysis);

        List<AnalysisConcept> mappings = new ArrayList<>();

        for (ProblemAnalysisResult.ConceptMatch match :
                result.getConcepts()) {

            var concept = conceptRepository.findBySlug(match.getSlug());

            if (concept.isEmpty()) {
                continue;
            }

            mappings.add(new AnalysisConcept(
                    analysis,
                    concept.get(),
                    match.getConfidence(),
                    match.getImportance(),
                    match.getEvidence()
            ));
        }

        if (mappings.isEmpty()) {
            throw new IllegalStateException(
                    "AI did not identify any valid DSA concepts."
            );
        }

        mappingRepository.saveAll(mappings);

        return analysis;
    }

    private String buildProblemContent(Problem problem) {

        return """
                Title: %s

                Difficulty: %s

                Description:
                %s

                Constraints:
                %s
                """.formatted(
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getDescription(),
                problem.getConstraintsText()
        );
    }

    private String generateHash(String content) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    content.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to generate problem content hash.",
                    exception
            );
        }
    }

    private String buildPrompt(String problemContent) {
       String catalogue = conceptRepository.findAll()
        .stream()
        .map(concept -> String.format(
                "Slug: %s | Name: %s | Type: %s | Description: %s",
                concept.getSlug(),
                concept.getName(),
                concept.getConceptType().name(),
                concept.getDescription() == null
                        ? ""
                        : concept.getDescription()
        ))
        .collect(java.util.stream.Collectors.joining("\n"));

        return """
        You are an expert Data Structures and Algorithms analyst.

        Your task is to identify the algorithmic concepts genuinely
        involved in solving the given programming problem.

        PROBLEM:
        %s

        AVAILABLE DSA CONCEPTS:
        %s

        ANALYSIS RULES:

        1. Analyze the problem statement, examples, and constraints
           before identifying its algorithmic techniques.

        2. Identify the actual techniques required for an efficient
           and appropriate solution, not merely broad categories.

        3. Select concepts ONLY from the supplied catalogue.
           Use their exact slugs.

        4. Distinguish between:
           - Primary concepts: central to the solution.
           - Secondary concepts: meaningfully support the solution.

        5. When a specific technique belongs to a broader pattern,
           include the parent pattern if it is also genuinely relevant.
           For example, Directional Simulation may be accompanied
           by Simulation when both accurately describe the approach.

        6. Do not automatically include every ancestor concept.
           Include only concepts that meaningfully describe the
           problem-solving approach.

        7. Do not identify a technique merely because it could
           theoretically be applied to the problem.

        8. Alternative approaches must be genuinely valid algorithmic
           approaches for this particular problem. Do not list unrelated
           techniques or speculative possibilities.

        9. If there is no meaningful alternative approach, return
           an empty string for alternativeApproaches.

        10. Provide specific evidence from the problem statement,
            examples, constraints, or solution requirements for
            every selected concept.

        11. Assign importance as follows:
            - 5: Essential primary technique.
            - 4: Important primary or supporting technique.
            - 3: Meaningful secondary technique.
            - 2: Minor supporting concept.
            - 1: Weak but relevant connection.

        12. Return confidence values between 0 and 1.
            Lower confidence when the evidence is uncertain.

        13. Do not invent concepts, slugs, or evidence.

        14. Prefer accurate classification over returning more concepts.
            Do not add irrelevant concepts just to increase the count.

        Return a structured response matching this schema:

        {
          "approachSummary": "Main algorithmic approach",
          "alternativeApproaches": "Genuinely viable alternatives, or empty string",
          "confidence": 0.95,
          "concepts": [
            {
              "slug": "exact-catalogue-slug",
              "confidence": 0.95,
              "importance": 5,
              "evidence": "Specific reason this concept applies"
            }
          ]
        }
        """.formatted(problemContent, catalogue);
    }

    private void validateResult(ProblemAnalysisResult result) {

        if (result == null) {
            throw new IllegalStateException(
                    "AI returned an empty analysis."
            );
        }

        if (result.getApproachSummary() == null
                || result.getApproachSummary().isBlank()) {
            throw new IllegalStateException(
                    "AI analysis is missing its approach summary."
            );
        }

        if (result.getConfidence() == null
                || result.getConfidence() < 0
                || result.getConfidence() > 1) {
            throw new IllegalStateException(
                    "AI returned invalid overall confidence."
            );
        }

        if (result.getConcepts() == null
                || result.getConcepts().isEmpty()) {
            throw new IllegalStateException(
                    "AI analysis contains no DSA concepts."
            );
        }

        for (ProblemAnalysisResult.ConceptMatch match :
                result.getConcepts()) {

            if (match.getSlug() == null || match.getSlug().isBlank()
                    || match.getConfidence() == null
                    || match.getConfidence() < 0
                    || match.getConfidence() > 1
                    || match.getImportance() == null
                    || match.getImportance() < 1
                    || match.getImportance() > 5
                    || match.getEvidence() == null
                    || match.getEvidence().isBlank()) {

                throw new IllegalStateException(
                        "AI returned an invalid concept mapping."
                );
            }
        }
    }
}
