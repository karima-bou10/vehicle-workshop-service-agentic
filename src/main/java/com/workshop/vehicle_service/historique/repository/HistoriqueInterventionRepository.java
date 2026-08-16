package com.workshop.vehicle_service.historique.repository;

import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoriqueInterventionRepository extends JpaRepository<HistoriqueIntervention, Long> {

    Page<HistoriqueIntervention> findByInterventionId(Long interventionId, Pageable pageable);

    /** Dates des transitions vers un statut donné dans une période — utilisé pour le dashboard (série "terminées"). */
    @Query("SELECT h.date FROM HistoriqueIntervention h "
            + "WHERE h.nouveauStatut = :statut AND h.date >= :debut AND h.date < :fin")
    List<LocalDateTime> findDatesTransitionDansPeriode(@Param("statut") StatutIntervention statut,
            @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
