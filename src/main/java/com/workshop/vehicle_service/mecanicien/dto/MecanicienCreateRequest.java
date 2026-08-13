package com.workshop.vehicle_service.mecanicien.dto;

import com.workshop.vehicle_service.mecanicien.enums.Specialite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MecanicienCreateRequest(
        @NotBlank @Size(min = 2, max = 100) String nom,
        @NotNull Specialite specialite) {
}
