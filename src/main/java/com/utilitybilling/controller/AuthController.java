package com.utilitybilling.controller;

import com.utilitybilling.dto.auth.*;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and profile management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Customer self-registration with national ID validation (ROLE_CUSTOMER only)")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Customer registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and obtain JWT token")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/first-login/change-password")
    @Operation(summary = "Change temporary password on first login (required before JWT login)")
    public ApiResponse<Void> completeFirstLogin(@Valid @RequestBody FirstLoginPasswordRequest request) {
        authService.completeFirstLogin(request);
        return ApiResponse.success("Password updated. You may now login.", null);
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change authenticated user password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ApiResponse.success("Password changed successfully", null);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update authenticated user profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        authService.updateProfile(userDetails.getUsername(), request);
        return ApiResponse.success("Profile updated successfully", null);
    }
}
