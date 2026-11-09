
package com.recallcode.repository;

import com.recallcode.model.AnalysisConcept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Provides database access to DSA techniques mapped to problem analyses.
public interface AnalysisConceptRepository
        extends JpaRepository<AnalysisConcept, Long> {

    List<AnalysisConcept> findByAnalysisId(Long analysisId);

    List<AnalysisConcept> findByConceptId(Long conceptId);

    List<AnalysisConcept> findByAnalysisProblemId(Long problemId);

    void deleteByAnalysisId(Long analysisId);

    boolean existsByAnalysisIdAndConceptId(
            Long analysisId,
            Long conceptId
    );
}
