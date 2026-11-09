
package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Stores the shared DSA knowledge catalogue and its hierarchy.
@Entity
@Table(
    name = "dsa_concepts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_dsa_concept_slug",
            columnNames = "slug"
        )
    }
)
public class DsaConcept {

    public enum ConceptType {
        DOMAIN,
        PATTERN,
        TECHNIQUE,
        VARIATION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 180)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConceptType conceptType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String recognitionSignals;

    @Column(columnDefinition = "TEXT")
    private String commonMistakes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DsaConcept parent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected DsaConcept() {
    }

    public DsaConcept(
            String name,
            String slug,
            ConceptType conceptType,
            String description
    ) {
        this.name = name;
        this.slug = slug;
        this.conceptType = conceptType;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public ConceptType getConceptType() {
        return conceptType;
    }

    public void setConceptType(ConceptType conceptType) {
        this.conceptType = conceptType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecognitionSignals() {
        return recognitionSignals;
    }

    public void setRecognitionSignals(String recognitionSignals) {
        this.recognitionSignals = recognitionSignals;
    }

    public String getCommonMistakes() {
        return commonMistakes;
    }

    public void setCommonMistakes(String commonMistakes) {
        this.commonMistakes = commonMistakes;
    }

    public DsaConcept getParent() {
        return parent;
    }

    public void setParent(DsaConcept parent) {
        this.parent = parent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
