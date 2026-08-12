package com.workshop.vehicle_service.auth.service;

import com.workshop.vehicle_service.auth.entity.Utilisateur;
import com.workshop.vehicle_service.auth.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur user = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if (user.getRole() == null) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(user.getRole().name()))
                .build();
    }
}
