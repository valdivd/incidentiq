package com.incidentiq.dto;

import com.incidentiq.model.ActionItem;

import java.time.Instant;
import java.time.LocalDate;

public record ActionItemResponse(
        Long id,
        Long incidentId,
        String description,
        String owner,
        LocalDate dueDate,
        boolean completed,
        Instant completedAt,
        Instant createdAt
) {
    public static ActionItemResponse from(ActionItem item) {
        return new ActionItemResponse(
                item.getId(),
                item.getIncident().getId(),
                item.getDescription(),
                item.getOwner(),
                item.getDueDate(),
                item.isCompleted(),
                item.getCompletedAt(),
                item.getCreatedAt()
        );
    }
}
