package com.nutriflow.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Added for logging
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Added for logging
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String otp) {
        log.info("Email sending process started. Recipient: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("NutriFlow <noreply@nutriflow.com>");
            message.setSubject("NutriFlow - Verification Code");
            message.setText("Hello!\n\n" +
                    "Thank you for joining NutriFlow. " +
                    "Your verification code to activate your account is: " + otp + "\n\n" +
                    "This code is valid for 5 minutes.\n\n" +
                    "Best regards,\nNutriFlow Team");

            log.debug("Mail object prepared, sending... OTP: {}", otp);

            mailSender.send(message);

            log.info("Email sent successfully: {}", to);

        } catch (Exception e) {
            // Since this is an async method, catching and logging the error here is essential
            log.error("An error occurred while sending the email! Recipient: {}, Error: {}", to, e.getMessage());
        }
    }
}