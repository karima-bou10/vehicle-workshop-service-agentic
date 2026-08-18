package com.workshop.vehicle_service.mecanicien.repository;

import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MecanicienRepository extends JpaRepository<Mecanicien, Long> {

    Page<Mecanicien> findByActifTrue(Pageable pageable);

    /** Liste complète (non paginée) des mécaniciens actifs — utilisée par le dashboard (charge par mécanicien). */
    List<Mecanicien> findByActifTrueOrderByNomAsc();
}
