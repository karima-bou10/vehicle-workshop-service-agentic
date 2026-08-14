package com.workshop.vehicle_service.mecanicien.dto;

import jakarta.validation.constraints.NotNull;

public record MecanicienDisponibiliteRequest(
        @NotNull Boolean disponible) {
}
