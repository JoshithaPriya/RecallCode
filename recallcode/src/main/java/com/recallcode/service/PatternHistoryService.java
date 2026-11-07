
package com.recallcode.service;

import com.recallcode.model.Attempt;
import com.recallcode.model.Problem;
import com.recallcode.repository.AttemptRepository;
import com.recallcode.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatternHistoryService {

    private final ProblemRepository problemRepository;
    private final AttemptRepository attemptRepository;

    public PatternHistoryService(
            ProblemRepository problemRepository,
            AttemptRepository attemptRepository) {
        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Object>> getPatternHistory() {
        List<Problem> problems = problemRepository.findAll();

        Map<String, Map<String, Object>> history = new TreeMap<>();

        for (Problem problem : problems) {
            Set<String> tags = problem.getTags();

            if (tags == null || tags.isEmpty()) {
                continue;
            }

            List<Attempt> attempts =
                    attemptRepository.findByProblemId(problem.getId());

            for (String tag : tags) {
                if (tag == null || tag.isBlank()) {
                    continue;
                }

                Map<String, Object> stats = history.computeIfAbsent(
                        tag, key -> {
                            Map<String, Object> values = new LinkedHashMap<>();
                            values.put("distinctProblems", 0);
                            values.put("totalAttempts", 0);
                            values.put("assessedAttempts", 0);
                            values.put("lastPracticed", null);
                            return values;
                        });

                stats.put("distinctProblems",
                        (Integer) stats.get("distinctProblems") + 1);

                stats.put("totalAttempts",
                        (Integer) stats.get("totalAttempts") + attempts.size());

                LocalDateTime latest = attempts.stream()
                        .map(Attempt::getAttemptedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

                LocalDateTime existing =
                        (LocalDateTime) stats.get("lastPracticed");

                if (latest != null &&
                        (existing == null || latest.isAfter(existing))) {
                    stats.put("lastPracticed", latest);
                }
            }
        }

        return history;
    }
}
