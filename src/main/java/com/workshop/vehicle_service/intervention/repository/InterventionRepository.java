package com.workshop.vehicle_service.intervention.repository;

import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {
    Page<Intervention> findByVehiculeId(Long vehiculeId, Pageable pageable);

    Page<Intervention> findByMecanicienId(Long mecanicienId, Pageable pageable);

    Optional<Intervention> findByNumero(String numero);

    Page<Intervention> findByActifTrue(Pageable pageable);

    Page<Intervention> findByVehiculeIdAndIdNotAndActifTrue(Long vehiculeId, Long excludeId, Pageable pageable);

    /** Interventions actives (non archivées) affectées à un mécanicien donné, paginé. */
    Page<Intervention> findByMecanicienIdAndActifTrue(Long mecanicienId, Pageable pageable);

    /** Vrai si le mécanicien possède au moins une intervention active dont le statut n'est pas final. */
    boolean existsByMecanicienIdAndActifTrueAndStatutNotIn(Long mecanicienId,
            Collection<StatutIntervention> statutsFinaux);
}
