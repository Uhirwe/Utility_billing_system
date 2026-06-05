package com.utilitybilling.controller;

import com.utilitybilling.dto.reading.MeterReadingRequest;
import com.utilitybilling.dto.reading.MeterReadingResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.MeterReadingService;
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

import java.util.List;

@RestController
@RequestMapping("/readings")
@RequiredArgsConstructor
@Tag(name = "Meter Reading Management", description = "Record and view meter readings")
@SecurityRequirement(name = "Bearer Authentication")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Record a new meter reading")
    public ApiResponse<MeterReadingResponse> recordReading(@Valid @RequestBody MeterReadingRequest request) {
        return ApiResponse.success("Reading recorded", meterReadingService.recordReading(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Update an existing meter reading")
    public ApiResponse<MeterReadingResponse> updateReading(
            @PathVariable Long id, @Valid @RequestBody MeterReadingRequest request) {
        return ApiResponse.success("Reading updated", meterReadingService.updateReading(id, request));
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "View reading history for a meter (paginated)",
            description = "Page starts at 1. Returns 20 readings per page, newest first. Results are in data.content.")
    public ApiResponse<Page<MeterReadingResponse>> getReadingHistory(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long meterId,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(meterReadingService.getReadingHistory(meterId, user.getUsername(),
                PageableHelper.ofPage(page, "readingDate", Sort.Direction.DESC)));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "View monthly readings across all meters",
            description = "Use month 1–12 (e.g. 6 for June, not 06). After Execute, read Server response — not Example Value.")
    public ApiResponse<List<MeterReadingResponse>> getMonthlyReadings(
            @Parameter(description = "Month (1–12)", example = "6") @RequestParam int month,
            @Parameter(description = "Year (4 digits)", example = "2026") @RequestParam int year) {
        return ApiResponse.success(meterReadingService.getMonthlyReadings(month, year));
    }
}
