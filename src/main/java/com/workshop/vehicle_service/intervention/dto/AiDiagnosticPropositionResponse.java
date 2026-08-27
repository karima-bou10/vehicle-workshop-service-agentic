package com.workshop.vehicle_service.intervention.dto;

import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import java.util.List;

public record AiDiagnosticPropositionResponse(
                String reformulation,
                List<String> hypotheses,
                List<String> pointsControle,
                PrioriteIntervention prioriteSuggeree) {
}
