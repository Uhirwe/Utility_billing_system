package com.utilitybilling.dto.tariff;

import com.utilitybilling.enums.MeterType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TariffResponse {
    private Long id;
    private MeterType meterType;
    private String tariffName;
    private BigDecimal ratePerUnit;
    private BigDecimal fixedCharge;
    private BigDecimal vatPercentage;
    private BigDecimal latePenaltyPercentage;
    private Integer versionNumber;
    private LocalDate effectiveDate;
    private Boolean active;
    private LocalDateTime createdAt;
}
