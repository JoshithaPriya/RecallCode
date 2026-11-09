
package com.recallcode.service;

import com.recallcode.model.AnalysisConcept;
import com.recallcode.model.Attempt;
import com.recallcode.model.DsaConcept;
import com.recallcode.model.UserConceptMastery;
import com.recallcode.model.UserProblemProgress;
import com.recallcode.repository.AnalysisConceptRepository;
import com.recallcode.repository.AttemptRepository;
import com.recallcode.repository.UserConceptMasteryRepository;
import com.recallcode.repository.UserProblemProgressRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



/*
 * RecommendationService.java
 *
 * Generates and ranks personalized DSA recommendations.
 *
 * Recommendation categories:
 * 1. DUE_REVISION - Previously solved problems due for revision.
 * 2. WEAK_CONCEPT - Problems associated with weak DSA concepts.
 * 3. TRANSFER - Unseen problems using previously learned techniques.
 *
 * All recommendations are generated using stored database data.
 * No LLM request is made when generating the revision queue.
 */
@Service
public class RecommendationService {

    private static final double WEAK_MASTERY_THRESHOLD = 0.60;
    private static final double TRANSFER_MASTERY_THRESHOLD = 0.60;

    private final RevisionPriorityService priorityService;
    private final UserProblemProgressRepository progressRepository;
    private final AttemptRepository attemptRepository;
    private final AnalysisConceptRepository analysisConceptRepository;
    private final UserConceptMasteryRepository masteryRepository;

    public RecommendationService(
            RevisionPriorityService priorityService,
            UserProblemProgressRepository progressRepository,
            AttemptRepository attemptRepository,
            AnalysisConceptRepository analysisConceptRepository,
            UserConceptMasteryRepository masteryRepository) {

        this.priorityService = priorityService;
        this.progressRepository = progressRepository;
        this.attemptRepository = attemptRepository;
        this.analysisConceptRepository = analysisConceptRepository;
        this.masteryRepository = masteryRepository;
    }

    /*
     * Generates the complete recommendation list for one user.
     */
    @Transactional(readOnly = true)
    public List<RecommendationCandidate> generateRecommendations(Long userId) {

        LocalDateTime now = LocalDateTime.now();

        Map<Long, RecommendationCandidate> candidates =
                new LinkedHashMap<>();

        List<Attempt> attempts = attemptRepository.findByUserId(userId);

        Set<Long> attemptedProblemIds = new HashSet<>();
        Set<Long> solvedProblemIds = new HashSet<>();

        for (Attempt attempt : attempts) {

            Long problemId = attempt.getProblem().getId();

            attemptedProblemIds.add(problemId);

            if ("SOLVED".equals(attempt.getOutcome())) {
                solvedProblemIds.add(problemId);
            }
        }

        List<UserConceptMastery> masteryRecords =
                masteryRepository.findByUserId(userId);

        Map<Long, Double> masteryByConcept = new HashMap<>();

        for (UserConceptMastery mastery : masteryRecords) {

            Long conceptId = mastery.getConcept().getId();

            masteryByConcept.put(
                    conceptId,
                    mastery.getMasteryProbability()
            );
        }

        // 1. Previously solved problems whose revision is due.
        addDueRevisions(
                userId,
                now,
                solvedProblemIds,
                masteryByConcept,
                candidates
        );

        // 2. Problems associated with concepts having low mastery.
        addWeakConceptRecommendations(
                masteryRecords,
                candidates
        );

        // 3. Unseen problems using previously learned techniques.
        addTransferRecommendations(
                masteryRecords,
                attemptedProblemIds,
                candidates
        );

        return rankRecommendations(
                new ArrayList<>(candidates.values())
        );
    }

