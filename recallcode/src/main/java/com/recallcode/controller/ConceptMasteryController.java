
package com.recallcode.controller;

import com.recallcode.dto.ConceptMasteryResponse;
import com.recallcode.model.User;
import com.recallcode.model.UserConceptMastery;
import com.recallcode.repository.UserConceptMasteryRepository;
import com.recallcode.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * ConceptMasteryController.java
 *
 * Provides authenticated API endpoints to retrieve a user's
 * DSA concept mastery, including strongest and weakest concepts.
 */

@RestController
@RequestMapping("/api/concepts/mastery")
public class ConceptMasteryController {

    private final UserConceptMasteryRepository masteryRepository;
    private final UserRepository userRepository;

    public ConceptMasteryController(
            UserConceptMasteryRepository masteryRepository,
            UserRepository userRepository) {

        this.masteryRepository = masteryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConceptMasteryResponse>> getAllMastery(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<ConceptMasteryResponse> response =
                masteryRepository.findByUserId(user.getId())
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/strongest")
    public ResponseEntity<List<ConceptMasteryResponse>> getStrongestConcepts(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<ConceptMasteryResponse> response =
                masteryRepository
                        .findByUserIdOrderByMasteryProbabilityDesc(user.getId())
                        .stream()
                        .limit(5)
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/weakest")
    public ResponseEntity<List<ConceptMasteryResponse>> getWeakestConcepts(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<ConceptMasteryResponse> response =
                masteryRepository
                        .findByUserIdOrderByMasteryProbabilityAsc(user.getId())
                        .stream()
                        .limit(5)
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    private User getAuthenticatedUser(Authentication authentication) {

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found"));
    }

    private ConceptMasteryResponse convertToResponse(
            UserConceptMastery mastery) {

        return new ConceptMasteryResponse(
                mastery.getConcept().getId(),
                mastery.getConcept().getName(),
                mastery.getConcept().getSlug(),
                mastery.getConcept().getConceptType().name(),
                mastery.getMasteryProbability(),
                mastery.getCorrectAttempts(),
                mastery.getIncorrectAttempts()
        );
    }
}
