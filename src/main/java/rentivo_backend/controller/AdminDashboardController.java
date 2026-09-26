package rentivo_backend.controller;

import org.springframework.web.bind.annotation.*;
import rentivo_backend.repository.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
public class AdminDashboardController {
    private final UserRepository users;
    private final ListingRepository listings;
    private final SubscriptionPlanRepository plans;
    public AdminDashboardController(UserRepository users, ListingRepository listings, SubscriptionPlanRepository plans) {
        this.users = users; this.listings = listings; this.plans = plans;
    }

    @GetMapping
    public Map<String, Object> dashboard() {
        return Map.of(
                "totalUsers", users.count(),
                "totalListings", listings.count(),
                "totalSubscriptionPlans", plans.count(),
                "businessModel", "POST_FREE_CONTACT_PAID"
        );
    }
}
