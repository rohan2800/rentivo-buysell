package com.rentivo.backend.user;

import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserAdminServiceTest {

    private static final long ADMIN = 1L;
    private static final long TARGET = 2L;

    @Mock UserRepository users;

    private final UserAdminService service = new UserAdminService(users);

    private User user(long id, Role role) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    @Test
    void adminCannotBlockTheirOwnAccount() {
        when(users.findById(ADMIN)).thenReturn(Optional.of(user(ADMIN, Role.ADMIN)));
        assertThrows(ConflictException.class, () -> service.setActive(ADMIN, ADMIN, false));
    }

    @Test
    void anAdminAccountCannotBeBlockedByAnotherAdmin() {
        when(users.findById(TARGET)).thenReturn(Optional.of(user(TARGET, Role.ADMIN)));
        assertThrows(ConflictException.class, () -> service.setActive(ADMIN, TARGET, false));
    }

    @Test
    void anOrdinaryUserCanBeBlocked() {
        when(users.findById(TARGET)).thenReturn(Optional.of(user(TARGET, Role.USER)));
        var res = service.setActive(ADMIN, TARGET, false);
        assertFalse(res.active());
    }

    @Test
    void reactivatingAUserNeedsNoSpecialChecks() {
        User blocked = user(TARGET, Role.USER);
        blocked.setActive(false);
        when(users.findById(TARGET)).thenReturn(Optional.of(blocked));
        var res = service.setActive(ADMIN, TARGET, true);
        assertTrue(res.active());
    }

    @Test
    void missingUserFails() {
        when(users.findById(TARGET)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.setActive(ADMIN, TARGET, false));
    }
}
