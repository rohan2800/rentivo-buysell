package com.rentivo.backend.config;

import com.rentivo.backend.common.PhoneNumbers;
import com.rentivo.backend.user.Role;
import com.rentivo.backend.user.User;
import com.rentivo.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Creates the first admin account when BOOTSTRAP_ADMIN_PHONE is set. The admin still has to
 * sign in through a real OTP; nothing here grants access by itself.
 */
@Component
public class BootstrapAdmin implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);

    private final UserRepository users;
    private final RentivoProperties props;
    private final Clock clock;

    public BootstrapAdmin(UserRepository users, RentivoProperties props, Clock clock) {
        this.users = users;
        this.props = props;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String raw = props.bootstrapAdminPhone();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String phone = PhoneNumbers.normalize(raw);
        users.findByPhone(phone).ifPresentOrElse(existing -> {
            if (existing.getRole() != Role.ADMIN) {
                log.warn("BOOTSTRAP_ADMIN_PHONE belongs to an existing non-admin user; role left unchanged");
            }
        }, () -> {
            User admin = new User();
            admin.setPhone(phone);
            admin.setName("Admin");
            admin.setPhoneVerified(true);
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(clock.instant());
            users.save(admin);
            log.info("Bootstrap admin account created");
        });
    }
}
