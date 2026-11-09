
package com.recallcode.dto;

/*
 * ConceptMasteryResponse.java
 *
 * Represents a user's mastery information for a DSA concept.
 * Used by the API to display concept mastery and practice history.
 */

public class ConceptMasteryResponse {

    private Long conceptId;
    private String conceptName;
    private String conceptSlug;
    private String conceptType;

    private double masteryProbability;

    private int correctAttempts;
    private int incorrectAttempts;

    public ConceptMasteryResponse() {
    }

    public ConceptMasteryResponse(
            Long conceptId,
            String conceptName,
            String conceptSlug,
            String conceptType,
            double masteryProbability,
            int correctAttempts,
            int incorrectAttempts) {

        this.conceptId = conceptId;
        this.conceptName = conceptName;
        this.conceptSlug = conceptSlug;
        this.conceptType = conceptType;
        this.masteryProbability = masteryProbability;
        this.correctAttempts = correctAttempts;
        this.incorrectAttempts = incorrectAttempts;
    }

    public Long getConceptId() {
        return conceptId;
    }

    public void setConceptId(Long conceptId) {
        this.conceptId = conceptId;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getConceptSlug() {
        return conceptSlug;
    }

    public void setConceptSlug(String conceptSlug) {
        this.conceptSlug = conceptSlug;
    }

    public String getConceptType() {
        return conceptType;
    }

    public void setConceptType(String conceptType) {
        this.conceptType = conceptType;
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
}
