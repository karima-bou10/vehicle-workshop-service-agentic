package com.workshop.vehicle_service.vehicule.repository;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long>, JpaSpecificationExecutor<Vehicule> {
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
    Optional<Vehicule> findByImmatriculationFictive(String immatriculationFictive);

    List<Vehicule> findByInterventionsStatut(StatutIntervention statut);
    Page<Vehicule> findByActifTrue(Pageable pageable);

        @Query("""
        SELECT v
        FROM Vehicule v
        WHERE LOWER(v.immatriculationFictive) LIKE
              CONCAT('%', LOWER(COALESCE(:immatriculation, '')), '%')
    
          AND LOWER(v.marque) LIKE
              CONCAT('%', LOWER(COALESCE(:marque, '')), '%')
    
          AND LOWER(v.modele) LIKE
              CONCAT('%', LOWER(COALESCE(:modele, '')), '%')
    
          AND (:annee IS NULL OR v.annee = :annee)
    
          AND LOWER(v.clientFictif) LIKE
              CONCAT('%', LOWER(COALESCE(:clientFictif, '')), '%')
    
          AND (:actif IS NULL OR v.actif = :actif)
        """)
        Page<Vehicule> searchVehicules(
                @Param("immatriculation") String immatriculation,
                @Param("marque") String marque,
                @Param("modele") String modele,
                @Param("annee") Integer annee,
                @Param("clientFictif") String clientFictif,
                @Param("actif") Boolean actif,
                Pageable pageable
        );







}
