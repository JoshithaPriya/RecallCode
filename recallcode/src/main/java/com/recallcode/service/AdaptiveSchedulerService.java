
package com.recallcode.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/*
 * AdaptiveSchedulerService.java
 *
 * Calculates the next revision date based on practice outcome
 * and the learner's current concept mastery.
 *
 * Lower mastery or an incorrect attempt results in an earlier
 * revision. Higher mastery allows a longer revision interval.
 */

@Service
public class AdaptiveSchedulerService {

    public LocalDateTime calculateNextRevision(
            boolean correct,
            double masteryProbability) {

        int intervalDays;

        if (!correct) {
            intervalDays = 1;
        } else if (masteryProbability < 0.40) {
            intervalDays = 1;
        } else if (masteryProbability < 0.60) {
            intervalDays = 3;
        } else if (masteryProbability < 0.80) {
            intervalDays = 5;
        } else {
            intervalDays = 7;
        }

        return LocalDateTime.now().plusDays(intervalDays);
    }
}
