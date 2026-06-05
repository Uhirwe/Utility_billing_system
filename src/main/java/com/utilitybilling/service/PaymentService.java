package com.utilitybilling.service;

import com.utilitybilling.dto.payment.PaymentRequest;
import com.utilitybilling.dto.payment.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponse recordPayment(PaymentRequest request);
    Page<PaymentResponse> getPaymentHistory(Long billId, Pageable pageable);
    Page<PaymentResponse> getCustomerPaymentHistory(Long customerId, Pageable pageable);
}
