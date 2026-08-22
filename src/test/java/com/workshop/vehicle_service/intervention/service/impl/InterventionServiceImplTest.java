package com.workshop.vehicle_service.intervention.service.impl;

import com.workshop.vehicle_service.common.exception.InterventionInactiveException;
import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.common.exception.ModificationInterventionNonAutoriseeException;
import com.workshop.vehicle_service.common.exception.ArchivageNonAutoriseException;
import com.workshop.vehicle_service.common.exception.VehiculeInactifException;
import com.workshop.vehicle_service.common.exception.VehiculeIntrouvableException;
import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionListFilter;
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
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
                PrioriteIntervention.NORMALE, LocalDateTime.now());
    }

    @Test
    void createShouldSetStatutRecueAndGeneratedNumeroWhenVehiculeActif() {
        Vehicule vehicule = Vehicule.builder().id(1L).actif(true).build();
        when(vehiculeService.findActifById(1L)).thenReturn(vehicule);
        when(numeroGenerator.nextNumero()).thenReturn("INT-2026-000001");
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
        when(interventionMapper.toResponse(any(Intervention.class), anyBoolean())).thenReturn(
                new InterventionResponse("INT-2026-000001", null, null, TypeIntervention.REPARATION,
                        "Bruit au freinage",
                        null, StatutIntervention.RECUE, PrioriteIntervention.NORMALE, null, LocalDateTime.now(), null,
                        null, true, false));

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
        when(interventionRepository.save(any(Intervention.class))).thenReturn(Intervention.builder()
                .numero("INT-2026-000042")
                .vehicule(vehicule)
                .type(TypeIntervention.REPARATION)
                .descriptionClient("Bruit au freinage")
                .statut(StatutIntervention.RECUE)
                .priorite(PrioriteIntervention.NORMALE)
                .dateDepot(LocalDateTime.now())
                .actif(true)
                .build());
        when(interventionMapper.toResponse(org.mockito.ArgumentMatchers.nullable(Intervention.class), anyBoolean()))
                .thenReturn(
                new InterventionResponse("INT-2026-000042", null, null, TypeIntervention.REPARATION,
                        "Bruit au freinage", null, StatutIntervention.RECUE, PrioriteIntervention.NORMALE, null,
                        LocalDateTime.now(), null, null, true, false));

        interventionService.create(createRequest(1L));

        verify(numeroGenerator, times(1)).nextNumero();
    }

    @Test
    void findByNumeroShouldReturnMappedResponseWhenFound() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000123").actif(true).build();
        when(interventionRepository.findByNumero("INT-2026-000123")).thenReturn(Optional.of(intervention));
        when(interventionMapper.toResponse(intervention, false)).thenReturn(
                new InterventionResponse("INT-2026-000123", null, null, null, null, null, null, null, null, null,
                        null, null, true, false));

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
        when(interventionRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Intervention>>any(),
                any(Pageable.class))).thenReturn(page);
        when(interventionMapper.toResponse(intervention, false)).thenReturn(
                new InterventionResponse("INT-2026-000001", null, null, null, null, null, null, null, null, null,
                        null, null, true, false));

        Page<InterventionResponse> result = interventionService.list(
                new InterventionListFilter(null, null, null, null, null, null), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("INT-2026-000001", result.getContent().getFirst().numero());
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
        when(interventionMapper.toResponse(any(Intervention.class), anyBoolean()))
                .thenAnswer(inv -> new InterventionResponse(
                        ((Intervention) inv.getArgument(0)).getNumero(), null, null,
                        ((Intervention) inv.getArgument(0)).getType(),
                        ((Intervention) inv.getArgument(0)).getDescriptionClient(), null,
                        ((Intervention) inv.getArgument(0)).getStatut(),
                        ((Intervention) inv.getArgument(0)).getPriorite(), null,
                        ((Intervention) inv.getArgument(0)).getDateDepot(), null, null,
                        ((Intervention) inv.getArgument(0)).isActif(),
                        false));

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.CONTROLE,
                "Nouvelle description", PrioriteIntervention.HAUTE, LocalDateTime.now());

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
                PrioriteIntervention.BASSE, LocalDateTime.now());

        assertThrows(InterventionInactiveException.class, () -> interventionService.update("INT-2026-000002", request));
        verify(interventionRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowWhenNumeroUnknown() {
        when(interventionRepository.findByNumero("INT-2026-999999")).thenReturn(Optional.empty());

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.CONTROLE, "desc",
                PrioriteIntervention.BASSE, LocalDateTime.now());

        assertThrows(InterventionIntrouvableException.class,
                () -> interventionService.update("INT-2026-999999", request));
    }

    @Test
    void deleteShouldSetActifFalseWhenCurrentlyActif() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000001")
                .statut(StatutIntervention.RESTITUEE)
                .actif(true)
                .build();
        when(interventionRepository.findByNumero("INT-2026-000001")).thenReturn(Optional.of(intervention));

        interventionService.delete("INT-2026-000001");

        assertFalse(intervention.isActif());
        verify(interventionRepository).save(intervention);
    }

    @Test
    void deleteShouldBeIdempotentWhenAlreadyInactive() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000001")
                .statut(StatutIntervention.ANNULEE)
                .actif(false)
                .build();
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
    void deleteShouldRejectWhenStatusIsNotTerminal() {
        Intervention nonTerminal = Intervention.builder().numero("INT-2026-000015")
                .statut(StatutIntervention.RECUE)
                .actif(true)
                .build();
        when(interventionRepository.findByNumero("INT-2026-000015")).thenReturn(Optional.of(nonTerminal));

        assertThrows(ArchivageNonAutoriseException.class, () -> interventionService.delete("INT-2026-000015"));
    }

    @Test
    void deleteShouldSetActifFalseWhenStatusIsAnnulee() {
        Intervention intervention = Intervention.builder().numero("INT-2026-000016")
                .statut(StatutIntervention.ANNULEE)
                .actif(true)
                .build();
        when(interventionRepository.findByNumero("INT-2026-000016")).thenReturn(Optional.of(intervention));

        interventionService.delete("INT-2026-000016");

        assertFalse(intervention.isActif());
        verify(interventionRepository).save(intervention);
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
        when(interventionMapper.toResponse(other, false)).thenReturn(
                new InterventionResponse("INT-2026-000002", null, null, null, null, null, null, null, null, null,
                        null, null, true, false));

        Page<InterventionResponse> result = interventionService.findAutresInterventionsDuVehicule("INT-2026-000001",
                pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("INT-2026-000002", result.getContent().getFirst().numero());
    }

    @Test
    void updateShouldRejectWhenStatusTerminee() {
        Vehicule vehicule = Vehicule.builder().id(1L).build();
        Intervention intervention = Intervention.builder()
                .numero("INT-2026-000050")
                .vehicule(vehicule)
                .type(TypeIntervention.REPARATION)
                .descriptionClient("desc")
                .statut(StatutIntervention.TERMINEE)
                .priorite(PrioriteIntervention.NORMALE)
                .dateDepot(LocalDateTime.of(2026, 1, 1, 10, 0))
                .actif(true)
                .build();

        when(interventionRepository.findByNumero("INT-2026-000050")).thenReturn(Optional.of(intervention));

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.CONTROLE,
                "desc",
                PrioriteIntervention.HAUTE,
                LocalDateTime.of(2026, 1, 1, 10, 0));

        assertThrows(ModificationInterventionNonAutoriseeException.class,
                () -> interventionService.update("INT-2026-000050", request));
        verify(interventionRepository, never()).save(any());
    }

    @Test
    void updateShouldRejectWhenStatusEnReparationAndDescriptionChanges() {
        Vehicule vehicule = Vehicule.builder().id(1L).build();
        LocalDateTime depot = LocalDateTime.of(2026, 1, 1, 10, 0);
        Intervention intervention = Intervention.builder()
                .numero("INT-2026-000051")
                .vehicule(vehicule)
                .type(TypeIntervention.REPARATION)
                .descriptionClient("desc ancienne")
                .statut(StatutIntervention.EN_REPARATION)
                .priorite(PrioriteIntervention.NORMALE)
                .dateDepot(depot)
                .actif(true)
                .build();

        when(interventionRepository.findByNumero("INT-2026-000051")).thenReturn(Optional.of(intervention));

        InterventionUpdateRequest request = new InterventionUpdateRequest(TypeIntervention.REPARATION,
                "desc nouvelle",
                PrioriteIntervention.NORMALE,
                depot);

        assertThrows(ModificationInterventionNonAutoriseeException.class,
                () -> interventionService.update("INT-2026-000051", request));
        verify(interventionRepository, never()).save(any());
    }
}
