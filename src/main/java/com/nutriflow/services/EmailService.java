package com.nutriflow.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Loglama üçün əlavə edildi
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Loglama üçün əlavə edildi
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String otp) {
        log.info("E-poçt göndərilməsi prosesi başladı. Alıcı: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("NutriFlow <noreply@nutriflow.com>");
            message.setSubject("NutriFlow - Təsdiq Kodu");
            message.setText("Salam!\n\n" +
                    "NutriFlow-a qoşulduğunuz üçün təşəkkür edirik. " +
                    "Hesabınızı aktivləşdirmək üçün təsdiq kodunuz: " + otp + "\n\n" +
                    "Bu kod 5 dəqiqə ərzində etibarlıdır.\n\n" +
                    "Hörmətlə,\nNutriFlow Komandası");

            log.debug("Mail obyekti hazırlandı, göndərilir... OTP: {}", otp);

            mailSender.send(message);

            log.info("E-poçt uğurla göndərildi: {}", to);

        } catch (Exception e) {
            // Asinxron metod olduğu üçün xətanı burada tutub loglamaq mütləqdir
            log.error("E-poçt göndərilərkən xəta baş verdi! Alıcı: {}, Xəta: {}", to, e.getMessage());
        }
    }
}