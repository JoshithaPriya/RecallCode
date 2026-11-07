package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * Stores the AI agent's validated assessment for a DSA attempt.
 * Preserves recall quality, understanding, strengths, difficulties,
 * supporting evidence, and confidence for future learning analysis.
 */
@Entity
@Table(
    name = "agent_assessments",
    uniqueConstraints = @UniqueConstraint(columnNames = "attempt_id")
)
public class AgentAssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false, unique = true)
    private Long attemptId;

    @Column(nullable = false)
    private String recallQuality;

    @Column(length = 1000)
    private String understandingLevel;

    @Column(length = 2000)
    private String strengths;

    @Column(length = 2000)
    private String difficulties;

    @Column(length = 3000, nullable = false)
    private String evidence;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false)
    private LocalDateTime analysedAt = LocalDateTime.now();

    protected AgentAssessmentRecord() {
    }

    public AgentAssessmentRecord(
            Long attemptId, AgentAssessment assessment) {
        this.attemptId = attemptId;
        this.recallQuality = assessment.getRecallQuality();
        this.understandingLevel = assessment.getUnderstandingLevel();
        this.strengths = assessment.getStrengths();
        this.difficulties = assessment.getDifficulties();
        this.evidence = assessment.getEvidence();
        this.confidence = assessment.getConfidence();
    }

    public Long getId() { return id; }
    public Long getAttemptId() { return attemptId; }
    public String getRecallQuality() { return recallQuality; }
    public String getUnderstandingLevel() { return understandingLevel; }
    public String getStrengths() { return strengths; }
    public String getDifficulties() { return difficulties; }
    public String getEvidence() { return evidence; }
    public double getConfidence() { return confidence; }
    public LocalDateTime getAnalysedAt() { return analysedAt; }
}