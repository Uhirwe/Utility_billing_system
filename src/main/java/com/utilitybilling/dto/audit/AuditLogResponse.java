package com.utilitybilling.dto.audit;

import com.utilitybilling.enums.AuditActionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String actorEmail;
    private AuditActionType actionType;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private LocalDateTime actionTime;
}
