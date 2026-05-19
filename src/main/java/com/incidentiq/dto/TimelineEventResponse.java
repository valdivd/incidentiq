package com.incidentiq.dto;

import com.incidentiq.model.TimelineEvent;

import java.time.Instant;

public record TimelineEventResponse(
        Long id,
        String description,
        String author,
        Instant occurredAt
) {
    public static TimelineEventResponse from(TimelineEvent event) {
        return new TimelineEventResponse(
                event.getId(),
                event.getDescription(),
                event.getAuthor(),
                event.getOccurredAt()
        );
    }
}
