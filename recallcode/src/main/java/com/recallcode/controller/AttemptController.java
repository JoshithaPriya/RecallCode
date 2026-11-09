
package com.recallcode.controller;

import com.recallcode.model.Attempt;
import com.recallcode.model.AgentAnalysisJob;
import com.recallcode.model.Problem;
import com.recallcode.model.User;
import com.recallcode.repository.AttemptRepository;
import com.recallcode.repository.ProblemRepository;
import com.recallcode.repository.AgentAnalysisJobRepository;
import com.recallcode.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import com.recallcode.service.BayesianKnowledgeTracingService;
/**
 * Manages DSA practice attempts.
 *
 * Security rules:
 * - Every saved attempt is linked to the authenticated user.
 * - Reads are scoped to the authenticated user; no user can see another's attempts.
 * - User identity is always derived from the Spring Security principal,
 *   never from a user-supplied request field.
 */
@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptRepository attemptRepository;
    private final ProblemRepository problemRepository;
    private final AgentAnalysisJobRepository jobRepository;
    private final UserService userService;
    private final BayesianKnowledgeTracingService bktService;

    public AttemptController(
        AttemptRepository attemptRepository,
        ProblemRepository problemRepository,
        AgentAnalysisJobRepository jobRepository,
        UserService userService,
        BayesianKnowledgeTracingService bktService) {

    this.attemptRepository = attemptRepository;
    this.problemRepository = problemRepository;
    this.jobRepository = jobRepository;
    this.userService = userService;
    this.bktService = bktService;
}

    /**
     * Records a new attempt for the authenticated user.
     * Sets the user from the Spring Security principal — never from the request body.
     * Queues an AI analysis job after the attempt is saved.
     */
    @PostMapping("/problems/{problemId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AttemptResponse addAttempt(
            @PathVariable Long problemId,
            @RequestBody AttemptRequest request,
            Authentication authentication) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Problem not found"));

        if (request.getTimeTakenMinutes() == null
                || request.getTimeTakenMinutes() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Time must be zero or greater");
        }

        if (request.getOutcome() == null
                || !List.of("SOLVED", "PARTIALLY_SOLVED", "NOT_SOLVED")
                        .contains(request.getOutcome())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid outcome");
        }

        // Resolve the authenticated user — never trust a user ID from the request.
        User user = userService.getUserByUsername(authentication.getName());

        Attempt attempt = new Attempt();
        attempt.setProblem(problem);
        attempt.setTimeTakenMinutes(request.getTimeTakenMinutes());
        attempt.setOutcome(request.getOutcome());
        attempt.setNotes(request.getNotes());
        attempt.setUser(user);

        Attempt saved = attemptRepository.save(attempt);

// Update BKT mastery using the practice outcome.
if ("SOLVED".equals(saved.getOutcome())) {

    bktService.updateMastery(
            user,
            problemId,
            true
    );

} else if ("NOT_SOLVED".equals(saved.getOutcome())) {

    bktService.updateMastery(
            user,
            problemId,
            false
    );
}

// Queue the background AI analysis job unchanged.
jobRepository.save(new AgentAnalysisJob(saved.getId()));

        return toResponse(saved);
    }

    /**
     * Returns the authenticated user's attempt history for a specific problem.
     * A user cannot see another user's attempts.
     */
    @GetMapping("/problems/{problemId}/attempts")
    public List<AttemptResponse> getAttempts(
            @PathVariable Long problemId,
            Authentication authentication) {

        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Problem not found");
        }

        User user = userService.getUserByUsername(authentication.getName());

        return attemptRepository
                .findByUserIdAndProblemId(user.getId(), problemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private AttemptResponse toResponse(Attempt attempt) {
        return new AttemptResponse(
                attempt.getId(),
                attempt.getProblem().getId(),
                attempt.getTimeTakenMinutes(),
                attempt.getOutcome(),
                attempt.getNotes(),
                attempt.getAttemptedAt(),
                attempt.getNextRevisionDate()
        );
    }

    // ── Inner DTOs ─────────────────────────────────────────────────────────

    /**
     * Incoming attempt data. Does not contain a user field — user is always
     * derived from the authenticated session.
     */
    public static class AttemptRequest {

        private Integer timeTakenMinutes;
        private String outcome;
        private String notes;

        public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
        public void setTimeTakenMinutes(Integer timeTakenMinutes) {
            this.timeTakenMinutes = timeTakenMinutes;
        }

        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * Safe attempt response — never includes the user object or password hash.
     */
    public static class AttemptResponse {

        private final Long id;
        private final Long problemId;
        private final Integer timeTakenMinutes;
        private final String outcome;
        private final String notes;
        private final LocalDateTime attemptedAt;
        private final java.time.LocalDate nextRevisionDate;

        public AttemptResponse(
                Long id,
                Long problemId,
                Integer timeTakenMinutes,
                String outcome,
                String notes,
                LocalDateTime attemptedAt,
                java.time.LocalDate nextRevisionDate) {

            this.id = id;
            this.problemId = problemId;
            this.timeTakenMinutes = timeTakenMinutes;
            this.outcome = outcome;
            this.notes = notes;
            this.attemptedAt = attemptedAt;
            this.nextRevisionDate = nextRevisionDate;
        }

        public Long getId() { return id; }
        public Long getProblemId() { return problemId; }
        public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
        public String getOutcome() { return outcome; }
        public String getNotes() { return notes; }
        public LocalDateTime getAttemptedAt() { return attemptedAt; }
        public java.time.LocalDate getNextRevisionDate() { return nextRevisionDate; }
    }
}
