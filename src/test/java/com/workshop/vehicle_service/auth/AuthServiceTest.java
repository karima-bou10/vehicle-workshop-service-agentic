package com.workshop.vehicle_service.auth;

import com.workshop.vehicle_service.auth.dto.LoginRequest;
import com.workshop.vehicle_service.auth.dto.LoginResponse;
import com.workshop.vehicle_service.auth.jwt.JwtService;
import com.workshop.vehicle_service.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginOkShouldReturnBearerToken() {
        UserDetails user = User.withUsername("user1").password("HASH").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches("pass123", "HASH")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        LoginResponse response = authService.login(new LoginRequest("user1", "pass123"));

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(3600000L, response.expiresIn());
    }

    @Test
    void loginKoShouldReturnGenericError() {
        UserDetails user = User.withUsername("user1").password("HASH").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("user1", "wrong")));
    }
}
