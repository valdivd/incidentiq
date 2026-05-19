package com.incidentiq.repository;

import com.incidentiq.model.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {

    List<TimelineEvent> findByIncidentIdOrderByOccurredAtAsc(Long incidentId);
}
