package com.workshop.vehicle_service.mecanicien.mapper;

import com.workshop.vehicle_service.mecanicien.dto.MecanicienSummaryResponse;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MecanicienMapper {

    MecanicienSummaryResponse toSummaryResponse(Mecanicien mecanicien);
}
