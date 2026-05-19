package com.incidentiq.repository;

import com.incidentiq.model.PostMortem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostMortemRepository extends JpaRepository<PostMortem, Long> {

    Optional<PostMortem> findByIncidentId(Long incidentId);

    boolean existsByIncidentId(Long incidentId);
}
