package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Champs de base uniquement (FR-010) : pas de
 * vehiculeId/numero/statut/mecanicienId/coutEstime/dateCloture/actif.
 */
public record InterventionUpdateRequest(
        @NotNull Long vehiculeId,
        @NotNull TypeIntervention type,
        @NotBlank String descriptionClient,
        @NotNull PrioriteIntervention priorite,
        @NotNull LocalDateTime dateDepot,
        LocalDateTime dateRestitutionPrevue,
        String diagnostic,
        BigDecimal coutEstime) {

}
