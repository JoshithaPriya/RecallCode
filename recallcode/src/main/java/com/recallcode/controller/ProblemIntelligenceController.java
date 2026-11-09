
package com.recallcode.controller;

import com.recallcode.model.AnalysisConcept;
import com.recallcode.model.ProblemAnalysis;
import com.recallcode.repository.AnalysisConceptRepository;
import com.recallcode.service.ProblemIntelligenceService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Exposes AI-powered DSA problem analysis through REST endpoints.
@RestController
@RequestMapping("/api/intelligence")
public class ProblemIntelligenceController {

    private final ProblemIntelligenceService intelligenceService;
    private final AnalysisConceptRepository mappingRepository;

    public ProblemIntelligenceController(
            ProblemIntelligenceService intelligenceService,
            AnalysisConceptRepository mappingRepository) {

        this.intelligenceService = intelligenceService;
        this.mappingRepository = mappingRepository;
    }

    @PostMapping("/problems/{problemId}/analyze")
    public ProblemAnalysisResponse analyzeProblem(
            @PathVariable Long problemId) {

        ProblemAnalysis analysis =
                intelligenceService.analyzeProblem(problemId);

        List<ConceptResponse> concepts =
                mappingRepository.findByAnalysisId(analysis.getId())
                        .stream()
                        .map(mapping -> new ConceptResponse(
                                mapping.getConcept().getName(),
                                mapping.getConcept().getSlug(),
                                mapping.getConcept().getConceptType().name(),
                                mapping.getConfidence(),
                                mapping.getImportance(),
                                mapping.getEvidence()
                        ))
                        .toList();

        return new ProblemAnalysisResponse(
                analysis.getId(),
                analysis.getProblem().getId(),
                analysis.getApproachSummary(),
                analysis.getAlternativeApproaches(),
                analysis.getConfidence(),
                analysis.getAnalyzedAt(),
                concepts
        );
    }

    public record ProblemAnalysisResponse(
            Long analysisId,
            Long problemId,
            String approachSummary,
            String alternativeApproaches,
            Double confidence,
            LocalDateTime analyzedAt,
            List<ConceptResponse> concepts
    ) {}

    public record ConceptResponse(
            String name,
            String slug,
            String conceptType,
            Double confidence,
            Integer importance,
            String evidence
    ) {}
}
