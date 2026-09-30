package com.rentivo.backend.subscription;

import com.rentivo.backend.security.AuthUser;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.ContactResponse;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanResponse;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.SubscriptionResponse;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.SubscriptionStatusResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans() {
        return service.activePlans();
    }

    @GetMapping("/status")
    public SubscriptionStatusResponse status(@AuthenticationPrincipal AuthUser user) {
        return service.status(user.id());
    }

    /** Development only; answers 404 unless explicitly enabled. */
    @PostMapping("/activate-dev")
    public SubscriptionResponse activateDev(@RequestParam Long planId, @AuthenticationPrincipal AuthUser user) {
        return service.activateDev(user.id(), planId);
    }

    @PostMapping("/contact/{listingId}")
    public ContactResponse unlock(@PathVariable Long listingId, @AuthenticationPrincipal AuthUser user) {
        return service.unlock(user.id(), listingId);
    }
}
