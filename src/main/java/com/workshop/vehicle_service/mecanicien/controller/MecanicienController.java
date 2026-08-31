package com.workshop.vehicle_service.mecanicien.controller;

import com.workshop.vehicle_service.mecanicien.dto.MecanicienCreateRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienDisponibiliteRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienResponse;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienUpdateRequest;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mecaniciens")
@RequiredArgsConstructor
@Tag(name = "Mécaniciens", description = "CRUD de base des mécaniciens")
public class MecanicienController {

    private final MecanicienService mecanicienService;

    @Operation(summary = "Créer un mécanicien (actif=true, disponible=true par défaut)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mécanicien créé", content = @Content(schema = @Schema(implementation = MecanicienResponse.class))),
            @ApiResponse(responseCode = "400", description = "Body invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au manager")
    })
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MecanicienResponse> create(@Valid @RequestBody MecanicienCreateRequest request) {
        MecanicienResponse response = mecanicienService.create(request);
        return ResponseEntity.created(URI.create("/api/mecaniciens/" + response.id())).body(response);
    }

    @Operation(summary = "Lister les mécaniciens actifs (paginé)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de mécaniciens actifs"),
            @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping
    public ResponseEntity<Page<MecanicienResponse>> list(
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(mecanicienService.list(pageable));
    }

    @Operation(summary = "Exporter les mécaniciens actifs en CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV généré"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> export() {
        String csv = mecanicienService.exportCsv();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mecaniciens.csv\"")
                .body(csv);
    }

    @Operation(summary = "Lister les mécaniciens actifs et disponibles (paginé)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de mécaniciens actifs disponibles"),
            @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/disponibles")
    public ResponseEntity<Page<MecanicienResponse>> listDisponibles(
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(mecanicienService.listDisponibles(pageable));
    }

    @Operation(summary = "Lister les mécaniciens actifs et indisponibles (paginé)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de mécaniciens actifs indisponibles"),
            @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/indisponibles")
    public ResponseEntity<Page<MecanicienResponse>> listIndisponibles(
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(mecanicienService.listIndisponibles(pageable));
    }

    @Operation(summary = "Rechercher les mécaniciens actifs par nom et/ou spécialité (paginé)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page de mécaniciens correspondant à la recherche"),
            @ApiResponse(responseCode = "400", description = "Paramètres de recherche ou de pagination invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/recherche")
    public ResponseEntity<Page<MecanicienResponse>> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String specialite,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(mecanicienService.search(nom, specialite, pageable));
    }

    @Operation(summary = "Consulter un mécanicien par son id (actif ou inactif)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mécanicien trouvé", content = @Content(schema = @Schema(implementation = MecanicienResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mécanicien introuvable"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MecanicienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mecanicienService.findById(id));
    }

    @Operation(summary = "Modifier le nom et/ou la spécialité d'un mécanicien actif")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mécanicien mis à jour", content = @Content(schema = @Schema(implementation = MecanicienResponse.class))),
            @ApiResponse(responseCode = "400", description = "Body invalide"),
            @ApiResponse(responseCode = "404", description = "Mécanicien introuvable"),
            @ApiResponse(responseCode = "422", description = "Mécanicien désactivé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au manager")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MecanicienResponse> update(@PathVariable Long id,
            @Valid @RequestBody MecanicienUpdateRequest request) {
        return ResponseEntity.ok(mecanicienService.update(id, request));
    }

    @Operation(summary = "Changer la disponibilité d'un mécanicien actif")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilité mise à jour", content = @Content(schema = @Schema(implementation = MecanicienResponse.class))),
            @ApiResponse(responseCode = "400", description = "Body invalide"),
            @ApiResponse(responseCode = "404", description = "Mécanicien introuvable"),
            @ApiResponse(responseCode = "422", description = "Mécanicien désactivé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au manager")
    })
    @PatchMapping("/{id}/disponibilite")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MecanicienResponse> updateDisponibilite(@PathVariable Long id,
            @Valid @RequestBody MecanicienDisponibiliteRequest request) {
        return ResponseEntity.ok(mecanicienService.updateDisponibilite(id, request));
    }

    @Operation(summary = "Désactivation logique (soft delete, actif=false) — idempotente, refusée si intervention active non terminale")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mécanicien désactivé (ou déjà désactivé)"),
            @ApiResponse(responseCode = "404", description = "Mécanicien introuvable"),
            @ApiResponse(responseCode = "409", description = "Le mécanicien possède au moins une intervention active dont le statut n'est pas final"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès réservé au manager")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        mecanicienService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
