package com.utilitybilling.dto.tariff;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffTierRequest {

    @NotBlank
    private String tierName;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal minUnits;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal maxUnits;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal ratePerUnit;
}
