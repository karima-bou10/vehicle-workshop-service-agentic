package com.workshop.vehicle_service.mecanicien.repository;

import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MecanicienRepository extends JpaRepository<Mecanicien, Long> {
}