    /*
     * Adds previously solved problems whose revision date has arrived.
     */
    private void addDueRevisions(
            Long userId,
            LocalDateTime now,
            Set<Long> solvedProblemIds,
            Map<Long, Double> masteryByConcept,
            Map<Long, RecommendationCandidate> candidates) {

        List<UserProblemProgress> dueProgress =
                progressRepository.findByUserIdAndNextRevisionAtLessThanEqual(
                        userId,
                        now
                );

        for (UserProblemProgress progress : dueProgress) {

            Long problemId = progress.getProblem().getId();

            // Only recommend problems the user has successfully solved.
            if (!solvedProblemIds.contains(problemId)) {
                continue;
            }

            double mastery = getProblemMastery(
                    problemId,
                    masteryByConcept
            );

            RecommendationCandidate candidate =
                    createCandidate(
                            progress.getProblem().getId(),
                            progress.getProblem().getTitle(),
                            "DUE_REVISION",
                            progress.getNextRevisionAt(),
                            mastery,
                            0.0
                    );

            populateProblemDetails(candidate, problemId);

            mergeCandidate(candidates, candidate);
        }
    }

    /*
     * Finds problems associated with concepts having low BKT mastery.
     */
    private void addWeakConceptRecommendations(
            List<UserConceptMastery> masteryRecords,
            Map<Long, RecommendationCandidate> candidates) {

        for (UserConceptMastery mastery : masteryRecords) {

            double probability = mastery.getMasteryProbability();

            if (probability >= WEAK_MASTERY_THRESHOLD) {
                continue;
            }

            // BKT tracks patterns and techniques, not broad domains.
            if (isDomain(mastery)) {
                continue;
            }

            Long conceptId = mastery.getConcept().getId();

            List<AnalysisConcept> mappings =
                    analysisConceptRepository.findByConceptId(conceptId);

            for (AnalysisConcept mapping : mappings) {

                Long problemId =
                        mapping.getAnalysis().getProblem().getId();

                double relevance = calculateRelevance(mapping);

                RecommendationCandidate candidate =
                        createCandidate(
                                problemId,
                                mapping.getAnalysis().getProblem().getTitle(),
                                "WEAK_CONCEPT",
                                null,
                                probability,
                                relevance
                        );
                populateProblemDetails(candidate, problemId);
                mergeCandidate(candidates, candidate);
            }
        }
    }

    /*
     * Finds unseen problems sharing concepts with techniques
     * the learner has already developed reasonable mastery in.
     */
    private void addTransferRecommendations(
            List<UserConceptMastery> masteryRecords,
            Set<Long> attemptedProblemIds,
            Map<Long, RecommendationCandidate> candidates) {

        for (UserConceptMastery mastery : masteryRecords) {

            double probability = mastery.getMasteryProbability();

            if (probability < TRANSFER_MASTERY_THRESHOLD) {
                continue;
            }

            if (isDomain(mastery)) {
                continue;
            }

            Long conceptId = mastery.getConcept().getId();

            List<AnalysisConcept> mappings =
                    analysisConceptRepository.findByConceptId(conceptId);

            for (AnalysisConcept mapping : mappings) {

                Long problemId =
                        mapping.getAnalysis().getProblem().getId();

                // Transfer recommendations must be unseen.
                if (attemptedProblemIds.contains(problemId)) {
                    continue;
                }

                double relevance =
                        calculateRelevance(mapping) * probability;

                RecommendationCandidate candidate =
                        createCandidate(
                                problemId,
                                mapping.getAnalysis().getProblem().getTitle(),
                                "TRANSFER",
                                null,
                                probability,
                                relevance
                        );
                populateProblemDetails(candidate, problemId);
                mergeCandidate(candidates, candidate);
            }
        }
    }

    /*
     * Excludes DOMAIN concepts from recommendation mastery signals.
     */
    private boolean isDomain(UserConceptMastery mastery) {

        return mastery.getConcept().getConceptType() == null
                || mastery.getConcept()
                        .getConceptType()
                        .name()
                        .equals("DOMAIN");
    }

