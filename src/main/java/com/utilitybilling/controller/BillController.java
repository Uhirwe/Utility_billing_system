package com.utilitybilling.controller;

import com.utilitybilling.dto.bill.BillRequest;
import com.utilitybilling.dto.bill.BillResponse;
import com.utilitybilling.dto.bill.GenerateMonthlyBillsRequest;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.BillService;
import com.utilitybilling.service.CustomerAccessService;
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
@RequestMapping("/bills")
@RequiredArgsConstructor
@Tag(name = "Billing Management", description = "Utility bill generation and management")
@SecurityRequirement(name = "Bearer Authentication")
public class BillController {

    private final BillService billService;
    private final CustomerAccessService customerAccessService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin generates a bill for a specific meter and period")
    public ApiResponse<BillResponse> generateBill(
            @AuthenticationPrincipal UserDetails actor,
            @Valid @RequestBody BillRequest request) {
        return ApiResponse.success("Bill generated", billService.generateBill(request, actor.getUsername()));
    }

    @PostMapping("/generate-monthly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate monthly bills for all active postpaid meters (stored procedure)")
    public ApiResponse<Void> generateMonthlyBills(@Valid @RequestBody GenerateMonthlyBillsRequest request) {
        billService.generateMonthlyBills(request);
        return ApiResponse.success("Monthly bills generation initiated", null);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Approve a generated bill (ADMIN or FINANCE)")
    public ApiResponse<BillResponse> approveBill(
            @AuthenticationPrincipal UserDetails actor,
            @PathVariable Long id) {
        return ApiResponse.success("Bill approved", billService.approveBill(id, actor.getUsername()));
    }

    @PostMapping("/process-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apply late penalties and disconnect meters for bills overdue > 30 days")
    public ApiResponse<Void> processOverdueBills() {
        billService.processOverdueBills();
        return ApiResponse.success("Overdue bill processing completed", null);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "View all bills (paginated)",
            description = "Page starts at 1. Returns 20 bills per page, newest first. Results are in data.content.")
    public ApiResponse<Page<BillResponse>> getBills(
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(billService.getBills(
                PageableHelper.ofPage(page, "generatedDate", Sort.Direction.DESC)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer views own bills (paginated)",
            description = "Page starts at 1. Returns 20 bills per page, newest first. Results are in data.content.")
    public ApiResponse<Page<BillResponse>> getMyBills(
            @AuthenticationPrincipal UserDetails user,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        Long customerId = customerAccessService.requireOwnCustomerId(user.getUsername());
        return ApiResponse.success(billService.getCustomerBills(customerId,
                PageableHelper.ofPage(page, "generatedDate", Sort.Direction.DESC)));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR', 'CUSTOMER')")
    @Operation(summary = "View bills for a customer (paginated)",
            description = "Page starts at 1. Returns 20 bills per page, newest first. Results are in data.content.")
    public ApiResponse<Page<BillResponse>> getCustomerBills(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long customerId,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        customerAccessService.assertStaffOrOwnCustomer(user.getUsername(), customerId);
        return ApiResponse.success(billService.getCustomerBills(customerId,
                PageableHelper.ofPage(page, "generatedDate", Sort.Direction.DESC)));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "View bills for a specific month and year (paginated)",
            description = "Page starts at 1. Use month 1–12. Results are in data.content.")
    public ApiResponse<Page<BillResponse>> getMonthlyBills(
            @Parameter(description = "Month (1–12)", example = "6") @RequestParam int month,
            @Parameter(description = "Year (4 digits)", example = "2026") @RequestParam int year,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(billService.getMonthlyBills(month, year,
                PageableHelper.ofPage(page, "generatedDate", Sort.Direction.DESC)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR', 'CUSTOMER')")
    @Operation(summary = "Get bill by ID")
    public ApiResponse<BillResponse> getBillById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ApiResponse.success(billService.getBillById(id, user.getUsername()));
    }
}
