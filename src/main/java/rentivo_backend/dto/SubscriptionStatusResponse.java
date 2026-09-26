package rentivo_backend.dto;

import java.time.LocalDateTime;

public record SubscriptionStatusResponse(
        boolean active,
        String planName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int contactLimit,
        int contactsUsed,
        int contactsRemaining,
        String message
) {}
