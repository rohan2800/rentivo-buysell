package com.rentivo.backend.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Housekeeping only. Correctness never depends on this job: every query already ignores
 * subscriptions whose end date has passed.
 */
@Component
public class SubscriptionExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryJob.class);

    private final UserSubscriptionRepository subs;
    private final Clock clock;

    public SubscriptionExpiryJob(UserSubscriptionRepository subs, Clock clock) {
        this.subs = subs;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT1M")
    @Transactional
    public void markExpired() {
        int updated = subs.expireDue(clock.instant(), SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED);
        if (updated > 0) {
            log.info("Marked {} subscriptions as expired", updated);
        }
    }
}
