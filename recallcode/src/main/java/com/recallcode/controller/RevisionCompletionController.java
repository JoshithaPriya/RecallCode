
package com.recallcode.controller;

import com.recallcode.model.Problem;
import com.recallcode.service.RevisionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


//The frontend now reports how well you remembered the solution instead of choosing the number of days itself

@RestController
@RequestMapping("/api/revisions")
public class RevisionCompletionController {

    private final RevisionService revisionService;

    public RevisionCompletionController(RevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @PostMapping("/{problemId}/complete")
    public Problem completeRevision(
            @PathVariable Long problemId,
            @RequestBody Map<String, String> request) {

        String quality = request.get("recallQuality");

        if (quality == null) {
            throw new IllegalArgumentException(
                    "recallQuality is required");
        }

        return revisionService.completeRevision(problemId, quality);
    }
}
