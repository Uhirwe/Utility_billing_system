package com.utilitybilling.service;

import com.utilitybilling.dto.email.EmailDeliveryResult;
import com.utilitybilling.enums.RoleName;

import java.math.BigDecimal;

public interface EmailService {
    EmailDeliveryResult sendCustomerRegistrationEmail(String to, String fullName);
    EmailDeliveryResult sendWelcomeEmail(String to, String fullName, String temporaryPassword, RoleName role, String loginUrl);
    EmailDeliveryResult sendRoleUpdateEmail(String to, String fullName, RoleName newRole);
    EmailDeliveryResult sendRoleRevokedEmail(String to, String fullName);
    EmailDeliveryResult sendBillGeneratedEmail(String to, String fullName, String billNumber, BigDecimal totalAmount, int month, int year);
    EmailDeliveryResult sendPaymentConfirmationEmail(String to, String fullName, String paymentReference, BigDecimal amountPaid, String billNumber);
}
