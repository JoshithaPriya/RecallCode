
package com.recallcode.service;

// Builds compact, evidence-based assessments while limiting prompt size.
// Prompt construction can be tested without making AI API calls.

import com.recallcode.model.AgentAssessment;
import com.recallcode.model.Attempt;
import com.recallcode.model.Problem;
import com.recallcode.repository.AttemptRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentAssessmentService {

    private static final int MAX_HISTORY = 3;
    private static final int MAX_NOTE_LENGTH = 200;
    private static final int MAX_TAGS = 5;

    private final ChatClient chatClient;
    private final AttemptRepository attemptRepository;

    public AgentAssessmentService(
            ChatClient.Builder builder,
            AttemptRepository attemptRepository) {
        this.chatClient = builder.build();
        this.attemptRepository = attemptRepository;
    }
    @Transactional(readOnly = true)
    public AgentAssessment analyseAttempt(Attempt attempt) {
        Problem problem = attempt.getProblem();

        List<Attempt> allAttempts =
                attemptRepository.findByProblemId(problem.getId());

        String prompt = buildAssessmentPrompt(attempt, allAttempts);

        // This is the only AI call in this service.
        AgentAssessment assessment = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AgentAssessment.class);

        validateAssessment(assessment);
        return assessment;
    }

    /*
     * Creates the compact prompt using only selected problem metadata
     * and a maximum of three previous attempts.
     * Package-private so unit tests can call it without invoking AI.
     */
    public String buildAssessmentPrompt(
            Attempt attempt,
            List<Attempt> allAttempts) {

        Problem problem = attempt.getProblem();

        List<Attempt> previousAttempts = allAttempts == null
                ? List.of()
                : allAttempts.stream()
                        .filter(Objects::nonNull)
                        .filter(previous ->
                                !Objects.equals(
                                        previous.getId(), attempt.getId()))
                        .sorted(Comparator.comparing(
                                Attempt::getAttemptedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder())))
                        .limit(MAX_HISTORY)
                        .toList();

        String historyText = previousAttempts.isEmpty()
                ? "No previous attempts."
                : previousAttempts.stream()
                        .map(previous ->
                                "Outcome: " + previous.getOutcome()
                                + "; Time: "
                                + previous.getTimeTakenMinutes()
                                + " minutes; Notes: "
                                + shorten(previous.getNotes())
                                + "\n")
                        .collect(Collectors.joining());

        String difficulty = problem.getDifficulty() == null
                ? "Unknown"
                : problem.getDifficulty();

        String tags = problem.getTags() == null
                ? "Not available"
                : problem.getTags().stream()
                        .filter(Objects::nonNull)
                        .limit(MAX_TAGS)
                        .collect(Collectors.joining(", "));

        if (tags.isBlank()) {
            tags = "Not available";
        }

        return """
                You assess learning progress for RecallCode, a DSA revision app.

                Evaluate only the CURRENT attempt using the supplied evidence.
                Use previous attempts only to identify learning trends.
                Do not invent facts or assume SOLVED proves independent recall.

                Problem: %s
                Difficulty: %s
                Topics: %s

                Current outcome: %s
                Current time: %d minutes
                Current notes: %s

                Recent previous attempts:
                %s

                Choose exactly one recall quality:
                FORGOT, HARD, GOOD, EASY.

                FORGOT: unable to recall the solution.
                HARD: substantial difficulty or assistance was needed.
                GOOD: successful recall with some effort.
                EASY: confident, independent recall.

                If evidence is insufficient, state uncertainty.
                Confidence must be between 0.0 and 1.0.
                Explain the evidence behind your assessment.
                Keep the explanation concise and evidence-based.
                """.formatted(
                problem.getTitle(),
                difficulty,
                tags,
                attempt.getOutcome(),
                attempt.getTimeTakenMinutes(),
                shorten(attempt.getNotes()),
                historyText
        );
    }

    // Caps note content at 200 characters; null or blank notes stay explicit.
    private String shorten(String value) {
        if (value == null || value.isBlank()) {
            return "No notes.";
        }

        String cleaned = value.trim();

        return cleaned.length() <= MAX_NOTE_LENGTH
                ? cleaned
                : cleaned.substring(0, MAX_NOTE_LENGTH);
    }

    /*
     * Validates the model response before RecallCode uses it.
     * Rejects missing fields, invalid recall labels and invalid confidence.
     */
    private void validateAssessment(AgentAssessment assessment) {
        if (assessment == null) {
            throw new IllegalStateException("AI returned no assessment");
        }

        if (!List.of("FORGOT", "HARD", "GOOD", "EASY")
                .contains(assessment.getRecallQuality())) {
            throw new IllegalStateException("Invalid AI recall quality");
        }

        if (assessment.getUnderstandingLevel() == null
                || assessment.getUnderstandingLevel().isBlank()) {
            throw new IllegalStateException(
                    "AI assessment is missing understanding level");
        }

        if (assessment.getEvidence() == null
                || assessment.getEvidence().isBlank()) {
            throw new IllegalStateException(
                    "AI assessment is missing supporting evidence");
        }

        if (!Double.isFinite(assessment.getConfidence())
                || assessment.getConfidence() < 0.0
                || assessment.getConfidence() > 1.0) {
            throw new IllegalStateException(
                    "AI confidence must be between 0.0 and 1.0");
        }
    }
}
