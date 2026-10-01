package com.rentivo.backend.listing;

import com.rentivo.backend.category.Category;
import com.rentivo.backend.category.CategoryField;
import com.rentivo.backend.category.CategoryRepository;
import com.rentivo.backend.common.PhoneNumbers;
import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.ForbiddenException;
import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.common.web.Paging;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.listing.dto.ListingDtos.CreateListingRequest;
import com.rentivo.backend.listing.dto.ListingDtos.ImageResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingFilter;
import com.rentivo.backend.listing.dto.ListingDtos.ListingResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingSummary;
import com.rentivo.backend.listing.dto.ListingDtos.PublicListingResponse;
import com.rentivo.backend.media.StorageService;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ListingService {

    private static final String IMAGE_FOLDER = "listings";

    private final ListingRepository listings;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final StorageService storage;
    private final RentivoProperties props;
    private final Clock clock;

    public ListingService(ListingRepository listings, UserRepository users, CategoryRepository categories,
                          StorageService storage, RentivoProperties props, Clock clock) {
        this.listings = listings;
        this.users = users;
        this.categories = categories;
        this.storage = storage;
        this.props = props;
        this.clock = clock;
    }

    // ---- owner operations ----

    @Transactional
    public ListingResponse create(Long userId, CreateListingRequest r) {
        User owner = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (!owner.isActive() || !owner.isPhoneVerified()) {
            throw new ForbiddenException("A verified, active account is required to post");
        }
        requireOwnNumber(owner, r.contactPhone());

        Listing l = new Listing();
        l.setOwner(owner);
        l.setStatus(ListingStatus.PENDING);
        l.setCreatedAt(clock.instant());
        apply(l, r, activeCategory(r.categoryId()));
        return ListingMapper.toResponse(listings.save(l));
    }

    @Transactional
    public ListingResponse update(Long userId, Long listingId, CreateListingRequest r) {
        Listing l = owned(userId, listingId);
        requireOwnNumber(l.getOwner(), r.contactPhone());

        apply(l, r, activeCategory(r.categoryId()));
        // Any edit sends the listing back through moderation.
        l.setStatus(ListingStatus.PENDING);
        l.setRejectionReason(null);
        l.setUpdatedAt(clock.instant());
        return ListingMapper.toResponse(l);
    }

    /** Extends an approved listing's expiry, or brings an expired one back live. */
    @Transactional
    public ListingResponse renew(Long userId, Long listingId) {
        Listing l = owned(userId, listingId);
        if (l.getStatus() != ListingStatus.APPROVED && l.getStatus() != ListingStatus.EXPIRED) {
            throw new ConflictException("Only approved or expired listings can be renewed");
        }
        l.setStatus(ListingStatus.APPROVED);
        l.setExpiresAt(clock.instant().plus(props.listings().validityDays(), ChronoUnit.DAYS));
        l.setUpdatedAt(clock.instant());
        return ListingMapper.toResponse(l);
    }

    @Transactional
    public void delete(Long userId, Long listingId) {
        Listing l = owned(userId, listingId);
        l.setStatus(ListingStatus.DELETED);
        l.setUpdatedAt(clock.instant());
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> mine(Long userId) {
        return listings.findByOwnerIdAndStatusNotOrderByCreatedAtDesc(userId, ListingStatus.DELETED).stream()
                .map(ListingMapper::toResponse).toList();
    }

    @Transactional
    public ImageResponse addImage(Long userId, Long listingId, MultipartFile file) {
        Listing l = owned(userId, listingId);
        int max = props.upload().maxImagesPerListing();
        if (l.getImages().size() >= max) {
            throw new ConflictException("A listing can have at most " + max + " images");
        }
        String url = storage.storeImage(file, IMAGE_FOLDER);
        try {
            ListingImage image = new ListingImage();
            image.setListing(l);
            image.setFileName(safeName(file.getOriginalFilename()));
            image.setFileUrl(url);
            image.setSortOrder(l.getImages().size());
            image.setUploadedAt(clock.instant());
            l.getImages().add(image);
            if (l.getStatus() == ListingStatus.APPROVED) {
                // New content on a live listing has not been reviewed.
                l.setStatus(ListingStatus.PENDING);
                l.setUpdatedAt(clock.instant());
            }
            listings.saveAndFlush(l);
            return new ImageResponse(image.getId(), image.getFileUrl(), image.getSortOrder());
        } catch (RuntimeException e) {
            storage.delete(url);
            throw e;
        }
    }

    @Transactional
    public void removeImage(Long userId, Long listingId, Long imageId) {
        Listing l = owned(userId, listingId);
        ListingImage image = l.getImages().stream()
                .filter(i -> i.getId().equals(imageId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Image not found"));
        String url = image.getFileUrl();
        l.getImages().remove(image);
        // Delete the file only once the database change is durable.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storage.delete(url);
            }
        });
    }

    // ---- public browsing ----

    @Transactional(readOnly = true)
    public PageResponse<ListingSummary> search(ListingFilter f, int page, int size) {
        Specification<Listing> spec = ListingSpecs.hasStatus(ListingStatus.APPROVED);
        if (f.categoryId() != null) {
            spec = spec.and(ListingSpecs.inCategory(f.categoryId()));
        }
        if (StringUtils.hasText(f.city())) {
            spec = spec.and(ListingSpecs.inCity(f.city()));
        }
        if (f.type() != null) {
            spec = spec.and(ListingSpecs.ofType(f.type()));
        }
        if (f.minPrice() != null) {
            spec = spec.and(ListingSpecs.priceAtLeast(f.minPrice()));
        }
        if (f.maxPrice() != null) {
            spec = spec.and(ListingSpecs.priceAtMost(f.maxPrice()));
        }
        if (StringUtils.hasText(f.q())) {
            spec = spec.and(ListingSpecs.titleContains(f.q()));
        }
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        return PageResponse.of(listings.findAll(spec, Paging.of(page, size, sort)), ListingMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public PublicListingResponse publicDetail(Long id) {
        Listing l = listings.findByIdAndStatus(id, ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        return ListingMapper.toPublic(l);
    }

    // ---- helpers ----

    /** Missing, deleted and someone else's listings all look the same to the caller. */
    private Listing owned(Long userId, Long listingId) {
        Listing l = listings.findById(listingId).orElseThrow(() -> new NotFoundException("Listing not found"));
        if (!l.getOwner().getId().equals(userId) || l.getStatus() == ListingStatus.DELETED) {
            throw new NotFoundException("Listing not found");
        }
        return l;
    }

    private Category activeCategory(Long id) {
        Category c = categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        if (!c.isActive()) {
            throw new ConflictException("Category is inactive");
        }
        return c;
    }

    private void requireOwnNumber(User owner, String contactPhone) {
        if (!owner.getPhone().equals(PhoneNumbers.normalize(contactPhone))) {
            throw new BadRequestException("Contact number must match your verified login mobile number");
        }
    }

    private void apply(Listing l, CreateListingRequest r, Category category) {
        Map<CategoryField, String> values = FieldValueValidator.validate(category, r.fields());

        l.setTitle(r.title().trim());
        l.setCategory(category);
        l.setListingType(r.listingType());
        l.setPrice(r.price());
        l.setPriceUnit(r.priceUnit());
        l.setDescription(r.description().trim());
        l.setState(r.state().trim());
        l.setCity(r.city().trim());
        l.setLocality(r.locality().trim());
        l.setPincode(blankToNull(r.pincode()));
        l.setAddress(blankToNull(r.address()));
        l.setLatitude(r.latitude());
        l.setLongitude(r.longitude());
        l.setContactPhone(PhoneNumbers.normalize(r.contactPhone()));
        syncFieldValues(l, values);
    }

    /**
     * Updates values in place instead of clear-and-reinsert: Hibernate runs inserts before
     * orphan deletes, which would trip the (listing_id, field_id) unique constraint.
     */
    private void syncFieldValues(Listing l, Map<CategoryField, String> values) {
        Map<Long, ListingFieldValue> existing = new HashMap<>();
        for (ListingFieldValue v : l.getFieldValues()) {
            existing.put(v.getField().getId(), v);
        }
        Set<Long> keep = new HashSet<>();
        values.forEach((field, value) -> {
            keep.add(field.getId());
            ListingFieldValue current = existing.get(field.getId());
            if (current == null) {
                ListingFieldValue fresh = new ListingFieldValue();
                fresh.setListing(l);
                fresh.setField(field);
                fresh.setValue(value);
                l.getFieldValues().add(fresh);
            } else {
                current.setValue(value);
            }
        });
        l.getFieldValues().removeIf(v -> !keep.contains(v.getField().getId()));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String safeName(String original) {
        String name = original == null ? "image" : StringUtils.getFilename(StringUtils.cleanPath(original));
        if (name == null || name.isBlank()) {
            name = "image";
        }
        return name.length() > 255 ? name.substring(0, 255) : name;
    }
}
