package com.utilitybilling.dto.bill;

import com.utilitybilling.enums.BillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BillResponse {
    private Long id;
    private String billNumber;
    private Long customerId;
    private String customerName;
    private Long meterId;
    private String meterNumber;
    private Integer billingMonth;
    private Integer billingYear;
    private BigDecimal consumption;
    private Long tariffId;
    private String tariffName;
    private BigDecimal fixedCharge;
    private BigDecimal taxAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private BillStatus billStatus;
    private Boolean approved;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDate dueDate;
    private LocalDateTime generatedDate;
    private LocalDateTime createdAt;
}
