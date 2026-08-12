package com.workshop.vehicle_service.vehicule.dto;

/**
 * Projection résumée exposée aux autres modules (ex. Intervention) — jamais
 * l'id technique.
 */
public record VehiculeSummaryResponse(
        String immatriculationFictive,
        String marque,
        String modele,
        Integer annee) {
}