    /*
     * Combines mapping confidence and concept importance.
     */
    private double calculateRelevance(AnalysisConcept mapping) {

        double confidence = mapping.getConfidence() == null
                ? 0.0
                : mapping.getConfidence();

        double importance = mapping.getImportance() == null
                ? 0.0
                : mapping.getImportance() / 5.0;

        confidence = Math.max(0.0, Math.min(1.0, confidence));
        importance = Math.max(0.0, Math.min(1.0, importance));

        return confidence * importance;
    }

    /*
     * Estimates the average mastery of a problem's mapped concepts.
     */
    private double getProblemMastery(
            Long problemId,
            Map<Long, Double> masteryByConcept) {

        List<AnalysisConcept> mappings =
                analysisConceptRepository.findByAnalysisProblemId(problemId);

        Set<Long> countedConcepts = new HashSet<>();

        double total = 0.0;
        int count = 0;

        for (AnalysisConcept mapping : mappings) {

            if (mapping.getConcept().getConceptType() == null
                    || mapping.getConcept().getConceptType()
                            .name().equals("DOMAIN")) {
                continue;
            }

            Long conceptId = mapping.getConcept().getId();

            if (!countedConcepts.add(conceptId)) {
                continue;
            }

            total += masteryByConcept.getOrDefault(conceptId, 0.10);
            count++;
        }

        return count == 0 ? 0.10 : total / count;
    }

    private RecommendationCandidate createCandidate(
            Long problemId,
            String title,
            String type,
            LocalDateTime nextRevisionAt,
            double mastery,
            double transferRelevance) {

        RecommendationCandidate candidate =
                new RecommendationCandidate();

        candidate.setProblemId(problemId);
        candidate.setProblemTitle(title);
        candidate.setRecommendationType(type);
        candidate.setNextRevisionAt(nextRevisionAt);
        candidate.setMasteryProbability(mastery);
        candidate.setTransferRelevance(transferRelevance);


        switch (type) {
    case "DUE_REVISION" ->
            candidate.setReason(
                    "You previously solved this problem and it is due for revision."
            );

    case "WEAK_CONCEPT" ->
            candidate.setReason(
                    "Recommended to strengthen a DSA concept where your mastery is below 60%."
            );

    case "TRANSFER" ->
            candidate.setReason(
                    "Recommended to help you apply a familiar DSA technique to an unseen problem."
            );

    default ->
            candidate.setReason("Recommended based on your learning progress.");
}


        return candidate;
    }

    /*
     * Merges multiple recommendation signals for the same problem.
     * A problem appears only once in the final queue.
     */
    private void mergeCandidate(
            Map<Long, RecommendationCandidate> candidates,
            RecommendationCandidate incoming) {

        RecommendationCandidate existing =
                candidates.get(incoming.getProblemId());

        if (existing == null) {
            candidates.put(incoming.getProblemId(), incoming);
            return;
        }

        existing.setTransferRelevance(
                Math.max(
                        existing.getTransferRelevance(),
                        incoming.getTransferRelevance()
                )
        );

        existing.setMasteryProbability(
                Math.min(
                        existing.getMasteryProbability(),
                        incoming.getMasteryProbability()
                )
        );

        if (incoming.getNextRevisionAt() != null) {
            existing.setNextRevisionAt(incoming.getNextRevisionAt());
        }

        if (getTypePriority(incoming.getRecommendationType())
                < getTypePriority(existing.getRecommendationType())) {

            existing.setRecommendationType(
                    incoming.getRecommendationType()
            );
        }
    }

    private int getTypePriority(String type) {

        return switch (type) {
            case "DUE_REVISION" -> 1;
            case "WEAK_CONCEPT" -> 2;
            case "TRANSFER" -> 3;
            default -> 4;
        };
    }

