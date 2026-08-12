package com.workshop.vehicle_service.vehicule.service.impl;

import com.workshop.vehicle_service.common.exception.VehiculeInactifException;
import com.workshop.vehicle_service.common.exception.VehiculeIntrouvableException;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.repository.VehiculeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @InjectMocks
    private VehiculeServiceImpl vehiculeService;

    @Test
    void findActifByIdShouldReturnVehiculeWhenFoundAndActif() {
        Vehicule vehicule = Vehicule.builder().id(1L).immatriculationFictive("AB-123-CD").actif(true).build();
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        Vehicule result = vehiculeService.findActifById(1L);

        assertEquals("AB-123-CD", result.getImmatriculationFictive());
    }

    @Test
    void findActifByIdShouldThrowWhenNotFound() {
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VehiculeIntrouvableException.class, () -> vehiculeService.findActifById(99L));
    }

    @Test
    void findActifByIdShouldThrowWhenInactif() {
        Vehicule vehicule = Vehicule.builder().id(2L).actif(false).build();
        when(vehiculeRepository.findById(2L)).thenReturn(Optional.of(vehicule));

        assertThrows(VehiculeInactifException.class, () -> vehiculeService.findActifById(2L));
    }
}
