package com.rentivo.backend.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class SubscriptionDtos {

    private SubscriptionDtos() {
    }

    public record PlanRequest(@NotBlank @Size(max = 100) String name,
                              @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
                              @NotNull @Min(1) @Max(3650) Integer validityDays,
                              @NotNull @Min(1) @Max(100000) Integer contactLimit,
                              @Size(max = 1000) String description,
                              Boolean active) {
    }

    public record PlanResponse(Long id, String name, BigDecimal price, Integer validityDays,
                               Integer contactLimit, String description, boolean active) {
    }

    public record SubscriptionResponse(Long id, String planName, Instant startAt, Instant endAt,
                                       int contactLimit) {
    }

    public record SubscriptionStatusResponse(boolean active, String planName, Instant startAt, Instant endAt,
                                             int contactLimit, int contactsUsed, int contactsRemaining,
                                             String message) {
    }

    public record ContactResponse(boolean unlocked, String ownerName, String ownerPhone, int contactsUsed,
                                  int contactsRemaining, String message) {
    }
}
