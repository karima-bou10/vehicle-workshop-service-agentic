package com.workshop.vehicle_service.intervention.service;

import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.InterventionUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterventionService {

    InterventionResponse create(InterventionCreateRequest request);

    InterventionResponse findByNumero(String numero);

    Page<InterventionResponse> list(Pageable pageable);

    InterventionResponse update(String numero, InterventionUpdateRequest request);

    void delete(String numero);

    Page<InterventionResponse> findAutresInterventionsDuVehicule(String numero, Pageable pageable);
}
