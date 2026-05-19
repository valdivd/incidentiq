package com.incidentiq.ai;

public record PostMortemSections(
        String summary,
        String rootCause,
        String contributingFactors,
        String impact,
        String timeline,
        String lessonsLearned
) {}
