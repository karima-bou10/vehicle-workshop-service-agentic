package com.workshop.vehicle_service.intervention.mapper;

import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.mecanicien.mapper.MecanicienMapper;
import com.workshop.vehicle_service.vehicule.mapper.VehiculeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { VehiculeMapper.class, MecanicienMapper.class })
public interface InterventionMapper {

    @Mapping(target = "enRetard", source = "enRetard")
    InterventionResponse toResponse(Intervention intervention, boolean enRetard);

    InterventionResponse toResponse(Intervention intervention);
}
