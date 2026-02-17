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
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    // ✅ Əgər repository lazımdırsa əlavə edin (amma bu service-də lazım deyil)
    // private final SubscriptionRepository subscriptionRepository;

    /**
     * Abunəlik 7 gün sonra bitəcək xəbərdarlığı
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
            message.setSubject("⚠️ NutriFlow Premium Abunəliyiniz Tezliklə Bitir");
            message.setText(buildExpirationWarningEmail(userName, endDate));

            mailSender.send(message);

            log.info("✅ [EMAIL] Abunəlik xəbərdarlığı göndərildi: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ [EMAIL] Email göndərilmədi: {}", e.getMessage(), e);
        }
    }

    /**
     * Abunəlik bitdi bildirişi
     */
    public void sendSubscriptionExpiredNotification(SubscriptionEntity subscription) {
        try {
            String userEmail = subscription.getUser().getEmail();
            String userName = subscription.getUser().getFirstName();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tural57535@gmail.com");
            message.setTo(userEmail);
            message.setSubject("❌ NutriFlow Premium Abunəliyiniz Bitdi");
            message.setText(buildExpiredEmail(userName));

            mailSender.send(message);

            log.info("✅ [EMAIL] Abunəlik bitdi bildirişi göndərildi: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ [EMAIL] Email göndərilmədi: {}", e.getMessage(), e);
        }
    }

    /**
     * Admin üçün həftəlik report
     */
    public void sendWeeklyReportToAdmin(long activeCount, long expiredCount, long cancelledCount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tural57535@gmail.com");
            message.setTo("admin@nutriflow.com");
            message.setSubject("📊 NutriFlow - Həftəlik Subscription Report");
            message.setText(buildWeeklyReportEmail(activeCount, expiredCount, cancelledCount));

            mailSender.send(message);

            log.info("✅ [EMAIL] Həftəlik report admin-ə göndərildi");

        } catch (Exception e) {
            log.error("❌ [EMAIL] Admin report göndərilmədi: {}", e.getMessage(), e);
        }
    }

    // ============== EMAIL TEMPLATE-LƏRİ ==============

    private String buildExpirationWarningEmail(String userName, String endDate) {
        return String.format("""
                Hörmətli %s,
                
                NutriFlow Premium abunəliyiniz tezliklə bitəcək! ⏰
                
                📅 Abunəlik bitiş tarixi: %s
                
                Premium xüsusiyyətlərinizi itirməmək üçün abunəliyi yeniləyin:
                
                ✅ Qida planlarına sınırsız giriş
                ✅ Dietoloqla birbaşa əlaqə
                ✅ Peşəkar menyu planları
                ✅ Çatdırılma xidməti
                
                Abunəliyi yeniləmək üçün: https://nutriflow.com/subscription
                
                Hörmətlə,
                NutriFlow Komandası
                """, userName, endDate);
    }

    private String buildExpiredEmail(String userName) {
        return String.format("""
                Hörmətli %s,
                
                NutriFlow Premium abunəliyiniz bitdi. 😔
                
                Premium xüsusiyyətlərinizə giriş dayandırılıb.
                
                Yenidən premium xidmətlərdən istifadə etmək üçün abunəliyi yeniləyin:
                https://nutriflow.com/subscription
                
                Hörmətlə,
                NutriFlow Komandası
                """, userName);
    }

    private String buildWeeklyReportEmail(long activeCount, long expiredCount, long cancelledCount) {
        long totalCount = activeCount + expiredCount + cancelledCount;
        return String.format("""
                📊 HƏFTƏLIK SUBSCRIPTION REPORT
                ================================
                
                ✅ Aktiv Abunəliklər: %d
                ❌ Bitmiş Abunəliklər: %d
                🚫 Ləğv Edilmiş: %d
                
                📈 Toplam: %d
                
                ---
                NutriFlow Admin Panel
                """, activeCount, expiredCount, cancelledCount, totalCount);
    }
}