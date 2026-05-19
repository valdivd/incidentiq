package com.incidentiq.service;

import com.incidentiq.ai.AiService;
import com.incidentiq.ai.PostMortemContext;
import com.incidentiq.ai.PostMortemSections;
import com.incidentiq.dto.PostMortemResponse;
import com.incidentiq.exception.IncidentNotFoundException;
import com.incidentiq.model.Incident;
import com.incidentiq.model.PostMortem;
import com.incidentiq.model.TimelineEvent;
import com.incidentiq.model.enums.IncidentStatus;
import com.incidentiq.model.enums.PostMortemStatus;
import com.incidentiq.repository.PostMortemRepository;
import com.incidentiq.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostMortemService {

    private final PostMortemRepository postMortemRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final IncidentService incidentService;
    private final AiService aiService;

    @Transactional(readOnly = true)
    public PostMortemResponse getForIncident(Long incidentId) {
        return postMortemRepository.findByIncidentId(incidentId)
                .map(PostMortemResponse::from)
                .orElseThrow(() -> new IncidentNotFoundException("No post-mortem found for incident: " + incidentId));
    }

    @Transactional
    public PostMortemResponse initiate(Long incidentId) {
        Incident incident = incidentService.getOrThrow(incidentId);

        if (incident.getStatus() == IncidentStatus.OPEN) {
            throw new IllegalStateException("Cannot generate post-mortem for an open incident");
        }
        if (postMortemRepository.existsByIncidentId(incidentId)) {
            throw new IllegalStateException("Post-mortem already exists for incident: " + incidentId);
        }

        PostMortem pm = new PostMortem();
        pm.setIncident(incident);
        pm.setStatus(PostMortemStatus.GENERATING);
        incident.setStatus(IncidentStatus.POST_MORTEM_PENDING);
        PostMortem saved = postMortemRepository.save(pm);

        generateAsync(saved.getId(), incidentId);

        return PostMortemResponse.from(saved);
    }

    @Async
    public void generateAsync(Long postMortemId, Long incidentId) {
        try {
            Incident incident = incidentService.getOrThrow(incidentId);
            List<TimelineEvent> events = timelineEventRepository.findByIncidentIdOrderByOccurredAtAsc(incidentId);

            String timelineText = events.stream()
                    .map(e -> "[%s] %s%s".formatted(
                            DateTimeFormatter.ISO_INSTANT.format(e.getOccurredAt()),
                            e.getAuthor() != null ? "(" + e.getAuthor() + ") " : "",
                            e.getDescription()))
                    .collect(Collectors.joining("\n"));

            PostMortemContext context = new PostMortemContext(
                    incident.getTitle(),
                    incident.getSeverity().name(),
                    incident.getDetectedAt(),
                    incident.getResolvedAt(),
                    incident.getAffectedServices(),
                    incident.getDescription(),
                    timelineText.isEmpty() ? "No timeline events recorded." : timelineText
            );

            PostMortemSections sections = aiService.generatePostMortem(context);
            applyGeneratedSections(postMortemId, sections, PostMortemStatus.READY);
        } catch (Exception e) {
            log.error("Post-mortem generation failed for postMortemId={}", postMortemId, e);
            markFailed(postMortemId, e.getMessage());
        }
    }

    @Transactional
    protected void applyGeneratedSections(Long postMortemId, PostMortemSections sections, PostMortemStatus status) {
        PostMortem pm = postMortemRepository.findById(postMortemId).orElseThrow();
        pm.setSummary(sections.summary());
        pm.setRootCause(sections.rootCause());
        pm.setContributingFactors(sections.contributingFactors());
        pm.setImpact(sections.impact());
        pm.setTimeline(sections.timeline());
        pm.setLessonsLearned(sections.lessonsLearned());
        pm.setStatus(status);
        postMortemRepository.save(pm);
    }

    @Transactional
    protected void markFailed(Long postMortemId, String message) {
        postMortemRepository.findById(postMortemId).ifPresent(pm -> {
            pm.setStatus(PostMortemStatus.FAILED);
            pm.setFailureMessage(message);
            postMortemRepository.save(pm);
        });
    }
}
