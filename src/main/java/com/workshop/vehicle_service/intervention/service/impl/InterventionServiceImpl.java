package com.workshop.vehicle_service.intervention.service.impl;

import com.workshop.vehicle_service.common.exception.ArchivageNonAutoriseException;
import com.workshop.vehicle_service.common.exception.InterventionInactiveException;
import com.workshop.vehicle_service.common.exception.InterventionIntrouvableException;
import com.workshop.vehicle_service.common.exception.ModificationInterventionNonAutoriseeException;
import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.InterventionUpdateRequest;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.mapper.InterventionMapper;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.intervention.service.InterventionNumeroGenerator;
import com.workshop.vehicle_service.intervention.service.InterventionService;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.service.VehiculeService;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InterventionServiceImpl implements InterventionService {

    private final InterventionRepository interventionRepository;
    private final VehiculeService vehiculeService;
    private final InterventionNumeroGenerator numeroGenerator;
    private final InterventionMapper interventionMapper;

    @Override
    public InterventionResponse create(InterventionCreateRequest request) {
        Vehicule vehicule = vehiculeService.findActifById(request.vehiculeId());
        if (vehicule == null) {
            throw new IllegalArgumentException("Véhicule introuvable pour l'id " + request.vehiculeId());
        }

        LocalDateTime dateDepot = request.dateDepot() != null ? request.dateDepot() : LocalDateTime.now();

        Intervention intervention = Intervention.builder()
                .numero(numeroGenerator.nextNumero())
                .vehicule(vehicule)
                .type(request.type())
                .descriptionClient(request.descriptionClient())
                .statut(StatutIntervention.RECUE)
                .priorite(request.priorite())
                .dateDepot(dateDepot)
                .actif(true)
                .build();

        Intervention saved = interventionRepository.save(intervention);
        return interventionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InterventionResponse findByNumero(String numero) {
        return interventionMapper.toResponse(getEntityByNumero(numero));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterventionResponse> list(Pageable pageable) {
        return interventionRepository.findByActifTrue(pageable).map(interventionMapper::toResponse);
    }

    @Override
    public InterventionResponse update(String numero, InterventionUpdateRequest request) {
        Intervention intervention = getEntityByNumero(numero);
        if (!intervention.isActif()) {
            throw new InterventionInactiveException(
                    "Intervention désactivée, modification impossible pour le numero " + numero);
        }
        validateUpdateAllowedByStatus(intervention, request);

        intervention.setType(request.type());
        intervention.setDescriptionClient(request.descriptionClient());
        intervention.setPriorite(request.priorite());
        intervention.setDateDepot(request.dateDepot());

        Intervention saved = interventionRepository.save(intervention);
        return interventionMapper.toResponse(saved);
    }

    @Override
    public void delete(String numero) {
        Intervention intervention = getEntityByNumero(numero);
        if (!isArchivedStatus(intervention.getStatut())) {
            throw new ArchivageNonAutoriseException(
                    "Archivage impossible pour une intervention au statut " + intervention.getStatut());
        }
        if (intervention.isActif()) {
            intervention.setActif(false);
            interventionRepository.save(intervention);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterventionResponse> findAutresInterventionsDuVehicule(String numero, Pageable pageable) {
        validatePageable(pageable);
        Intervention sourceIntervention = getEntityByNumero(numero);
        return interventionRepository
                .findByVehiculeIdAndIdNotAndActifTrue(sourceIntervention.getVehicule().getId(),
                        sourceIntervention.getId(),
                        pageable)
                .map(interventionMapper::toResponse);
    }

    private Intervention getEntityByNumero(String numero) {
        return interventionRepository.findByNumero(numero)
                .orElseThrow(() -> new InterventionIntrouvableException(
                        "Intervention introuvable pour le numero " + numero));
    }

    private void validateUpdateAllowedByStatus(Intervention intervention, InterventionUpdateRequest request) {
        StatutIntervention statut = intervention.getStatut();
        if (statut == StatutIntervention.RESTITUEE || statut == StatutIntervention.ANNULEE) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Modification interdite pour une intervention au statut " + statut);
        }

        if (statut != StatutIntervention.RECUE && !Objects.equals(request.type(), intervention.getType())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ type est modifiable uniquement au statut RECUE");
        }

        if ((statut == StatutIntervention.EN_REPARATION || statut == StatutIntervention.TERMINEE)
                && !Objects.equals(request.descriptionClient(), intervention.getDescriptionClient())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ descriptionClient est modifiable jusqu'au statut DEVIS_A_VALIDER");
        }

        if ((statut == StatutIntervention.TERMINEE)
                && !Objects.equals(request.priorite(), intervention.getPriorite())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ priorite est modifiable jusqu'au statut EN_REPARATION");
        }

        if (statut != StatutIntervention.RECUE && !Objects.equals(request.dateDepot(), intervention.getDateDepot())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ dateDepot est modifiable uniquement au statut RECUE");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides");
        }

    }

    private boolean isArchivedStatus(StatutIntervention statut) {
        return statut == StatutIntervention.RESTITUEE || statut == StatutIntervention.ANNULEE;
    }
}
