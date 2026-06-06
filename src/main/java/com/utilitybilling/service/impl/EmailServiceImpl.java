package com.utilitybilling.service.impl;

import com.utilitybilling.dto.email.EmailDeliveryResult;
import com.utilitybilling.enums.RoleName;
import com.utilitybilling.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final Optional<JavaMailSender> mailSender;

    @Autowired
    public EmailServiceImpl(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${app.mail.from:}")
    private String fromEmail;

    @Value("${app.login-url:http://localhost:8080/api/swagger-ui.html}")
    private String defaultLoginUrl;

    @Value("${app.mail.provider:smtp-with-fallback}")
    private String mailProvider;

    @Override
    public EmailDeliveryResult sendCustomerRegistrationEmail(String to, String fullName) {
        String body = """
                Dear %s,

                Welcome to the WASAC/REG Utility Billing System.

                Your customer account has been created successfully after national ID validation.

                You can now log in with your registered email and password.
                Login URL: %s

                Regards,
                Utility Billing System
                """.formatted(fullName, defaultLoginUrl);
        return deliver(to, "Welcome - Utility Billing Customer Account", body);
    }

    @Override
    public EmailDeliveryResult sendWelcomeEmail(String to, String fullName, String temporaryPassword,
                                                RoleName role, String loginUrl) {
        String body = """
                Dear %s,

                Your Utility Billing System account has been created.

                Email/Username: %s
                Temporary Password: %s
                Assigned Role: %s
                Login URL: %s

                IMPORTANT: Change your temporary password using:
                POST /auth/first-login/change-password

                Regards,
                Utility Billing System
                """.formatted(fullName, to, temporaryPassword, role.name(),
                loginUrl != null ? loginUrl : defaultLoginUrl);
        return deliver(to, "Welcome - Utility Billing System Account Created", body);
    }

    @Override
    public EmailDeliveryResult sendRoleUpdateEmail(String to, String fullName, RoleName newRole) {
        String body = """
                Dear %s,

                Your system role has been upgraded to: %s

                Regards,
                Utility Billing System
                """.formatted(fullName, newRole.name());
        return deliver(to, "Role Upgraded - Utility Billing System", body);
    }

    @Override
    public EmailDeliveryResult sendRoleRevokedEmail(String to, String fullName) {
        String body = """
                Dear %s,

                Your elevated system role has been revoked. Your account is now assigned ROLE_CUSTOMER.

                Regards,
                Utility Billing System
                """.formatted(fullName);
        return deliver(to, "Role Revoked - Utility Billing System", body);
    }

    @Override
    public EmailDeliveryResult sendBillGeneratedEmail(String to, String fullName, String billNumber,
                                                    BigDecimal totalAmount, int month, int year,
                                                    LocalDate dueDate) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String body = """
                Dear %s,

                Your %s %d utility bill has been generated.

                Bill Number: %s
                Amount Due: %s FRW
                Due Date: %s

                View your bills after login: %s

                Regards,
                WASAC/REG Utility Billing System
                """.formatted(fullName, monthName, year, billNumber, totalAmount, dueDate, defaultLoginUrl);
        return deliver(to, "Utility Bill - " + billNumber, body);
    }

    @Override
    public EmailDeliveryResult sendPaymentConfirmationEmail(String to, String fullName, String paymentReference,
                                                            BigDecimal amountPaid, String billNumber) {
        String body = "Dear %s, Your payment of %s FRW has been successfully received."
                .formatted(fullName, amountPaid);
        return deliver(to, "Payment Completed", body);
    }

    private EmailDeliveryResult deliver(String to, String subject, String body) {
        if ("console".equalsIgnoreCase(mailProvider)) {
            logToConsole(to, subject, body);
            return EmailDeliveryResult.builder()
                    .sent(false).channel("CONSOLE").recipient(to)
                    .detail("Email printed in server log only (console mode).")
                    .build();
        }

        if (trySmtp(to, subject, body)) {
            return EmailDeliveryResult.builder()
                    .sent(true).channel("SMTP").recipient(to)
                    .detail("Email delivered via Gmail SMTP.")
                    .build();
        }

        if ("smtp-with-fallback".equalsIgnoreCase(mailProvider)) {
            logToConsole(to, subject, body);
            return EmailDeliveryResult.builder()
                    .sent(false).channel("CONSOLE_FALLBACK").recipient(to)
                    .detail("SMTP failed. Email logged in server console.")
                    .build();
        }

        return EmailDeliveryResult.builder()
                .sent(false).channel("FAILED").recipient(to)
                .detail("SMTP delivery failed.")
                .build();
    }

    private boolean trySmtp(String to, String subject, String body) {
        if (mailSender.isEmpty()) {
            log.error("SMTP skipped — JavaMailSender not configured. Copy application-local.properties.example and set spring.mail.*.");
            return false;
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            log.error("SMTP skipped — app.mail.from is empty.");
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.get().send(message);
            log.info("SMTP email sent to={} subject={}", to, subject);
            return true;
        } catch (Exception ex) {
            log.error("SMTP failed to={} error={}", to, ex.getMessage(), ex);
            return false;
        }
    }

    private void logToConsole(String to, String subject, String body) {
        log.warn("""
                ==================== EMAIL (NOT SENT TO INBOX) ====================
                TO: {}
                SUBJECT: {}
                {}
                ====================================================================
                """, to, subject, body);
    }
}
