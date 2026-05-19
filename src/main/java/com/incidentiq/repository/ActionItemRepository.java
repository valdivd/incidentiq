package com.incidentiq.repository;

import com.incidentiq.model.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);

    List<ActionItem> findByCompletedFalseOrderByDueDateAsc();

    List<ActionItem> findByOwnerAndCompletedFalse(String owner);
}
