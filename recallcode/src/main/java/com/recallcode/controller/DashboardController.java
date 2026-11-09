
package com.recallcode.controller;

import com.recallcode.model.Attempt;
import com.recallcode.model.User;
import com.recallcode.repository.AttemptRepository;
import com.recallcode.repository.ProblemRepository;
import com.recallcode.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides per-user dashboard statistics.
 * Attempt totals and timing are scoped to the authenticated user.
 * Problem count is global — problems are shared across users.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProblemRepository problemRepository;
    private final AttemptRepository attemptRepository;
    private final UserService userService;

    public DashboardController(
            ProblemRepository problemRepository,
            AttemptRepository attemptRepository,
            UserService userService) {

        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
        this.userService = userService;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(Authentication authentication) {

        User user = userService.getUserByUsername(authentication.getName());
        List<Attempt> attempts = attemptRepository.findByUserId(user.getId());

        long solvedAttempts = attempts.stream()
                .filter(a -> "SOLVED".equals(a.getOutcome()))
                .count();

        double averageTime = attempts.stream()
                .filter(a -> a.getTimeTakenMinutes() != null)
                .mapToInt(Attempt::getTimeTakenMinutes)
                .average()
                .orElse(0.0);

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("totalProblems",
        problemRepository.findVisibleToUser(user.getId()).size());
    
        summary.put("totalAttempts", attempts.size());
        summary.put("solvedAttempts", solvedAttempts);
        summary.put("averageTimeMinutes",
                Math.round(averageTime * 100.0) / 100.0);

        return summary;
    }
}
