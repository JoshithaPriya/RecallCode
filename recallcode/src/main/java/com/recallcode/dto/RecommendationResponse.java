
package com.recallcode.dto;

import java.time.LocalDateTime;

/**
 * Represents one problem recommended by the adaptive
 * revision engine.
 *
 * Contains recommendation details, learner mastery,
 * transfer relevance, priority score, and explanation.
 */
public class RecommendationResponse {

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

    public RecommendationResponse() {
    }

    public RecommendationResponse(
            Long problemId,
            String problemTitle,
            String recommendationType,
            LocalDateTime nextRevisionAt,
            double masteryProbability,
            double transferRelevance,
            double priorityScore) {

        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.recommendationType = recommendationType;
        this.nextRevisionAt = nextRevisionAt;
        this.masteryProbability = masteryProbability;
        this.transferRelevance = transferRelevance;
        this.priorityScore = priorityScore;
    }

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
