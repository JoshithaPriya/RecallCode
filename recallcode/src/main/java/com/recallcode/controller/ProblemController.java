package com.recallcode.controller;

import com.recallcode.model.Problem;
import com.recallcode.repository.ProblemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//The controller receives HTTP requests from the RecallCode frontend and connects them to our repository.

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepository;

    public ProblemController(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    // Get all problems
    @GetMapping
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    // Add a new problem
    @PostMapping
    public Problem addProblem(@RequestBody Problem problem) {
        return problemRepository.save(problem);
    }
}