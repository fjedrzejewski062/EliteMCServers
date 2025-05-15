package com.example.elitemcservers.controller;

import com.example.elitemcservers.facade.StripeFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/donate")
public class StripeController {

    private final StripeFacade stripeFacade;

    public StripeController(StripeFacade stripeFacade) {
        this.stripeFacade = stripeFacade;
    }

    @PostMapping
    public ResponseEntity<?> createCheckoutSession(@RequestBody DonationRequest donationRequest) {
        try {
            BigDecimal amount = new BigDecimal(donationRequest.getAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Amount must be greater than zero.");
            }

            String checkoutUrl = stripeFacade.createCheckoutSession(
                    amount,
                    "http://localhost:8080/donate/success",
                    "http://localhost:8080/donate/cancel"
            );
            return ResponseEntity.ok(checkoutUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Stripe Error");
        }
    }

    public static class DonationRequest {
        private String amount;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }
}
