package com.rentivo.backend.favorite;

import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.listing.ListingRepository;
import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.user.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FavoriteServiceTest {

    private static final long USER = 1L;
    private static final long LISTING = 10L;

    @Mock FavoriteRepository favorites;
    @Mock ListingRepository listings;
    @Mock UserRepository users;

    private final FavoriteService service = new FavoriteService(favorites, listings, users,
            Clock.fixed(Instant.parse("2026-09-30T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void addingAnUnapprovedListingFails() {
        when(favorites.existsByUserIdAndListingId(USER, LISTING)).thenReturn(false);
        when(listings.findByIdAndStatus(LISTING, ListingStatus.APPROVED)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.add(USER, LISTING));
        verify(favorites, never()).save(any());
    }

    @Test
    void addingAlreadyFavoritedListingIsANoOp() {
        when(favorites.existsByUserIdAndListingId(USER, LISTING)).thenReturn(true);

        service.add(USER, LISTING);

        verify(listings, never()).findByIdAndStatus(any(), any());
        verify(favorites, never()).save(any());
    }

    @Test
    void addingAnApprovedListingSavesIt() {
        when(favorites.existsByUserIdAndListingId(USER, LISTING)).thenReturn(false);
        Listing listing = mock(Listing.class);
        when(listings.findByIdAndStatus(LISTING, ListingStatus.APPROVED)).thenReturn(Optional.of(listing));
        when(users.getReferenceById(USER)).thenReturn(new com.rentivo.backend.user.User());

        service.add(USER, LISTING);

        verify(favorites).save(any(Favorite.class));
    }

    @Test
    void removingSomethingNotFavoritedIsANoOp() {
        when(favorites.findByUserIdAndListingId(USER, LISTING)).thenReturn(Optional.empty());

        service.remove(USER, LISTING);

        verify(favorites, never()).delete(any());
    }

    @Test
    void removingAnExistingFavoriteDeletesIt() {
        Favorite fav = new Favorite();
        ReflectionTestUtils.setField(fav, "id", 5L);
        when(favorites.findByUserIdAndListingId(USER, LISTING)).thenReturn(Optional.of(fav));

        service.remove(USER, LISTING);

        verify(favorites).delete(fav);
    }
}
