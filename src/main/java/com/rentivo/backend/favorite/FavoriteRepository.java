package com.rentivo.backend.favorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    Optional<Favorite> findByUserIdAndListingId(Long userId, Long listingId);

    @EntityGraph(attributePaths = {"listing", "listing.category", "listing.images"})
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByListingId(Long listingId);
}
