package com.rentivo.backend.notification;

import com.rentivo.backend.common.exception.NotFoundException;
import com.rentivo.backend.notification.dto.NotificationDtos.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    private static final long USER = 1L;

    @Mock NotificationRepository repo;

    private final NotificationService service = new NotificationService(repo,
            Clock.fixed(Instant.parse("2026-09-30T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void markingSomeoneElsesOrMissingNotificationFails() {
        when(repo.findByIdAndUserId(99L, USER)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.markRead(USER, 99L));
    }

    @Test
    void markReadFlipsTheFlagAndReturnsIt() {
        Notification n = new Notification();
        ReflectionTestUtils.setField(n, "id", 5L);
        n.setType(NotificationType.LISTING_APPROVED);
        n.setTitle("Listing approved");
        n.setBody("Your listing is live.");
        n.setRead(false);
        n.setCreatedAt(Instant.parse("2026-09-30T09:00:00Z"));
        when(repo.findByIdAndUserId(5L, USER)).thenReturn(Optional.of(n));

        NotificationResponse res = service.markRead(USER, 5L);

        assertTrue(n.isRead());
        assertTrue(res.read());
        assertEquals("Listing approved", res.title());
    }

    @Test
    void unreadCountDelegatesToRepository() {
        when(repo.countByUserIdAndReadFalse(USER)).thenReturn(3L);
        assertEquals(3L, service.unreadCount(USER));
    }
}
