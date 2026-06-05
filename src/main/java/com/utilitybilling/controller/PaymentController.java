package com.utilitybilling.controller;

import com.utilitybilling.dto.payment.PaymentRequest;
import com.utilitybilling.dto.payment.PaymentResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.CustomerAccessService;
import com.utilitybilling.service.PaymentService;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Bill payment recording and history")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;
    private final CustomerAccessService customerAccessService;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance records a payment against an approved bill")
    public ApiResponse<PaymentResponse> recordPayment(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success("Payment recorded", paymentService.recordPayment(request));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "View payment history for a bill (paginated)",
            description = "Page starts at 1. Returns 20 payments per page, newest first. Results are in data.content.")
    public ApiResponse<Page<PaymentResponse>> getPaymentHistory(
            @PathVariable Long billId,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(paymentService.getPaymentHistory(billId,
                PageableHelper.ofPage(page, "paymentDate", Sort.Direction.DESC)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer views own payment history (paginated)",
            description = "Page starts at 1. Returns 20 payments per page, newest first. Results are in data.content.")
    public ApiResponse<Page<PaymentResponse>> getMyPaymentHistory(
            @AuthenticationPrincipal UserDetails user,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        Long customerId = customerAccessService.requireOwnCustomerId(user.getUsername());
        return ApiResponse.success(paymentService.getCustomerPaymentHistory(customerId,
                PageableHelper.ofPage(page, "paymentDate", Sort.Direction.DESC)));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR', 'CUSTOMER')")
    @Operation(summary = "View payment history for a customer (paginated)",
            description = "Page starts at 1. Returns 20 payments per page, newest first. Results are in data.content.")
    public ApiResponse<Page<PaymentResponse>> getCustomerPaymentHistory(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long customerId,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        customerAccessService.assertStaffOrOwnCustomer(user.getUsername(), customerId);
        return ApiResponse.success(paymentService.getCustomerPaymentHistory(customerId,
                PageableHelper.ofPage(page, "paymentDate", Sort.Direction.DESC)));
    }
}
