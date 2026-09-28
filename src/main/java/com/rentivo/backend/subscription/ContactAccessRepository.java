package com.rentivo.backend.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactAccessRepository extends JpaRepository<ContactAccess, Long> {

    Optional<ContactAccess> findByUserIdAndListingId(Long userId, Long listingId);
}
