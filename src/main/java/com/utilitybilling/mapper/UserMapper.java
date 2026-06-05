package com.utilitybilling.mapper;

import com.utilitybilling.dto.user.UserResponse;
import com.utilitybilling.entity.User;
import com.utilitybilling.enums.RoleName;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullNames(user.getFullNames())
                .email(user.getEmail())
                .phoneCountryCode(user.getPhoneCountryCode())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .passwordExpired(user.getPasswordExpired())
                .accountLocked(user.getAccountLocked())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
