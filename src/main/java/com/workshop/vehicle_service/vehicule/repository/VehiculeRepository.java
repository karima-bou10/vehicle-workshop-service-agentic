package com.workshop.vehicle_service.vehicule.repository;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    @Query("""
    SELECT v
    FROM Vehicule v
    WHERE v.actif = true
      AND (
            LOWER(v.immatriculationFictive) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(v.clientFictif) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<Vehicule> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByImmatriculationFictive(String immatriculationFictive);

    List<Vehicule> findByInterventionsStatut(StatutIntervention statut);
    Page<Vehicule> findByActifTrue(Pageable pageable);

}
