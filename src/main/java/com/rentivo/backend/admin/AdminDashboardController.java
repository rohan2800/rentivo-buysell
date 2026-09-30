package com.rentivo.backend.admin;

import com.rentivo.backend.listing.ListingRepository;
import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.payment.PaymentRepository;
import com.rentivo.backend.payment.PaymentStatus;
import com.rentivo.backend.subscription.SubscriptionPlanRepository;
import com.rentivo.backend.subscription.SubscriptionStatus;
import com.rentivo.backend.subscription.UserSubscriptionRepository;
import com.rentivo.backend.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Clock;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    public record Dashboard(long totalUsers, long activeUsers, long totalListings, long pendingListings,
                            long approvedListings, long rejectedListings, long subscriptionPlans,
                            long activeSubscriptions, long successfulPayments, BigDecimal revenue) {
    }

    private final UserRepository users;
    private final ListingRepository listings;
    private final SubscriptionPlanRepository plans;
    private final UserSubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final Clock clock;

    public AdminDashboardController(UserRepository users, ListingRepository listings,
                                    SubscriptionPlanRepository plans, UserSubscriptionRepository subscriptions,
                                    PaymentRepository payments, Clock clock) {
        this.users = users;
        this.listings = listings;
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.clock = clock;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Dashboard dashboard() {
        return new Dashboard(
                users.count(),
                users.countByActiveTrue(),
                listings.count(),
                listings.countByStatus(ListingStatus.PENDING),
                listings.countByStatus(ListingStatus.APPROVED),
                listings.countByStatus(ListingStatus.REJECTED),
                plans.count(),
                subscriptions.countByStatusAndEndAtAfter(SubscriptionStatus.ACTIVE, clock.instant()),
                payments.countByStatus(PaymentStatus.SUCCESS),
                payments.sumAmountByStatus(PaymentStatus.SUCCESS));
    }
}
