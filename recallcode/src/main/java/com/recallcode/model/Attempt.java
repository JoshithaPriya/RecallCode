
package com.recallcode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


//One problem can have multiple attempts, and we need to track the time and result of each attempt separately.

@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer timeTakenMinutes;

    @Column(nullable = false)
    private String outcome;

    private String notes;
    private java.time.LocalDate nextRevisionDate;

    @Column(nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();


    /**
 * Identifies the user who performed this attempt.
 * Enables independent learning history and progress tracking for each user.
 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    
    public Attempt() {}

    public Long getId() { return id; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }

    public java.time.LocalDate getNextRevisionDate() {
        return nextRevisionDate;
    }

    public void setNextRevisionDate(java.time.LocalDate nextRevisionDate) {
        this.nextRevisionDate = nextRevisionDate;
    }

}
