package com.workshop.vehicle_service.mecanicien.service.impl;

import com.workshop.vehicle_service.common.exception.MecanicienInactifException;
import com.workshop.vehicle_service.common.exception.MecanicienIntrouvableException;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.mecanicien.repository.MecanicienRepository;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MecanicienServiceImpl implements MecanicienService {

    private final MecanicienRepository mecanicienRepository;

    @Override
    public Mecanicien findActifById(Long id) {
        Mecanicien mecanicien = mecanicienRepository.findById(id)
                .orElseThrow(() -> new MecanicienIntrouvableException("Mécanicien introuvable pour l'id " + id));
        if (!mecanicien.isActif()) {
            throw new MecanicienInactifException("Mécanicien inactif, affectation impossible pour l'id " + id);
        }
        return mecanicien;
    }
}
