package com.rentivo.backend.listing;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.notification.NotificationService;
import com.rentivo.backend.notification.NotificationType;
import com.rentivo.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListingAdminServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-30T10:00:00Z");
    private static final long LISTING = 1L;

    @Mock ListingRepository listings;
    @Mock NotificationService notifications;

    private final ListingAdminService service = new ListingAdminService(listings, notifications,
            new RentivoProperties(null, null, null, null, null, new RentivoProperties.Listings(30), null),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private Listing listing(ListingStatus status) {
        Listing l = new Listing();
        ReflectionTestUtils.setField(l, "id", LISTING);
        l.setTitle("A nice flat");
        l.setStatus(status);
        l.setOwner(new User());
        return l;
    }

    @BeforeEach
    void setUp() {
        when(listings.findById(LISTING)).thenReturn(Optional.of(listing(ListingStatus.PENDING)));
    }

    @Test
    void approvingSetsExpiryThirtyDaysOutAndNotifiesTheOwner() {
        Listing l = listing(ListingStatus.PENDING);
        when(listings.findById(LISTING)).thenReturn(Optional.of(l));

        service.decide(LISTING, ListingStatus.APPROVED, null);

        assertEquals(NOW.plusSeconds(30L * 24 * 3600), l.getExpiresAt());
        verify(notifications).create(any(User.class), eq(NotificationType.LISTING_APPROVED), any(), any(), any());
    }

    @Test
    void rejectingRequiresAReason() {
        assertThrows(BadRequestException.class, () -> service.decide(LISTING, ListingStatus.REJECTED, "  "));
    }

    @Test
    void rejectingWithAReasonNotifiesTheOwner() {
        service.decide(LISTING, ListingStatus.REJECTED, "Photos are too blurry");

        verify(notifications).create(any(User.class), eq(NotificationType.LISTING_REJECTED), any(), any(), any());
    }

    @Test
    void adminCannotSetAnyOtherStatus() {
        assertThrows(BadRequestException.class, () -> service.decide(LISTING, ListingStatus.DELETED, null));
    }

    @Test
    void cannotApproveAnAlreadyDeletedListing() {
        when(listings.findById(LISTING)).thenReturn(Optional.of(listing(ListingStatus.DELETED)));
        assertThrows(ConflictException.class, () -> service.decide(LISTING, ListingStatus.APPROVED, null));
    }
}
