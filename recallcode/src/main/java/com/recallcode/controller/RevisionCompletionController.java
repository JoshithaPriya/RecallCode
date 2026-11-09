package com.recallcode.controller;

import com.recallcode.model.User;
import com.recallcode.model.UserProblemProgress;
import com.recallcode.service.RevisionService;
import com.recallcode.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

// Completes a revision and updates only the authenticated user's revision schedule.
@RestController
@RequestMapping("/api/revisions")
public class RevisionCompletionController {

    private final RevisionService revisionService;
    private final UserService userService;

    public RevisionCompletionController(
            RevisionService revisionService,
            UserService userService) {
        this.revisionService = revisionService;
        this.userService = userService;
    }

    @PostMapping("/{problemId}/complete")
    public RevisionCompletionResponse completeRevision(
            @PathVariable Long problemId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String quality = request.get("recallQuality");

        if (quality == null || quality.isBlank()) {
            throw new IllegalArgumentException(
                    "recallQuality is required"
            );
        }

        User user = userService.getUserByUsername(
                authentication.getName()
        );

        UserProblemProgress progress =
                revisionService.completeRevision(
                        user.getId(),
                        problemId,
                        quality
                );

        return new RevisionCompletionResponse(
                progress.getProblem().getId(),
                progress.getRevisionStreak(),
                progress.getRevisionIntervalDays(),
                progress.getNextRevisionAt()
        );
    }

    // Returns revision details without exposing the user's entity.
    public record RevisionCompletionResponse(
            Long problemId,
            Integer revisionStreak,
            Integer revisionIntervalDays,
            LocalDateTime nextRevisionAt
    ) {}
}