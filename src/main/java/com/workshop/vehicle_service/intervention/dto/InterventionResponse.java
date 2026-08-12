package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienSummaryResponse;
import com.workshop.vehicle_service.vehicule.dto.VehiculeSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vue exposée en API — jamais l'entité JPA, jamais l'id technique (numero =
 * identifiant métier).
 */
public record InterventionResponse(
        String numero,
        VehiculeSummaryResponse vehicule,
        MecanicienSummaryResponse mecanicien,
        TypeIntervention type,
        String descriptionClient,
        String diagnostic,
        StatutIntervention statut,
        PrioriteIntervention priorite,
        BigDecimal coutEstime,
        LocalDateTime dateDepot,
        LocalDateTime dateRestitutionPrevue,
        LocalDateTime dateCloture,
        boolean actif) {
}
