package com.utilitybilling.dto.user;

import com.utilitybilling.enums.RoleName;
import com.utilitybilling.util.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Admin creates ADMIN, OPERATOR, or FINANCE account with temporary password")
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = ValidationConstants.EMAIL_MESSAGE)
    @Pattern(regexp = ValidationConstants.EMAIL_PATTERN, message = ValidationConstants.EMAIL_MESSAGE)
    private String email;

    @Pattern(regexp = ValidationConstants.COUNTRY_CODE_PATTERN, message = ValidationConstants.COUNTRY_CODE_MESSAGE)
    @Schema(example = "+250", defaultValue = "+250")
    private String phoneCountryCode = ValidationConstants.DEFAULT_COUNTRY_CODE;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationConstants.RWANDA_PHONE_PATTERN, message = ValidationConstants.RWANDA_PHONE_MESSAGE)
    private String phoneNumber;

    @NotNull(message = "Role is required")
    @Schema(description = "ROLE_ADMIN, ROLE_OPERATOR, or ROLE_FINANCE", example = "ROLE_OPERATOR")
    private RoleName role;
}
