package com.utilitybilling.controller;

import com.utilitybilling.dto.customer.CustomerProfileUpdateRequest;
import com.utilitybilling.dto.customer.CustomerResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.utilitybilling.util.PageableHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Customer self-service and staff customer views")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer views own profile")
    public ApiResponse<CustomerResponse> getMyProfile(@AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(customerService.getMyProfile(user.getUsername()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer updates own profile (address, phone)")
    public ApiResponse<CustomerResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CustomerProfileUpdateRequest request) {
        return ApiResponse.success("Profile updated", customerService.updateMyProfile(user.getUsername(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get customer by ID (customers may only view own record)")
    public ApiResponse<CustomerResponse> getCustomerById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ApiResponse.success(customerService.getCustomerById(id, user.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Get all customers (paginated)",
            description = "Page starts at 1. Returns 20 customers per page, sorted by name. Results are in data.content.")
    public ApiResponse<Page<CustomerResponse>> getAllCustomers(
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        var pageable = PageableHelper.ofPage(page, "fullNames", Sort.Direction.ASC);
        return ApiResponse.success(customerService.getAllCustomers(pageable));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Activate customer (operator only)")
    public ApiResponse<Void> activateCustomer(@PathVariable Long id) {
        customerService.activateCustomer(id);
        return ApiResponse.success("Customer activated", null);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Deactivate customer (operator only)")
    public ApiResponse<Void> deactivateCustomer(@PathVariable Long id) {
        customerService.deactivateCustomer(id);
        return ApiResponse.success("Customer deactivated", null);
    }
}
