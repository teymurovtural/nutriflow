package com.nutriflow.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
            mailSender.send(message);
            log.info("Verification email sent successfully: {}", to);
        } catch (Exception e) {
            log.error("Error sending verification email! Recipient: {}, Error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendForgotPasswordEmail(String to, String otp) {
        log.info("Forgot password email sending started. Recipient: {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("NutriFlow <noreply@nutriflow.com>");
            message.setSubject("NutriFlow - Password Reset Code");
            message.setText("Hello!\n\n" +
                    "We received a request to reset your NutriFlow account password.\n" +
                    "Your password reset code is: " + otp + "\n\n" +
                    "This code is valid for 5 minutes.\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Best regards,\nNutriFlow Team");
            mailSender.send(message);
            log.info("Forgot password email sent successfully: {}", to);
        } catch (Exception e) {
            log.error("Error sending forgot password email! Recipient: {}, Error: {}", to, e.getMessage());
        }
    }
}