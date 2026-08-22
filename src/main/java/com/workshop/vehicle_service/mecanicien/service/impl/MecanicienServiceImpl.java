package com.workshop.vehicle_service.mecanicien.service.impl;

import com.workshop.vehicle_service.common.exception.MecanicienDesactivationInterditeException;
import com.workshop.vehicle_service.common.exception.MecanicienInactifException;
import com.workshop.vehicle_service.common.exception.MecanicienIntrouvableException;
import com.workshop.vehicle_service.intervention.service.InterventionService;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienCreateRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienDisponibiliteRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienResponse;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienUpdateRequest;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.mecanicien.enums.Specialite;
import com.workshop.vehicle_service.mecanicien.mapper.MecanicienMapper;
import com.workshop.vehicle_service.mecanicien.repository.MecanicienRepository;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class MecanicienServiceImpl implements MecanicienService {

    private final MecanicienRepository mecanicienRepository;
    private final MecanicienMapper mecanicienMapper;

    /**
     * Injecté via ObjectProvider (résolution différée) pour casser la dépendance
     * circulaire mecanicien -> intervention -> mecanicien (WorkflowServiceImpl
     * dépend déjà de MecanicienService).
     */
    private final ObjectProvider<InterventionService> interventionServiceProvider;

    @Override
    @Transactional(readOnly = true)
    public Mecanicien findActifById(Long id) {
        Mecanicien mecanicien = mecanicienRepository.findById(id)
                .orElseThrow(() -> new MecanicienIntrouvableException("Mécanicien introuvable pour l'id " + id));
        if (!mecanicien.isActif()) {
            throw new MecanicienInactifException("Mécanicien inactif, affectation impossible pour l'id " + id);
        }
        return mecanicien;
    }

    @Override
    public MecanicienResponse create(MecanicienCreateRequest request) {
        Mecanicien mecanicien = mecanicienMapper.toEntity(request);
        mecanicien.setDisponible(true);
        mecanicien.setActif(true);
        Mecanicien saved = mecanicienRepository.save(mecanicien);
        return mecanicienMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MecanicienResponse> list(Pageable pageable) {
        validatePageable(pageable);
        return mecanicienRepository.findByActifTrue(pageable).map(mecanicienMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MecanicienResponse findById(Long id) {
        return mecanicienMapper.toResponse(getEntityById(id));
    }

    @Override
    public MecanicienResponse update(Long id, MecanicienUpdateRequest request) {
        Mecanicien mecanicien = getEntityById(id);
        if (!mecanicien.isActif()) {
            throw new MecanicienInactifException(
                    "Mécanicien désactivé, modification impossible pour l'id " + id);
        }
        mecanicienMapper.updateEntityFromRequest(request, mecanicien);
        Mecanicien saved = mecanicienRepository.save(mecanicien);
        return mecanicienMapper.toResponse(saved);
    }

    @Override
    public MecanicienResponse updateDisponibilite(Long id, MecanicienDisponibiliteRequest request) {
        Mecanicien mecanicien = getEntityById(id);
        if (!mecanicien.isActif()) {
            throw new MecanicienInactifException(
                    "Mécanicien désactivé, disponibilité non modifiable pour l'id " + id);
        }
        mecanicien.setDisponible(request.disponible());
        Mecanicien saved = mecanicienRepository.save(mecanicien);
        return mecanicienMapper.toResponse(saved);
    }

    @Override
    public void desactiver(Long id) {
        Mecanicien mecanicien = getEntityById(id);
        if (mecanicien.isActif()) {
            boolean interventionsNonFinales = interventionServiceProvider.getObject()
                    .hasInterventionsActivesNonFinales(id);
            if (interventionsNonFinales) {
                throw new MecanicienDesactivationInterditeException(
                        "Désactivation impossible : le mécanicien possède au moins une intervention active dont le statut n'est pas final (id "
                                + id + ")");
            }
            mecanicien.setActif(false);
            mecanicienRepository.save(mecanicien);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MecanicienResponse> listActifs() {
        return mecanicienRepository.findByActifTrueOrderByNomAsc().stream()
                .map(mecanicienMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MecanicienResponse> listDisponibles(Pageable pageable) {
        validatePageable(pageable);
        return mecanicienRepository.findByActifTrueAndDisponible(true, pageable).map(mecanicienMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MecanicienResponse> listIndisponibles(Pageable pageable) {
        validatePageable(pageable);
        return mecanicienRepository.findByActifTrueAndDisponible(false, pageable).map(mecanicienMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MecanicienResponse> search(String nom, String specialite, Pageable pageable) {
        validatePageable(pageable);
        //String nomNormalise = StringUtils.hasText(nom) ? nom.trim() : null;

        String nomNormalise = StringUtils.hasText(nom) ? nom.trim().toLowerCase(Locale.ROOT) : null;
        String specialiteNormalisee = normaliserSpecialite(specialite);

        return mecanicienRepository.search(nomNormalise, specialiteNormalisee, pageable)
                .map(mecanicienMapper::toResponse);
    }

    private String normaliserSpecialite(String specialite) {
        if (!StringUtils.hasText(specialite)) {
            return null;
        }
        try {
            return Specialite.valueOf(specialite.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Spécialité invalide : " + specialite);
        }
    }

    private Mecanicien getEntityById(Long id) {
        return mecanicienRepository.findById(id)
                .orElseThrow(() -> new MecanicienIntrouvableException("Mécanicien introuvable pour l'id " + id));
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides");
        }
    }
}
