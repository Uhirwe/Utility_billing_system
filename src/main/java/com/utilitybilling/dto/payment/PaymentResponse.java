package com.utilitybilling.dto.payment;

import com.utilitybilling.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private String paymentReference;
    private Long billId;
    private String billNumber;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
}
