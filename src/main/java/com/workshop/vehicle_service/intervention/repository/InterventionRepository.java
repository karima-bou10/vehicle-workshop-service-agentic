package com.workshop.vehicle_service.intervention.repository;

import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterventionRepository
        extends JpaRepository<Intervention, Long>, JpaSpecificationExecutor<Intervention> {
    Page<Intervention> findByVehiculeId(Long vehiculeId, Pageable pageable);

    Page<Intervention> findByMecanicienId(Long mecanicienId, Pageable pageable);

    Optional<Intervention> findByNumero(String numero);

    Page<Intervention> findByActifTrue(Pageable pageable);

    Page<Intervention> findByVehiculeIdAndIdNotAndActifTrue(Long vehiculeId, Long excludeId, Pageable pageable);

    // --- Lecture agrégée pour le dashboard (§3.6) — toujours en lecture seule ---

    long countByActifTrueAndDateDepotGreaterThanEqualAndDateDepotLessThan(LocalDateTime debut,
            LocalDateTime finExclusive);

    long countByActifTrueAndStatut(StatutIntervention statut);

    long countByActifTrueAndDateRestitutionPrevueBeforeAndStatutNotIn(LocalDateTime maintenant,
            Collection<StatutIntervention> statutsExclus);

    Page<Intervention> findByActifTrueAndDateRestitutionPrevueBeforeAndStatutNotIn(LocalDateTime maintenant,
            Collection<StatutIntervention> statutsExclus, Pageable pageable);

    @Query("SELECT i.statut AS statut, COUNT(i) AS total FROM Intervention i WHERE i.actif = true GROUP BY i.statut")
    List<StatutCount> countActifsGroupeParStatut();

    @Query("SELECT i.type AS type, COUNT(i) AS total FROM Intervention i WHERE i.actif = true GROUP BY i.type")
    List<TypeCount> countActifsGroupeParType();

    @Query("SELECT i.mecanicien.id AS mecanicienId, COUNT(i) AS total FROM Intervention i "
            + "WHERE i.actif = true AND i.mecanicien IS NOT NULL AND i.statut NOT IN :statutsClotures "
            + "GROUP BY i.mecanicien.id")
    List<MecanicienCharge> chargeActiveParMecanicien(
            @Param("statutsClotures") Collection<StatutIntervention> statutsClotures);

    @Query("SELECT i.dateDepot FROM Intervention i WHERE i.actif = true AND i.dateDepot >= :debut AND i.dateDepot < :fin")
    List<LocalDateTime> findDateDepotDansPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /** Projection de comptage par statut (dashboard — répartition par statut). */
    interface StatutCount {
        StatutIntervention getStatut();

        long getTotal();
    }

    /** Projection de comptage par type (dashboard — répartition par type). */
    interface TypeCount {
        TypeIntervention getType();

        long getTotal();
    }

    /**
     * Projection de charge active par mécanicien (dashboard — charge par
     * mécanicien).
     */
    interface MecanicienCharge {
        Long getMecanicienId();

        long getTotal();
    }

    /** Interventions actives (non archivées) affectées à un mécanicien donné, paginé. */
    Page<Intervention> findByMecanicienIdAndActifTrue(Long mecanicienId, Pageable pageable);

    /** Vrai si le mécanicien possède au moins une intervention active dont le statut n'est pas final. */
    boolean existsByMecanicienIdAndActifTrueAndStatutNotIn(Long mecanicienId,
                                                           Collection<StatutIntervention> statutsFinaux);
}
