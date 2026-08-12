package com.workshop.vehicle_service.vehicule.mapper;

import com.workshop.vehicle_service.vehicule.dto.VehiculeSummaryResponse;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    VehiculeSummaryResponse toSummary(Vehicule vehicule);
}
