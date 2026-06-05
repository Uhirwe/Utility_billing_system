package com.utilitybilling.dto.auth;

import com.utilitybilling.util.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = ValidationConstants.EMAIL_MESSAGE)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
