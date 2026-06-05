package com.utilitybilling.service.impl;

import com.utilitybilling.dto.audit.AuditLogResponse;
import com.utilitybilling.entity.AuditLog;
import com.utilitybilling.enums.AuditActionType;
import com.utilitybilling.repository.AuditLogRepository;
import com.utilitybilling.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(String actorEmail, AuditActionType actionType, String entityType,
                    Long entityId, String oldValue, String newValue) {
        auditLogRepository.save(AuditLog.builder()
                .actorEmail(actorEmail)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .actionTime(LocalDateTime.now())
                .build());
    }

    @Override
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(log -> AuditLogResponse.builder()
                .id(log.getId())
                .actorEmail(log.getActorEmail())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .actionTime(log.getActionTime())
                .build());
    }
}
