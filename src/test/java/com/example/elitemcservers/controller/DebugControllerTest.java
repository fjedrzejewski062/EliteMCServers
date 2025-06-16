package com.example.elitemcservers.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DebugControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DebugController()).build();
    }

    @Test
    @DisplayName("GET /debug-auth - returns Not authenticated if no auth provided")
    void testDebugAuth_NoAuthentication() throws Exception {
        mockMvc.perform(get("/debug-auth"))
                .andExpect(status().isOk())
                .andExpect(content().string("Not authenticated"));
    }

    @Test
    @DisplayName("GET /debug-auth - returns authentication info when authenticated")
    void testDebugAuth_WithAuthentication() throws Exception {
        // Tworzymy mock Authentication
        Authentication auth = new TestingAuthenticationToken("testUser", "password", "ROLE_USER");

        mockMvc.perform(get("/debug-auth").principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Authenticated as: testUser")))
                .andExpect(content().string(containsString("ROLE_USER")));
    }
}
