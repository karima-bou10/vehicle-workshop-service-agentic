package com.workshop.vehicle_service.dashboard.dto;

import com.workshop.vehicle_service.intervention.enums.TypeIntervention;

/** Élément de la répartition du nombre d'interventions actives par type. */
public record TypeRepartitionItem(TypeIntervention type, long total) {
}
