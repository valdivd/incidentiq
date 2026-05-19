package com.incidentiq.service;

import com.incidentiq.dto.*;
import com.incidentiq.exception.IncidentNotFoundException;
import com.incidentiq.model.ActionItem;
import com.incidentiq.model.Incident;
import com.incidentiq.model.TimelineEvent;
import com.incidentiq.model.enums.IncidentStatus;
import com.incidentiq.model.enums.Severity;
import com.incidentiq.repository.ActionItemRepository;
import com.incidentiq.repository.IncidentRepository;
import com.incidentiq.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final ActionItemRepository actionItemRepository;

    public List<IncidentResponse> findAll(IncidentStatus status, Severity severity) {
        List<Incident> incidents;
        if (status != null && severity != null) {
            incidents = incidentRepository.findByStatusAndSeverityOrderByDetectedAtDesc(status, severity);
        } else if (status != null) {
            incidents = incidentRepository.findByStatusOrderByDetectedAtDesc(status);
        } else if (severity != null) {
            incidents = incidentRepository.findBySeverityOrderByDetectedAtDesc(severity);
        } else {
            incidents = incidentRepository.findAll();
        }
        return incidents.stream().map(IncidentResponse::from).toList();
    }

    public IncidentResponse findById(Long id) {
        return IncidentResponse.from(getOrThrow(id));
    }

    @Transactional
    public IncidentResponse create(CreateIncidentRequest req) {
        Incident incident = new Incident();
        incident.setTitle(req.title());
        incident.setDescription(req.description());
        incident.setSeverity(req.severity());
        incident.setAffectedServices(req.affectedServices());
        incident.setIncidentCommander(req.incidentCommander());
        incident.setDetectedAt(req.detectedAt());
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentResponse resolve(Long id) {
        Incident incident = getOrThrow(id);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(Instant.now());
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentResponse close(Long id) {
        Incident incident = getOrThrow(id);
        incident.setStatus(IncidentStatus.CLOSED);
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    public List<TimelineEventResponse> getTimeline(Long incidentId) {
        getOrThrow(incidentId);
        return timelineEventRepository.findByIncidentIdOrderByOccurredAtAsc(incidentId)
                .stream().map(TimelineEventResponse::from).toList();
    }

    @Transactional
    public TimelineEventResponse addTimelineEvent(Long incidentId, AddTimelineEventRequest req) {
        Incident incident = getOrThrow(incidentId);
        TimelineEvent event = new TimelineEvent();
        event.setIncident(incident);
        event.setDescription(req.description());
        event.setAuthor(req.author());
        event.setOccurredAt(req.occurredAt());
        return TimelineEventResponse.from(timelineEventRepository.save(event));
    }

    public List<ActionItemResponse> getActionItems(Long incidentId) {
        getOrThrow(incidentId);
        return actionItemRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream().map(ActionItemResponse::from).toList();
    }

    @Transactional
    public ActionItemResponse createActionItem(Long incidentId, CreateActionItemRequest req) {
        Incident incident = getOrThrow(incidentId);
        ActionItem item = new ActionItem();
        item.setIncident(incident);
        item.setDescription(req.description());
        item.setOwner(req.owner());
        item.setDueDate(req.dueDate());
        return ActionItemResponse.from(actionItemRepository.save(item));
    }

    @Transactional
    public ActionItemResponse completeActionItem(Long actionItemId) {
        ActionItem item = actionItemRepository.findById(actionItemId)
                .orElseThrow(() -> new IncidentNotFoundException("Action item not found: " + actionItemId));
        item.setCompleted(true);
        item.setCompletedAt(Instant.now());
        return ActionItemResponse.from(actionItemRepository.save(item));
    }

    public Incident getOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found: " + id));
    }
}
