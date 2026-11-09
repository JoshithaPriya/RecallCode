
package com.recallcode.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/*
 * RevisionPriorityService.java
 *
 * Calculates recommendation priority using revision urgency,
 * learner concept weakness, and transfer relevance.
 *
 * Higher scores indicate that a problem should receive
 * greater priority in the adaptive revision queue.
 */

@Service
public class RevisionPriorityService {

    private static final double REVISION_WEIGHT = 0.45;
    private static final double WEAKNESS_WEIGHT = 0.35;
    private static final double TRANSFER_WEIGHT = 0.20;

    private static final double MAX_OVERDUE_DAYS = 7.0;

    public double calculatePriority(
            LocalDateTime nextRevisionAt,
            double masteryProbability,
            double transferRelevance) {

        double revisionScore = calculateRevisionUrgency(
                nextRevisionAt
        );

        double weaknessScore = calculateWeakness(
                masteryProbability
        );

        double transferScore = normalize(transferRelevance);

        return
                (revisionScore * REVISION_WEIGHT)
                + (weaknessScore * WEAKNESS_WEIGHT)
                + (transferScore * TRANSFER_WEIGHT);
    }

    private double calculateRevisionUrgency(
            LocalDateTime nextRevisionAt) {

        if (nextRevisionAt == null) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();

        if (nextRevisionAt.isAfter(now)) {
            return 0.0;
        }

        long overdueDays = Duration.between(
                nextRevisionAt,
                now
        ).toDays();

        if (overdueDays == 0) {
            return 0.5;
        }

        return Math.min(
                1.0,
                0.5 + (overdueDays / MAX_OVERDUE_DAYS) * 0.5
        );
    }

    private double calculateWeakness(
            double masteryProbability) {

        return 1.0 - normalize(masteryProbability);
    }

    private double normalize(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
