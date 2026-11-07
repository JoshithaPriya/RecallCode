
package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/*
 * Stores a coding problem's identity, source metadata and revision schedule.
 * Metadata fields are populated by source collectors when real data is available.
 */
@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String platform;

    @Column(unique = true)
    private String problemUrl;

    private String difficulty;

    // External platform identifiers and metadata.
    private String externalId;
    private Integer contestId;
    private String problemIndex;
    private Integer platformRating;
    private Integer solvedCount;
    private Double points;

    // Text fields remain null unless a permitted source provides them.
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String inputFormat;

    @Column(columnDefinition = "TEXT")
    private String outputFormat;

    @Column(columnDefinition = "TEXT")
    private String constraintsText;

    // Actual topic tags collected from the source.
    @ElementCollection
    @CollectionTable(
        name = "problem_tags",
        joinColumns = @JoinColumn(name = "problem_id")
    )
    @Column(name = "tag", nullable = false)
    private Set<String> tags = new HashSet<>();

    // Provenance and collection status.
    private String metadataSource;
    private String metadataStatus;
    private LocalDateTime metadataFetchedAt;

    // Existing revision scheduling fields.
    private LocalDate nextRevisionDate;

    @Column(nullable = false)
    private Integer revisionIntervalDays = 1;

    @Column(nullable = false)
    private Integer revisionStreak = 0;

    // Extra LeetCode metadata; stored locally and not automatically sent to AI.
    @Column(columnDefinition = "TEXT")
    private String hintsText;

    @Column(columnDefinition = "TEXT")
    private String exampleTestcases;

    @Column(columnDefinition = "TEXT")
    private String starterCodeText;

    @Column(columnDefinition = "TEXT")
    private String similarQuestionsJson;

    public Problem() {
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getProblemUrl() { return problemUrl; }
    public void setProblemUrl(String problemUrl) { this.problemUrl = problemUrl; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public Integer getContestId() { return contestId; }
    public void setContestId(Integer contestId) { this.contestId = contestId; }

    public String getProblemIndex() { return problemIndex; }
    public void setProblemIndex(String problemIndex) { this.problemIndex = problemIndex; }

    public Integer getPlatformRating() { return platformRating; }
    public void setPlatformRating(Integer platformRating) { this.platformRating = platformRating; }

    public Integer getSolvedCount() { return solvedCount; }
    public void setSolvedCount(Integer solvedCount) { this.solvedCount = solvedCount; }

    public Double getPoints() { return points; }
    public void setPoints(Double points) { this.points = points; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public String getConstraintsText() { return constraintsText; }
    public void setConstraintsText(String constraintsText) { this.constraintsText = constraintsText; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) {
        this.tags = tags == null ? new HashSet<>() : new HashSet<>(tags);
    }

    public String getMetadataSource() { return metadataSource; }
    public void setMetadataSource(String metadataSource) { this.metadataSource = metadataSource; }

    public String getMetadataStatus() { return metadataStatus; }
    public void setMetadataStatus(String metadataStatus) { this.metadataStatus = metadataStatus; }

    public LocalDateTime getMetadataFetchedAt() { return metadataFetchedAt; }
    public void setMetadataFetchedAt(LocalDateTime metadataFetchedAt) {
        this.metadataFetchedAt = metadataFetchedAt;
    }

    public LocalDate getNextRevisionDate() { return nextRevisionDate; }
    public void setNextRevisionDate(LocalDate nextRevisionDate) {
        this.nextRevisionDate = nextRevisionDate;
    }

    public Integer getRevisionIntervalDays() { return revisionIntervalDays; }
    public void setRevisionIntervalDays(Integer revisionIntervalDays) {
        this.revisionIntervalDays = revisionIntervalDays;
    }

    public Integer getRevisionStreak() { return revisionStreak; }
    public void setRevisionStreak(Integer revisionStreak) {
        this.revisionStreak = revisionStreak;
    }

    public String getHintsText() {
    return hintsText;
}

public void setHintsText(String hintsText) {
    this.hintsText = hintsText;
}

public String getExampleTestcases() {
    return exampleTestcases;
}

public void setExampleTestcases(String exampleTestcases) {
    this.exampleTestcases = exampleTestcases;
}

public String getStarterCodeText() {
    return starterCodeText;
}

public void setStarterCodeText(String starterCodeText) {
    this.starterCodeText = starterCodeText;
}

public String getSimilarQuestionsJson() {
    return similarQuestionsJson;
}

public void setSimilarQuestionsJson(String similarQuestionsJson) {
    this.similarQuestionsJson = similarQuestionsJson;
}
}
