package com.workshop.vehicle_service.intervention.dto;

import jakarta.validation.constraints.NotNull;

public record MecanicienAffectationRequest(
        @NotNull Long mecanicienId) {
}
