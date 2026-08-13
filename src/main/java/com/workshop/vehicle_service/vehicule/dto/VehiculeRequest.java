package com.workshop.vehicle_service.vehicule.dto;

import jakarta.validation.constraints.NotBlank;

public record VehiculeRequest(

        @NotBlank String immatriculationFictive,
        @NotBlank String marque,
        @NotBlank String modele,
        Integer annee,
        Integer kilometrage,
        String clientFictif

) {}
