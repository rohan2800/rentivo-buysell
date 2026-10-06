package com.rentivo.backend.admin;

import com.rentivo.backend.audit.AdminAuditService;
import com.rentivo.backend.security.AuthUser;
import com.rentivo.backend.subscription.SubscriptionPlanAdminService;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanRequest;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subscriptions/plans")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanController {

    private final SubscriptionPlanAdminService service;
    private final AdminAuditService audit;

    public AdminPlanController(SubscriptionPlanAdminService service, AdminAuditService audit) {
        this.service = service;
        this.audit = audit;
    }

    @GetMapping
    public List<PlanResponse> list() {
        return service.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse create(@Valid @RequestBody PlanRequest request, @AuthenticationPrincipal AuthUser admin) {
        PlanResponse result = service.create(request);
        audit.record(admin, "PLAN_CREATED", "PLAN", result.id(), result.name());
        return result;
    }

    @PutMapping("/{id}")
    public PlanResponse update(@PathVariable Long id, @Valid @RequestBody PlanRequest request,
                               @AuthenticationPrincipal AuthUser admin) {
        PlanResponse result = service.update(id, request);
        audit.record(admin, "PLAN_UPDATED", "PLAN", id, result.name());
        return result;
    }

    @PatchMapping("/{id}/active")
    public PlanResponse setActive(@PathVariable Long id, @RequestParam boolean value,
                                  @AuthenticationPrincipal AuthUser admin) {
        PlanResponse result = service.setActive(id, value);
        audit.record(admin, value ? "PLAN_ACTIVATED" : "PLAN_DEACTIVATED", "PLAN", id, result.name());
        return result;
    }
}
