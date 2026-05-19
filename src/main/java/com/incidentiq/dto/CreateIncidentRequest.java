package com.incidentiq.dto;

import com.incidentiq.model.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateIncidentRequest(
        @NotBlank String title,
        String description,
        @NotNull Severity severity,
        String affectedServices,
        String incidentCommander,
        @NotNull Instant detectedAt
) {}
