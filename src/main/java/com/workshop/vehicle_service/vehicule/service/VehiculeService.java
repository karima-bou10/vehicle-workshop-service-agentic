package com.workshop.vehicle_service.vehicule.service;

import com.workshop.vehicle_service.vehicule.dto.VehiculeRequest;
import com.workshop.vehicle_service.vehicule.dto.VehiculeResponse;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Point d'entrée public du module vehicule (ADR-001) — seul moyen pour un autre
 * module
 * de résoudre/valider un véhicule, jamais d'accès direct à VehiculeRepository.
 */
public interface VehiculeService {

    /**
     * @throws com.workshop.vehicle_service.common.exception.VehiculeIntrouvableException si
     *                                                                                    aucun
     *                                                                                    véhicule
     *                                                                                    ne
     *                                                                                    correspond
     *                                                                                    à
     *                                                                                    id
     * @throws com.workshop.vehicle_service.common.exception.VehiculeInactifException     si
     *                                                                                    le
     *                                                                                    véhicule
     *                                                                                    existe
     *                                                                                    mais
     *                                                                                    actif
     *                                                                                    ==
     *                                                                                    false
     */
    Vehicule findActifById(Long id);


    Page<VehiculeResponse> getAllVehicules(String search, Pageable pageable);

    VehiculeResponse getVehiculeById(Long id);

    VehiculeResponse createVehicule(VehiculeRequest request);

    VehiculeResponse updateVehicule(Long id, VehiculeRequest request);

    Void deleteVehicule(Long id);
}
