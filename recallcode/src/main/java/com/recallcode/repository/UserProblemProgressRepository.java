
package com.recallcode.repository;

import com.recallcode.model.UserProblemProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Provides database operations for user-specific problem progress.
 * Retrieves revision schedules and tracks problems due for revision.
 */
public interface UserProblemProgressRepository
        extends JpaRepository<UserProblemProgress, Long> {

    Optional<UserProblemProgress> findByUserIdAndProblemId(
            Long userId,
            Long problemId
    );

    List<UserProblemProgress> findByUserId(Long userId);

    List<UserProblemProgress> findByUserIdAndNextRevisionAtBefore(
            Long userId,
            LocalDateTime dateTime
    );

    List<UserProblemProgress> findByUserIdAndNextRevisionAtLessThanEqual(
        Long userId,
        LocalDateTime dateTime
);

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);
}
