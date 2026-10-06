package com.rentivo.backend.listing;

import com.rentivo.backend.category.Category;
import com.rentivo.backend.category.CategoryRepository;
import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.ForbiddenException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.listing.dto.ListingDtos.CreateListingRequest;
import com.rentivo.backend.listing.dto.ListingDtos.ListingResponse;
import com.rentivo.backend.media.StorageService;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListingServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-30T10:00:00Z");
    private static final long USER = 1L;
    private static final long LISTING = 10L;
    private static final long CATEGORY = 100L;

    @Mock ListingRepository listings;
    @Mock UserRepository users;
    @Mock CategoryRepository categories;
    @Mock StorageService storage;

    private final ListingService service = new ListingService(listings, users, categories, storage,
            new RentivoProperties(null, null,
                    new RentivoProperties.Upload("local", "./uploads", 2, 10_485_760L, null, "ap-south-1", null),
                    null, null, new RentivoProperties.Listings(30), null),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private User owner(boolean active, boolean verified) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", USER);
        u.setPhone("9876543210");
        u.setActive(active);
        u.setPhoneVerified(verified);
        return u;
    }

    private Category category() {
        Category c = new Category();
        ReflectionTestUtils.setField(c, "id", CATEGORY);
        c.setName("Furniture");
        c.setActive(true);
        return c;
    }

    private CreateListingRequest request(String contactPhone) {
        return new CreateListingRequest("Sofa", CATEGORY, ListingType.SALE, new BigDecimal("5000"),
                PriceUnit.TOTAL, "A comfy sofa", "MH", "Solapur", "Vijapur Road", null, null, null, null,
                contactPhone, Map.of());
    }

    @BeforeEach
    void setUp() {
        when(users.findById(USER)).thenReturn(Optional.of(owner(true, true)));
        when(categories.findById(CATEGORY)).thenReturn(Optional.of(category()));
    }

    @Test
    void unverifiedOwnerCannotPost() {
        when(users.findById(USER)).thenReturn(Optional.of(owner(true, false)));
        assertThrows(ForbiddenException.class, () -> service.create(USER, request("9876543210")));
    }

    @Test
    void blockedOwnerCannotPost() {
        when(users.findById(USER)).thenReturn(Optional.of(owner(false, true)));
        assertThrows(ForbiddenException.class, () -> service.create(USER, request("9876543210")));
    }

    @Test
    void contactPhoneMustMatchOwnersLoginNumber() {
        assertThrows(BadRequestException.class, () -> service.create(USER, request("9999999999")));
    }

    @Test
    void inactiveCategoryIsRejected() {
        Category inactive = category();
        inactive.setActive(false);
        when(categories.findById(CATEGORY)).thenReturn(Optional.of(inactive));
        assertThrows(ConflictException.class, () -> service.create(USER, request("9876543210")));
    }

    @Test
    void validCreateSucceeds() {
        when(listings.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));
        ListingResponse res = service.create(USER, request("9876543210"));
        assertEquals("Sofa", res.title());
        assertEquals(ListingStatus.PENDING, res.status());
    }

    private Listing listingOwnedBy(long ownerId, ListingStatus status) {
        Listing l = new Listing();
        ReflectionTestUtils.setField(l, "id", LISTING);
        User o = new User();
        ReflectionTestUtils.setField(o, "id", ownerId);
        l.setOwner(o);
        l.setStatus(status);
        return l;
    }

    @Test
    void cannotRenewAPendingListing() {
        when(listings.findById(LISTING)).thenReturn(Optional.of(listingOwnedBy(USER, ListingStatus.PENDING)));
        assertThrows(ConflictException.class, () -> service.renew(USER, LISTING));
    }

    @Test
    void renewingAnExpiredListingBringsItBackToApproved() {
        when(listings.findById(LISTING)).thenReturn(Optional.of(listingOwnedBy(USER, ListingStatus.EXPIRED)));
        ListingResponse res = service.renew(USER, LISTING);
        assertEquals(ListingStatus.APPROVED, res.status());
        assertEquals(NOW.plusSeconds(30L * 24 * 3600), res.expiresAt());
    }

    @Test
    void cannotRenewSomeoneElsesListing() {
        when(listings.findById(LISTING)).thenReturn(Optional.of(listingOwnedBy(99L, ListingStatus.APPROVED)));
        assertThrows(NotFoundException.class, () -> service.renew(USER, LISTING));
    }

    @Test
    void reorderRejectsAMismatchedIdSet() {
        Listing l = listingOwnedBy(USER, ListingStatus.APPROVED);
        ListingImage img = new ListingImage();
        ReflectionTestUtils.setField(img, "id", 1L);
        l.getImages().add(img);
        when(listings.findById(LISTING)).thenReturn(Optional.of(l));

        assertThrows(BadRequestException.class, () -> service.reorderImages(USER, LISTING, List.of(999L)));
    }

    @Test
    void reorderRejectsDuplicateIds() {
        Listing l = listingOwnedBy(USER, ListingStatus.APPROVED);
        ListingImage a = new ListingImage();
        ReflectionTestUtils.setField(a, "id", 1L);
        ListingImage b = new ListingImage();
        ReflectionTestUtils.setField(b, "id", 2L);
        l.getImages().add(a);
        l.getImages().add(b);
        when(listings.findById(LISTING)).thenReturn(Optional.of(l));

        assertThrows(BadRequestException.class, () -> service.reorderImages(USER, LISTING, List.of(1L, 1L)));
    }

    @Test
    void reorderAppliesTheNewOrder() {
        Listing l = listingOwnedBy(USER, ListingStatus.APPROVED);
        ListingImage a = new ListingImage();
        ReflectionTestUtils.setField(a, "id", 1L);
        ListingImage b = new ListingImage();
        ReflectionTestUtils.setField(b, "id", 2L);
        l.getImages().add(a);
        l.getImages().add(b);
        when(listings.findById(LISTING)).thenReturn(Optional.of(l));

        service.reorderImages(USER, LISTING, List.of(2L, 1L));

        assertEquals(0, b.getSortOrder());
        assertEquals(1, a.getSortOrder());
    }
}
