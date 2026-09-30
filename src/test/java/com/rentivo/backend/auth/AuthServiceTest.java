package com.rentivo.backend.auth;

import com.rentivo.backend.auth.AuthDtos.AuthResponse;
import com.rentivo.backend.auth.AuthDtos.SendOtpResponse;
import com.rentivo.backend.auth.AuthDtos.VerifyOtpRequest;
import com.rentivo.backend.common.exception.ForbiddenException;
import com.rentivo.backend.common.exception.TooManyRequestsException;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.security.JwtService;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");
    private static final String PHONE = "9876543210";

    @Mock UserRepository users;
    @Mock OtpChallengeRepository otps;
    @Mock JwtService jwt;
    @Mock OtpSender sender;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private AuthService service(boolean devMode) {
        RentivoProperties props = new RentivoProperties(null,
                new RentivoProperties.Otp(devMode, "123456", 5, 3, 60, 5), null, null, null, null);
        return new AuthService(users, otps, jwt, sender, encoder, props, clock);
    }

    private OtpChallenge challenge(String code, int attempts, Instant expiresAt) {
        OtpChallenge c = new OtpChallenge();
        c.setPhone(PHONE);
        c.setCodeHash(encoder.encode(code));
        c.setAttempts(attempts);
        c.setCreatedAt(NOW.minusSeconds(30));
        c.setExpiresAt(expiresAt);
        return c;
    }

    @Test
    void resendWithinCooldownIsRejected() {
        OtpChallenge recent = challenge("111111", 0, NOW.plusSeconds(200));
        recent.setCreatedAt(NOW.minusSeconds(10));
        when(otps.findTopByPhoneOrderByIdDesc(PHONE)).thenReturn(Optional.of(recent));

        assertThrows(TooManyRequestsException.class, () -> service(false).sendOtp(PHONE));
    }

    @Test
    void hourlyCapIsEnforced() {
        when(otps.countByPhoneAndCreatedAtAfter(any(), any())).thenReturn(5L);
        assertThrows(TooManyRequestsException.class, () -> service(false).sendOtp(PHONE));
    }

    @Test
    void devModeExposesCodeOnlyWhenEnabled() {
        SendOtpResponse dev = service(true).sendOtp(PHONE);
        assertEquals("123456", dev.developmentOtp());
        verify(sender).send(PHONE, "123456");
    }

    @Test
    void productionModeNeverReturnsTheCode() {
        SendOtpResponse prod = service(false).sendOtp(PHONE);
        assertNull(prod.developmentOtp());
    }

    @Test
    void blockedUserCannotRequestOtp() {
        User blocked = new User();
        blocked.setActive(false);
        when(users.findByPhone(PHONE)).thenReturn(Optional.of(blocked));
        assertThrows(ForbiddenException.class, () -> service(false).sendOtp(PHONE));
    }

    @Test
    void wrongCodeCountsAnAttempt() {
        OtpChallenge c = challenge("111111", 0, NOW.plusSeconds(200));
        when(otps.findTopByPhoneAndUsedFalseOrderByIdDesc(PHONE)).thenReturn(Optional.of(c));

        assertThrows(InvalidOtpException.class,
                () -> service(false).verify(new VerifyOtpRequest(PHONE, "000000", null)));
        assertEquals(1, c.getAttempts());
    }

    @Test
    void challengeIsBurnedAfterMaxAttempts() {
        OtpChallenge c = challenge("111111", 2, NOW.plusSeconds(200));
        when(otps.findTopByPhoneAndUsedFalseOrderByIdDesc(PHONE)).thenReturn(Optional.of(c));

        assertThrows(InvalidOtpException.class,
                () -> service(false).verify(new VerifyOtpRequest(PHONE, "000000", null)));
        assertTrue(c.isUsed());
    }

    @Test
    void expiredCodeIsRejected() {
        OtpChallenge c = challenge("111111", 0, NOW.minusSeconds(1));
        when(otps.findTopByPhoneAndUsedFalseOrderByIdDesc(PHONE)).thenReturn(Optional.of(c));

        assertThrows(InvalidOtpException.class,
                () -> service(false).verify(new VerifyOtpRequest(PHONE, "111111", null)));
    }

    @Test
    void correctCodeCreatesUserAndIssuesToken() {
        OtpChallenge c = challenge("111111", 0, NOW.plusSeconds(200));
        when(otps.findTopByPhoneAndUsedFalseOrderByIdDesc(PHONE)).thenReturn(Optional.of(c));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generate(any(User.class))).thenReturn("signed-token");

        AuthResponse res = service(false).verify(new VerifyOtpRequest(PHONE, "111111", "Asha"));

        assertEquals("signed-token", res.token());
        assertEquals("Asha", res.name());
        assertEquals("USER", res.role());
        assertTrue(c.isUsed());
    }

    @Test
    void existingUsersNameIsNotOverwrittenByLogin() {
        OtpChallenge c = challenge("111111", 0, NOW.plusSeconds(200));
        when(otps.findTopByPhoneAndUsedFalseOrderByIdDesc(PHONE)).thenReturn(Optional.of(c));
        User existing = new User();
        existing.setPhone(PHONE);
        existing.setName("Original");
        existing.setRole(com.rentivo.backend.user.Role.USER);
        existing.setActive(true);
        when(users.findByPhone(PHONE)).thenReturn(Optional.of(existing));
        when(jwt.generate(any(User.class))).thenReturn("t");

        AuthResponse res = service(false).verify(new VerifyOtpRequest(PHONE, "111111", "Attacker"));

        assertEquals("Original", res.name());
    }
}
