package com.incidentiq.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateActionItemRequest(
        @NotBlank String description,
        String owner,
        LocalDate dueDate
) {}
