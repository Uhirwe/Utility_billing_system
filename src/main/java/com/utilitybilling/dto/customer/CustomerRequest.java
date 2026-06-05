package com.utilitybilling.dto.customer;

import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {

    @NotBlank(message = "Full names are required")
    @Size(max = 150)
    private String fullNames;

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = ValidationConstants.NATIONAL_ID_PATTERN, message = ValidationConstants.NATIONAL_ID_MESSAGE)
    private String nationalId;

    @NotBlank(message = "Email is required")
    @Email(message = ValidationConstants.EMAIL_MESSAGE)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationConstants.RWANDA_PHONE_PATTERN, message = ValidationConstants.RWANDA_PHONE_MESSAGE)
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String address;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
}
