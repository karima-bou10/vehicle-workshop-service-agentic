package com.workshop.vehicle_service.intervention.service.impl;

import com.workshop.vehicle_service.common.exception.InterventionInactiveException;
import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.common.exception.VehiculeInactifException;
import com.workshop.vehicle_service.common.exception.VehiculeIntrouvableException;
import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.InterventionUpdateRequest;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.intervention.mapper.InterventionMapper;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.intervention.service.InterventionNumeroGenerator;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.service.VehiculeService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterventionServiceImplTest {

    @Mock
    private InterventionRepository interventionRepository;

    @Mock
    private VehiculeService vehiculeService;

    @Mock
    private InterventionNumeroGenerator numeroGenerator;

    @Mock
    private InterventionMapper interventionMapper;

    @InjectMocks
    private InterventionServiceImpl interventionService;

    private InterventionCreateRequest createRequest(Long vehiculeId) {
        return new InterventionCreateRequest(vehiculeId, TypeIntervention.REPARATION, "Bruit au freinage",
                null, PrioriteIntervention.NORMALE, LocalDateTime.now(), null);
    }

    @Test
    void createShouldSetStatutRecueAndGeneratedNumeroWhenVehiculeActif() {
        Vehicule vehicule = Vehicule.builder().id(1L).actif(true).build();
        when(vehiculeService.findActifById(1L)).thenReturn(vehicule);
        when(numeroGenerator.nextNumero()).thenReturn("INT-2026-000001");
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
        when(interventionMapper.toResponse(any(Intervention.class))).thenReturn(
            new InterventionResponse("INT-2026-000001", null, null, TypeIntervention.REPARATION, "Bruit au freinage",
                        null, StatutIntervention.RECUE, PrioriteIntervention.NORMALE, null, LocalDateTime.now(), null,
                        null, true));

        InterventionResponse response = interventionService.create(createRequest(1L));

        assertEquals("INT-2026-000001", response.numero());
        assertEquals(StatutIntervention.RECUE, response.statut());

        ArgumentCaptor<Intervention> captor = ArgumentCaptor.forClass(Intervention.class);
        verify(interventionRepository).save(captor.capture());
        assertEquals(StatutIntervention.RECUE, captor.getValue().getStatut());
        assertEquals("INT-2026-000001", captor.getValue().getNumero());
        assertEquals(vehicule, captor.getValue().getVehicule());
    }

    @Test
    void createShouldRejectAndNotSaveWhenVehiculeIntrouvable() {
        when(vehiculeService.findActifById(99L))
                .thenThrow(new VehiculeIntrouvableException("Véhicule introuvable pour l'id 99"));

        assertThrows(VehiculeIntrouvableException.class, () -> interventionService.create(createRequest(99L)));

        verify(interventionRepository, never()).save(any());
        verify(numeroGenerator, never()).nextNumero();
    }

    @Test
    void createShouldRejectAndNotSaveWhenVehiculeInactif() {
        when(vehiculeService.findActifById(2L)).thenThrow(new VehiculeInactifException("Véhicule inactif"));

        assertThrows(VehiculeInactifException.class, () -> interventionService.create(createRequest(2L)));

        verify(interventionRepository, never()).save(any());
    }

    @Test
    void createShouldDelegateNumeroGenerationExactlyOnce() {
        Vehicule vehicule = Vehicule.builder().id(1L).actif(true).build();
        when(vehiculeService.findActifById(1L)).thenReturn(vehicule);
        when(numeroGenerator.nextNumero()).thenReturn("INT-2026-000042");

        interventionService.create(createRequest(1L));

        verify(numeroGenerator, times(1)).nextNumero();
    }

    @Test
    void findByNumeroShouldReturnMappedResponseWhenFound() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000123").actif(true).build();
        when(interventionRepository.findByNumero("INT-2026-000123")).thenReturn(Optional.of(intervention));
        when(interventionMapper.toResponse(intervention)).thenReturn(
            new InterventionResponse("INT-2026-000123", null, null, null, null, null, null, null, null, null,
                null, null, true));

        InterventionResponse response = interventionService.findByNumero("INT-2026-000123");

        assertEquals("INT-2026-000123", response.numero());
    }

    @Test
    void findByNumeroShouldThrowWhenNotFound() {
        when(interventionRepository.findByNumero("INT-2026-999999")).thenReturn(Optional.empty());

        assertThrows(InterventionIntrouvableException.class, () -> interventionService.findByNumero("INT-2026-999999"));
    }

    @Test
    void listShouldReturnOnlyActifPageMappedToResponse() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000001").actif(true).build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Intervention> page = new PageImpl<>(List.of(intervention), pageable, 1);
        when(interventionRepository.findByActifTrue(pageable)).thenReturn(page);
        when(interventionMapper.toResponse(intervention)).thenReturn(
            new InterventionResponse("INT-2026-000001", null, null, null, null, null, null, null, null, null,
                null, null, true));

        Page<InterventionResponse> result = interventionService.list(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("INT-2026-000001", result.getContent().get(0).numero());
    }

    @Test
    void updateShouldModifyBaseFieldsOnlyWhenActif() {
        Vehicule vehicule = Vehicule.builder().id(1L).build();
        Intervention intervention = Intervention.builder()
                .numero("INT-2026-000001")
                .vehicule(vehicule)
                .statut(StatutIntervention.RECUE)
                .actif(true)
                .build();
        when(interventionRepository.findByNumero("INT-2026-000001")).thenReturn(Optional.of(intervention));
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.CONTROLE,
                "Nouvelle description",
                "Plaquettes usées", PrioriteIntervention.HAUTE, LocalDateTime.now(), null);

        interventionService.update("INT-2026-000001", request);

        assertEquals(TypeIntervention.CONTROLE, intervention.getType());
        assertEquals("Nouvelle description", intervention.getDescriptionClient());
        assertEquals("INT-2026-000001", intervention.getNumero());
        assertEquals(vehicule, intervention.getVehicule());
        assertEquals(StatutIntervention.RECUE, intervention.getStatut());
    }

    @Test
    void updateShouldThrowWhenInterventionInactive() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000002").actif(false).build();
        when(interventionRepository.findByNumero("INT-2026-000002")).thenReturn(Optional.of(intervention));

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.CONTROLE, "desc",
                null, PrioriteIntervention.BASSE, LocalDateTime.now(), null);

        assertThrows(InterventionInactiveException.class, () -> interventionService.update("INT-2026-000002", request));
        verify(interventionRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowWhenNumeroUnknown() {
        when(interventionRepository.findByNumero("INT-2026-999999")).thenReturn(Optional.empty());

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.CONTROLE, "desc",
                null, PrioriteIntervention.BASSE, LocalDateTime.now(), null);

        assertThrows(InterventionIntrouvableException.class,
                () -> interventionService.update("INT-2026-999999", request));
    }

    @Test
    void deleteShouldSetActifFalseWhenCurrentlyActif() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000001").actif(true).build();
        when(interventionRepository.findByNumero("INT-2026-000001")).thenReturn(Optional.of(intervention));

        interventionService.delete("INT-2026-000001");

        assertFalse(intervention.isActif());
        verify(interventionRepository).save(intervention);
    }

    @Test
    void deleteShouldBeIdempotentWhenAlreadyInactive() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000001").actif(false).build();
        when(interventionRepository.findByNumero("INT-2026-000001")).thenReturn(Optional.of(intervention));

        interventionService.delete("INT-2026-000001");

        verify(interventionRepository, never()).save(any());
    }

    @Test
    void deleteShouldThrowWhenNumeroUnknown() {
        when(interventionRepository.findByNumero("INT-2026-999999")).thenReturn(Optional.empty());

        assertThrows(InterventionIntrouvableException.class, () -> interventionService.delete("INT-2026-999999"));
    }

        @Test
        void findAutresInterventionsDuVehiculeShouldExcludeSourceAndMapPage() {
        Vehicule vehicule = Vehicule.builder().id(50L).build();
        Intervention source = Intervention.builder().id(1L).numero("INT-2026-000001").vehicule(vehicule).actif(true)
            .build();
        Intervention other = Intervention.builder().id(2L).numero("INT-2026-000002").vehicule(vehicule).actif(true)
            .build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Intervention> page = new PageImpl<>(List.of(other), pageable, 1);

        when(interventionRepository.findByNumero("INT-2026-000001")).thenReturn(Optional.of(source));
        when(interventionRepository.findByVehiculeIdAndIdNotAndActifTrue(50L, 1L, pageable)).thenReturn(page);
        when(interventionMapper.toResponse(other)).thenReturn(
            new InterventionResponse("INT-2026-000002", null, null, null, null, null, null, null, null, null,
                null, null, true));

        Page<InterventionResponse> result = interventionService.findAutresInterventionsDuVehicule("INT-2026-000001",
            pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("INT-2026-000002", result.getContent().get(0).numero());
        }
}
