package com.rentivo.backend.listing;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.common.web.Paging;
import com.rentivo.backend.listing.dto.ListingDtos.ListingResponse;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Moderation: admins approve or reject; everything else about a listing belongs to its owner. */
@Service
public class ListingAdminService {

    private final ListingRepository listings;
    private final Clock clock;

    public ListingAdminService(ListingRepository listings, Clock clock) {
        this.listings = listings;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> list(ListingStatus status, int page, int size) {
        Specification<Listing> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            spec = spec.and(ListingSpecs.hasStatus(status));
        }
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        return PageResponse.of(listings.findAll(spec, Paging.of(page, size, sort)), ListingMapper::toResponse);
    }

    @Transactional
    public ListingResponse decide(Long listingId, ListingStatus target, String reason) {
        if (target != ListingStatus.APPROVED && target != ListingStatus.REJECTED) {
            throw new BadRequestException("Admins can only approve or reject a listing");
        }
        if (target == ListingStatus.REJECTED && (reason == null || reason.isBlank())) {
            throw new BadRequestException("A reason is required when rejecting a listing");
        }
        Listing l = listings.findById(listingId).orElseThrow(() -> new NotFoundException("Listing not found"));
        if (!l.getStatus().canTransitionTo(target)) {
            throw new ConflictException("Cannot change a " + l.getStatus() + " listing to " + target);
        }
        l.setStatus(target);
        l.setRejectionReason(target == ListingStatus.REJECTED ? reason.trim() : null);
        l.setUpdatedAt(clock.instant());
        return ListingMapper.toResponse(l);
    }
}
