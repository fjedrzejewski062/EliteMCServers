package com.example.elitemcservers.service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StripeServiceTest {

    @InjectMocks
    private StripeService stripeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create a Stripe Checkout Session successfully")
    public void testCreateCheckoutSession_Success() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(150);
        String successUrl = "http://localhost:8080/success";
        String cancelUrl = "http://localhost:8080/cancel";

        Session sessionMock = mock(Session.class);
        when(sessionMock.getUrl()).thenReturn("http://stripe-checkout-url.com");

        try (MockedStatic<Session> sessionStaticMock = mockStatic(Session.class)) {
            sessionStaticMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(sessionMock);

            String checkoutUrl = stripeService.createCheckoutSession(amount, successUrl, cancelUrl);

            assertNotNull(checkoutUrl);
            assertEquals("http://stripe-checkout-url.com", checkoutUrl);

            sessionStaticMock.verify(() -> Session.create(any(SessionCreateParams.class)), times(1));
        }
    }
}
