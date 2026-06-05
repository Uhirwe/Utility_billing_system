package com.utilitybilling.dto.customer;

import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerProfileUpdateRequest {

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String address;

    @Pattern(regexp = ValidationConstants.COUNTRY_CODE_PATTERN, message = ValidationConstants.COUNTRY_CODE_MESSAGE)
    private String phoneCountryCode;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationConstants.RWANDA_PHONE_PATTERN, message = ValidationConstants.RWANDA_PHONE_MESSAGE)
    private String phoneNumber;
}
