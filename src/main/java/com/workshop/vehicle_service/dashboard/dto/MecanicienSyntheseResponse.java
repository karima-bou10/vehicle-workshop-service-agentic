package com.workshop.vehicle_service.dashboard.dto;

import com.workshop.vehicle_service.mecanicien.enums.Specialite;

public record MecanicienSyntheseResponse(
        Long id,
        String nom,
        Specialite specialite,
        boolean disponible,
        long nombreInterventionsEnCours,
        long nombreInterventionsEnReparation,
        long nombreInterventionsTerminees,
        long nombreInterventionsEnRetard,
        Double delaiMoyenTraitementHeures) {
}
