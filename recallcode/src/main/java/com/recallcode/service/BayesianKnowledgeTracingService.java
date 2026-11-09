
package com.recallcode.service;

import com.recallcode.model.AnalysisConcept;
import com.recallcode.model.ProblemAnalysis;
import com.recallcode.model.User;
import com.recallcode.model.UserConceptMastery;
import com.recallcode.repository.AnalysisConceptRepository;
import com.recallcode.repository.ProblemAnalysisRepository;
import com.recallcode.repository.UserConceptMasteryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 * BayesianKnowledgeTracingService.java
 *
 * Implements Bayesian Knowledge Tracing (BKT) to estimate
 * a user's mastery probability for individual DSA concepts.
 *
 * Mastery is updated using the user's practice outcome and
 * the concepts associated with the attempted problem.
 */

@Service
public class BayesianKnowledgeTracingService {

    private static final double INITIAL_MASTERY = 0.10;
    private static final double LEARNING_PROBABILITY = 0.20;
    private static final double GUESS_PROBABILITY = 0.20;
    private static final double SLIP_PROBABILITY = 0.10;

    private final UserConceptMasteryRepository masteryRepository;
    private final ProblemAnalysisRepository analysisRepository;
    private final AnalysisConceptRepository analysisConceptRepository;

    public BayesianKnowledgeTracingService(
            UserConceptMasteryRepository masteryRepository,
            ProblemAnalysisRepository analysisRepository,
            AnalysisConceptRepository analysisConceptRepository) {

        this.masteryRepository = masteryRepository;
        this.analysisRepository = analysisRepository;
        this.analysisConceptRepository = analysisConceptRepository;
    }

    @Transactional
    public void updateMastery(
            User user,
            Long problemId,
            boolean correct) {

        ProblemAnalysis analysis = analysisRepository
                .findFirstByProblemIdOrderByAnalyzedAtDesc(problemId)
                .orElse(null);

        if (analysis == null) {
            return;
        }

        List<AnalysisConcept> mappings =
                analysisConceptRepository.findByAnalysisId(
                        analysis.getId()
                );

        for (AnalysisConcept mapping : mappings) {

            var concept = mapping.getConcept();
            
             if (concept.getConceptType() == null
            || concept.getConceptType().name().equals("DOMAIN")) {
        continue;
    }
    
            UserConceptMastery mastery = masteryRepository
                    .findByUserIdAndConceptId(
                            user.getId(),
                            concept.getId()
                    )
                    .orElseGet(() -> createInitialMastery(user, concept));

            double updatedProbability = calculateMastery(
                    mastery.getMasteryProbability(),
                    correct
            );

            mastery.setMasteryProbability(updatedProbability);

            if (correct) {
                mastery.setCorrectAttempts(
                        mastery.getCorrectAttempts() + 1
                );
            } else {
                mastery.setIncorrectAttempts(
                        mastery.getIncorrectAttempts() + 1
                );
            }

            mastery.setLastUpdatedAt(LocalDateTime.now());

            masteryRepository.save(mastery);
        }
    }

    private UserConceptMastery createInitialMastery(
            User user,
            com.recallcode.model.DsaConcept concept) {

        UserConceptMastery mastery = new UserConceptMastery();

        mastery.setUser(user);
        mastery.setConcept(concept);
        mastery.setMasteryProbability(INITIAL_MASTERY);

        return mastery;
    }

    public double calculateMastery(
            double previousMastery,
            boolean correct) {

        double posterior;

        if (correct) {

            posterior =
                    (previousMastery * (1 - SLIP_PROBABILITY))
                    /
                    (
                        previousMastery * (1 - SLIP_PROBABILITY)
                        +
                        (1 - previousMastery) * GUESS_PROBABILITY
                    );

        } else {

            posterior =
                    (previousMastery * SLIP_PROBABILITY)
                    /
                    (
                        previousMastery * SLIP_PROBABILITY
                        +
                        (1 - previousMastery) * (1 - GUESS_PROBABILITY)
                    );
        }

        double updatedMastery =
                posterior
                + (1 - posterior) * LEARNING_PROBABILITY;

        return Math.max(0.0, Math.min(1.0, updatedMastery));
    }
}