    /*
     * Calculates priority and sorts recommendations from highest
     * to lowest priority.
     */
    public List<RecommendationCandidate> rankRecommendations(
            List<RecommendationCandidate> candidates) {

        for (RecommendationCandidate candidate : candidates) {

            candidate.setPriorityScore(
                    priorityService.calculatePriority(
                            candidate.getNextRevisionAt(),
                            candidate.getMasteryProbability(),
                            candidate.getTransferRelevance()
                    )
            );
        }

        return candidates.stream()
                .filter(candidate -> candidate.getProblemId() != null)
                .sorted(
                        Comparator.comparingDouble(
                                RecommendationCandidate::getPriorityScore
                        ).reversed()
                )
                .toList();
    }

    /*
     * Internal candidate representation used before creating
     * the API response DTO.
     */
    public static class RecommendationCandidate {

        private Long problemId;
        private String problemTitle;
        private String recommendationType;
        private LocalDateTime nextRevisionAt;
        private double masteryProbability;
        private double transferRelevance;
        private double priorityScore;

        private String reason;
        private String relevantConcept;
        private String difficulty;

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
        }

        public String getProblemTitle() {
            return problemTitle;
        }

        public void setProblemTitle(String problemTitle) {
            this.problemTitle = problemTitle;
        }

        public String getRecommendationType() {
            return recommendationType;
        }

        public void setRecommendationType(String recommendationType) {
            this.recommendationType = recommendationType;
        }

        public LocalDateTime getNextRevisionAt() {
            return nextRevisionAt;
        }

        public void setNextRevisionAt(LocalDateTime nextRevisionAt) {
            this.nextRevisionAt = nextRevisionAt;
        }

        public double getMasteryProbability() {
            return masteryProbability;
        }

        public void setMasteryProbability(double masteryProbability) {
            this.masteryProbability = masteryProbability;
        }

        public double getTransferRelevance() {
            return transferRelevance;
        }

        public void setTransferRelevance(double transferRelevance) {
            this.transferRelevance = transferRelevance;
        }

        public double getPriorityScore() {
            return priorityScore;
        }

        public void setPriorityScore(double priorityScore) {
            this.priorityScore = priorityScore;
        }

        public String getReason() {
    return reason;
}

public void setReason(String reason) {
    this.reason = reason;
}

public String getRelevantConcept() {
    return relevantConcept;
}

public void setRelevantConcept(String relevantConcept) {
    this.relevantConcept = relevantConcept;
}

public String getDifficulty() {
    return difficulty;
}

public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
}
    }


    private String getProblemDifficulty(Long problemId) {
    List<AnalysisConcept> mappings =
            analysisConceptRepository.findByAnalysisProblemId(problemId);

    // Difficulty belongs to Problem, not AnalysisConcept.
    // We'll populate it directly from the Problem entity.
    return mappings.isEmpty()
            ? null
            : mappings.get(0).getAnalysis().getProblem().getDifficulty();
}

private void populateProblemDetails(
        RecommendationCandidate candidate,
        Long problemId) {

    List<AnalysisConcept> mappings =
            analysisConceptRepository.findByAnalysisProblemId(problemId);

    if (mappings.isEmpty()) {
        return;
    }

    // Difficulty comes from the associated Problem.
    candidate.setDifficulty(
            mappings.get(0).getAnalysis().getProblem().getDifficulty()
    );

    // Select the strongest mapped PATTERN or TECHNIQUE.
    AnalysisConcept bestMapping = mappings.stream()
            .filter(mapping ->
                    mapping.getConcept().getConceptType() != null
                    && mapping.getConcept().getConceptType()
                            != DsaConcept.ConceptType.DOMAIN)
            .max(Comparator.comparingDouble(mapping ->
                    mapping.getImportance() == null
                            ? 0.0
                            : mapping.getImportance()))
            .orElse(null);

    if (bestMapping != null) {
        candidate.setRelevantConcept(
                bestMapping.getConcept().getName()
        );
    }
}

}
