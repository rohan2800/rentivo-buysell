package com.rentivo.backend.listing;

public enum ListingStatus {
    PENDING, APPROVED, REJECTED, DELETED;

    /** The moderation state machine. DELETED is terminal. */
    public boolean canTransitionTo(ListingStatus next) {
        return switch (this) {
            case PENDING -> next == APPROVED || next == REJECTED || next == DELETED;
            case APPROVED -> next == REJECTED || next == PENDING || next == DELETED;
            case REJECTED -> next == APPROVED || next == PENDING || next == DELETED;
            case DELETED -> false;
        };
    }
}
