package com.rentivo.backend.notification.dto;

import com.rentivo.backend.notification.NotificationType;

import java.time.Instant;

public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationResponse(Long id, NotificationType type, String title, String body,
                                       Long listingId, boolean read, Instant createdAt) {
    }

    public record UnreadCountResponse(long count) {
    }
}
