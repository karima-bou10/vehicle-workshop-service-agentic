package com.workshop.vehicle_service.historique.service;

import com.workshop.vehicle_service.common.dto.JourCompte;
import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoriqueInterventionService {

    Page<HistoriqueInterventionResponse> findByInterventionNumero(String numero, Pageable pageable);

    /** Série journalière des interventions passées en statut TERMINEE sur [debut, finInclusive], sans trou. */
    List<JourCompte> volumeTermineesParJour(LocalDate debut, LocalDate finInclusive);
}
