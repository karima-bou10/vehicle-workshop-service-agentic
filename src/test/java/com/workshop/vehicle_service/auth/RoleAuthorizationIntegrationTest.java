package com.workshop.vehicle_service.auth;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleAuthorizationIntegrationTest extends AuthIntegrationTestBase {

    @Test
    void managerRouteShouldAllowManagerAndRejectUser() throws Exception {
        String userToken = loginAndGetToken("user1", "pass123");
        String managerToken = loginAndGetToken("manager1", "pass123");

        mockMvc.perform(get("/api/auth/manager/ping")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/manager/ping")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }
}
