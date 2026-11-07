
package com.recallcode.controller;

import com.recallcode.repository.AttemptRepository;
import com.recallcode.repository.ProblemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

//This API calculates your DSA progress from real database records instead of hardcoded numbers.

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProblemRepository problemRepository;
    private final AttemptRepository attemptRepository;

    public DashboardController(
            ProblemRepository problemRepository,
            AttemptRepository attemptRepository) {
        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        var problems = problemRepository.findAll();
        var attempts = attemptRepository.findAll();

        long solvedAttempts = attempts.stream()
                .filter(a -> "SOLVED".equals(a.getOutcome()))
                .count();

        double averageTime = attempts.stream()
                .filter(a -> a.getTimeTakenMinutes() != null)
                .mapToInt(a -> a.getTimeTakenMinutes())
                .average()
                .orElse(0.0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProblems", problems.size());
        summary.put("totalAttempts", attempts.size());
        summary.put("solvedAttempts", solvedAttempts);
        summary.put("averageTimeMinutes",
                Math.round(averageTime * 100.0) / 100.0);

        return summary;
    }
}
