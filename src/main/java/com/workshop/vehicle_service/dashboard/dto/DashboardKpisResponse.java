package com.workshop.vehicle_service.dashboard.dto;

/** Synthèse chiffrée du jour affichée sur les cartes KPI du dashboard atelier. */
public record DashboardKpisResponse(
        long recuesAujourdHui,
        long enDiagnosticEnCours,
        long enReparation,
        long terminees,
        long retardsRestitution) {
}
