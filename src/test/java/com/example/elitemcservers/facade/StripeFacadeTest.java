package com.example.elitemcservers.facade;

import com.example.elitemcservers.service.StripeService;
import com.example.elitemcservers.facade.StripeFacade;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StripeFacadeTest {

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private StripeFacade stripeFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createCheckoutSession returns checkout URL successfully")
    void createCheckoutSession_Success() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");
        String successUrl = "http://success.url";
        String cancelUrl = "http://cancel.url";
        String expectedUrl = "http://checkout.session.url";

        when(stripeService.createCheckoutSession(amount, successUrl, cancelUrl)).thenReturn(expectedUrl);

        String actualUrl = stripeFacade.createCheckoutSession(amount, successUrl, cancelUrl);

        assertEquals(expectedUrl, actualUrl);
        verify(stripeService, times(1)).createCheckoutSession(amount, successUrl, cancelUrl);
    }

    @Test
    @DisplayName("createCheckoutSession throws StripeException when StripeService fails")
    void createCheckoutSession_ThrowsStripeException() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");
        String successUrl = "http://success.url";
        String cancelUrl = "http://cancel.url";

        // mockujemy StripeException, bo nie da się go normalnie stworzyć (jest abstrakcyjny lub wymaga wielu argumentów)
        StripeException stripeException = mock(StripeException.class);
        when(stripeException.getMessage()).thenReturn("Stripe API error");

        when(stripeService.createCheckoutSession(amount, successUrl, cancelUrl))
                .thenThrow(stripeException);

        StripeException exception = assertThrows(StripeException.class, () -> {
            stripeFacade.createCheckoutSession(amount, successUrl, cancelUrl);
        });

        assertEquals("Stripe API error", exception.getMessage());
        verify(stripeService, times(1)).createCheckoutSession(amount, successUrl, cancelUrl);
    }
}
