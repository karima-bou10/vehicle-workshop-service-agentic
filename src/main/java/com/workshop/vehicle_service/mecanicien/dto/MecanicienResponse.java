package com.workshop.vehicle_service.mecanicien.dto;

import com.workshop.vehicle_service.mecanicien.enums.Specialite;

public record MecanicienResponse(
        Long id,
        String nom,
        Specialite specialite,
        boolean disponible,
        boolean actif) {
}
