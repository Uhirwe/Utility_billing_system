package com.utilitybilling.dto.meter;

import com.utilitybilling.enums.BillingMode;
import com.utilitybilling.enums.MeterType;
import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MeterRequest {

    @NotBlank(message = "Meter number is required")
    @Pattern(regexp = ValidationConstants.METER_NUMBER_PATTERN, message = ValidationConstants.METER_NUMBER_MESSAGE)
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotNull(message = "Installation date is required")
    @PastOrPresent(message = "Installation date cannot be in the future")
    private LocalDate installationDate;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private BillingMode billingMode = BillingMode.POSTPAID;
}
