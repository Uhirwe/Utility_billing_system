package com.utilitybilling.dto.auth;

import com.utilitybilling.util.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Customer self-registration (ROLE_CUSTOMER only). National ID is validated during signup.")
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = ValidationConstants.NATIONAL_ID_PATTERN, message = ValidationConstants.NATIONAL_ID_MESSAGE)
    private String nationalId;

    @NotBlank(message = "Email is required")
    @Email(message = ValidationConstants.EMAIL_MESSAGE)
    @Pattern(regexp = ValidationConstants.EMAIL_PATTERN, message = ValidationConstants.EMAIL_MESSAGE)
    private String email;

    @Pattern(regexp = ValidationConstants.COUNTRY_CODE_PATTERN, message = ValidationConstants.COUNTRY_CODE_MESSAGE)
    @Schema(description = "Phone country code", example = "+250", defaultValue = "+250")
    private String phoneCountryCode = ValidationConstants.DEFAULT_COUNTRY_CODE;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationConstants.RWANDA_PHONE_PATTERN, message = ValidationConstants.RWANDA_PHONE_MESSAGE)
    @Schema(description = "Local Rwanda phone number without country code", example = "0788123456")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String address;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_MESSAGE)
    private String password;
}
