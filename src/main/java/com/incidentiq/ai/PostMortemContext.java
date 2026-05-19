package com.incidentiq.ai;

import java.time.Instant;

public record PostMortemContext(
        String title,
        String severity,
        Instant detectedAt,
        Instant resolvedAt,
        String affectedServices,
        String description,
        String timelineText
) {}
