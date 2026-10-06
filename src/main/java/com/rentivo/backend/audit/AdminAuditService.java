package com.rentivo.backend.audit;

import com.rentivo.backend.audit.dto.AuditDtos.AuditLogResponse;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.common.web.Paging;
import com.rentivo.backend.security.AuthUser;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Records what an admin did. Called from the admin controllers, right after each action succeeds. */
@Service
public class AdminAuditService {

    private final AdminAuditLogRepository logs;
    private final Clock clock;

    public AdminAuditService(AdminAuditLogRepository logs, Clock clock) {
        this.logs = logs;
        this.clock = clock;
    }

    @Transactional
    public void record(AuthUser admin, String action, String targetType, Long targetId, String detail) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminId(admin.id());
        log.setAdminPhone(admin.phone());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(clock.instant());
        logs.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return PageResponse.of(logs.findAllByOrderByCreatedAtDesc(Paging.of(page, size, sort)),
                l -> new AuditLogResponse(l.getId(), l.getAdminId(), l.getAdminPhone(), l.getAction(),
                        l.getTargetType(), l.getTargetId(), l.getDetail(), l.getCreatedAt()));
    }
}
