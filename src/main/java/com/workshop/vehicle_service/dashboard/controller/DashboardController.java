package com.workshop.vehicle_service.dashboard.controller;

import com.workshop.vehicle_service.dashboard.dto.DashboardKpisResponse;
import com.workshop.vehicle_service.dashboard.dto.MecanicienChargeResponse;
import com.workshop.vehicle_service.dashboard.dto.StatutRepartitionItem;
import com.workshop.vehicle_service.dashboard.dto.TypeRepartitionItem;
import com.workshop.vehicle_service.dashboard.dto.VolumeJournalierItem;
import com.workshop.vehicle_service.dashboard.service.DashboardService;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Synthèse en lecture seule de l'atelier (KPI, charge, répartitions, volume)")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Compteurs KPI du jour (reçues aujourd'hui, en diagnostic, en réparation, terminées, retards)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synthèse KPI", content = @Content(schema = @Schema(implementation = DashboardKpisResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpisResponse> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    @Operation(summary = "Lister les interventions en retard (paginé, échéance la plus ancienne en premier par défaut)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page d'interventions en retard"),
            @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/retards")
    public ResponseEntity<Page<InterventionResponse>> getRetards(
            @PageableDefault(size = 20, sort = "dateRestitutionPrevue", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(dashboardService.getRetards(pageable));
    }

    @Operation(summary = "Charge active par mécanicien actif, triée par nom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des charges par mécanicien", content = @Content(schema = @Schema(implementation = MecanicienChargeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/charge-mecaniciens")
    public ResponseEntity<List<MecanicienChargeResponse>> getChargeMecaniciens() {
        return ResponseEntity.ok(dashboardService.getChargeMecaniciens());
    }

    @Operation(summary = "Répartition des interventions actives par statut (7 éléments, y compris à 0)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Répartition par statut", content = @Content(schema = @Schema(implementation = StatutRepartitionItem.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/repartition-statuts")
    public ResponseEntity<List<StatutRepartitionItem>> getRepartitionStatuts() {
        return ResponseEntity.ok(dashboardService.getRepartitionStatuts());
    }

    @Operation(summary = "Répartition des interventions actives par type (6 éléments, y compris à 0)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Répartition par type", content = @Content(schema = @Schema(implementation = TypeRepartitionItem.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/repartition-types")
    public ResponseEntity<List<TypeRepartitionItem>> getRepartitionTypes() {
        return ResponseEntity.ok(dashboardService.getRepartitionTypes());
    }

    @Operation(summary = "Série journalière « reçues vs terminées » sur une fenêtre glissante se terminant aujourd'hui")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Série journalière, sans trou", content = @Content(schema = @Schema(implementation = VolumeJournalierItem.class))),
            @ApiResponse(responseCode = "400", description = "jours<1 ou jours>366"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/volume")
    public ResponseEntity<List<VolumeJournalierItem>> getVolume(
            @RequestParam(defaultValue = "30")
            @Parameter(description = "Taille de la fenêtre glissante en jours (1 à 366), aujourd'hui inclus") int jours) {
        return ResponseEntity.ok(dashboardService.getVolume(jours));
    }
}
