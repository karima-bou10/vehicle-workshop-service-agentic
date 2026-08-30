package com.workshop.vehicle_service.vehicule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehiculeRequest(

        @NotBlank(message = "L'immatriculation est obligatoire")
        @Pattern(
                regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$",
                message = "Le format doit être de type AA-123-AA"
        )
        String immatriculationFictive,

        @NotBlank(message = "La marque est obligatoire")
        @Size(min = 2, max = 50)
        String marque,

        @NotBlank(message = "Le modèle est obligatoire")
        @Size(min = 1, max = 50)
        String modele,

        @NotNull(message = "L'année est obligatoire")
        @Min(1900)
        @Max(2100)
        Integer annee,

        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero
        Integer kilometrage,

        @NotBlank(message = "Le client est obligatoire")
        @Size(min = 2, max = 100)
        String clientFictif

) {}
