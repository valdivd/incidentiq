package com.incidentiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AddTimelineEventRequest(
        @NotBlank String description,
        String author,
        @NotNull Instant occurredAt
) {}
