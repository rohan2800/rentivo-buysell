package com.rentivo.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    Optional<OtpChallenge> findTopByPhoneAndUsedFalseOrderByIdDesc(String phone);

    Optional<OtpChallenge> findTopByPhoneOrderByIdDesc(String phone);

    long countByPhoneAndCreatedAtAfter(String phone, Instant since);

    @Modifying
    @Query("update OtpChallenge c set c.used = true where c.phone = :phone and c.used = false")
    int invalidateOpen(@Param("phone") String phone);

    @Modifying
    @Query("delete from OtpChallenge c where c.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
