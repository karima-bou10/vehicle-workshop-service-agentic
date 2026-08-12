package com.workshop.vehicle_service.intervention.entity;

import com.workshop.vehicle_service.common.config.SpringContextProvider;
import com.workshop.vehicle_service.intervention.service.InterventionNumeroGenerator;
import jakarta.persistence.PrePersist;

public class InterventionEntityListener {

    @PrePersist
    public void prePersist(Intervention intervention) {
        if (intervention.getNumero() == null || intervention.getNumero().isBlank()) {
            InterventionNumeroGenerator generator = SpringContextProvider.getBean(InterventionNumeroGenerator.class);
            intervention.setNumero(generator.nextNumero());
        }
    }
}
