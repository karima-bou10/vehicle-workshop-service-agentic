package com.workshop.vehicle_service.historique.service.impl;

import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import com.workshop.vehicle_service.historique.mapper.HistoriqueInterventionMapper;
import com.workshop.vehicle_service.historique.repository.HistoriqueInterventionRepository;
import com.workshop.vehicle_service.historique.service.HistoriqueInterventionService;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoriqueInterventionServiceImpl implements HistoriqueInterventionService {

    private final InterventionRepository interventionRepository;
    private final HistoriqueInterventionRepository historiqueInterventionRepository;
    private final HistoriqueInterventionMapper historiqueInterventionMapper;

    @Override
    public Page<HistoriqueInterventionResponse> findByInterventionNumero(String numero, Pageable pageable) {
        validatePageable(pageable);
        Intervention intervention = interventionRepository.findByNumero(numero)
                .orElseThrow(() -> new InterventionIntrouvableException(
                        "Intervention introuvable pour le numero " + numero));

        Pageable chronologicalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "date"));

        return historiqueInterventionRepository
                .findByInterventionId(intervention.getId(), chronologicalPageable)
                .map(historiqueInterventionMapper::toResponse);
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides");
        }
    }
}
