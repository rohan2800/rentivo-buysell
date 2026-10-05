package com.rentivo.backend.notification;

import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.common.web.Paging;
import com.rentivo.backend.listing.Listing;
import com.rentivo.backend.notification.dto.NotificationDtos.NotificationResponse;
import com.rentivo.backend.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Creates and serves in-app notifications. create(...) is called by other services (listing
 * moderation, contact unlock) — there is no public endpoint to create one directly.
 */
@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final Clock clock;

    public NotificationService(NotificationRepository notifications, Clock clock) {
        this.notifications = notifications;
        this.clock = clock;
    }

    @Transactional
    public void create(User recipient, NotificationType type, String title, String body, Listing listing) {
        Notification n = new Notification();
        n.setUser(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setListing(listing);
        n.setCreatedAt(clock.instant());
        notifications.save(n);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> mine(Long userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return PageResponse.of(notifications.findByUserIdOrderByCreatedAtDesc(userId, Paging.of(page, size, sort)),
                NotificationService::toResponse);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notifications.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long id) {
        Notification n = notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        n.setRead(true);
        return toResponse(n);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notifications.markAllRead(userId);
    }

    private static NotificationResponse toResponse(Notification n) {
        Long listingId = n.getListing() == null ? null : n.getListing().getId();
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getBody(), listingId,
                n.isRead(), n.getCreatedAt());
    }
}
