package com.workshop.vehicle_service.historique.service.impl;

import com.workshop.vehicle_service.common.dto.JourCompte;
import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import com.workshop.vehicle_service.historique.mapper.HistoriqueInterventionMapper;
import com.workshop.vehicle_service.historique.repository.HistoriqueInterventionRepository;
import com.workshop.vehicle_service.historique.service.HistoriqueInterventionService;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    @Override
    public List<JourCompte> volumeTermineesParJour(LocalDate debut, LocalDate finInclusive) {
        LocalDateTime debutDateTime = debut.atStartOfDay();
        LocalDateTime finExclusiveDateTime = finInclusive.plusDays(1).atStartOfDay();
        Map<LocalDate, Long> parJour = historiqueInterventionRepository
                .findDatesTransitionDansPeriode(StatutIntervention.TERMINEE, debutDateTime, finExclusiveDateTime)
                .stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        List<JourCompte> serie = new ArrayList<>();
        for (LocalDate jour = debut; !jour.isAfter(finInclusive); jour = jour.plusDays(1)) {
            serie.add(new JourCompte(jour, parJour.getOrDefault(jour, 0L)));
        }
        return serie;
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides");
        }
    }
}
