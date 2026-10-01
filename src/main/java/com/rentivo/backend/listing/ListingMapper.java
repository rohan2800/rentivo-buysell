package com.rentivo.backend.listing;

import com.rentivo.backend.listing.dto.ListingDtos.CategoryRef;
import com.rentivo.backend.listing.dto.ListingDtos.FieldValueResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ImageResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingSummary;
import com.rentivo.backend.listing.dto.ListingDtos.PublicListingResponse;

import java.util.List;

/** Entity to DTO conversion. Call inside a transaction: it touches lazy associations. */
public final class ListingMapper {

    private ListingMapper() {
    }

    public static ListingResponse toResponse(Listing l) {
        return new ListingResponse(l.getId(), l.getOwner().getId(), l.getTitle(), category(l),
                l.getListingType(), l.getPrice(), l.getPriceUnit(), l.getDescription(), l.getState(),
                l.getCity(), l.getLocality(), l.getPincode(), l.getAddress(), l.getLatitude(),
                l.getLongitude(), l.getContactPhone(), l.getStatus(), l.getRejectionReason(),
                images(l), fields(l), l.getExpiresAt(), l.getCreatedAt(), l.getUpdatedAt());
    }

    public static PublicListingResponse toPublic(Listing l) {
        return new PublicListingResponse(l.getId(), l.getTitle(), category(l), l.getListingType(),
                l.getPrice(), l.getPriceUnit(), l.getDescription(), l.getState(), l.getCity(),
                l.getLocality(), l.getPincode(), l.getLatitude(), l.getLongitude(),
                images(l), fields(l), true, l.getExpiresAt(), l.getCreatedAt());
    }

    public static ListingSummary toSummary(Listing l) {
        String thumbnail = l.getImages().isEmpty() ? null : l.getImages().get(0).getFileUrl();
        return new ListingSummary(l.getId(), l.getTitle(), l.getCategory().getName(), l.getListingType(),
                l.getPrice(), l.getPriceUnit(), l.getCity(), l.getLocality(), thumbnail, l.getCreatedAt());
    }

    private static CategoryRef category(Listing l) {
        return new CategoryRef(l.getCategory().getId(), l.getCategory().getName());
    }

    private static List<ImageResponse> images(Listing l) {
        return l.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), i.getFileUrl(), i.getSortOrder())).toList();
    }

    private static List<FieldValueResponse> fields(Listing l) {
        return l.getFieldValues().stream()
                .filter(v -> v.getField().isActive())
                .map(v -> new FieldValueResponse(v.getField().getId(), v.getField().getName(), v.getValue()))
                .toList();
    }
}
