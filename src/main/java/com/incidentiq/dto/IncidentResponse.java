package com.incidentiq.dto;

import com.incidentiq.model.Incident;
import com.incidentiq.model.enums.IncidentStatus;
import com.incidentiq.model.enums.Severity;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        Severity severity,
        IncidentStatus status,
        String affectedServices,
        String incidentCommander,
        Instant detectedAt,
        Instant resolvedAt,
        Instant createdAt,
        boolean hasPostMortem
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getAffectedServices(),
                incident.getIncidentCommander(),
                incident.getDetectedAt(),
                incident.getResolvedAt(),
                incident.getCreatedAt(),
                incident.getPostMortem() != null
        );
    }
}
