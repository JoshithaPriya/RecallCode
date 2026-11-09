
package com.recallcode.repository;

import com.recallcode.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

// This repository lets RecallCode save, retrieve, update, and delete problems
// in PostgreSQL. It also finds an existing problem using its URL.
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByProblemUrl(String problemUrl);
    Optional<Problem> findFirstByPlatformAndExternalId(
        String platform, String externalId);

    @Query("""
    SELECT DISTINCT p
    FROM Problem p
    WHERE p.user.id = :userId
       OR EXISTS (
           SELECT a.id
           FROM Attempt a
           WHERE a.problem.id = p.id
             AND a.user.id = :userId
       )
    """)
    List<Problem> findVisibleToUser(@Param("userId") Long userId);
    List<Problem> findByUserId(Long userId);
}
