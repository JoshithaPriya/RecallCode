
package com.recallcode.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/*
 * RetentionTrackingService.java
 *
 * Evaluates whether a learner retains previously learned
 * problem-solving knowledge during a later revision attempt.
 *
 * Measures recall success and the time elapsed between attempts.
 */

@Service
public class RetentionTrackingService {

    public RetentionResult evaluateRetention(
            LocalDateTime previousAttemptAt,
            LocalDateTime revisionAttemptAt,
            boolean solved) {

        if (previousAttemptAt == null || revisionAttemptAt == null) {
            throw new IllegalArgumentException(
                    "Both attempt timestamps are required."
            );
        }

        if (revisionAttemptAt.isBefore(previousAttemptAt)) {
            throw new IllegalArgumentException(
                    "Revision attempt cannot precede the previous attempt."
            );
        }

        long elapsedDays = Duration.between(
                previousAttemptAt,
                revisionAttemptAt
        ).toDays();

        return new RetentionResult(
                solved,
                elapsedDays,
                solved ? "RETAINED" : "NOT_RETAINED"
        );
    }

    public static class RetentionResult {

        private final boolean recalledSuccessfully;
        private final long elapsedDays;
        private final String retentionStatus;

        public RetentionResult(
                boolean recalledSuccessfully,
                long elapsedDays,
                String retentionStatus) {

            this.recalledSuccessfully = recalledSuccessfully;
            this.elapsedDays = elapsedDays;
            this.retentionStatus = retentionStatus;
        }

        public boolean isRecalledSuccessfully() {
            return recalledSuccessfully;
        }

        public long getElapsedDays() {
            return elapsedDays;
        }

        public String getRetentionStatus() {
            return retentionStatus;
        }
    }
}
