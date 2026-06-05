package com.utilitybilling.dto.user;

import com.utilitybilling.enums.RoleName;
import com.utilitybilling.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullNames;
    private String email;
    private String phoneCountryCode;
    private String phoneNumber;
    private UserStatus status;
    private Boolean passwordExpired;
    private Boolean accountLocked;
    private Set<RoleName> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
