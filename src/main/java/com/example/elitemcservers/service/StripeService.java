package com.example.elitemcservers.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StripeService {

    public String createCheckoutSession(BigDecimal amount, String successUrl, String cancelUrl) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = List.of(
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("pln")
                                        .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("Donation")
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
        );

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}