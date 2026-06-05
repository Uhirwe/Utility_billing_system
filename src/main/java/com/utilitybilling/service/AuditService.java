package com.utilitybilling.service;

import com.utilitybilling.dto.audit.AuditLogResponse;
import com.utilitybilling.enums.AuditActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void log(String actorEmail, AuditActionType actionType, String entityType, Long entityId, String oldValue, String newValue);
    Page<AuditLogResponse> getAuditLogs(Pageable pageable);
}
