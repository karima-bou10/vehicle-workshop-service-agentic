package com.workshop.vehicle_service.dashboard.dto;

import com.workshop.vehicle_service.mecanicien.enums.Specialite;

/** Charge de travail active d'un mécanicien (graphique « charge par mécanicien »). */
public record MecanicienChargeResponse(
        Long id,
        String nom,
        Specialite specialite,
        boolean disponible,
        long chargeActive) {
}
