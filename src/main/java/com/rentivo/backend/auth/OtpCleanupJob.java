package com.rentivo.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.temporal.ChronoUnit;

@Component
public class OtpCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OtpCleanupJob.class);

    private final OtpChallengeRepository otps;
    private final Clock clock;

    public OtpCleanupJob(OtpChallengeRepository otps, Clock clock) {
        this.otps = otps;
        this.clock = clock;
    }

    /** Keeps one day of history for abuse investigation, then deletes. */
    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void purgeExpired() {
        int removed = otps.deleteExpiredBefore(clock.instant().minus(1, ChronoUnit.DAYS));
        if (removed > 0) {
            log.info("Purged {} expired OTP challenges", removed);
        }
    }
}
