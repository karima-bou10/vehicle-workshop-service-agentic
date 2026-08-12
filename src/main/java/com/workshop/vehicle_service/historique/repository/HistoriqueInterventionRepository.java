package com.workshop.vehicle_service.historique.repository;

import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueInterventionRepository extends JpaRepository<HistoriqueIntervention, Long> {
}
