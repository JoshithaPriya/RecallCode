package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


/*
 * Represents an AI analysis job created when a user completes a DSA attempt.
 * Stores the attempt ID, job status, retry count, creation time, and error details.
 * Helps RecallCode track analysis progress and recover from temporary failures.
 */
@Entity
@Table(name = "agent_analysis_jobs")
public class AgentAnalysisJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long attemptId;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(nullable = false)
    private int retryCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;
    private LocalDateTime processingStartedAt;

    @Column(length = 2000)
    private String lastError;


    protected AgentAnalysisJob() {
    }

    public AgentAnalysisJob(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getId() {
        return id;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }
}