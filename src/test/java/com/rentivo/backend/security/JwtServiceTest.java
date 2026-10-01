package com.rentivo.backend.security;

import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private static JwtService service(String secret, long minutes) {
        return new JwtService(new RentivoProperties(
                new RentivoProperties.Jwt(secret, minutes, "rentivo"), null, null, null, null, null, null));
    }

    private static User user() {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", 42L);
        u.setPhone("9876543210");
        return u;
    }

    @Test
    void roundTripReturnsUserId() {
        JwtService jwt = service(SECRET, 60);
        assertEquals(Optional.of(42L), jwt.parseUserId(jwt.generate(user())));
    }

    @Test
    void rejectsTamperedAndForeignTokens() {
        JwtService jwt = service(SECRET, 60);
        String token = jwt.generate(user());

        assertTrue(jwt.parseUserId(token + "x").isEmpty());
        assertTrue(jwt.parseUserId("not-a-token").isEmpty());
        assertTrue(service("another-secret-another-secret-1234", 60).parseUserId(token).isEmpty());
    }

    @Test
    void rejectsExpiredTokens() {
        JwtService jwt = service(SECRET, -1);
        assertTrue(jwt.parseUserId(jwt.generate(user())).isEmpty());
    }

    @Test
    void refusesWeakOrMissingSecret() {
        assertThrows(IllegalArgumentException.class, () -> service("too-short", 60));
        assertThrows(IllegalArgumentException.class, () -> service(null, 60));
    }
}
