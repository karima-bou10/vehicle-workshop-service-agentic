package com.workshop.vehicle_service.intervention.service.impl;

import com.workshop.vehicle_service.common.exception.CoutEstimeManquantException;
import com.workshop.vehicle_service.common.exception.AnnulationNonAutoriseeException;
import com.workshop.vehicle_service.common.exception.DateRestitutionInvalideException;
import com.workshop.vehicle_service.common.exception.DiagnosticManquantException;
import com.workshop.vehicle_service.common.exception.InterventionInactiveException;
import com.workshop.vehicle_service.common.exception.MecanicienNonAffecteException;
import com.workshop.vehicle_service.common.exception.MotifAnnulationManquantException;
import com.workshop.vehicle_service.common.exception.RestitutionNonAutoriseeException;
import com.workshop.vehicle_service.common.exception.TransitionIllegaleException;
import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.MecanicienAffectationRequest;
import com.workshop.vehicle_service.intervention.dto.TransitionRequest;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.intervention.mapper.InterventionMapper;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

        @Mock
        private InterventionRepository interventionRepository;

        @Mock
        private MecanicienService mecanicienService;

        @Mock
        private InterventionMapper interventionMapper;

        @InjectMocks
        private WorkflowServiceImpl workflowService;

        @AfterEach
        void clearSecurity() {
                SecurityContextHolder.clearContext();
        }

        private Intervention intervention(String numero, StatutIntervention statut, boolean actif,
                        Mecanicien mecanicien) {
                return Intervention.builder()
                                .numero(numero)
                                .vehicule(Vehicule.builder().id(10L).actif(true).build())
                                .type(TypeIntervention.REPARATION)
                                .descriptionClient("desc")
                                .statut(statut)
                                .priorite(PrioriteIntervention.NORMALE)
                                .dateDepot(LocalDateTime.now())
                                .actif(actif)
                                .mecanicien(mecanicien)
                                .historiques(new ArrayList<>())
                                .build();
        }

        @Test
        void transitionLegalShouldUpdateStatutAndCreateHistorique() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("user1", "n/a", java.util.List.of()));
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.RECUE, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));
                when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
                when(interventionMapper.toResponse(any(Intervention.class))).thenReturn(
                                new InterventionResponse(entity.getNumero(), null, null, entity.getType(),
                                                entity.getDescriptionClient(),
                                                entity.getDiagnostic(), StatutIntervention.DIAGNOSTIC_EN_COURS,
                                                entity.getPriorite(),
                                                entity.getCoutEstime(), entity.getDateDepot(),
                                                entity.getDateRestitutionPrevue(),
                                                entity.getDateCloture(), entity.isActif()));

                InterventionResponse result = workflowService.transition(entity.getNumero(),
                                new TransitionRequest(StatutIntervention.DIAGNOSTIC_EN_COURS,
                                                "Diagnostic initial",
                                                null,
                                                null,
                                                null));

                assertEquals(StatutIntervention.DIAGNOSTIC_EN_COURS, entity.getStatut());
                assertEquals(1, entity.getHistoriques().size());
                HistoriqueIntervention hist = entity.getHistoriques().get(0);
                assertEquals(StatutIntervention.RECUE, hist.getAncienStatut());
                assertEquals(StatutIntervention.DIAGNOSTIC_EN_COURS, hist.getNouveauStatut());
                assertEquals("user1", hist.getAuteur());
                assertNotNull(hist.getDate());
                assertEquals(StatutIntervention.DIAGNOSTIC_EN_COURS, result.statut());
        }

        @Test
        void transitionToDiagnosticShouldRequireDiagnostic() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.RECUE, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(DiagnosticManquantException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.DIAGNOSTIC_EN_COURS,
                                                                "   ",
                                                                null,
                                                                null,
                                                                null)));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void transitionIllegalShouldBeRejected() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.RECUE, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(TransitionIllegaleException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.EN_REPARATION,
                                                                null,
                                                                null,
                                                                null,
                                                                null)));

                verify(interventionRepository, never()).save(any());
                assertEquals(0, entity.getHistoriques().size());
        }

        @Test
        void transitionToDevisShouldRequireCoutEstime() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.DIAGNOSTIC_EN_COURS, true,
                                null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(CoutEstimeManquantException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.DEVIS_A_VALIDER,
                                                                null,
                                                                null,
                                                                null,
                                                                null)));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void transitionToDevisShouldRequireDateRestitutionPrevueNotPast() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.DIAGNOSTIC_EN_COURS, true,
                                null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(DateRestitutionInvalideException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.DEVIS_A_VALIDER,
                                                                null,
                                                                new BigDecimal("200.00"),
                                                                LocalDateTime.now().minusDays(1),
                                                                null)));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void transitionToEnReparationShouldRequireMecanicien() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.DEVIS_A_VALIDER, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(MecanicienNonAffecteException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.EN_REPARATION,
                                                                null,
                                                                null,
                                                                null,
                                                                null)));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void restitutionShouldRequireManagerRole() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("user1", "n/a", java.util.List.of()));
                Mecanicien mecanicien = Mecanicien.builder().id(7L).actif(true).build();
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.TERMINEE, true, mecanicien);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(RestitutionNonAutoriseeException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.RESTITUEE,
                                                                null,
                                                                null,
                                                                null,
                                                                null)));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void restitutionShouldSucceedForManager() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("manager1", "n/a",
                                                java.util.List.of(() -> "ROLE_MANAGER")));
                Mecanicien mecanicien = Mecanicien.builder().id(7L).actif(true).build();
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.TERMINEE, true, mecanicien);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));
                when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
                when(interventionMapper.toResponse(any(Intervention.class))).thenReturn(
                                new InterventionResponse(entity.getNumero(), null, null, entity.getType(),
                                                entity.getDescriptionClient(),
                                                entity.getDiagnostic(), StatutIntervention.RESTITUEE,
                                                entity.getPriorite(),
                                                entity.getCoutEstime(), entity.getDateDepot(),
                                                entity.getDateRestitutionPrevue(),
                                                entity.getDateCloture(), entity.isActif()));

                InterventionResponse result = workflowService.transition(entity.getNumero(),
                                new TransitionRequest(StatutIntervention.RESTITUEE,
                                                null,
                                                null,
                                                null,
                                                null));

                assertEquals(StatutIntervention.RESTITUEE, result.statut());
                assertEquals(1, entity.getHistoriques().size());
                assertNotNull(entity.getDateCloture());
        }

        @Test
        void transitionShouldRejectInactiveIntervention() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.RECUE, false, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(InterventionInactiveException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.DIAGNOSTIC_EN_COURS,
                                                                "Diagnostic",
                                                                null,
                                                                null,
                                                                null)));
        }

        @Test
        void affecterMecanicienShouldAssignWhenInterventionActif() {
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.RECUE, true, null);
                Mecanicien mecanicien = Mecanicien.builder().id(7L).actif(true).build();
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));
                when(mecanicienService.findActifById(7L)).thenReturn(mecanicien);
                when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
                when(interventionMapper.toResponse(any(Intervention.class))).thenReturn(
                                new InterventionResponse(entity.getNumero(), null, null, entity.getType(),
                                                entity.getDescriptionClient(),
                                                entity.getDiagnostic(), entity.getStatut(), entity.getPriorite(),
                                                entity.getCoutEstime(),
                                                entity.getDateDepot(), entity.getDateRestitutionPrevue(),
                                                entity.getDateCloture(),
                                                entity.isActif()));

                workflowService.affecterMecanicien(entity.getNumero(), new MecanicienAffectationRequest(7L));

                assertEquals(mecanicien, entity.getMecanicien());
        }

        @Test
        void transitionToDevisShouldPersistCoutEstimeFromRequest() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("user1", "n/a", java.util.List.of()));
                Intervention entity = intervention("INT-2026-000001", StatutIntervention.DIAGNOSTIC_EN_COURS, true,
                                null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));
                when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
                when(interventionMapper.toResponse(any(Intervention.class))).thenReturn(
                                new InterventionResponse(entity.getNumero(), null, null, entity.getType(),
                                                entity.getDescriptionClient(),
                                                entity.getDiagnostic(), StatutIntervention.DEVIS_A_VALIDER,
                                                entity.getPriorite(),
                                                new BigDecimal("450.00"), entity.getDateDepot(),
                                                entity.getDateRestitutionPrevue(),
                                                entity.getDateCloture(), entity.isActif()));

                workflowService.transition(entity.getNumero(),
                                new TransitionRequest(StatutIntervention.DEVIS_A_VALIDER,
                                                null,
                                                new BigDecimal("450.00"),
                                                LocalDateTime.now().plusDays(2),
                                                null));

                ArgumentCaptor<Intervention> captor = ArgumentCaptor.forClass(Intervention.class);
                verify(interventionRepository).save(captor.capture());
                assertEquals(new BigDecimal("450.00"), captor.getValue().getCoutEstime());
        }

        @Test
        void annulationShouldBeRejectedForUserRoleEvenWhenSourceIsLegal() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("user1", "n/a", java.util.List.of()));
                Intervention entity = intervention("INT-2026-000010", StatutIntervention.RECUE, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(AnnulationNonAutoriseeException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.ANNULEE,
                                                                null,
                                                                null,
                                                                null,
                                                                "Demande client")));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void annulationShouldRejectMissingMotifEvenForManager() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("manager1", "n/a",
                                                java.util.List.of(() -> "ROLE_MANAGER")));
                Intervention entity = intervention("INT-2026-000011", StatutIntervention.RECUE, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(MotifAnnulationManquantException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.ANNULEE,
                                                                null,
                                                                null,
                                                                null,
                                                                "   ")));

                verify(interventionRepository, never()).save(any());
        }

        @Test
        void annulationShouldSucceedForManagerAndStoreMotifInHistorique() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("manager1", "n/a",
                                                java.util.List.of(() -> "ROLE_MANAGER")));
                Intervention entity = intervention("INT-2026-000012", StatutIntervention.DIAGNOSTIC_EN_COURS, true,
                                null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));
                when(interventionRepository.save(any(Intervention.class))).thenAnswer(inv -> inv.getArgument(0));
                when(interventionMapper.toResponse(any(Intervention.class))).thenReturn(
                                new InterventionResponse(entity.getNumero(), null, null, entity.getType(),
                                                entity.getDescriptionClient(),
                                                entity.getDiagnostic(), StatutIntervention.ANNULEE,
                                                entity.getPriorite(),
                                                entity.getCoutEstime(), entity.getDateDepot(),
                                                entity.getDateRestitutionPrevue(),
                                                entity.getDateCloture(), entity.isActif()));

                workflowService.transition(entity.getNumero(),
                                new TransitionRequest(StatutIntervention.ANNULEE,
                                                null,
                                                null,
                                                null,
                                                "Demande client"));

                assertEquals(StatutIntervention.ANNULEE, entity.getStatut());
                assertEquals(1, entity.getHistoriques().size());
                assertEquals("Demande client", entity.getHistoriques().get(0).getCommentaire());
        }

        @Test
        void annulationShouldRejectWhenSourceNotAllowed() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken("manager1", "n/a",
                                                java.util.List.of(() -> "ROLE_MANAGER")));
                Intervention entity = intervention("INT-2026-000013", StatutIntervention.EN_REPARATION, true, null);
                when(interventionRepository.findByNumero(entity.getNumero())).thenReturn(Optional.of(entity));

                assertThrows(TransitionIllegaleException.class,
                                () -> workflowService.transition(entity.getNumero(),
                                                new TransitionRequest(StatutIntervention.ANNULEE,
                                                                null,
                                                                null,
                                                                null,
                                                                "Demande client")));

                verify(interventionRepository, never()).save(any());
        }
}
