
package com.recallcode.config;

import com.recallcode.model.DsaConcept;
import com.recallcode.model.DsaConcept.ConceptType;
import com.recallcode.repository.DsaConceptRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Initializes the shared DSA knowledge catalogue without creating duplicates.
@Component
public class DsaConceptInitializer implements CommandLineRunner {

    private final DsaConceptRepository conceptRepository;

    public DsaConceptInitializer(DsaConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @Override
    public void run(String... args) {

        create("Arrays", "arrays", ConceptType.DOMAIN,
                "Techniques for processing indexed sequential data.", null);

        create("Strings", "strings", ConceptType.DOMAIN,
                "Techniques for processing character sequences.", null);

        create("Linked Lists", "linked-lists", ConceptType.DOMAIN,
                "Techniques for manipulating linked data structures.", null);

        create("Trees", "trees", ConceptType.DOMAIN,
                "Hierarchical data structures and traversal techniques.", null);

        create("Graphs", "graphs", ConceptType.DOMAIN,
                "Techniques for representing and traversing relationships.", null);

        create("Dynamic Programming", "dynamic-programming", ConceptType.DOMAIN,
                "Solving problems using overlapping subproblems and stored results.", null);

        create("Searching and Sorting", "searching-sorting", ConceptType.DOMAIN,
                "Techniques for locating and ordering data.", null);

        create("Two Pointers", "two-pointers", ConceptType.PATTERN,
                "Using two indices or references to process data efficiently.", "arrays");

        create("Sliding Window", "sliding-window", ConceptType.PATTERN,
                "Maintaining a changing range over sequential data.", "arrays");

        create("Prefix Sum", "prefix-sum", ConceptType.PATTERN,
                "Precomputing cumulative values for efficient range calculations.", "arrays");

        create("Fast and Slow Pointers", "fast-slow-pointers", ConceptType.PATTERN,
                "Using pointers moving at different speeds.", "linked-lists");

        create("Binary Search", "binary-search", ConceptType.PATTERN,
                "Reducing a search space using an ordering property.", "searching-sorting");

        create("Backtracking", "backtracking", ConceptType.PATTERN,
                "Exploring candidate solutions and undoing choices when necessary.", null);

        create("Floyd's Cycle Detection", "floyd-cycle-detection", ConceptType.TECHNIQUE,
                "Detecting cycles using two pointers moving at different speeds.",
                "fast-slow-pointers");

        create("Fixed Size Sliding Window", "fixed-sliding-window", ConceptType.TECHNIQUE,
                "Processing consecutive elements using a window of fixed length.",
                "sliding-window");

        create("Variable Size Sliding Window", "variable-sliding-window", ConceptType.TECHNIQUE,
                "Expanding and shrinking a range according to a condition.",
                "sliding-window");

        create("Kadane's Algorithm", "kadanes-algorithm", ConceptType.TECHNIQUE,
                "Finding a maximum subarray sum using a running local optimum.",
                "arrays");

        create("Binary Search on Answer", "binary-search-on-answer", ConceptType.TECHNIQUE,
                "Searching for an optimal feasible answer using monotonicity.",
                "binary-search");

        create("Merge Sort", "merge-sort", ConceptType.TECHNIQUE,
                "Sorting by recursively dividing and merging ordered portions.",
                "searching-sorting");


        create("Simulation", "simulation", ConceptType.PATTERN,
        "Solving problems by explicitly modelling and following a sequence of operations.",
        "strings");

        create("Directional Simulation", "directional-simulation", ConceptType.TECHNIQUE,
        
        "Simulating movement through states or positions by following defined directions and changing direction when required.",
        "simulation");
    }

    private void create(
            String name,
            String slug,
            ConceptType type,
            String description,
            String parentSlug
    ) {
        if (conceptRepository.existsBySlug(slug)) {
            return;
        }

        DsaConcept concept = new DsaConcept(name, slug, type, description);

        if (parentSlug != null) {
            DsaConcept parent = conceptRepository.findBySlug(parentSlug)
                    .orElseThrow(() -> new IllegalStateException(
                            "Parent concept not found: " + parentSlug
                    ));

            concept.setParent(parent);
        }

        conceptRepository.save(concept);
    }
}
