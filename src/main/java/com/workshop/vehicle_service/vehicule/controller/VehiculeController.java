package com.workshop.vehicle_service.vehicule.controller;

import com.workshop.vehicle_service.vehicule.dto.VehiculeRequest;
import com.workshop.vehicle_service.vehicule.dto.VehiculeResponse;
import com.workshop.vehicle_service.vehicule.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {
    private final VehiculeService vehiculeService;

    @GetMapping("/getVehicules")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER')")
    public ResponseEntity<Page<VehiculeResponse>> getAllVehicules(@RequestParam(required = false) String search, Pageable pageable){
        return ResponseEntity.ok(vehiculeService.getAllVehicules(search, pageable));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER')")
    @Operation(summary = "Recuperer un vehicule par son id")
    public ResponseEntity<VehiculeResponse> getById(@PathVariable Long id){
         return ResponseEntity.ok(vehiculeService.getVehiculeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER')")
    @Operation(summary = "Creer un vehicule")
    public ResponseEntity<VehiculeResponse> create(@Valid @RequestBody VehiculeRequest vehiculeRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculeService.createVehicule(vehiculeRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER')")
    @Operation(summary = "Mettre a jour un vehicule")
    public  ResponseEntity<VehiculeResponse> update(@PathVariable Long id, @Valid @RequestBody VehiculeRequest vehiculeRequest){
           return ResponseEntity.ok(vehiculeService.updateVehicule(id, vehiculeRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Supprimer un vehicule (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
               vehiculeService.deleteVehicule(id);
               return ResponseEntity.noContent().build();
    }

}
