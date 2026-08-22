package com.workshop.vehicle_service.common.dto;

import java.time.LocalDate;

/**
 * Projection transverse (jour + total) utilisée pour construire des séries
 * temporelles journalières (ex. volume dans le temps du dashboard).
 * Produite indépendamment par plusieurs modules (intervention, historique)
 * puis fusionnée par le module consommateur.
 */
public record JourCompte(LocalDate date, long total) {
}
