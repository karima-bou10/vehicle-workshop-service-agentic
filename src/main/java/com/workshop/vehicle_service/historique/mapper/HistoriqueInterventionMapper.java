package com.workshop.vehicle_service.historique.mapper;

import com.workshop.vehicle_service.historique.dto.HistoriqueInterventionResponse;
import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoriqueInterventionMapper {

    HistoriqueInterventionResponse toResponse(HistoriqueIntervention historiqueIntervention);
}
