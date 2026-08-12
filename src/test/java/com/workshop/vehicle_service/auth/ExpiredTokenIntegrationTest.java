package com.workshop.vehicle_service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpiredTokenIntegrationTest extends AuthIntegrationTestBase {

    @Test
    void expiredTokenShouldReturnUnauthorized() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor("test-secret-key-for-jwt-signing-which-is-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user1")
                .issuedAt(new Date(System.currentTimeMillis() - 3600000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
