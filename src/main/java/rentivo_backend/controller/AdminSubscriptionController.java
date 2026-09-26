package rentivo_backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rentivo_backend.dto.SubscriptionPlanRequest;
import rentivo_backend.entity.SubscriptionPlan;
import rentivo_backend.repository.SubscriptionPlanRepository;

@RestController
@RequestMapping("/api/admin/subscriptions")
@CrossOrigin
public class AdminSubscriptionController {
    private final SubscriptionPlanRepository repository;
    public AdminSubscriptionController(SubscriptionPlanRepository repository) { this.repository = repository; }

    @GetMapping("/plans")
    public Object allPlans() { return repository.findAll(); }

    @PostMapping("/plans")
    public SubscriptionPlan create(@Valid @RequestBody SubscriptionPlanRequest req) {
        return repository.save(toEntity(new SubscriptionPlan(), req));
    }

    @PutMapping("/plans/{id}")
    public SubscriptionPlan update(@PathVariable Long id, @Valid @RequestBody SubscriptionPlanRequest req) {
        SubscriptionPlan plan = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        return repository.save(toEntity(plan, req));
    }

    @PatchMapping("/plans/{id}/active")
    public SubscriptionPlan active(@PathVariable Long id, @RequestParam boolean value) {
        SubscriptionPlan plan = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        plan.setActive(value); return repository.save(plan);
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id); return ResponseEntity.noContent().build();
    }

    private SubscriptionPlan toEntity(SubscriptionPlan p, SubscriptionPlanRequest r) {
        p.setName(r.name()); p.setPrice(r.price()); p.setValidityDays(r.validityDays()); p.setContactLimit(r.contactLimit());
        p.setDescription(r.description()); if (r.active() != null) p.setActive(r.active());
        return p;
    }
}
