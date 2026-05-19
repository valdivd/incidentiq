package com.incidentiq.controller;

import com.incidentiq.dto.PostMortemResponse;
import com.incidentiq.service.PostMortemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents/{incidentId}/postmortem")
@RequiredArgsConstructor
@Tag(name = "Post-Mortems", description = "AI-generated blameless post-mortems")
public class PostMortemController {

    private final PostMortemService postMortemService;

    @GetMapping
    @Operation(summary = "Get post-mortem for an incident")
    public PostMortemResponse get(@PathVariable Long incidentId) {
        return postMortemService.getForIncident(incidentId);
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Trigger async AI post-mortem generation. Poll GET to check status.")
    public PostMortemResponse generate(@PathVariable Long incidentId) {
        return postMortemService.initiate(incidentId);
    }
}
