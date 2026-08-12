package com.workshop.vehicle_service.auth.service;

import com.workshop.vehicle_service.auth.dto.LoginRequest;
import com.workshop.vehicle_service.auth.dto.LoginResponse;
import com.workshop.vehicle_service.auth.dto.UserResponse;
import com.workshop.vehicle_service.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(request.username());
        } catch (Exception ex) {
            throw new BadCredentialsException("Authentication failed");
        }

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new BadCredentialsException("Authentication failed");
        }

        String token = jwtService.generateToken(userDetails);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationMs());
    }

    public UserResponse currentUser(Authentication authentication) {
        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("Authentication failed"));

        return new UserResponse(authentication.getName(), role);
    }
}
