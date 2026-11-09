
package com.recallcode.model;

import java.util.List;

// Defines the structured response returned by AI problem analysis.
public class ProblemAnalysisResult {

    private String approachSummary;

    private String alternativeApproaches;

    private Double confidence;

    private List<ConceptMatch> concepts;

    public ProblemAnalysisResult() {
    }

    public String getApproachSummary() {
        return approachSummary;
    }

    public void setApproachSummary(String approachSummary) {
        this.approachSummary = approachSummary;
    }

    public String getAlternativeApproaches() {
        return alternativeApproaches;
    }

    public void setAlternativeApproaches(String alternativeApproaches) {
        this.alternativeApproaches = alternativeApproaches;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<ConceptMatch> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<ConceptMatch> concepts) {
        this.concepts = concepts;
    }

    public static class ConceptMatch {

        private String slug;

        private Double confidence;

        private Integer importance;

        private String evidence;

        public ConceptMatch() {
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public Integer getImportance() {
            return importance;
        }

        public void setImportance(Integer importance) {
            this.importance = importance;
        }

        public String getEvidence() {
            return evidence;
        }

        public void setEvidence(String evidence) {
            this.evidence = evidence;
        }
    }
}
