package com.workshop.vehicle_service.intervention.service;

import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.MecanicienAffectationRequest;
import com.workshop.vehicle_service.intervention.dto.TransitionRequest;

public interface WorkflowService {

    InterventionResponse transition(String numero, TransitionRequest request);

    InterventionResponse affecterMecanicien(String numero, MecanicienAffectationRequest request);
}
