package com.utilitybilling.controller;

import com.utilitybilling.dto.tariff.TariffRequest;
import com.utilitybilling.dto.tariff.TariffResponse;
import com.utilitybilling.enums.MeterType;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.TariffService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariff Management", description = "Versioned utility tariff configuration (ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin creates a new tariff version (flat or tier-based)")
    public ApiResponse<TariffResponse> createTariff(@Valid @RequestBody TariffRequest request) {
        return ApiResponse.success("Tariff created", tariffService.createTariff(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin updates tariff details")
    public ApiResponse<TariffResponse> updateTariff(
            @PathVariable Long id, @Valid @RequestBody TariffRequest request) {
        return ApiResponse.success("Tariff updated", tariffService.updateTariff(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "View all tariffs (paginated)",
            description = "Page starts at 1. Returns 20 tariffs per page, newest version first. Results are in data.content.")
    public ApiResponse<Page<TariffResponse>> getTariffs(
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(tariffService.getTariffs(
                PageableHelper.ofPage(page, "versionNumber", Sort.Direction.DESC)));
    }

    @GetMapping("/type/{meterType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "View tariffs by meter type (paginated)",
            description = "Page starts at 1. Returns 20 tariffs per page, newest version first. Results are in data.content.")
    public ApiResponse<Page<TariffResponse>> getTariffsByType(
            @PathVariable MeterType meterType,
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(tariffService.getTariffsByType(meterType,
                PageableHelper.ofPage(page, "versionNumber", Sort.Direction.DESC)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "Get tariff by ID")
    public ApiResponse<TariffResponse> getTariffById(@PathVariable Long id) {
        return ApiResponse.success(tariffService.getTariffById(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate tariff")
    public ApiResponse<Void> activateTariff(@PathVariable Long id) {
        tariffService.activateTariff(id);
        return ApiResponse.success("Tariff activated", null);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate tariff")
    public ApiResponse<Void> deactivateTariff(@PathVariable Long id) {
        tariffService.deactivateTariff(id);
        return ApiResponse.success("Tariff deactivated", null);
    }
}
