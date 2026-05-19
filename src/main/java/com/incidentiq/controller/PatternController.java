package com.incidentiq.controller;

import com.incidentiq.service.PatternService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
@Tag(name = "Patterns", description = "AI-powered incident pattern detection across history")
public class PatternController {

    private final PatternService patternService;

    @GetMapping
    @Operation(summary = "Get the latest pattern analysis (updated daily)")
    public Map<String, Object> getLatest() {
        return Map.of(
                "analysis", patternService.getLatestAnalysis(),
                "analyzedAt", patternService.getLastAnalyzedAt() != null
                        ? patternService.getLastAnalyzedAt()
                        : "not yet run"
        );
    }

    @PostMapping("/analyze")
    @Operation(summary = "Trigger an immediate pattern analysis over the last 30 days")
    public Map<String, Object> analyzeNow() {
        String result = patternService.analyzeNow();
        return Map.of(
                "analysis", result,
                "analyzedAt", Instant.now()
        );
    }
}
