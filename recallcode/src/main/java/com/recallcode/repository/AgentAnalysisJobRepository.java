package com.recallcode.repository;

import com.recallcode.model.AgentAnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Provides database operations for managing AI analysis jobs.
 * Retrieves pending jobs in creation order and checks whether a job
 * already exists for an attempt to help prevent duplicate processing.
 */

public interface AgentAnalysisJobRepository
        extends JpaRepository<AgentAnalysisJob, Long> {

    List<AgentAnalysisJob> findTop20ByStatusOrderByCreatedAtAsc(
            String status);

    boolean existsByAttemptId(Long attemptId);

    List<AgentAnalysisJob> findByStatusAndCreatedAtBefore(
        String status, LocalDateTime cutoff);

    List<AgentAnalysisJob> findByStatusAndProcessingStartedAtBefore(
        String status, LocalDateTime cutoff);
}