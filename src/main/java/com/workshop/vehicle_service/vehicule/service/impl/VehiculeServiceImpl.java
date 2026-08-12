package com.workshop.vehicle_service.vehicule.service.impl;

import com.workshop.vehicle_service.common.exception.VehiculeInactifException;
import com.workshop.vehicle_service.common.exception.VehiculeIntrouvableException;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.repository.VehiculeRepository;
import com.workshop.vehicle_service.vehicule.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;

    @Override
    public Vehicule findActifById(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new VehiculeIntrouvableException("Véhicule introuvable pour l'id " + id));
        if (!vehicule.isActif()) {
            throw new VehiculeInactifException("Véhicule inactif, opération impossible pour l'id " + id);
        }
        return vehicule;
    }
}
