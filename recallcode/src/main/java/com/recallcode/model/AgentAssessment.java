package com.recallcode.model;

/*
 * Represents the AI's assessment of a user's DSA attempt.
 * Stores the estimated recall quality, understanding, identified
 * difficulties, supporting evidence, and confidence in the assessment.
 */
public class AgentAssessment {

    private String recallQuality;
    private String understandingLevel;
    private String strengths;
    private String difficulties;
    private String evidence;
    private double confidence;

    public AgentAssessment() {
    }

    public String getRecallQuality() {
        return recallQuality;
    }

    public void setRecallQuality(String recallQuality) {
        this.recallQuality = recallQuality;
    }

    public String getUnderstandingLevel() {
        return understandingLevel;
    }

    public void setUnderstandingLevel(String understandingLevel) {
        this.understandingLevel = understandingLevel;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getDifficulties() {
        return difficulties;
    }

    public void setDifficulties(String difficulties) {
        this.difficulties = difficulties;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}