package com.workshop.vehicle_service.intervention.controller;

import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import com.workshop.vehicle_service.historique.service.HistoriqueInterventionService;
import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.MecanicienAffectationRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.TransitionRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionUpdateRequest;
import com.workshop.vehicle_service.intervention.service.InterventionService;
import com.workshop.vehicle_service.intervention.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interventions")
@RequiredArgsConstructor
@Tag(name = "Interventions", description = "CRUD de base des interventions & Workflow des statuts & Historique")
public class InterventionController {

        private final InterventionService interventionService;
        private final WorkflowService workflowService;
        private final HistoriqueInterventionService historiqueInterventionService;

        @Operation(summary = "Créer une intervention liée à un véhicule (statut initial RECUE, numero auto-généré)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Intervention créée", content = @Content(schema = @Schema(implementation = InterventionResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Body invalide"),
                        @ApiResponse(responseCode = "404", description = "Véhicule introuvable"),
                        @ApiResponse(responseCode = "422", description = "Véhicule inactif"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @PostMapping
        public ResponseEntity<InterventionResponse> create(@Valid @RequestBody InterventionCreateRequest request) {
                InterventionResponse response = interventionService.create(request);
                return ResponseEntity.created(URI.create("/api/interventions/" + response.numero())).body(response);
        }

        @Operation(summary = "Consulter une intervention par son numero")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Intervention trouvée", content = @Content(schema = @Schema(implementation = InterventionResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Intervention introuvable"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @GetMapping("/{numero}")
        public ResponseEntity<InterventionResponse> getByNumero(
                        @PathVariable @Parameter(description = "Numero au format INT-AAAA-NNNNNN") String numero) {
                return ResponseEntity.ok(interventionService.findByNumero(numero));
        }

        @Operation(summary = "Lister les interventions actives (paginé)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Page d'interventions actives"),
                        @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @GetMapping
        public ResponseEntity<Page<InterventionResponse>> list(
                        @PageableDefault(size = 20, sort = "dateDepot", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity.ok(interventionService.list(pageable));
        }

        @Operation(summary = "Éditer les champs métier autorisés selon le statut courant")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Intervention mise à jour", content = @Content(schema = @Schema(implementation = InterventionResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Body invalide"),
                        @ApiResponse(responseCode = "422", description = "Champ non autorisé pour le statut courant"),
                        @ApiResponse(responseCode = "404", description = "Intervention introuvable"),
                        @ApiResponse(responseCode = "409", description = "Intervention désactivée ou conflit métier"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @PutMapping("/{numero}")
        public ResponseEntity<InterventionResponse> update(@PathVariable String numero,
                        @Valid @RequestBody InterventionUpdateRequest request) {
                return ResponseEntity.ok(interventionService.update(numero, request));
        }

        @Operation(summary = "Archivage conditionnel (soft delete, actif=false) — statuts terminaux uniquement")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Intervention désactivée (ou déjà désactivée)"),
                        @ApiResponse(responseCode = "409", description = "Archivage interdit pour le statut courant"),
                        @ApiResponse(responseCode = "404", description = "Intervention introuvable"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @DeleteMapping("/{numero}")
        public ResponseEntity<Void> delete(@PathVariable String numero) {
                interventionService.delete(numero);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Faire progresser le statut d'une intervention avec validations conditionnelles")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statut mis à jour", content = @Content(schema = @Schema(implementation = InterventionResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Transition illégale pour le statut courant"),
                        @ApiResponse(responseCode = "422", description = "Pré-requis métier manquant ou invalide"),
                        @ApiResponse(responseCode = "403", description = "Restitution ou annulation non autorisée"),
                        @ApiResponse(responseCode = "404", description = "Intervention introuvable"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @PostMapping("/{numero}/transitions")
        public ResponseEntity<InterventionResponse> transition(@PathVariable String numero,
                        @Valid @RequestBody TransitionRequest request) {
                return ResponseEntity.ok(workflowService.transition(numero, request));
        }

        @Operation(summary = "Affecter un mécanicien à une intervention")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Mécanicien affecté", content = @Content(schema = @Schema(implementation = InterventionResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Body invalide"),
                        @ApiResponse(responseCode = "403", description = "Accès réservé au manager"),
                        @ApiResponse(responseCode = "404", description = "Intervention ou mécanicien introuvable"),
                        @ApiResponse(responseCode = "409", description = "Intervention désactivée"),
                        @ApiResponse(responseCode = "422", description = "Mécanicien inactif"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @PatchMapping("/{numero}/mecanicien")
        @PreAuthorize("hasRole('MANAGER')")
        public ResponseEntity<InterventionResponse> affecterMecanicien(@PathVariable String numero,
                        @Valid @RequestBody MecanicienAffectationRequest request) {
                return ResponseEntity.ok(workflowService.affecterMecanicien(numero, request));
        }

        @Operation(summary = "Consulter la timeline d'une intervention")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Historique paginé"),
                        @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
                        @ApiResponse(responseCode = "404", description = "Intervention introuvable"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @GetMapping("/{numero}/historique")
        public ResponseEntity<Page<HistoriqueInterventionResponse>> getHistorique(@PathVariable String numero,
                        @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
                return ResponseEntity.ok(historiqueInterventionService.findByInterventionNumero(numero, pageable));
        }

        @Operation(summary = "Lister les autres interventions actives du même véhicule")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Related list paginée"),
                        @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
                        @ApiResponse(responseCode = "404", description = "Intervention introuvable"),
                        @ApiResponse(responseCode = "401", description = "Non authentifié")
        })
        @GetMapping("/{numero}/autres-interventions-vehicule")
        public ResponseEntity<Page<InterventionResponse>> getAutresInterventionsVehicule(@PathVariable String numero,
                        @PageableDefault(size = 20, sort = "dateDepot", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity.ok(interventionService.findAutresInterventionsDuVehicule(numero, pageable));
        }
}
