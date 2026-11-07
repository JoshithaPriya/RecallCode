
package com.recallcode.service;

import com.recallcode.model.Problem;
import com.recallcode.model.User;
import com.recallcode.model.UserProblemProgress;
import com.recallcode.repository.ProblemRepository;
import com.recallcode.repository.UserProblemProgressRepository;
import com.recallcode.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProblemProgressService {

    private final UserProblemProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public UserProblemProgressService(
            UserProblemProgressRepository progressRepository,
            UserRepository userRepository,
            ProblemRepository problemRepository) {

        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
    }

    /**
     * Retrieves an existing progress record or creates one for a user and problem.
     */
    @Transactional
    public UserProblemProgress getOrCreateProgress(
            Long userId,
            Long problemId) {

        return progressRepository
                .findByUserIdAndProblemId(userId, problemId)
                .orElseGet(() -> createProgress(userId, problemId));
    }

    /**
     * Creates an independent progress record for a user and problem.
     */
    @Transactional
    public UserProblemProgress createProgress(
            Long userId,
            Long problemId) {

        if (progressRepository.existsByUserIdAndProblemId(userId, problemId)) {
            throw new IllegalStateException(
                    "Progress already exists for this user and problem."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userId));

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Problem not found: " + problemId));

        UserProblemProgress progress =
                new UserProblemProgress(user, problem);

        return progressRepository.save(progress);
    }
}
