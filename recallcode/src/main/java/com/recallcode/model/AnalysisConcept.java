
package com.recallcode.model;

import jakarta.persistence.*;

// Connects an AI problem analysis to identified DSA concepts.
@Entity
@Table(
    name = "analysis_concepts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_analysis_concept",
            columnNames = {"analysis_id", "concept_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_analysis_concepts_concept",
            columnList = "concept_id"
        )
    }
)
public class AnalysisConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ProblemAnalysis analysis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private DsaConcept concept;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private Integer importance;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    protected AnalysisConcept() {
    }

    public AnalysisConcept(
            ProblemAnalysis analysis,
            DsaConcept concept,
            Double confidence,
            Integer importance,
            String evidence
    ) {
        this.analysis = analysis;
        this.concept = concept;
        this.confidence = confidence;
        this.importance = importance;
        this.evidence = evidence;
    }

    public Long getId() {
        return id;
    }

    public ProblemAnalysis getAnalysis() {
        return analysis;
    }

    public DsaConcept getConcept() {
        return concept;
    }

    public Double getConfidence() {
        return confidence;
    }

    public Integer getImportance() {
        return importance;
    }

    public String getEvidence() {
        return evidence;
    }
}
