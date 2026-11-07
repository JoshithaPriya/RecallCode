
package com.recallcode.controller;

import com.recallcode.model.Attempt;
import com.recallcode.model.Problem;
import com.recallcode.repository.AttemptRepository;
import com.recallcode.repository.ProblemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import com.recallcode.model.AgentAnalysisJob;
import com.recallcode.repository.AgentAnalysisJobRepository;
import org.springframework.transaction.annotation.Transactional;


@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptRepository attemptRepository;
    private final ProblemRepository problemRepository;
    private final AgentAnalysisJobRepository jobRepository;

 
    public AttemptController(
        AttemptRepository attemptRepository,
        ProblemRepository problemRepository,
        AgentAnalysisJobRepository jobRepository) {

    this.attemptRepository = attemptRepository;
    this.problemRepository = problemRepository;
    this.jobRepository = jobRepository;
}

    @PostMapping("/problems/{problemId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Attempt addAttempt(
            @PathVariable Long problemId,
            @RequestBody Attempt request) {

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

        request.setProblem(problem);
        Attempt savedAttempt = attemptRepository.save(request);
        AgentAnalysisJob job = new AgentAnalysisJob(savedAttempt.getId());
        jobRepository.save(job);
        return savedAttempt;
    }

    @GetMapping("/problems/{problemId}/attempts")
    public List<Attempt> getAttempts(@PathVariable Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Problem not found");
        }

        return attemptRepository.findByProblemId(problemId);
    }
}
