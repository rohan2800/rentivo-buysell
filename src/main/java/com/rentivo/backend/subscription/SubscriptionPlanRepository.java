package com.rentivo.backend.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();

    List<SubscriptionPlan> findAllByOrderByPriceAsc();

    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);
}
