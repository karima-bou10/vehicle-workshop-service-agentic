package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransitionRequest(
        @NotNull StatutIntervention statutCible,
        BigDecimal coutEstime) {
}
