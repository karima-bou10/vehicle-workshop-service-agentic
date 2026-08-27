package com.workshop.vehicle_service.intervention.service;

import com.workshop.vehicle_service.intervention.dto.AiDiagnosticPropositionResponse;

public interface AiDiagnosticService {

    AiDiagnosticPropositionResponse generateProposition(String descriptionClient);
}
