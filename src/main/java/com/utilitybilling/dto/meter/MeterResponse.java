package com.utilitybilling.dto.meter;

import com.utilitybilling.enums.BillingMode;
import com.utilitybilling.enums.MeterStatus;
import com.utilitybilling.enums.MeterType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MeterResponse {
    private Long id;
    private String meterNumber;
    private MeterType meterType;
    private LocalDate installationDate;
    private MeterStatus status;
    private BillingMode billingMode;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
