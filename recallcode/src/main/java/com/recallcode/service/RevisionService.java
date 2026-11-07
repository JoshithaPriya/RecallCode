
package com.recallcode.service;

import com.recallcode.model.Problem;
import com.recallcode.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RevisionService {

    private final ProblemRepository problemRepository;

    public RevisionService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional
    public Problem completeRevision(Long problemId, String recallQuality) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Problem not found"));

        int streak = problem.getRevisionStreak() == null
                ? 0 : problem.getRevisionStreak();

        int interval = problem.getRevisionIntervalDays() == null
                ? 1 : problem.getRevisionIntervalDays();

        switch (recallQuality) {
            case "FORGOT" -> {
                streak = 0;
                interval = 1;
            }
            case "HARD" -> {
                streak = 0;
                interval = 2;
            }
            case "GOOD" -> {
                streak++;
                interval = streak == 1 ? 3
                        : streak == 2 ? 7
                        : Math.min(30, interval * 2);
            }
            case "EASY" -> {
                streak++;
                interval = streak == 1 ? 4
                        : streak == 2 ? 10
                        : Math.min(45, (int) Math.ceil(interval * 2.5));
            }
            default -> throw new IllegalArgumentException(
                    "Quality must be FORGOT, HARD, GOOD, or EASY");
        }

        problem.setRevisionStreak(streak);
        problem.setRevisionIntervalDays(interval);
        problem.setNextRevisionDate(LocalDate.now().plusDays(interval));

        return problemRepository.save(problem);
    }
}
