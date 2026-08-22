package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;

public record InterventionListFilter(
        StatutIntervention statut,
        Long mecanicienId,
        Long vehiculeId,
        String immatriculation,
        String q,
        Boolean enRetard) {

    public InterventionListFilter {
        immatriculation = normalize(immatriculation);
        q = normalize(q);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
