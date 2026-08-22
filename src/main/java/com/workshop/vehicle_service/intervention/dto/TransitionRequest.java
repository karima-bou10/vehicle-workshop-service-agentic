package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransitionRequest(
                @NotNull StatutIntervention statutCible,
                String diagnostic,
                BigDecimal coutEstime,
                LocalDateTime dateRestitutionPrevue,
                String motifAnnulation) {
}
