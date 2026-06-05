package com.utilitybilling.dto.tariff;

import com.utilitybilling.enums.MeterType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TariffRequest {

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotBlank(message = "Tariff name is required")
    private String tariffName;

    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.01", message = "Rate per unit must be greater than zero")
    private BigDecimal ratePerUnit;

    @NotNull(message = "Fixed charge is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Service charge cannot be negative")
    private BigDecimal fixedCharge;

    @NotNull(message = "VAT percentage is required")
    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal vatPercentage;

    @NotNull(message = "Late penalty percentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Late penalty cannot be negative")
    private BigDecimal latePenaltyPercentage;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @Valid
    private List<TariffTierRequest> tiers;
}
