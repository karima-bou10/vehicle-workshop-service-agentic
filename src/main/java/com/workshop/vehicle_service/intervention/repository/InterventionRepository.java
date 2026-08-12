package com.workshop.vehicle_service.intervention.repository;

import com.workshop.vehicle_service.intervention.entity.Intervention;
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
}
