package com.incidentiq.dto;

import com.incidentiq.model.PostMortem;
import com.incidentiq.model.enums.PostMortemStatus;

import java.time.Instant;

public record PostMortemResponse(
        Long id,
        Long incidentId,
        PostMortemStatus status,
        String summary,
        String rootCause,
        String contributingFactors,
        String impact,
        String timeline,
        String lessonsLearned,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostMortemResponse from(PostMortem pm) {
        return new PostMortemResponse(
                pm.getId(),
                pm.getIncident().getId(),
                pm.getStatus(),
                pm.getSummary(),
                pm.getRootCause(),
                pm.getContributingFactors(),
                pm.getImpact(),
                pm.getTimeline(),
                pm.getLessonsLearned(),
                pm.getCreatedAt(),
                pm.getUpdatedAt()
        );
    }
}
