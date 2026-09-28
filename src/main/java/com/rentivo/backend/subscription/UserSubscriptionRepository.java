package com.rentivo.backend.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findFirstByUserIdAndStatusAndEndAtAfterOrderByEndAtDesc(
            Long userId, SubscriptionStatus status, Instant now);

    Optional<UserSubscription> findFirstByUserIdOrderByEndAtDesc(Long userId);

    long countByStatusAndEndAtAfter(SubscriptionStatus status, Instant now);

    /**
     * Atomically spends one contact. The WHERE clause is the guard: it only matches while the
     * subscription is active, unexpired and under its limit, so concurrent requests can never
     * overspend. Returns 0 when nothing was updated.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserSubscription s set s.contactsUsed = s.contactsUsed + 1 "
            + "where s.id = :id and s.status = :active and s.endAt > :now and s.contactsUsed < s.contactLimit")
    int consumeContact(@Param("id") Long id, @Param("now") Instant now,
                       @Param("active") SubscriptionStatus active);

    @Modifying
    @Query("update UserSubscription s set s.status = :expired where s.status = :active and s.endAt <= :now")
    int expireDue(@Param("now") Instant now, @Param("active") SubscriptionStatus active,
                  @Param("expired") SubscriptionStatus expired);
}
