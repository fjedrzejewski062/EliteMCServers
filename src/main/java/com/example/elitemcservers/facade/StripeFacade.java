package com.example.elitemcservers.facade;

import com.example.elitemcservers.service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripeFacade {

    private final StripeService stripeService;

    public StripeFacade(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    public String createCheckoutSession(BigDecimal amount, String successUrl, String cancelUrl) throws StripeException {
        return stripeService.createCheckoutSession(amount, successUrl, cancelUrl);
    }
}
