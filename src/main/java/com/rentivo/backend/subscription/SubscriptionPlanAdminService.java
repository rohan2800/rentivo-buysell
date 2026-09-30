package com.rentivo.backend.subscription;

import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanRequest;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Plans are never hard-deleted (payments and subscriptions reference them); admins deactivate.
 * Editing a plan only affects future purchases.
 */
@Service
public class SubscriptionPlanAdminService {

    private final SubscriptionPlanRepository plans;

    public SubscriptionPlanAdminService(SubscriptionPlanRepository plans) {
        this.plans = plans;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listAll() {
        return plans.findAllByOrderByPriceAsc().stream().map(SubscriptionService::toResponse).toList();
    }

    @Transactional
    public PlanResponse create(PlanRequest r) {
        requireUniqueName(r.name(), null);
        return SubscriptionService.toResponse(plans.save(apply(new SubscriptionPlan(), r)));
    }

    @Transactional
    public PlanResponse update(Long id, PlanRequest r) {
        SubscriptionPlan plan = find(id);
        requireUniqueName(r.name(), id);
        return SubscriptionService.toResponse(apply(plan, r));
    }

    @Transactional
    public PlanResponse setActive(Long id, boolean active) {
        SubscriptionPlan plan = find(id);
        plan.setActive(active);
        return SubscriptionService.toResponse(plan);
    }

    private SubscriptionPlan find(Long id) {
        return plans.findById(id).orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    private void requireUniqueName(String name, Long selfId) {
        plans.findByNameIgnoreCase(name.trim())
                .filter(p -> !p.getId().equals(selfId))
                .ifPresent(p -> {
                    throw new ConflictException("A plan with this name already exists");
                });
    }

    private SubscriptionPlan apply(SubscriptionPlan p, PlanRequest r) {
        p.setName(r.name().trim());
        p.setPrice(r.price());
        p.setValidityDays(r.validityDays());
        p.setContactLimit(r.contactLimit());
        p.setDescription(r.description());
        if (r.active() != null) {
            p.setActive(r.active());
        }
        return p;
    }
}
