package com.recallcode.repository;

import com.recallcode.model.AgentAssessmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * Provides database access to saved AI assessments.
 * Allows RecallCode to retrieve the analysis for an attempt and
 * check whether that attempt has already been analysed.
 */
public interface AgentAssessmentRecordRepository
        extends JpaRepository<AgentAssessmentRecord, Long> {

    Optional<AgentAssessmentRecord> findByAttemptId(Long attemptId);

    boolean existsByAttemptId(Long attemptId);
}