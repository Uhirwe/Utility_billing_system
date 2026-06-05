package com.utilitybilling.dto.auth;

import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;

    @Pattern(regexp = ValidationConstants.COUNTRY_CODE_PATTERN, message = ValidationConstants.COUNTRY_CODE_MESSAGE)
    private String phoneCountryCode;

    @Pattern(regexp = ValidationConstants.RWANDA_PHONE_PATTERN, message = ValidationConstants.RWANDA_PHONE_MESSAGE)
    private String phoneNumber;
}
