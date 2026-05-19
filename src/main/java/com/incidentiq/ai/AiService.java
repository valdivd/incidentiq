package com.incidentiq.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.anthropic.model:claude-haiku-4-5-20251001}")
    private String model;

    public AiService(RestClient.Builder builder,
                     @Value("${ai.anthropic.api-key}") String apiKey,
                     ObjectMapper objectMapper) {
        this.restClient = builder
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    public PostMortemSections generatePostMortem(PostMortemContext context) {
        String prompt = buildPostMortemPrompt(context);
        String raw = callClaude(prompt);
        return parsePostMortemResponse(raw);
    }

    public String detectPatterns(List<String> incidentSummaries) {
        if (incidentSummaries.isEmpty()) return "No incidents to analyze.";

        String bulletList = incidentSummaries.stream()
                .map(s -> "- " + s)
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = """
                You are analyzing a set of recent engineering incidents to identify recurring patterns.

                Incidents:
                %s

                Identify the top recurring themes, root cause categories, and systemic gaps.
                Be specific and actionable. Format as plain text with sections:
                RECURRING THEMES, ROOT CAUSE CATEGORIES, SYSTEMIC GAPS, RECOMMENDED FOCUS AREAS.
                """.formatted(bulletList);

        return callClaude(prompt);
    }

    private String buildPostMortemPrompt(PostMortemContext ctx) {
        return """
                You are an expert site reliability engineer writing a blameless post-mortem.

                INCIDENT DETAILS
                Title: %s
                Severity: %s
                Detected: %s
                Resolved: %s
                Affected Services: %s
                Description: %s

                TIMELINE
                %s

                Write a structured blameless post-mortem in JSON format with exactly these fields:
                {
                  "summary": "2-3 sentence executive summary",
                  "rootCause": "primary root cause",
                  "contributingFactors": "contributing factors as a numbered list",
                  "impact": "customer and business impact",
                  "timeline": "narrative timeline summary",
                  "lessonsLearned": "key lessons and preventive measures as a numbered list"
                }

                Be specific, blameless, and actionable. Return only valid JSON.
                """.formatted(
                ctx.title(),
                ctx.severity(),
                ctx.detectedAt(),
                ctx.resolvedAt() != null ? ctx.resolvedAt().toString() : "unresolved",
                ctx.affectedServices() != null ? ctx.affectedServices() : "unknown",
                ctx.description() != null ? ctx.description() : "not provided",
                ctx.timelineText()
        );
    }

    private String callClaude(String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 2048,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        try {
            String response = restClient.post()
                    .uri("/v1/messages")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    private PostMortemSections parsePostMortemResponse(String raw) {
        try {
            // Strip markdown code fences if present
            String json = raw.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }
            JsonNode node = objectMapper.readTree(json);
            return new PostMortemSections(
                    node.path("summary").asText(),
                    node.path("rootCause").asText(),
                    node.path("contributingFactors").asText(),
                    node.path("impact").asText(),
                    node.path("timeline").asText(),
                    node.path("lessonsLearned").asText()
            );
        } catch (Exception e) {
            log.warn("Failed to parse post-mortem JSON, using raw text as summary. Error: {}", e.getMessage());
            return new PostMortemSections(raw, null, null, null, null, null);
        }
    }
}
