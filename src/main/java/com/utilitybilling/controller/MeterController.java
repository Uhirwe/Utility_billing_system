package com.utilitybilling.controller;

import com.utilitybilling.dto.meter.MeterRequest;
import com.utilitybilling.dto.meter.MeterResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.CustomerAccessService;
import com.utilitybilling.service.MeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meters")
@RequiredArgsConstructor
@Tag(name = "Meter Management", description = "Utility meter operations")
@SecurityRequirement(name = "Bearer Authentication")
public class MeterController {

    private final MeterService meterService;
    private final CustomerAccessService customerAccessService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Create a new meter")
    public ApiResponse<MeterResponse> createMeter(@Valid @RequestBody MeterRequest request) {
        return ApiResponse.success("Meter created", meterService.createMeter(request));
    }

    @PutMapping("/{id}/assign/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Assign meter to customer")
    public ApiResponse<MeterResponse> assignMeter(
            @PathVariable Long id, @PathVariable Long customerId) {
        return ApiResponse.success("Meter assigned", meterService.assignMeter(id, customerId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Update meter details")
    public ApiResponse<MeterResponse> updateMeter(
            @PathVariable Long id, @Valid @RequestBody MeterRequest request) {
        return ApiResponse.success("Meter updated", meterService.updateMeter(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Activate meter")
    public ApiResponse<Void> activateMeter(@PathVariable Long id) {
        meterService.activateMeter(id);
        return ApiResponse.success("Meter activated", null);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Deactivate meter")
    public ApiResponse<Void> deactivateMeter(@PathVariable Long id) {
        meterService.deactivateMeter(id);
        return ApiResponse.success("Meter deactivated", null);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer views own meters")
    public ApiResponse<List<MeterResponse>> getMyMeters(@AuthenticationPrincipal UserDetails user) {
        Long customerId = customerAccessService.requireOwnCustomerId(user.getUsername());
        return ApiResponse.success(meterService.getCustomerMeters(customerId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get all meters for a customer")
    public ApiResponse<List<MeterResponse>> getCustomerMeters(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long customerId) {
        customerAccessService.assertStaffOrOwnCustomer(user.getUsername(), customerId);
        return ApiResponse.success(meterService.getCustomerMeters(customerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Get meter by ID")
    public ApiResponse<MeterResponse> getMeterById(@PathVariable Long id) {
        return ApiResponse.success(meterService.getMeterById(id));
    }
}
