
package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * UserConceptMastery.java
 *
 * Stores an individual user's estimated mastery of a DSA concept.
 * BKT updates the mastery probability whenever the user practises
 * a problem associated with that concept.
 */

@Entity
@Table(
    name = "user_concept_mastery",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "concept_id"}
        )
    }
)
public class UserConceptMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id", nullable = false)
    private DsaConcept concept;

    @Column(nullable = false)
    private double masteryProbability = 0.10;

    @Column(nullable = false)
    private int correctAttempts = 0;

    @Column(nullable = false)
    private int incorrectAttempts = 0;

    private LocalDateTime lastUpdatedAt;

    public UserConceptMastery() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DsaConcept getConcept() {
        return concept;
    }

    public void setConcept(DsaConcept concept) {
        this.concept = concept;
    }

    public double getMasteryProbability() {
        return masteryProbability;
    }

    public void setMasteryProbability(double masteryProbability) {
        this.masteryProbability = masteryProbability;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(int correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public int getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public void setIncorrectAttempts(int incorrectAttempts) {
        this.incorrectAttempts = incorrectAttempts;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
