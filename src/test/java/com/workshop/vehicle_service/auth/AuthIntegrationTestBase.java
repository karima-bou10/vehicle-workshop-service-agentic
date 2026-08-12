package com.workshop.vehicle_service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.vehicle_service.auth.entity.Utilisateur;
import com.workshop.vehicle_service.auth.enums.Role;
import com.workshop.vehicle_service.auth.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AuthIntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UtilisateurRepository utilisateurRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpUsers() {
        utilisateurRepository.deleteAll();
        utilisateurRepository.save(Utilisateur.builder()
                .username("user1")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.ROLE_USER)
                .build());
        utilisateurRepository.save(Utilisateur.builder()
                .username("manager1")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.ROLE_MANAGER)
                .build());
    }

    protected String loginAndGetToken(String username, String password) throws Exception {
        String payload = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}
