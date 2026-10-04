package com.rentivo.backend.favorite;

import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.listing.dto.ListingDtos.ListingSummary;
import com.rentivo.backend.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ListingSummary> mine(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @AuthenticationPrincipal AuthUser user) {
        return service.mine(user.id(), page, size);
    }

    public record FavoriteStatus(boolean favorited) {
    }

    @GetMapping("/{listingId}")
    public FavoriteStatus status(@PathVariable Long listingId, @AuthenticationPrincipal AuthUser user) {
        return new FavoriteStatus(service.isFavorited(user.id(), listingId));
    }

    @PostMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@PathVariable Long listingId, @AuthenticationPrincipal AuthUser user) {
        service.add(user.id(), listingId);
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long listingId, @AuthenticationPrincipal AuthUser user) {
        service.remove(user.id(), listingId);
    }
}
