package com.nutriflow.services;

import com.stripe.exception.StripeException;

public interface PaymentService {
    String createCheckoutSession(Long userId) throws StripeException;
    void handleStripeWebhook(String payload, String sigHeader);
}