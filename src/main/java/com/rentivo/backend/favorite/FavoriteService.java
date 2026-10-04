package com.rentivo.backend.favorite;

import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.common.web.Paging;
import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.listing.ListingMapper;
import com.rentivo.backend.listing.ListingRepository;
import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.listing.dto.ListingDtos.ListingSummary;
import com.rentivo.backend.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class FavoriteService {

    private final FavoriteRepository favorites;
    private final ListingRepository listings;
    private final UserRepository users;
    private final Clock clock;

    public FavoriteService(FavoriteRepository favorites, ListingRepository listings, UserRepository users,
                           Clock clock) {
        this.favorites = favorites;
        this.listings = listings;
        this.users = users;
        this.clock = clock;
    }

    /** Adding an already-favorited listing is a no-op, not an error. */
    @Transactional
    public void add(Long userId, Long listingId) {
        if (favorites.existsByUserIdAndListingId(userId, listingId)) {
            return;
        }
        Listing listing = listings.findByIdAndStatus(listingId, ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        Favorite fav = new Favorite();
        fav.setUser(users.getReferenceById(userId));
        fav.setListing(listing);
        fav.setCreatedAt(clock.instant());
        favorites.save(fav);
    }

    /** Removing something that isn't favorited is a no-op, not an error — DELETE is idempotent. */
    @Transactional
    public void remove(Long userId, Long listingId) {
        favorites.findByUserIdAndListingId(userId, listingId).ifPresent(favorites::delete);
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long listingId) {
        return favorites.existsByUserIdAndListingId(userId, listingId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ListingSummary> mine(Long userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return PageResponse.of(favorites.findByUserIdOrderByCreatedAtDesc(userId, Paging.of(page, size, sort)),
                fav -> ListingMapper.toSummary(fav.getListing()));
    }
}
