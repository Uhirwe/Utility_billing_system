package com.utilitybilling.dto.bill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateMonthlyBillsRequest {

    @NotNull(message = "Billing month is required")
    @Min(1) @Max(12)
    private Integer billingMonth;

    @NotNull(message = "Billing year is required")
    @Min(2000)
    private Integer billingYear;
}
