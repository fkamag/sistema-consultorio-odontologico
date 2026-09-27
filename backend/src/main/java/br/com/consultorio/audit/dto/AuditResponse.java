package br.com.consultorio.audit.dto;

import br.com.consultorio.audit.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditResponse(
        UUID id,
        UUID userId,
        String userName,
        String action,
        String entity,
        UUID entityId,
        String details,
        String ip,
        LocalDateTime createdAt
) {
    public static AuditResponse from(AuditLog log) {
        return new AuditResponse(
                log.getId(),
                log.getUserId(),
                log.getUserName(),
                log.getAction(),
                log.getEntity(),
                log.getEntityId(),
                log.getDetails(),
                log.getIp(),
                log.getCreatedAt()
        );
    }
}
