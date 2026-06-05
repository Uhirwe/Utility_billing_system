package com.utilitybilling.dto.auth;

import com.utilitybilling.dto.user.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private UserResponse user;
    private Boolean passwordChangeRequired;
}
