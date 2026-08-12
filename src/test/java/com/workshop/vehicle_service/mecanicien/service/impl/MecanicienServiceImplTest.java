package com.workshop.vehicle_service.mecanicien.service.impl;

import com.workshop.vehicle_service.common.exception.MecanicienInactifException;
import com.workshop.vehicle_service.common.exception.MecanicienIntrouvableException;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.mecanicien.repository.MecanicienRepository;
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
class MecanicienServiceImplTest {

    @Mock
    private MecanicienRepository mecanicienRepository;

    @InjectMocks
    private MecanicienServiceImpl mecanicienService;

    @Test
    void findActifByIdShouldReturnMecanicienWhenFoundAndActif() {
        Mecanicien mecanicien = Mecanicien.builder().id(1L).nom("Mec A").actif(true).build();
        when(mecanicienRepository.findById(1L)).thenReturn(Optional.of(mecanicien));

        Mecanicien result = mecanicienService.findActifById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findActifByIdShouldThrowWhenNotFound() {
        when(mecanicienRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MecanicienIntrouvableException.class, () -> mecanicienService.findActifById(99L));
    }

    @Test
    void findActifByIdShouldThrowWhenInactive() {
        Mecanicien mecanicien = Mecanicien.builder().id(2L).nom("Mec B").actif(false).build();
        when(mecanicienRepository.findById(2L)).thenReturn(Optional.of(mecanicien));

        assertThrows(MecanicienInactifException.class, () -> mecanicienService.findActifById(2L));
    }
}
