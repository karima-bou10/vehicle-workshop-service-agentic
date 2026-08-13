package com.workshop.vehicle_service.vehicule.mapper;

import com.workshop.vehicle_service.vehicule.dto.VehiculeRequest;
import com.workshop.vehicle_service.vehicule.dto.VehiculeResponse;
import com.workshop.vehicle_service.vehicule.dto.VehiculeSummaryResponse;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    VehiculeSummaryResponse toSummary(Vehicule vehicule);
    VehiculeResponse toResponse(Vehicule vehicule);
    Vehicule toEntity(VehiculeRequest vehiculeRequest);
    void updateEntityFromRequest(VehiculeRequest request, @MappingTarget Vehicule vehicule);
}
