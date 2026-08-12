package com.workshop.vehicle_service.auth.repository;

import com.workshop.vehicle_service.auth.entity.Utilisateur;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);
}
