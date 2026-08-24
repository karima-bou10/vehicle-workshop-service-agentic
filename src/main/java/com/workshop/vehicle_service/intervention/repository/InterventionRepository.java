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
    List<MecanicienCharge> chargeActiveParMecanicien(@Param("statutsClotures") Collection<StatutIntervention> statutsClotures);

        @Query("SELECT i.mecanicien.id AS mecanicienId, COUNT(i) AS total FROM Intervention i "
            + "WHERE i.actif = true AND i.dateRestitutionPrevue < :now AND i.statut NOT IN :statutsExclus "
            + "AND i.mecanicien IS NOT NULL GROUP BY i.mecanicien.id")
        List<MecanicienCharge> countRetardsParMecanicien(@Param("now") LocalDateTime now,
                                @Param("statutsExclus") Collection<StatutIntervention> statutsExclus);

        @Query("SELECT i.mecanicien.id AS mecanicienId, i.statut AS statut, COUNT(i) AS total FROM Intervention i "
            + "WHERE i.actif = true AND i.mecanicien IS NOT NULL GROUP BY i.mecanicien.id, i.statut")
        List<MecanicienStatutCount> countActifsGroupeParMecanicienEtStatut();

        /** Projection: compteur par mécanicien et statut */
        interface MecanicienStatutCount {
        Long getMecanicienId();

        StatutIntervention getStatut();

        long getTotal();
        }

        /** Projection: moyenne délai (secondes) par mécanicien (native query, Postgres). */
        interface MecanicienAvgDelay {
        Long getMecanicienId();

        Double getAvgSeconds();
        }

        @Query(value = "SELECT i.mecanicien_id AS mecanicienId, AVG(EXTRACT(EPOCH FROM (i.date_cloture - i.date_depot))) AS avg_seconds "
            + "FROM intervention i "
            + "WHERE i.actif = true AND i.statut = :statut AND i.date_depot IS NOT NULL AND i.date_cloture IS NOT NULL "
            + "GROUP BY i.mecanicien_id", nativeQuery = true)
        List<MecanicienAvgDelay> moyenneDelaiTraitementParMecanicien(@Param("statut") String statut);

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

    /** Projection de charge active par mécanicien (dashboard — charge par mécanicien). */
    interface MecanicienCharge {
        Long getMecanicienId();

        long getTotal();
    }
}
