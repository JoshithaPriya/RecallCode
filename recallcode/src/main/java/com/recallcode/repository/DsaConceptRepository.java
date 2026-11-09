
package com.recallcode.repository;

import com.recallcode.model.DsaConcept;
import com.recallcode.model.DsaConcept.ConceptType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Provides database access to the shared DSA concept catalogue.
public interface DsaConceptRepository
        extends JpaRepository<DsaConcept, Long> {

    Optional<DsaConcept> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<DsaConcept> findByConceptType(ConceptType conceptType);

    List<DsaConcept> findByParentId(Long parentId);

    List<DsaConcept> findByParentIsNull();

    List<DsaConcept> findByNameContainingIgnoreCase(String name);
}
