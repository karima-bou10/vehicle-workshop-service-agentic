package com.workshop.vehicle_service.intervention.service.impl;

import com.workshop.vehicle_service.common.exception.CoutEstimeManquantException;
import com.workshop.vehicle_service.common.exception.DateRestitutionInvalideException;
import com.workshop.vehicle_service.common.exception.DiagnosticManquantException;
import com.workshop.vehicle_service.common.exception.AnnulationNonAutoriseeException;
import com.workshop.vehicle_service.common.exception.InterventionInactiveException;
import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.common.exception.MecanicienNonAffecteException;
import com.workshop.vehicle_service.common.exception.MotifAnnulationManquantException;
import com.workshop.vehicle_service.common.exception.RestitutionNonAutoriseeException;
import com.workshop.vehicle_service.common.exception.TransitionIllegaleException;
import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.MecanicienAffectationRequest;
import com.workshop.vehicle_service.intervention.dto.TransitionRequest;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.mapper.InterventionMapper;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.intervention.service.WorkflowService;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private static final java.util.Map<StatutIntervention, List<StatutIntervention>> LEGAL_TRANSITIONS = java.util.Map
            .of(
                    StatutIntervention.RECUE,
                    List.of(StatutIntervention.DIAGNOSTIC_EN_COURS, StatutIntervention.ANNULEE),
                    StatutIntervention.DIAGNOSTIC_EN_COURS,
                    List.of(StatutIntervention.DEVIS_A_VALIDER, StatutIntervention.ANNULEE),
                    StatutIntervention.DEVIS_A_VALIDER,
                    List.of(StatutIntervention.EN_REPARATION, StatutIntervention.ANNULEE),
                    StatutIntervention.EN_REPARATION, List.of(StatutIntervention.TERMINEE),
                    StatutIntervention.TERMINEE, List.of(StatutIntervention.RESTITUEE),
                    StatutIntervention.RESTITUEE, List.of(),
                    StatutIntervention.ANNULEE, List.of());

    private final InterventionRepository interventionRepository;
    private final MecanicienService mecanicienService;
    private final InterventionMapper interventionMapper;

    @Override
    public InterventionResponse transition(String numero, TransitionRequest request) {
        Intervention intervention = getEntityByNumero(numero);
        ensureActive(intervention, "transition");

        StatutIntervention current = intervention.getStatut();
        StatutIntervention target = request.statutCible();
        ensureLegalTransition(current, target);

        if (target == StatutIntervention.DIAGNOSTIC_EN_COURS) {
            String diagnostic = normalizeText(request.diagnostic());
            if (diagnostic == null) {
                throw new DiagnosticManquantException("diagnostic requis avant le passage à Diagnostic en cours");
            }
            intervention.setDiagnostic(diagnostic);
        }

        if (target == StatutIntervention.DEVIS_A_VALIDER) {
            if (request.coutEstime() == null || request.coutEstime().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CoutEstimeManquantException("coutEstimé requis avant le passage à Devis à valider");
            }
            if (request.dateRestitutionPrevue() == null) {
                throw new DateRestitutionInvalideException(
                        "dateRestitutionPrevue requise avant le passage à Devis à valider");
            }
            if (request.dateRestitutionPrevue().isBefore(LocalDateTime.now())) {
                throw new DateRestitutionInvalideException("dateRestitutionPrevue ne peut pas être dans le passé");
            }
            intervention.setCoutEstime(request.coutEstime());
            intervention.setDateRestitutionPrevue(request.dateRestitutionPrevue());
        }

        if (target == StatutIntervention.EN_REPARATION && intervention.getMecanicien() == null) {
            throw new MecanicienNonAffecteException("Un mécanicien doit être affecté avant le passage à En réparation");
        }

        if (target == StatutIntervention.RESTITUEE && !hasManagerRole()) {
            throw new RestitutionNonAutoriseeException("Seul un responsable atelier peut restituer une intervention");
        }

        if (target == StatutIntervention.RESTITUEE) {
            intervention.setDateCloture(LocalDateTime.now());
        }

        if (target == StatutIntervention.ANNULEE) {
            if (!hasManagerRole()) {
                throw new AnnulationNonAutoriseeException("Seul un responsable atelier peut annuler une intervention");
            }
            String commentaire = normalizeText(request.motifAnnulation());
            if (commentaire == null) {
                throw new MotifAnnulationManquantException("motifAnnulation must not be blank");
            }
            intervention.setStatut(target);
            intervention.getHistoriques().add(HistoriqueIntervention.builder()
                    .intervention(intervention)
                    .ancienStatut(current)
                    .nouveauStatut(target)
                    .auteur(getAuthenticatedUsername())
                    .date(LocalDateTime.now())
                    .commentaire(commentaire)
                    .build());

            Intervention saved = interventionRepository.save(intervention);
            return interventionMapper.toResponse(saved);
        }

        intervention.setStatut(target);
        intervention.getHistoriques().add(HistoriqueIntervention.builder()
                .intervention(intervention)
                .ancienStatut(current)
                .nouveauStatut(target)
                .auteur(getAuthenticatedUsername())
                .date(LocalDateTime.now())
                .commentaire(null)
                .build());

        Intervention saved = interventionRepository.save(intervention);
        return interventionMapper.toResponse(saved);
    }

    @Override
    public InterventionResponse affecterMecanicien(String numero, MecanicienAffectationRequest request) {
        Intervention intervention = getEntityByNumero(numero);
        ensureActive(intervention, "affectation");

        intervention.setMecanicien(mecanicienService.findActifById(request.mecanicienId()));

        Intervention saved = interventionRepository.save(intervention);
        return interventionMapper.toResponse(saved);
    }

    private Intervention getEntityByNumero(String numero) {
        return interventionRepository.findByNumero(numero)
                .orElseThrow(() -> new InterventionIntrouvableException(
                        "Intervention introuvable pour le numero " + numero));
    }

    private void ensureActive(Intervention intervention, String operation) {
        if (!intervention.isActif()) {
            throw new InterventionInactiveException(
                    "Intervention désactivée, " + operation + " impossible pour le numero " + intervention.getNumero());
        }
    }

    private void ensureLegalTransition(StatutIntervention current, StatutIntervention target) {
        List<StatutIntervention> allowedTargets = LEGAL_TRANSITIONS.get(current);
        if (allowedTargets == null || !allowedTargets.contains(target)) {
            throw new TransitionIllegaleException("Transition illégale : " + current + " -> " + target);
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasManagerRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MANAGER".equals(authority.getAuthority()));
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "unknown";
        }
        return authentication.getName();
    }
}
