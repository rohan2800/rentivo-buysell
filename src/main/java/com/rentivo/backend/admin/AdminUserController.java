package com.rentivo.backend.admin;

import com.rentivo.backend.audit.AdminAuditService;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.security.AuthUser;
import com.rentivo.backend.user.UserAdminService;
import com.rentivo.backend.user.UserAdminService.UserView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserAdminService service;
    private final AdminAuditService audit;

    public AdminUserController(UserAdminService service, AdminAuditService audit) {
        this.service = service;
        this.audit = audit;
    }

    @GetMapping
    public PageResponse<UserView> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @PatchMapping("/{id}/active")
    public UserView setActive(@PathVariable Long id, @RequestParam boolean value,
                              @AuthenticationPrincipal AuthUser admin) {
        UserView result = service.setActive(admin.id(), id, value);
        audit.record(admin, value ? "USER_UNBLOCKED" : "USER_BLOCKED", "USER", id, null);
        return result;
    }
}
