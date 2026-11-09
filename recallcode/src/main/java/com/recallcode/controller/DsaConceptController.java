
package com.recallcode.controller;

import com.recallcode.model.DsaConcept;
import com.recallcode.repository.DsaConceptRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

// Provides read-only API access to the shared DSA knowledge catalogue.
@RestController
@RequestMapping("/api/concepts")
public class DsaConceptController {

    private final DsaConceptRepository conceptRepository;

    public DsaConceptController(DsaConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @GetMapping
    public List<ConceptResponse> getAllConcepts() {
        return conceptRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ConceptResponse getConcept(@PathVariable Long id) {
        DsaConcept concept = conceptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "DSA concept not found: " + id
                ));

        return toResponse(concept);
    }

    @GetMapping("/type/{type}")
    public List<ConceptResponse> getConceptsByType(
            @PathVariable DsaConcept.ConceptType type) {

        return conceptRepository.findByConceptType(type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ConceptResponse toResponse(DsaConcept concept) {
        return new ConceptResponse(
                concept.getId(),
                concept.getName(),
                concept.getSlug(),
                concept.getConceptType().name(),
                concept.getDescription(),
                concept.getParent() == null
                        ? null
                        : concept.getParent().getId(),
                concept.getParent() == null
                        ? null
                        : concept.getParent().getName()
        );
    }

    public record ConceptResponse(
            Long id,
            String name,
            String slug,
            String conceptType,
            String description,
            Long parentId,
            String parentName
    ) {}
}
