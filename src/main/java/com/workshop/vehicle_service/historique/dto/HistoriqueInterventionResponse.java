package com.workshop.vehicle_service.historique.dto;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import java.time.LocalDateTime;

public record HistoriqueInterventionResponse(
        StatutIntervention ancienStatut,
        StatutIntervention nouveauStatut,
        String auteur,
        LocalDateTime date,
        String commentaire) {
}
