package com.rentivo.backend.notification;

import com.rentivo.backend.common.web.PageResponse;
import com.rentivo.backend.notification.dto.NotificationDtos.NotificationResponse;
import com.rentivo.backend.notification.dto.NotificationDtos.UnreadCountResponse;
import com.rentivo.backend.security.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<NotificationResponse> mine(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @AuthenticationPrincipal AuthUser user) {
        return service.mine(user.id(), page, size);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal AuthUser user) {
        return new UnreadCountResponse(service.unreadCount(user.id()));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return service.markRead(user.id(), id);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal AuthUser user) {
        service.markAllRead(user.id());
    }
}
