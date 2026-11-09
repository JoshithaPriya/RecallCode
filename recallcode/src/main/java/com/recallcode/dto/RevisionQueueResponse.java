
package com.recallcode.dto;

import java.time.LocalDateTime;
import java.util.List;

/*
 * RevisionQueueResponse.java
 *
 * Represents the complete adaptive revision queue
 * returned to the authenticated learner.
 *
 * Contains the recommendations, total count,
 * and the time at which the queue was generated.
 */
public class RevisionQueueResponse {

    private List<RecommendationResponse> recommendations;
    private int totalRecommendations;
    private LocalDateTime generatedAt;

    public RevisionQueueResponse() {
    }

    public RevisionQueueResponse(
            List<RecommendationResponse> recommendations,
            LocalDateTime generatedAt) {

        this.recommendations = recommendations;
        this.totalRecommendations =
                recommendations == null ? 0 : recommendations.size();
        this.generatedAt = generatedAt;
    }

    public List<RecommendationResponse> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(
            List<RecommendationResponse> recommendations) {

        this.recommendations = recommendations;
        this.totalRecommendations =
                recommendations == null ? 0 : recommendations.size();
    }

    public int getTotalRecommendations() {
        return totalRecommendations;
    }

    public void setTotalRecommendations(int totalRecommendations) {
        this.totalRecommendations = totalRecommendations;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
