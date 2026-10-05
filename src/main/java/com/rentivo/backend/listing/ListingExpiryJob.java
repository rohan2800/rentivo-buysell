package com.rentivo.backend.listing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Moves approved listings past their expires_at to EXPIRED. Owners can bring one back via renew. */
@Component
public class ListingExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(ListingExpiryJob.class);

    private final ListingRepository listings;
    private final Clock clock;

    public ListingExpiryJob(ListingRepository listings, Clock clock) {
        this.listings = listings;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT3M")
    @Transactional
    public void markExpired() {
        int updated = listings.expireDue(clock.instant(), ListingStatus.APPROVED, ListingStatus.EXPIRED);
        if (updated > 0) {
            log.info("Expired {} listings", updated);
        }
    }
}
