
package com.recallcode.repository;

import com.recallcode.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// This repository lets RecallCode save, retrieve, update, and delete problems
// in PostgreSQL. It also finds an existing problem using its URL.
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByProblemUrl(String problemUrl);
    Optional<Problem> findFirstByPlatformAndExternalId(
        String platform, String externalId);
}
