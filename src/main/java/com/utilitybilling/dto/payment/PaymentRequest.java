package com.utilitybilling.dto.payment;

import com.utilitybilling.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRequest {

    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime paymentDate;
}
