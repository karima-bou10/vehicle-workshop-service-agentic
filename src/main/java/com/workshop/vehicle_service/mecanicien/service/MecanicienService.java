package com.workshop.vehicle_service.mecanicien.service;

import com.workshop.vehicle_service.mecanicien.dto.MecanicienCreateRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienDisponibiliteRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienResponse;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienUpdateRequest;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MecanicienService {

    Mecanicien findActifById(Long id);

    MecanicienResponse create(MecanicienCreateRequest request);

    Page<MecanicienResponse> list(Pageable pageable);

    MecanicienResponse findById(Long id);

    MecanicienResponse update(Long id, MecanicienUpdateRequest request);

    MecanicienResponse updateDisponibilite(Long id, MecanicienDisponibiliteRequest request);

    void desactiver(Long id);
}
