
package com.recallcode.controller;

import com.recallcode.model.Problem;
import com.recallcode.repository.ProblemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

//This filters problems by their scheduled revision date without incorrectly excluding previously solved problems.

@RestController
@RequestMapping("/api/revisions")
public class RevisionController {

    private final ProblemRepository problemRepository;

    public RevisionController(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @GetMapping
    public List<Problem> getDueRevisions() {
        LocalDate today = LocalDate.now();

        return problemRepository.findAll()
                .stream()
                .filter(problem ->
                        problem.getNextRevisionDate() != null
                        && !problem.getNextRevisionDate().isAfter(today))
                .toList();
    }
}
