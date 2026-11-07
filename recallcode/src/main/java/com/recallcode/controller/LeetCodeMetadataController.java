
package com.recallcode.controller;

// Receives a LeetCode URL from the frontend and returns the metadata saved by the service.
// This controller does not call the AI model.

import com.recallcode.model.Problem;
import com.recallcode.service.LeetCodeMetadataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/problems/metadata")
public class LeetCodeMetadataController {

    private final LeetCodeMetadataService metadataService;

    public LeetCodeMetadataController(
            LeetCodeMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    // POST /api/problems/metadata/leetcode
    @PostMapping("/leetcode")
    public ResponseEntity<Map<String, Object>> collectProblem(
            @RequestBody Map<String, String> request) {

        try {
            Problem problem =
                    metadataService.collectProblem(request.get("url"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Problem metadata saved successfully.");
            response.put("id", problem.getId());
            response.put("title", problem.getTitle());
            response.put("platform", problem.getPlatform());
            response.put("url", problem.getProblemUrl());
            response.put("difficulty", problem.getDifficulty());
            response.put("tags", problem.getTags());
            response.put("metadataStatus", problem.getMetadataStatus());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", exception.getMessage()
            ));

        } catch (RestClientException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message",
                    "Could not contact LeetCode. Please try again later."
            ));

        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message", exception.getMessage()
            ));
        }
    }
}
