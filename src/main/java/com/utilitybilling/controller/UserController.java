package com.utilitybilling.controller;

import com.utilitybilling.dto.user.CreateUserRequest;
import com.utilitybilling.dto.user.CreateUserResponse;
import com.utilitybilling.dto.user.UpdateUserRoleRequest;
import com.utilitybilling.dto.user.UserResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.UserService;
import com.utilitybilling.util.PageableHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin staff user management (create, view, upgrade, revoke)")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin creates ADMIN, OPERATOR, or FINANCE user",
            description = "Generates temporary password, sets passwordExpired=true, sends credentials by email. Customers use /auth/register. New user must call POST /auth/first-login/change-password before login.")
    public ApiResponse<CreateUserResponse> createStaffUser(
            @AuthenticationPrincipal UserDetails actor,
            @Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse result = userService.createStaffUser(request, actor.getUsername());
        String message = result.getEmailDelivery().isSent()
                ? "User created. Credentials emailed to " + result.getEmailDelivery().getRecipient()
                : "User created but email NOT sent: " + result.getEmailDelivery().getDetail();
        return ApiResponse.success(message, result);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin upgrades a user's role (ADMIN, OPERATOR, or FINANCE)")
    public ApiResponse<UserResponse> upgradeUserRole(
            @AuthenticationPrincipal UserDetails actor,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ApiResponse.success("Role upgraded", userService.upgradeUserRole(id, request, actor.getUsername()));
    }

    @DeleteMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin revokes elevated role — user reverts to ROLE_CUSTOMER")
    public ApiResponse<UserResponse> revokeUserRole(
            @AuthenticationPrincipal UserDetails actor,
            @PathVariable Long id) {
        return ApiResponse.success("Role revoked to ROLE_CUSTOMER", userService.revokeUserRole(id, actor.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (paginated)",
            description = "Page starts at 1. Returns 20 users per page, sorted by id. Results are in data.content.")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(userService.getAllUsers(
                PageableHelper.ofPage(page, "id", Sort.Direction.ASC)));
    }
}
