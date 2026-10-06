package com.rentivo.backend.admin;

import com.rentivo.backend.audit.AdminAuditService;
import com.rentivo.backend.audit.dto.AuditDtos.AuditLogResponse;
import com.rentivo.backend.common.web.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final AdminAuditService service;

    public AdminAuditController(AdminAuditService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }
}
