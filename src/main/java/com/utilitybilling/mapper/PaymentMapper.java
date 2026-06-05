package com.utilitybilling.mapper;

import com.utilitybilling.dto.payment.PaymentResponse;
import com.utilitybilling.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .billId(payment.getBill().getId())
                .billNumber(payment.getBill().getBillNumber())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
