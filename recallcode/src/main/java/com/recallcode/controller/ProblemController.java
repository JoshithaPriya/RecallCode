
package com.recallcode.controller;

import com.recallcode.model.Problem;
import com.recallcode.model.User;
import com.recallcode.repository.ProblemRepository;
import com.recallcode.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepository;
    private final UserService userService;

    public ProblemController(
            ProblemRepository problemRepository,
            UserService userService) {
        this.problemRepository = problemRepository;
        this.userService = userService;
    }

    @GetMapping
    public List<Problem> getAllProblems(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        return problemRepository.findVisibleToUser(user.getId());
    }

    @PostMapping
    public Problem addProblem(
            @RequestBody Problem problem,
            Authentication authentication) {

        User user = userService.getUserByUsername(authentication.getName());

        problem.setUser(user);

        return problemRepository.save(problem);
    }
}
