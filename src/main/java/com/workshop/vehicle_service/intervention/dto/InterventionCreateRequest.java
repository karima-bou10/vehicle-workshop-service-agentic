package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Pas de numero/statut/mecanicienId/coutEstime/dateCloture/actif : générés ou
 * hors périmètre (RG-AUTO-01, RG-AUTO-08).
 */
public record InterventionCreateRequest(
                @NotNull Long vehiculeId,
                @NotNull TypeIntervention type,
                @NotBlank String descriptionClient,
                @NotNull PrioriteIntervention priorite,
                LocalDateTime dateDepot) {
}
