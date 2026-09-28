package com.rentivo.backend.subscription;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.exception.PaymentRequiredException;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.listing.ListingRepository;
import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.payment.Payment;
import com.rentivo.backend.payment.PaymentRepository;
import com.rentivo.backend.payment.PaymentStatus;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.ContactResponse;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.PlanResponse;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.SubscriptionResponse;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.SubscriptionStatusResponse;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final UserRepository users;
    private final SubscriptionPlanRepository plans;
    private final UserSubscriptionRepository subs;
    private final ListingRepository listings;
    private final ContactAccessRepository accesses;
    private final PaymentRepository payments;
    private final RentivoProperties props;
    private final TransactionTemplate tx;
    private final Clock clock;

    public SubscriptionService(UserRepository users, SubscriptionPlanRepository plans,
                               UserSubscriptionRepository subs, ListingRepository listings,
                               ContactAccessRepository accesses, PaymentRepository payments,
                               RentivoProperties props, TransactionTemplate tx, Clock clock) {
        this.users = users;
        this.plans = plans;
        this.subs = subs;
        this.listings = listings;
        this.accesses = accesses;
        this.payments = payments;
        this.props = props;
        this.tx = tx;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> activePlans() {
        return plans.findByActiveTrueOrderByPriceAsc().stream().map(SubscriptionService::toResponse).toList();
    }

    public static PlanResponse toResponse(SubscriptionPlan p) {
        return new PlanResponse(p.getId(), p.getName(), p.getPrice(), p.getValidityDays(),
                p.getContactLimit(), p.getDescription(), p.isActive());
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse status(Long userId) {
        Optional<UserSubscription> active = currentSubscription(userId, clock.instant());
        if (active.isPresent()) {
            UserSubscription s = active.get();
            return new SubscriptionStatusResponse(true, s.getPlanName(), s.getStartAt(), s.getEndAt(),
                    s.getContactLimit(), s.getContactsUsed(), remaining(s), "Subscription active");
        }
        return subs.findFirstByUserIdOrderByEndAtDesc(userId)
                .map(s -> new SubscriptionStatusResponse(false, s.getPlanName(), s.getStartAt(), s.getEndAt(),
                        s.getContactLimit(), s.getContactsUsed(), 0, "Subscription expired"))
                .orElseGet(() -> new SubscriptionStatusResponse(false, null, null, null, 0, 0, 0,
                        "No subscription"));
    }

    /**
     * Development shortcut that grants a plan without payment. Disabled unless
     * rentivo.subscriptions.dev-activation-enabled is true; the real flow (payment order plus a
     * verified webhook) replaces it in Phase 2.
     */
    @Transactional
    public SubscriptionResponse activateDev(Long userId, Long planId) {
        if (!props.subscriptions().devActivationEnabled()) {
            throw new NotFoundException("Not found");
        }
        Instant now = clock.instant();
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        SubscriptionPlan plan = plans.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found"));
        if (!plan.isActive()) {
            throw new ConflictException("This plan is not available");
        }
        if (currentSubscription(userId, now).isPresent()) {
            throw new ConflictException("You already have an active subscription");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPlan(plan);
        payment.setAmount(plan.getPrice());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProvider("DEV");
        payment.setProviderPaymentId("dev-" + UUID.randomUUID());
        payment.setCreatedAt(now);
        payments.save(payment);

        UserSubscription s = new UserSubscription();
        s.setUser(user);
        s.setPlan(plan);
        s.setPlanName(plan.getName());
        s.setContactLimit(plan.getContactLimit());
        s.setStartAt(now);
        s.setEndAt(now.plus(plan.getValidityDays(), ChronoUnit.DAYS));
        s.setContactsUsed(0);
        s.setStatus(SubscriptionStatus.ACTIVE);
        subs.save(s);
        return new SubscriptionResponse(s.getId(), s.getPlanName(), s.getStartAt(), s.getEndAt(),
                s.getContactLimit());
    }

    /**
     * Reveals a listing owner's phone number. Each listing costs one contact, once per user.
     * Not annotated @Transactional itself so the duplicate-click race can be handled after the
     * losing transaction has rolled back.
     */
    public ContactResponse unlock(Long userId, Long listingId) {
        try {
            return tx.execute(status -> doUnlock(userId, listingId));
        } catch (DataIntegrityViolationException e) {
            // Two identical requests raced. The loser rolled back (its contact was refunded);
            // the winner has already recorded the unlock, so answer as "already unlocked".
            return tx.execute(status -> alreadyUnlocked(userId, listingId).orElseThrow(() -> e));
        }
    }

    private ContactResponse doUnlock(Long userId, Long listingId) {
        Instant now = clock.instant();
        Listing listing = listings.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        if (listing.getOwner().getId().equals(userId)) {
            throw new BadRequestException("You cannot unlock your own contact");
        }
        if (listing.getStatus() != ListingStatus.APPROVED) {
            throw new NotFoundException("Listing not found");
        }
        // Read what the response needs before the bulk update below clears the persistence context.
        String ownerName = listing.getOwner().getName();
        String ownerPhone = listing.getContactPhone();

        if (accesses.findByUserIdAndListingId(userId, listingId).isPresent()) {
            return alreadyUnlockedResponse(userId, ownerName, ownerPhone, now);
        }

        UserSubscription sub = currentSubscription(userId, now)
                .orElseThrow(() -> new PaymentRequiredException("An active subscription is required"));
        if (subs.consumeContact(sub.getId(), now, SubscriptionStatus.ACTIVE) == 0) {
            throw new PaymentRequiredException(sub.getContactsUsed() >= sub.getContactLimit()
                    ? "You have used all contacts in your plan"
                    : "Your subscription has expired");
        }

        ContactAccess access = new ContactAccess();
        access.setUser(users.getReferenceById(userId));
        access.setListing(listings.getReferenceById(listingId));
        access.setSubscription(subs.getReferenceById(sub.getId()));
        access.setUnlockedAt(now);
        accesses.save(access);

        UserSubscription fresh = subs.findById(sub.getId()).orElseThrow();
        return new ContactResponse(true, ownerName, ownerPhone, fresh.getContactsUsed(), remaining(fresh),
                "Owner contact unlocked");
    }

    private Optional<ContactResponse> alreadyUnlocked(Long userId, Long listingId) {
        return accesses.findByUserIdAndListingId(userId, listingId).map(a -> {
            Listing l = a.getListing();
            return alreadyUnlockedResponse(userId, l.getOwner().getName(), l.getContactPhone(), clock.instant());
        });
    }

    /** Re-opening an unlocked listing is free and does not depend on the plan still being valid. */
    private ContactResponse alreadyUnlockedResponse(Long userId, String ownerName, String ownerPhone, Instant now) {
        Optional<UserSubscription> current = currentSubscription(userId, now);
        int used = current.map(UserSubscription::getContactsUsed).orElse(0);
        int remaining = current.map(SubscriptionService::remaining).orElse(0);
        return new ContactResponse(true, ownerName, ownerPhone, used, remaining, "Already unlocked");
    }

    private Optional<UserSubscription> currentSubscription(Long userId, Instant now) {
        return subs.findFirstByUserIdAndStatusAndEndAtAfterOrderByEndAtDesc(userId, SubscriptionStatus.ACTIVE, now);
    }

    private static int remaining(UserSubscription s) {
        return Math.max(0, s.getContactLimit() - s.getContactsUsed());
    }
}
