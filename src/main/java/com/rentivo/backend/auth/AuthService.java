package com.rentivo.backend.auth;

import com.rentivo.backend.auth.AuthDtos.AuthResponse;
import com.rentivo.backend.auth.AuthDtos.SendOtpResponse;
import com.rentivo.backend.auth.AuthDtos.VerifyOtpRequest;
import com.rentivo.backend.common.PhoneNumbers;
import com.rentivo.backend.common.exception.ForbiddenException;
import com.rentivo.backend.common.exception.TooManyRequestsException;
import com.rentivo.backend.config.RentivoProperties;
import com.rentivo.backend.security.JwtService;
import com.rentivo.backend.user.Role;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private static final String BLOCKED = "This account cannot sign in";

    private final UserRepository users;
    private final OtpChallengeRepository otps;
    private final JwtService jwt;
    private final OtpSender otpSender;
    private final PasswordEncoder encoder;
    private final RentivoProperties.Otp cfg;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, OtpChallengeRepository otps, JwtService jwt, OtpSender otpSender,
                       PasswordEncoder encoder, RentivoProperties props, Clock clock) {
        this.users = users;
        this.otps = otps;
        this.jwt = jwt;
        this.otpSender = otpSender;
        this.encoder = encoder;
        this.cfg = props.otp();
        this.clock = clock;
    }

    @Transactional
    public SendOtpResponse sendOtp(String rawPhone) {
        String phone = PhoneNumbers.normalize(rawPhone);
        Instant now = clock.instant();

        otps.findTopByPhoneOrderByIdDesc(phone).ifPresent(last -> {
            if (last.getCreatedAt().plusSeconds(cfg.resendCooldownSeconds()).isAfter(now)) {
                throw new TooManyRequestsException("Please wait a moment before requesting another OTP");
            }
        });
        if (otps.countByPhoneAndCreatedAtAfter(phone, now.minus(1, ChronoUnit.HOURS)) >= cfg.maxPerHour()) {
            throw new TooManyRequestsException("Too many OTP requests. Please try again later");
        }
        if (users.findByPhone(phone).filter(u -> !u.isActive()).isPresent()) {
            throw new ForbiddenException(BLOCKED);
        }

        otps.invalidateOpen(phone);

        String code = cfg.devMode() ? cfg.devCode() : generateCode();
        OtpChallenge challenge = new OtpChallenge();
        challenge.setPhone(phone);
        challenge.setCodeHash(encoder.encode(code));
        challenge.setCreatedAt(now);
        challenge.setExpiresAt(now.plus(cfg.expirationMinutes(), ChronoUnit.MINUTES));
        otps.save(challenge);

        otpSender.send(phone, code);
        return new SendOtpResponse("OTP sent", cfg.devMode() ? code : null);
    }

    /** noRollbackFor keeps the incremented attempt counter when the code is wrong. */
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public AuthResponse verify(VerifyOtpRequest req) {
        String phone = PhoneNumbers.normalize(req.phone());
        Instant now = clock.instant();

        OtpChallenge challenge = otps.findTopByPhoneAndUsedFalseOrderByIdDesc(phone)
                .orElseThrow(InvalidOtpException::new);
        if (challenge.getExpiresAt().isBefore(now) || challenge.getAttempts() >= cfg.maxAttempts()) {
            challenge.setUsed(true);
            throw new InvalidOtpException();
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!encoder.matches(req.code(), challenge.getCodeHash())) {
            if (challenge.getAttempts() >= cfg.maxAttempts()) {
                challenge.setUsed(true);
            }
            throw new InvalidOtpException();
        }
        challenge.setUsed(true);

        User user = users.findByPhone(phone).orElseGet(() -> createUser(phone, req.name(), now));
        if (!user.isActive()) {
            throw new ForbiddenException(BLOCKED);
        }
        return new AuthResponse(jwt.generate(user), user.getId(), user.getName(), user.getPhone(),
                user.getRole().name());
    }

    private User createUser(String phone, String name, Instant now) {
        User user = new User();
        user.setPhone(phone);
        user.setName(name == null || name.isBlank() ? "User" : name.trim());
        user.setPhoneVerified(true);
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(now);
        return users.save(user);
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
