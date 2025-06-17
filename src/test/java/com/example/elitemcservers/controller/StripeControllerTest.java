package com.example.elitemcservers.controller;

import com.example.elitemcservers.facade.StripeFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StripeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StripeFacade stripeFacade;

    @InjectMocks
    private StripeController stripeController;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(stripeController).build();
    }

    @Test
    @DisplayName("POST /api/donate - successfully creates checkout session with valid amount")
    void testCreateCheckoutSession_Success() throws Exception {
        String mockCheckoutUrl = "http://checkout-session-url.com";
        when(stripeFacade.createCheckoutSession(
                any(BigDecimal.class),
                anyString(),
                anyString()
        )).thenReturn(mockCheckoutUrl);

        String donationRequestJson = "{ \"amount\": \"100\" }";

        mockMvc.perform(post("/api/donate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donationRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(mockCheckoutUrl));

        verify(stripeFacade, times(1)).createCheckoutSession(
                any(BigDecimal.class),
                eq("http://localhost:8080/donate/success"),
                eq("http://localhost:8080/donate/cancel")
        );
    }

    @Test
    @DisplayName("POST /api/donate - returns BadRequest for invalid amount (zero or less)")
    void testCreateCheckoutSession_InvalidAmount() throws Exception {
        String donationRequestJson = "{ \"amount\": \"0\" }";

        mockMvc.perform(post("/api/donate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donationRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount must be greater than zero."));

        verify(stripeFacade, never()).createCheckoutSession(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/donate - returns InternalServerError on StripeFacade exception")
    void testCreateCheckoutSession_InternalServerError() throws Exception {
        when(stripeFacade.createCheckoutSession(any(BigDecimal.class), anyString(), anyString()))
                .thenThrow(new RuntimeException("Stripe Error"));

        String donationRequestJson = "{ \"amount\": \"100\" }";

        mockMvc.perform(post("/api/donate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donationRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Stripe Error"));

        verify(stripeFacade, times(1)).createCheckoutSession(any(BigDecimal.class), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/donate - returns BadRequest for invalid number format")
    void testCreateCheckoutSession_InvalidNumberFormat() throws Exception {
        String donationRequestJson = "{ \"amount\": \"invalidNumber\" }";

        mockMvc.perform(post("/api/donate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donationRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Stripe Error"));

        verify(stripeFacade, never()).createCheckoutSession(any(), anyString(), anyString());
    }
}
