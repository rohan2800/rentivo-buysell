package com.rentivo.backend.admin;

import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.listing.ListingAdminService;
import com.rentivo.backend.listing.ListingStatus;
import com.rentivo.backend.listing.dto.ListingDtos.ListingDecisionRequest;
import com.rentivo.backend.listing.dto.ListingDtos.ListingResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/listings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminListingController {

    private final ListingAdminService service;

    public AdminListingController(ListingAdminService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ListingResponse> list(@RequestParam(required = false) ListingStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return service.list(status, page, size);
    }

    @PatchMapping("/{id}/status")
    public ListingResponse decide(@PathVariable Long id, @Valid @RequestBody ListingDecisionRequest request) {
        return service.decide(id, request.status(), request.reason());
    }
}
