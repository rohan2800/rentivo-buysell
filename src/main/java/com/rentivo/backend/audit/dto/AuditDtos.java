package com.rentivo.backend.audit.dto;

import java.time.Instant;

public final class AuditDtos {

    private AuditDtos() {
    }

    public record AuditLogResponse(Long id, Long adminId, String adminPhone, String action,
                                   String targetType, Long targetId, String detail, Instant createdAt) {
    }
}
