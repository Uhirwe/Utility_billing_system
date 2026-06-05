package com.utilitybilling.dto.auth;

import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FirstLoginPasswordRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String temporaryPassword;

    @NotBlank
    @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_MESSAGE)
    private String newPassword;
}
