package com.workshop.vehicle_service.auth;

import com.workshop.vehicle_service.auth.entity.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvalidAccountIntegrationTest extends AuthIntegrationTestBase {

    @Test
    void nonExistingAccountShouldReturnGenericUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"password\":\"pass123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }

    @Test
    void missingRoleShouldReturnGenericUnauthorized() throws Exception {
        utilisateurRepository.save(Utilisateur.builder()
                .username("norole")
                .password(passwordEncoder.encode("pass123"))
                .role(null)
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"norole\",\"password\":\"pass123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }
}
