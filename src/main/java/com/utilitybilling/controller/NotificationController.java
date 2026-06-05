package com.utilitybilling.controller;

import com.utilitybilling.dto.notification.NotificationResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.CustomerAccessService;
import com.utilitybilling.service.NotificationService;
import com.utilitybilling.util.PageableHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Customer notification operations")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;
    private final CustomerAccessService customerAccessService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer views own notifications (paginated)",
            description = "Page starts at 1. Returns 20 notifications per page, newest first. Results are in data.content.")
    public ApiResponse<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetails user,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        Long customerId = customerAccessService.requireOwnCustomerId(user.getUsername());
        return ApiResponse.success(notificationService.getNotifications(customerId,
                PageableHelper.ofPage(page, "notificationDate", Sort.Direction.DESC)));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get notifications for a customer (paginated)",
            description = "Page starts at 1. Returns 20 notifications per page, newest first. Results are in data.content.")
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long customerId,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        customerAccessService.assertStaffOrOwnCustomer(user.getUsername(), customerId);
        return ApiResponse.success(notificationService.getNotifications(customerId,
                PageableHelper.ofPage(page, "notificationDate", Sort.Direction.DESC)));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Mark notification as read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        notificationService.markAsRead(id, user.getUsername());
        return ApiResponse.success("Notification marked as read", null);
    }
}
