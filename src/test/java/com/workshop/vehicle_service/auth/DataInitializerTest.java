package com.workshop.vehicle_service.auth;

import com.workshop.vehicle_service.auth.entity.Utilisateur;
import com.workshop.vehicle_service.auth.init.DataInitializer;
import com.workshop.vehicle_service.auth.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void shouldNotCreateUsersWhenTableNotEmpty() throws Exception {
        when(utilisateurRepository.count()).thenReturn(1L);

        dataInitializer.run();

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void shouldCreateDefaultUsersWhenTableEmpty() throws Exception {
        when(utilisateurRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password123");
        when(passwordEncoder.encode("password123@")).thenReturn("encoded-password123@");

        dataInitializer.run();

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository, times(2)).save(captor.capture());

        Utilisateur first = captor.getAllValues().get(0);
        Utilisateur second = captor.getAllValues().get(1);

        assertEquals("conseiller", first.getUsername());
        assertEquals("responsable", second.getUsername());
        assertEquals("encoded-password123", first.getPassword());
        assertEquals("encoded-password123@", second.getPassword());
        assertNotEquals("password123", first.getPassword());
        assertNotEquals("password123@", second.getPassword());
    }
}
