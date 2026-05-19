package com.incidentiq.repository;

import com.incidentiq.model.Incident;
import com.incidentiq.model.enums.IncidentStatus;
import com.incidentiq.model.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatusOrderByDetectedAtDesc(IncidentStatus status);

    List<Incident> findBySeverityOrderByDetectedAtDesc(Severity severity);

    List<Incident> findByStatusAndSeverityOrderByDetectedAtDesc(IncidentStatus status, Severity severity);

    List<Incident> findByDetectedAtBetweenOrderByDetectedAtDesc(Instant from, Instant to);

    @Query("SELECT i FROM Incident i WHERE i.status = 'RESOLVED' AND i.postMortem IS NULL")
    List<Incident> findResolvedWithoutPostMortem();

    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.timeline WHERE i.id = :id")
    java.util.Optional<Incident> findByIdWithTimeline(Long id);
}
