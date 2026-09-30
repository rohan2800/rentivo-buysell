package com.rentivo.backend.listing;

import org.junit.jupiter.api.Test;

import static com.rentivo.backend.listing.ListingStatus.APPROVED;
import static com.rentivo.backend.listing.ListingStatus.DELETED;
import static com.rentivo.backend.listing.ListingStatus.PENDING;
import static com.rentivo.backend.listing.ListingStatus.REJECTED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingStatusTest {

    @Test
    void pendingCanBeModerated() {
        assertTrue(PENDING.canTransitionTo(APPROVED));
        assertTrue(PENDING.canTransitionTo(REJECTED));
    }

    @Test
    void sameStateIsNotATransition() {
        assertFalse(APPROVED.canTransitionTo(APPROVED));
        assertFalse(REJECTED.canTransitionTo(REJECTED));
    }

    @Test
    void deletedIsTerminal() {
        for (ListingStatus next : ListingStatus.values()) {
            assertFalse(DELETED.canTransitionTo(next));
        }
    }

    @Test
    void rejectedCanBeApprovedOnAppeal() {
        assertTrue(REJECTED.canTransitionTo(APPROVED));
    }
}
