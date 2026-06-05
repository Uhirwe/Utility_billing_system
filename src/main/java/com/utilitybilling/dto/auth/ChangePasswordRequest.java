package com.utilitybilling.dto.auth;

import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_MESSAGE)
    private String newPassword;
}
