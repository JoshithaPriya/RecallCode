
package com.recallcode.repository;

import com.recallcode.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Provides database operations for DSA attempts.
 * Supports user-specific attempt history and fetching attempts with problem metadata.
 */
public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByProblemId(Long problemId);

    List<Attempt> findByUserId(Long userId);

    List<Attempt> findByUserIdAndProblemId(Long userId, Long problemId);

    Optional<Attempt> findByIdAndUserId(Long attemptId, Long userId);

    @Query("""
        SELECT DISTINCT a
        FROM Attempt a
        JOIN FETCH a.problem p
        LEFT JOIN FETCH p.tags
        WHERE a.id = :attemptId
        """)
    Optional<Attempt> findByIdWithProblemAndTags(
            @Param("attemptId") Long attemptId
    );
    @Query("""
    SELECT DISTINCT a
    FROM Attempt a
    JOIN FETCH a.problem p
    LEFT JOIN FETCH p.tags
    JOIN FETCH a.user u
    WHERE a.id = :attemptId
    """)
Optional<Attempt> findByIdWithProblemTagsAndUser(
        @Param("attemptId") Long attemptId
);

    @Query("""
        SELECT DISTINCT a
        FROM Attempt a
        JOIN FETCH a.problem p
        LEFT JOIN FETCH p.tags
        WHERE a.id = :attemptId
        AND a.user.id = :userId
        """)
    Optional<Attempt> findByIdAndUserIdWithProblemAndTags(
            @Param("attemptId") Long attemptId,
            @Param("userId") Long userId
    );
}
