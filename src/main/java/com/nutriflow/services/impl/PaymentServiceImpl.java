package com.nutriflow.services.impl;

import com.nutriflow.entities.PaymentEntity;
import com.nutriflow.entities.SubscriptionEntity;
import com.nutriflow.entities.UserEntity;
import com.nutriflow.enums.PaymentStatus;
import com.nutriflow.exceptions.UserNotFoundException;
import com.nutriflow.exceptions.WebhookProcessingException;
import com.nutriflow.helpers.SubscriptionHelper;
import com.nutriflow.repositories.PaymentRepository;
import com.nutriflow.repositories.SubscriptionRepository;
import com.nutriflow.repositories.UserRepository;
import com.nutriflow.services.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment Service Implementation (Refactored).
 * Assignment logic separated using Subscription Helper.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    // Helper
    private final SubscriptionHelper subscriptionHelper;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("✅ Stripe API initialized successfully");
    }

    @Override
    public String createCheckoutSession(Long userId) throws StripeException {
        log.info("Stripe Checkout Session creation started: UserId={}", userId);

        // Store userId in metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", String.valueOf(userId));

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/payment-cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("azn")
                                                .setUnitAmount(150000L) // 1500 AZN = 150000 qepik
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Premium Plan")
                                                                .setDescription("Monthly Premium Subscription")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putAllMetadata(metadata)
                .build();

        Session session = Session.create(params);
        log.info("✅ Stripe Session created: ID={}, URL={}", session.getId(), session.getUrl());

        return session.getUrl();
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        log.info("📩 Webhook notification received from Stripe");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("✅ Webhook event verified: EventId={}, Type={}", event.getId(), event.getType());
        } catch (SignatureVerificationException e) {
            log.error("❌ Webhook signature verification failed: {}", e.getMessage());
            throw new WebhookProcessingException("Invalid signature");
        }

        // Process based on event type
        switch (event.getType()) {
            case "checkout.session.completed" -> {
                log.info("💳 Payment completed successfully (checkout.session.completed)");
                handleCheckoutSessionCompleted(event);
            }
            case "payment_intent.succeeded", "charge.succeeded", "payment_intent.created" ->
                    log.debug("ℹ️ No special processing required for this event type: {}", event.getType());
            default ->
                    log.warn("⚠️ Unknown event type: {}", event.getType());
        }
    }

    /**
     * Handles the checkout session completed event.
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject;

            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {
                log.warn("⚠️ Deserializer could not find object, performing manual casting");
                stripeObject = (StripeObject) event.getData().getObject();
            }

            Session session = (Session) stripeObject;

            // Get userId from metadata
            Map<String, String> metadata = session.getMetadata();
            Long userId = Long.parseLong(metadata.get("userId"));
            String stripeSessionId = session.getId();

            log.info("📋 Metadata read: UserId={}, StripeSessionId={}", userId, stripeSessionId);

            // Finalize subscription
            finalizeSubscription(userId, stripeSessionId);

        } catch (Exception e) {
            log.error("❌ Unexpected error during webhook processing: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void finalizeSubscription(Long userId, String stripeSessionId) {
        log.info("========== SUBSCRIPTION FINALIZATION STARTED ==========");
        log.info("UserId: {}, StripeSessionId: {}", userId, stripeSessionId);

        // Duplicate payment check
        if (paymentRepository.existsByTransactionRef(stripeSessionId)) {
            log.warn("⚠️ This Stripe session has already been processed, skipping: {}", stripeSessionId);
            return;
        }

        // Find user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Duplicate subscription check
        if (subscriptionRepository.findByUser(user).isPresent()) {
            log.warn("⚠️ User already has an active subscription: UserId={}", userId);
            return;
        }

        // 🚀 Get subscription returned from HELPER
        SubscriptionEntity savedSubscription = subscriptionHelper.finalizeSubscriptionWithResources(user, "Premium", 1500.0, 1);

        // ✅ Use savedSubscription directly when creating payment
        PaymentEntity payment = PaymentEntity.builder()
                .subscription(savedSubscription) // <-- used instead of user.getSubscription()
                .amount(1500.0)
                .provider("stripe")
                .status(PaymentStatus.SUCCESS)
                .transactionRef(stripeSessionId)
                .paymentDate(LocalDateTime.now())
                .description("Premium Plan Subscription")
                .build();

        paymentRepository.save(payment);
        log.info("✅ Payment record created: TransactionRef={}", payment.getTransactionRef());

        log.info("========== SUBSCRIPTION FINALIZATION COMPLETED ==========");
    }
}