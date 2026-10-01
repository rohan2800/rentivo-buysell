package com.rentivo.backend.listing;

import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.listing.dto.ListingDtos.CreateListingRequest;
import com.rentivo.backend.listing.dto.ListingDtos.ImageResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingFilter;
import com.rentivo.backend.listing.dto.ListingDtos.ListingResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingSummary;
import com.rentivo.backend.listing.dto.ListingDtos.PublicListingResponse;
import com.rentivo.backend.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService service;

    public ListingController(ListingService service) {
        this.service = service;
    }

    // ---- public ----

    @GetMapping("/approved")
    public PageResponse<ListingSummary> browse(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ListingType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(new ListingFilter(categoryId, city, type, minPrice, maxPrice, q), page, size);
    }

    @GetMapping("/{id}")
    public PublicListingResponse detail(@PathVariable Long id) {
        return service.publicDetail(id);
    }

    // ---- authenticated owner ----

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponse create(@Valid @RequestBody CreateListingRequest request,
                                  @AuthenticationPrincipal AuthUser user) {
        return service.create(user.id(), request);
    }

    @GetMapping("/mine")
    public List<ListingResponse> mine(@AuthenticationPrincipal AuthUser user) {
        return service.mine(user.id());
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable Long id, @Valid @RequestBody CreateListingRequest request,
                                  @AuthenticationPrincipal AuthUser user) {
        return service.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        service.delete(user.id(), id);
    }

    @PostMapping("/{id}/renew")
    public ListingResponse renew(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return service.renew(user.id(), id);
    }

    @PostMapping("/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse addImage(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                  @AuthenticationPrincipal AuthUser user) {
        return service.addImage(user.id(), id, file);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(@PathVariable Long id, @PathVariable Long imageId,
                            @AuthenticationPrincipal AuthUser user) {
        service.removeImage(user.id(), id, imageId);
    }
}
