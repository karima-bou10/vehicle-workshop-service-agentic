package com.workshop.vehicle_service.mecanicien.repository;

import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MecanicienRepository extends JpaRepository<Mecanicien, Long> {

    Page<Mecanicien> findByActifTrue(Pageable pageable);

    /** Mécaniciens actifs filtrés par disponibilité, paginé (filtrage en base). */
    Page<Mecanicien> findByActifTrueAndDisponible(boolean disponible, Pageable pageable);

    /**
     * Recherche paginée des mécaniciens actifs par nom (partiel, insensible à la
     * casse) et/ou spécialité (exacte, insensible à la casse). Les deux critères
     * sont optionnels (null = pas de filtre) et combinés en ET.
     */
    @Query("SELECT m FROM Mecanicien m WHERE m.actif = true "
            + "AND (:nom IS NULL OR LOWER(m.nom) LIKE CONCAT('%', :nom, '%')) "
            + "AND (:specialite IS NULL OR UPPER(m.specialite) = :specialite)")
    Page<Mecanicien> search(@Param("nom") String nom, @Param("specialite") String specialite, Pageable pageable);
}
