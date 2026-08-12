package com.workshop.vehicle_service.historique.service.impl;

import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import com.workshop.vehicle_service.historique.mapper.HistoriqueInterventionMapper;
import com.workshop.vehicle_service.historique.repository.HistoriqueInterventionRepository;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoriqueInterventionServiceImplTest {

    @Mock
    private InterventionRepository interventionRepository;

    @Mock
    private HistoriqueInterventionRepository historiqueInterventionRepository;

    @Mock
    private HistoriqueInterventionMapper historiqueInterventionMapper;

    @InjectMocks
    private HistoriqueInterventionServiceImpl historiqueInterventionService;

    @Test
    void findByInterventionNumeroShouldReturnPagedTimelineChronological() {
        Intervention intervention = Intervention.builder().id(5L).numero("INT-2026-000001").build();
        HistoriqueIntervention h = HistoriqueIntervention.builder()
                .intervention(intervention)
                .ancienStatut(StatutIntervention.RECUE)
                .nouveauStatut(StatutIntervention.DIAGNOSTIC_EN_COURS)
                .auteur("user1")
                .date(LocalDateTime.now())
                .build();
        Page<HistoriqueIntervention> page = new PageImpl<>(List.of(h), PageRequest.of(0, 20), 1);

        when(interventionRepository.findByNumero("INT-2026-000001")).thenReturn(Optional.of(intervention));
        when(historiqueInterventionRepository.findByInterventionId(any(), any())).thenReturn(page);
        when(historiqueInterventionMapper.toResponse(h))
                .thenReturn(new HistoriqueInterventionResponse(StatutIntervention.RECUE,
                        StatutIntervention.DIAGNOSTIC_EN_COURS, "user1", h.getDate(), null));

        Page<HistoriqueInterventionResponse> result = historiqueInterventionService
                .findByInterventionNumero("INT-2026-000001", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        verify(historiqueInterventionRepository).findByInterventionId(any(), any());
    }

    @Test
    void findByInterventionNumeroShouldThrowWhenInterventionUnknown() {
        when(interventionRepository.findByNumero("INT-2026-999999")).thenReturn(Optional.empty());

        assertThrows(InterventionIntrouvableException.class,
                () -> historiqueInterventionService.findByInterventionNumero("INT-2026-999999", PageRequest.of(0, 20)));
    }
}
