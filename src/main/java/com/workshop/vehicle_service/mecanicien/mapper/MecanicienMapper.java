package com.workshop.vehicle_service.mecanicien.mapper;

import com.workshop.vehicle_service.mecanicien.dto.MecanicienCreateRequest;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienResponse;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienSummaryResponse;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienUpdateRequest;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MecanicienMapper {

    MecanicienSummaryResponse toSummaryResponse(Mecanicien mecanicien);

    MecanicienResponse toResponse(Mecanicien mecanicien);

    Mecanicien toEntity(MecanicienCreateRequest request);

    void updateEntityFromRequest(MecanicienUpdateRequest request, @MappingTarget Mecanicien mecanicien);
}
