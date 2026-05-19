package com.incidentiq.controller;

import com.incidentiq.dto.*;
import com.incidentiq.model.enums.IncidentStatus;
import com.incidentiq.model.enums.Severity;
import com.incidentiq.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident lifecycle management")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "List incidents with optional filters")
    public List<IncidentResponse> list(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Severity severity) {
        return incidentService.findAll(status, severity);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public IncidentResponse get(@PathVariable Long id) {
        return incidentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new incident")
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request) {
        return incidentService.create(request);
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Mark an incident as resolved")
    public IncidentResponse resolve(@PathVariable Long id) {
        return incidentService.resolve(id);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close an incident")
    public IncidentResponse close(@PathVariable Long id) {
        return incidentService.close(id);
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get the timeline for an incident")
    public List<TimelineEventResponse> getTimeline(@PathVariable Long id) {
        return incidentService.getTimeline(id);
    }

    @PostMapping("/{id}/timeline")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a timeline event to an incident")
    public TimelineEventResponse addTimelineEvent(
            @PathVariable Long id,
            @Valid @RequestBody AddTimelineEventRequest request) {
        return incidentService.addTimelineEvent(id, request);
    }

    @GetMapping("/{id}/action-items")
    @Operation(summary = "Get action items for an incident")
    public List<ActionItemResponse> getActionItems(@PathVariable Long id) {
        return incidentService.getActionItems(id);
    }

    @PostMapping("/{id}/action-items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an action item to an incident")
    public ActionItemResponse createActionItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateActionItemRequest request) {
        return incidentService.createActionItem(id, request);
    }

    @PostMapping("/action-items/{actionItemId}/complete")
    @Operation(summary = "Mark an action item as complete")
    public ActionItemResponse completeActionItem(@PathVariable Long actionItemId) {
        return incidentService.completeActionItem(actionItemId);
    }
}
