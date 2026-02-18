package com.nutriflow.controllers;

import com.nutriflow.dto.response.PaymentResponse;
import com.nutriflow.security.SecurityUser;
import com.nutriflow.services.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> createCheckoutSession(@AuthenticationPrincipal SecurityUser securityUser) {
        try {
            Long userId = securityUser.getId();
            log.info("Subscription request received for userId: {}", userId);

            // Getting the Stripe link from the service
            String checkoutUrl = paymentService.createCheckoutSession(userId);
            log.info("Checkout URL created: {}", checkoutUrl);

            // Using the DTO here
            PaymentResponse response = new PaymentResponse(
                    checkoutUrl,
                    "Redirect to this URL to complete payment"
            );

            // Returning the object in JSON format
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error creating payment: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("=======================================");
        log.info("Stripe Webhook received");
        log.info("Signature: {}", sigHeader);
        log.info("Payload length: {}", payload.length());
        log.info("=======================================");

        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            log.info("Webhook processed successfully");
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Webhook error: " + e.getMessage());
        }
    }
}