package com.rentivo.backend.listing.dto;

import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.listing.ListingType;
import com.rentivo.backend.listing.PriceUnit;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ListingDtos {

    private ListingDtos() {
    }

    public record CreateListingRequest(
            @NotBlank @Size(max = 200) String title,
            @NotNull Long categoryId,
            @NotNull ListingType listingType,
            @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal price,
            @NotNull PriceUnit priceUnit,
            @NotBlank @Size(max = 3000) String description,
            @NotBlank @Size(max = 100) String state,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 150) String locality,
            @Size(max = 10) String pincode,
            @Size(max = 500) String address,
            @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @NotBlank @Pattern(regexp = "\\+?[\\d\\s\\-()]{10,20}", message = "must be a valid mobile number")
            String contactPhone,
            Map<Long, String> fields) {
    }

    public record ImageResponse(Long id, String url, int sortOrder) {
    }

    public record FieldValueResponse(Long fieldId, String name, String value) {
    }

    public record CategoryRef(Long id, String name) {
    }

    /** Owner / admin view. Includes the contact number, so it is never returned to other users. */
    public record ListingResponse(Long id, Long ownerId, String title, CategoryRef category,
                                  ListingType listingType, BigDecimal price, PriceUnit priceUnit,
                                  String description, String state, String city, String locality,
                                  String pincode, String address, Double latitude, Double longitude,
                                  String contactPhone, ListingStatus status, String rejectionReason,
                                  List<ImageResponse> images, List<FieldValueResponse> fields,
                                  Instant expiresAt, Instant createdAt, Instant updatedAt) {
    }

    /** Public detail: no contact number and no street address. Contact is unlocked via subscription. */
    public record PublicListingResponse(Long id, String title, CategoryRef category,
                                        ListingType listingType, BigDecimal price, PriceUnit priceUnit,
                                        String description, String state, String city, String locality,
                                        String pincode, Double latitude, Double longitude,
                                        List<ImageResponse> images, List<FieldValueResponse> fields,
                                        boolean contactLocked, Instant expiresAt, Instant createdAt) {
    }

    public record ListingSummary(Long id, String title, String category, ListingType listingType,
                                 BigDecimal price, PriceUnit priceUnit, String city, String locality,
                                 String thumbnailUrl, Instant createdAt) {
    }

    public record ListingFilter(Long categoryId, String city, ListingType type,
                                BigDecimal minPrice, BigDecimal maxPrice, String q) {
    }

    public record ListingDecisionRequest(@NotNull ListingStatus status, @Size(max = 500) String reason) {
    }
}
