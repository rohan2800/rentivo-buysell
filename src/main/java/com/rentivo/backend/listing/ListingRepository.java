package com.rentivo.backend.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    @Override
    @EntityGraph(attributePaths = {"category"})
    Page<Listing> findAll(Specification<Listing> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<Listing> findByOwnerIdAndStatusNotOrderByCreatedAtDesc(Long ownerId, ListingStatus status);

    Optional<Listing> findByIdAndStatus(Long id, ListingStatus status);

    long countByStatus(ListingStatus status);
}
