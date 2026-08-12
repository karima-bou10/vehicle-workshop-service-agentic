package com.workshop.vehicle_service.auth.init;

import com.workshop.vehicle_service.auth.entity.Utilisateur;
import com.workshop.vehicle_service.auth.enums.Role;
import com.workshop.vehicle_service.auth.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }

        utilisateurRepository.save(Utilisateur.builder()
                .username("conseiller")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_USER)
                .build());

        utilisateurRepository.save(Utilisateur.builder()
                .username("responsable")
                .password(passwordEncoder.encode("password123@"))
                .role(Role.ROLE_MANAGER)
                .build());
    }
}
