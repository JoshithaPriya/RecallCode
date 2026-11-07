
package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks an individual user's progress and revision schedule for each problem.
 * Ensures every user has an independent learning and revision history.
 */
@Entity
@Table(
    name = "user_problem_progress",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_problem_progress",
        columnNames = {"user_id", "problem_id"}
    )
)
public class UserProblemProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer revisionStreak = 0;

    @Column(nullable = false)
    private Integer revisionIntervalDays = 1;

    private LocalDateTime nextRevisionAt;

    private LocalDateTime lastAttemptAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected UserProblemProgress() {
    }

    public UserProblemProgress(User user, Problem problem) {
        this.user = user;
        this.problem = problem;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Problem getProblem() {
        return problem;
    }

    public Integer getRevisionStreak() {
        return revisionStreak;
    }

    public void setRevisionStreak(Integer revisionStreak) {
        this.revisionStreak = revisionStreak;
    }

    public Integer getRevisionIntervalDays() {
        return revisionIntervalDays;
    }

    public void setRevisionIntervalDays(Integer revisionIntervalDays) {
        this.revisionIntervalDays = revisionIntervalDays;
    }

    public LocalDateTime getNextRevisionAt() {
        return nextRevisionAt;
    }

    public void setNextRevisionAt(LocalDateTime nextRevisionAt) {
        this.nextRevisionAt = nextRevisionAt;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
