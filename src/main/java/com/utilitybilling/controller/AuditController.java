package com.utilitybilling.controller;

import com.utilitybilling.dto.audit.AuditLogResponse;
import com.utilitybilling.payload.ApiResponse;
import com.utilitybilling.service.AuditService;
import com.utilitybilling.util.PageableHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Trail", description = "Critical action audit logs")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View audit trail (paginated)",
            description = "Page starts at 1. Returns 50 audit logs per page, newest first. Results are in data.content.")
    public ApiResponse<Page<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Page number (1 = first page)", example = "1")
            @RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(auditService.getAuditLogs(
                PageableHelper.ofPage(page, PageableHelper.AUDIT_SIZE, "actionTime", Sort.Direction.DESC)));
    }
}
