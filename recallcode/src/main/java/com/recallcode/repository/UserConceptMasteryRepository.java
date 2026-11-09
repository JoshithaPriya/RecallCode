
package com.recallcode.repository;

import com.recallcode.model.UserConceptMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * UserConceptMasteryRepository.java
 *
 * Provides database operations for retrieving and updating
 * individual users' BKT mastery records for DSA concepts.
 */

@Repository
public interface UserConceptMasteryRepository
        extends JpaRepository<UserConceptMastery, Long> {

    Optional<UserConceptMastery> findByUserIdAndConceptId(
            Long userId,
            Long conceptId
    );

    List<UserConceptMastery> findByUserId(Long userId);

    List<UserConceptMastery> findByUserIdOrderByMasteryProbabilityAsc(
            Long userId
    );

    List<UserConceptMastery> findByUserIdOrderByMasteryProbabilityDesc(
            Long userId
    );
}
