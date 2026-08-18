package com.workshop.vehicle_service.dashboard.dto;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;

/** Élément de la répartition du nombre d'interventions actives par statut. */
public record StatutRepartitionItem(StatutIntervention statut, long total) {
}
