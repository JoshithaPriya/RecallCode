package com.recallcode.controller;

import com.recallcode.model.User;
import com.recallcode.model.UserProblemProgress;
import com.recallcode.service.UserService;
import com.recallcode.repository.UserProblemProgressRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Returns only the authenticated user's problems that are due for revision.
@RestController
@RequestMapping("/api/revisions")
public class RevisionController {

    private final UserProblemProgressRepository progressRepository;
    private final UserService userService;

    public RevisionController(
            UserProblemProgressRepository progressRepository,
            UserService userService) {
        this.progressRepository = progressRepository;
        this.userService = userService;
    }

    @GetMapping
    public List<RevisionResponse> getDueRevisions(
            Authentication authentication) {

        User user = userService.getUserByUsername(
                authentication.getName()
        );

        List<UserProblemProgress> dueProgress =
                progressRepository.findByUserIdAndNextRevisionAtLessThanEqual(
                        user.getId(),
                        LocalDateTime.now()
                );

        return dueProgress.stream()
                .map(progress -> new RevisionResponse(
                        progress.getProblem().getId(),
                        progress.getProblem().getTitle(),
                        progress.getProblem().getPlatform(),
                        progress.getProblem().getDifficulty(),
                        progress.getProblem().getProblemUrl(),
                        progress.getRevisionStreak(),
                        progress.getRevisionIntervalDays(),
                        progress.getNextRevisionAt()
                ))
                .toList();
    }

    // Safe response containing revision details without exposing user data.
    public record RevisionResponse(
            Long problemId,
            String title,
            String platform,
            String difficulty,
            String problemUrl,
            Integer revisionStreak,
            Integer revisionIntervalDays,
            LocalDateTime nextRevisionAt
    ) {}
}