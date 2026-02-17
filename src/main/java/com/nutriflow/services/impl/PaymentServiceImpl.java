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
 * Subscription Helper istifadə edərək assignment logic-i ayrılıb.
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
        log.info("✅ Stripe API uğurla inisializasiya olundu");
    }

    @Override
    public String createCheckoutSession(Long userId) throws StripeException {
        log.info("Stripe Checkout Session yaradılması başladı: UserId={}", userId);

        // Metadata-da userId saxla
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
                                                .setUnitAmount(150000L) // 1500 AZN = 150000 qəpik
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Premium Plan")
                                                                .setDescription("Aylıq Premium Abunəlik")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putAllMetadata(metadata)
                .build();

        Session session = Session.create(params);
        log.info("✅ Stripe Session yaradıldı: ID={}, URL={}", session.getId(), session.getUrl());

        return session.getUrl();
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        log.info("📩 Stripe-dan Webhook bildirişi alındı");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("✅ Webhook eventi doğrulandı: EventId={}, Type={}", event.getId(), event.getType());
        } catch (SignatureVerificationException e) {
            log.error("❌ Webhook signature doğrulanmadı: {}", e.getMessage());
            throw new WebhookProcessingException("Invalid signature");
        }

        // Event tipinə görə işləmə
        switch (event.getType()) {
            case "checkout.session.completed" -> {
                log.info("💳 Ödəniş uğurla tamamlanıb (checkout.session.completed)");
                handleCheckoutSessionCompleted(event);
            }
            case "payment_intent.succeeded", "charge.succeeded", "payment_intent.created" ->
                    log.debug("ℹ️ Bu event növü üçün xüsusi emal tələb olunmur: {}", event.getType());
            default ->
                    log.warn("⚠️ Naməlum event tipi: {}", event.getType());
        }
    }

    /**
     * Checkout session completed event-ini işləyir.
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject;

            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {
                log.warn("⚠️ Deserializer obyekti tapmadı, manual casting edilir");
                stripeObject = (StripeObject) event.getData().getObject();
            }

            Session session = (Session) stripeObject;

            // Metadata-dan userId al
            Map<String, String> metadata = session.getMetadata();
            Long userId = Long.parseLong(metadata.get("userId"));
            String stripeSessionId = session.getId();

            log.info("📋 Metadata oxundu: UserId={}, StripeSessionId={}", userId, stripeSessionId);

            // Subscription finalize et
            finalizeSubscription(userId, stripeSessionId);

        } catch (Exception e) {
            log.error("❌ Webhook emalı zamanı gözlənilməz xəta: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void finalizeSubscription(Long userId, String stripeSessionId) {
        log.info("========== SUBSCRIPTION FİNALİZATİON BAŞLADI ==========");
        log.info("UserId: {}, StripeSessionId: {}", userId, stripeSessionId);

        // Duplicate payment yoxlaması
        if (paymentRepository.existsByTransactionRef(stripeSessionId)) {
            log.warn("⚠️ Bu Stripe session artıq işlənilib, təkrar emal edilmir: {}", stripeSessionId);
            return;
        }

        // User-i tap
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        // Duplicate subscription yoxlaması
        if (subscriptionRepository.findByUser(user).isPresent()) {
            log.warn("⚠️ İstifadəçinin artıq aktiv subscription-ı var: UserId={}", userId);
            return;
        }

        // 🚀 HELPER-dən qayıdan abunəliyi alırıq
        SubscriptionEntity savedSubscription = subscriptionHelper.finalizeSubscriptionWithResources(user, "Premium", 1500.0, 1);

        // ✅ Payment yaradarkən birbaşa savedSubscription istifadə edirik
        PaymentEntity payment = PaymentEntity.builder()
                .subscription(savedSubscription) // <-- user.getSubscription() yerinə bunu yazdıq
                .amount(1500.0)
                .provider("stripe")
                .status(PaymentStatus.SUCCESS)
                .transactionRef(stripeSessionId)
                .paymentDate(LocalDateTime.now())
                .description("Premium Plan Subscription")
                .build();

        paymentRepository.save(payment);
        log.info("✅ Ödəniş rekordu yaradıldı: TransactionRef={}", payment.getTransactionRef());

        log.info("========== SUBSCRIPTION FİNALİZATİON TAMAMLANDI ==========");
    }
}