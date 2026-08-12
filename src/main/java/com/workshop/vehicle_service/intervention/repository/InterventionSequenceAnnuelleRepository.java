package com.workshop.vehicle_service.intervention.repository;

import com.workshop.vehicle_service.intervention.entity.InterventionSequenceAnnuelle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionSequenceAnnuelleRepository extends JpaRepository<InterventionSequenceAnnuelle, Integer> {
}
