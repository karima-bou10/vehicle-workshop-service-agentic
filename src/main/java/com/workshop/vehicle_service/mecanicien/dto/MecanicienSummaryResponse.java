package com.workshop.vehicle_service.mecanicien.dto;

public record MecanicienSummaryResponse(
        String nom,
        String specialite,
        boolean disponible) {
}
