package com.incidentiq.service;

import com.incidentiq.ai.AiService;
import com.incidentiq.model.Incident;
import com.incidentiq.model.enums.IncidentStatus;
import com.incidentiq.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatternService {

    private final IncidentRepository incidentRepository;
    private final AiService aiService;

    private volatile String cachedPatternAnalysis = "No analysis available yet.";
    private volatile Instant lastAnalyzedAt;

    public String getLatestAnalysis() {
        return cachedPatternAnalysis;
    }

    public Instant getLastAnalyzedAt() {
        return lastAnalyzedAt;
    }

    // Runs daily at 2am
    @Scheduled(cron = "0 0 2 * * *")
    public void runScheduledAnalysis() {
        log.info("Running scheduled incident pattern analysis");
        analyzeRecentIncidents();
    }

    public String analyzeNow() {
        return analyzeRecentIncidents();
    }

    private String analyzeRecentIncidents() {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Incident> recent = incidentRepository
                .findByDetectedAtBetweenOrderByDetectedAtDesc(thirtyDaysAgo, Instant.now())
                .stream()
                .filter(i -> i.getStatus() != IncidentStatus.OPEN)
                .toList();

        if (recent.isEmpty()) {
            cachedPatternAnalysis = "No resolved incidents in the last 30 days.";
            lastAnalyzedAt = Instant.now();
            return cachedPatternAnalysis;
        }

        List<String> summaries = recent.stream()
                .map(i -> "[%s] %s — %s".formatted(i.getSeverity(), i.getTitle(),
                        i.getDescription() != null ? i.getDescription() : "no description"))
                .toList();

        String analysis = aiService.detectPatterns(summaries);
        cachedPatternAnalysis = analysis;
        lastAnalyzedAt = Instant.now();
        log.info("Pattern analysis complete for {} incidents", recent.size());
        return analysis;
    }
}
