
package com.recallcode.controller;

import com.recallcode.dto.RecommendationResponse;
import com.recallcode.dto.RevisionQueueResponse;
import com.recallcode.model.User;
import com.recallcode.service.RecommendationService;
import com.recallcode.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/*
 * RecommendationController.java
 *
 * Exposes personalized DSA recommendations through REST APIs.
 *
 * The authenticated user's identity is obtained from Spring Security.
 * Users cannot provide another user's ID to access their queue.
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    public RecommendationController(
            RecommendationService recommendationService,
            UserService userService) {

        this.recommendationService = recommendationService;
        this.userService = userService;
    }

    /*
     * Returns the personalized adaptive revision queue
     * for the currently authenticated user.
     */
    @GetMapping
    public RevisionQueueResponse getRecommendations(
            Authentication authentication) {

        User user = userService.getUserByUsername(
                authentication.getName()
        );

        List<RecommendationResponse> recommendations =
                recommendationService.generateRecommendations(user.getId())
                        .stream()
                       .map(candidate -> {
                        RecommendationResponse response = new RecommendationResponse(
            candidate.getProblemId(),
            candidate.getProblemTitle(),
            candidate.getRecommendationType(),
            candidate.getNextRevisionAt(),
            candidate.getMasteryProbability(),
            candidate.getTransferRelevance(),
            candidate.getPriorityScore()
    );

    response.setReason(candidate.getReason());
    response.setRelevantConcept(candidate.getRelevantConcept());
    response.setDifficulty(candidate.getDifficulty());

    return response;
})
                        .toList();

        return new RevisionQueueResponse(
                recommendations,
                LocalDateTime.now()
        );
    }
}
