
package com.recallcode.repository;

import com.recallcode.model.ProblemAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Provides database access to cached and versioned problem analyses.
public interface ProblemAnalysisRepository
        extends JpaRepository<ProblemAnalysis, Long> {

    List<ProblemAnalysis> findByProblemIdOrderByAnalyzedAtDesc(
            Long problemId
    );

    Optional<ProblemAnalysis> findFirstByProblemIdAndContentHashAndPromptVersionOrderByAnalyzedAtDesc(
        Long problemId, String contentHash, String promptVersion);

    Optional<ProblemAnalysis> findFirstByProblemIdOrderByAnalyzedAtDesc(
            Long problemId
    );

    boolean existsByProblemIdAndContentHash(
            Long problemId,
            String contentHash
    );
}
