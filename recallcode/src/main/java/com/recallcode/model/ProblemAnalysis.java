
package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Stores versioned AI analysis of a shared DSA problem.
/* 
This entity stores the AI's analysis of a problem.
 It is separate from Problem.java because one problem 
 may have multiple analysis versions as our classification improves.
*/
@Entity
@Table(
    name = "problem_analysis",
    indexes = {
        @Index(
            name = "idx_problem_analysis_problem",
            columnList = "problem_id"
        )
    }
)
public class ProblemAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false, length = 50)
    private String promptVersion;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Column(columnDefinition = "TEXT")
    private String approachSummary;

    @Column(columnDefinition = "TEXT")
    private String alternativeApproaches;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    protected ProblemAnalysis() {
    }

    public ProblemAnalysis(
            Problem problem,
            String modelName,
            String promptVersion,
            String contentHash,
            String approachSummary,
            String alternativeApproaches,
            Double confidence
    ) {
        this.problem = problem;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
        this.contentHash = contentHash;
        this.approachSummary = approachSummary;
        this.alternativeApproaches = alternativeApproaches;
        this.confidence = confidence;
    }

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public String getModelName() {
        return modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getApproachSummary() {
        return approachSummary;
    }

    public String getAlternativeApproaches() {
        return alternativeApproaches;
    }

    public Double getConfidence() {
        return confidence;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}
