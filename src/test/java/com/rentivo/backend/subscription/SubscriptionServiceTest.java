package com.rentivo.backend.subscription;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.exception.PaymentRequiredException;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.listing.ListingRepository;
import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.payment.PaymentRepository;
import com.rentivo.backend.subscription.dto.SubscriptionDtos.ContactResponse;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");
    private static final long BUYER = 1L;
    private static final long OWNER = 2L;
    private static final long LISTING = 10L;
    private static final long SUB = 100L;

    @Mock UserRepository users;
    @Mock SubscriptionPlanRepository plans;
    @Mock UserSubscriptionRepository subs;
    @Mock ListingRepository listings;
    @Mock ContactAccessRepository accesses;
    @Mock PaymentRepository payments;
    @Mock com.rentivo.backend.notification.NotificationService notifications;
    @Mock TransactionTemplate tx;

    private Listing listing;

    @BeforeEach
    void setUp() {
        // Run transaction callbacks inline.
        when(tx.execute(any())).thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", OWNER);
        owner.setName("Seller");
        listing = new Listing();
        ReflectionTestUtils.setField(listing, "id", LISTING);
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.APPROVED);
        listing.setContactPhone("9000000001");
        when(listings.findById(LISTING)).thenReturn(Optional.of(listing));
    }

    private SubscriptionService service(boolean devActivation) {
        RentivoProperties props = new RentivoProperties(null, null, null, null,
                new RentivoProperties.Subscriptions(devActivation), null);
        return new SubscriptionService(users, plans, subs, listings, accesses, payments, notifications, props, tx,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private UserSubscription sub(int limit, int used) {
        UserSubscription s = new UserSubscription();
        ReflectionTestUtils.setField(s, "id", SUB);
        s.setContactLimit(limit);
        s.setContactsUsed(used);
        s.setPlanName("Basic");
        s.setStartAt(NOW.minusSeconds(3600));
        s.setEndAt(NOW.plusSeconds(3600));
        s.setStatus(SubscriptionStatus.ACTIVE);
        return s;
    }

    private void hasActive(UserSubscription s) {
        when(subs.findFirstByUserIdAndStatusAndEndAtAfterOrderByEndAtDesc(
                BUYER, SubscriptionStatus.ACTIVE, NOW)).thenReturn(Optional.of(s));
    }

    @Test
    void ownerCannotUnlockTheirOwnListing() {
        assertThrows(BadRequestException.class, () -> service(false).unlock(OWNER, LISTING));
    }

    @Test
    void unapprovedListingLooksNonExistent() {
        listing.setStatus(ListingStatus.PENDING);
        assertThrows(NotFoundException.class, () -> service(false).unlock(BUYER, LISTING));
    }

    @Test
    void needsAnActiveSubscription() {
        assertThrows(PaymentRequiredException.class, () -> service(false).unlock(BUYER, LISTING));
    }

    @Test
    void refusesWhenContactLimitIsUsedUp() {
        UserSubscription s = sub(2, 2);
        hasActive(s);
        when(subs.consumeContact(SUB, NOW, SubscriptionStatus.ACTIVE)).thenReturn(0);

        PaymentRequiredException e = assertThrows(PaymentRequiredException.class,
                () -> service(false).unlock(BUYER, LISTING));
        assertTrue(e.getMessage().contains("all contacts"));
        verify(accesses, never()).save(any());
    }

    @Test
    void unlockSpendsOneContactAndRevealsPhone() {
        hasActive(sub(5, 0));
        when(subs.consumeContact(SUB, NOW, SubscriptionStatus.ACTIVE)).thenReturn(1);
        when(subs.findById(SUB)).thenReturn(Optional.of(sub(5, 1)));

        ContactResponse res = service(false).unlock(BUYER, LISTING);

        assertTrue(res.unlocked());
        assertEquals("9000000001", res.ownerPhone());
        assertEquals("Seller", res.ownerName());
        assertEquals(1, res.contactsUsed());
        assertEquals(4, res.contactsRemaining());
        verify(accesses).save(any(ContactAccess.class));
    }

    @Test
    void reopeningAnUnlockedListingIsFree() {
        hasActive(sub(5, 1));
        when(accesses.findByUserIdAndListingId(BUYER, LISTING)).thenReturn(Optional.of(new ContactAccess()));

        ContactResponse res = service(false).unlock(BUYER, LISTING);

        assertEquals("Already unlocked", res.message());
        verify(subs, never()).consumeContact(anyLong(), any(), any());
    }

    @Test
    void doubleClickRaceReturnsTheExistingUnlock() {
        hasActive(sub(5, 0));
        when(subs.consumeContact(SUB, NOW, SubscriptionStatus.ACTIVE)).thenReturn(1);
        when(accesses.save(any())).thenThrow(new DataIntegrityViolationException("uk_contact_access_user_listing"));
        ContactAccess winner = new ContactAccess();
        winner.setListing(listing);
        // First lookup (inside the losing transaction): nothing yet. Second (recovery): the winner's row.
        when(accesses.findByUserIdAndListingId(BUYER, LISTING))
                .thenReturn(Optional.empty(), Optional.of(winner));

        ContactResponse res = service(false).unlock(BUYER, LISTING);

        assertEquals("Already unlocked", res.message());
        assertEquals("9000000001", res.ownerPhone());
    }

    @Test
    void devActivationIsHiddenWhenDisabled() {
        assertThrows(NotFoundException.class, () -> service(false).activateDev(BUYER, 1L));
    }
}
