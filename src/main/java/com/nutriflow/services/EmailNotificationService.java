package com.nutriflow.services;

import com.nutriflow.entities.SubscriptionEntity;
import com.nutriflow.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EmailNotificationService {

    private final JavaMailSender mailSender;


    // ✅ Add if repository is needed (not required in this service)
    // private final SubscriptionRepository subscriptionRepository;

    /**
     * Subscription expiration warning - 7 days remaining
     */
    public void sendSubscriptionExpirationWarning(SubscriptionEntity subscription) {
        try {
            String userEmail = subscription.getUser().getEmail();
            String userName = subscription.getUser().getFirstName();
            String endDate = subscription.getEndDate()
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tural57535@gmail.com");
            message.setTo(userEmail);
            message.setSubject("⚠️ Your NutriFlow Premium Subscription is Expiring Soon");
            message.setText(buildExpirationWarningEmail(userName, endDate));

            mailSender.send(message);

            log.info("✅ [EMAIL] Subscription expiration warning sent: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ [EMAIL] Email could not be sent: {}", e.getMessage(), e);
        }
    }

    /**
     * Subscription expired notification
     */
    public void sendSubscriptionExpiredNotification(SubscriptionEntity subscription) {
        try {
            String userEmail = subscription.getUser().getEmail();
            String userName = subscription.getUser().getFirstName();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tural57535@gmail.com");
            message.setTo(userEmail);
            message.setSubject("❌ Your NutriFlow Premium Subscription Has Expired");
            message.setText(buildExpiredEmail(userName));

            mailSender.send(message);

            log.info("✅ [EMAIL] Subscription expired notification sent: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ [EMAIL] Email could not be sent: {}", e.getMessage(), e);
        }
    }

    /**
     * Weekly report for admin
     */
    public void sendWeeklyReportToAdmin(long activeCount, long expiredCount, long cancelledCount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tural57535@gmail.com");
            message.setTo("tural57535@gmail.com");
            message.setSubject("📊 NutriFlow - Weekly Subscription Report");
            message.setText(buildWeeklyReportEmail(activeCount, expiredCount, cancelledCount));

            mailSender.send(message);

            log.info("✅ [EMAIL] Weekly report sent to admin");

        } catch (Exception e) {
            log.error("❌ [EMAIL] Admin report could not be sent: {}", e.getMessage(), e);
        }
    }

    // ============== EMAIL TEMPLATES ==============

    private String buildExpirationWarningEmail(String userName, String endDate) {
        return String.format("""
                Dear %s,
                
                Your NutriFlow Premium subscription is expiring soon! ⏰
                
                📅 Subscription end date: %s
                
                Renew your subscription to keep your Premium features:
                
                ✅ Unlimited access to nutrition plans
                ✅ Direct contact with your dietitian
                ✅ Professional menu plans
                ✅ Delivery service
                
                To renew your subscription: https://nutriflow.com/subscription
                
                Best regards,
                NutriFlow Team
                """, userName, endDate);
    }

    private String buildExpiredEmail(String userName) {
        return String.format("""
                Dear %s,
                
                Your NutriFlow Premium subscription has expired. 😔
                
                Access to your Premium features has been suspended.
                
                To use premium services again, please renew your subscription:
                https://nutriflow.com/subscription
                
                Best regards,
                NutriFlow Team
                """, userName);
    }

    private String buildWeeklyReportEmail(long activeCount, long expiredCount, long cancelledCount) {
        long totalCount = activeCount + expiredCount + cancelledCount;
        return String.format("""
                📊 WEEKLY SUBSCRIPTION REPORT
                ================================
                
                ✅ Active Subscriptions: %d
                ❌ Expired Subscriptions: %d
                🚫 Cancelled: %d
                
                📈 Total: %d
                
                ---
                NutriFlow Admin Panel
                """, activeCount, expiredCount, cancelledCount, totalCount);
    }
}