package com.workshop.vehicle_service.historique.service;

import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoriqueInterventionService {

    Page<HistoriqueInterventionResponse> findByInterventionNumero(String numero, Pageable pageable);
}
