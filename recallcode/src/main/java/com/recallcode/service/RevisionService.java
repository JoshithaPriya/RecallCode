// Manages revision schedules independently for each user's problem progress.

package com.recallcode.service;

import com.recallcode.model.UserProblemProgress;
import com.recallcode.repository.UserProblemProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RevisionService {

    private final UserProblemProgressService progressService;
    private final UserProblemProgressRepository progressRepository;

    public RevisionService(
            UserProblemProgressService progressService,
            UserProblemProgressRepository progressRepository) {

        this.progressService = progressService;
        this.progressRepository = progressRepository;
    }

    @Transactional
    public UserProblemProgress completeRevision(
            Long userId,
            Long problemId,
            String recallQuality) {

        UserProblemProgress progress =
                progressService.getOrCreateProgress(userId, problemId);

        int streak = progress.getRevisionStreak();
        int interval = progress.getRevisionIntervalDays();

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
                        : Math.min(45,
                                (int) Math.ceil(interval * 2.5));
            }
            default -> throw new IllegalArgumentException(
                    "Quality must be FORGOT, HARD, GOOD, or EASY");
        }

        progress.setRevisionStreak(streak);
        progress.setRevisionIntervalDays(interval);

        progress.setNextRevisionAt(
        java.time.LocalDateTime.now().plusDays(interval)
);
        return progressRepository.save(progress);
    }
}