package com.utilitybilling.dto.reading;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MeterReadingRequest {

    @NotNull(message = "Meter ID is required")
    private Long meterId;

    @NotNull(message = "Previous reading is required")
    @DecimalMin(value = "0.0", message = "Previous reading cannot be negative")
    private BigDecimal previousReading;

    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.0", message = "Current reading cannot be negative")
    private BigDecimal currentReading;

    @NotNull(message = "Reading date is required")
    @PastOrPresent(message = "Reading date cannot be in the future")
    private LocalDate readingDate;
}
