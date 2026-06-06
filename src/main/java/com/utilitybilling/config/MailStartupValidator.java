package com.utilitybilling.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Tests Gmail SMTP on startup and prints actionable instructions if auth fails.
 */
@Slf4j
@Component
public class MailStartupValidator {

    private final Optional<JavaMailSender> mailSender;

    @Autowired
    public MailStartupValidator(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${app.mail.validate-on-startup:true}")
    private boolean validateOnStartup;

    @Value("${app.mail.provider:smtp-with-fallback}")
    private String mailProvider;

    @PostConstruct
    public void validate() {
        if (!validateOnStartup) {
            return;
        }
        if ("console".equalsIgnoreCase(mailProvider)) {
            log.info("Mail provider=console — emails print in server log only.");
            return;
        }

        JavaMailSender sender = mailSender.orElse(null);
        if (!(sender instanceof JavaMailSenderImpl impl)) {
            if (sender == null) {
                log.warn("JavaMailSender not configured — set spring.mail.* in application-local.properties for SMTP.");
            }
            return;
        }

        String user = impl.getUsername();
        if (user == null || user.isBlank()) {
            log.error("""
                    
                    ========== EMAIL NOT CONFIGURED ==========
                    Set in application-local.properties:
                      spring.mail.username=your@gmail.com
                      spring.mail.password=your-16-char-app-password
                      app.mail.from=your@gmail.com
                    Or set app.mail.provider=console to print emails in this log.
                    ==========================================
                    """);
            return;
        }

        try {
            impl.testConnection();
            log.info("Gmail SMTP connection OK for user={}", user);
        } catch (Exception ex) {
            log.error("""
                    
                    ========== GMAIL SMTP AUTH FAILED ==========
                    User: {}
                    Error: {}
                    
                    FIX (do ALL steps):
                    1. Open https://myaccount.google.com/security
                    2. Enable 2-Step Verification
                    3. Open https://myaccount.google.com/apppasswords
                    4. Create App Password for "Mail" / "Other (Utility Billing)"
                    5. Copy the 16-character password (no spaces) into application-local.properties:
                       spring.mail.password=xxxxxxxxxxxxxxxx
                    6. spring.mail.username MUST be the SAME Gmail account
                    7. Restart the application
                    
                    TEMP WORKAROUND: set app.mail.provider=console
                    Emails will print in this console until Gmail is fixed.
                    =============================================
                    """, user, ex.getMessage());
        }
    }
}
