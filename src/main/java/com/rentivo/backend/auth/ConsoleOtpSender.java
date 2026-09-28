package com.rentivo.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Development only: writes the code to the log instead of sending an SMS. */
@Component
@ConditionalOnProperty(name = "rentivo.otp.dev-mode", havingValue = "true")
public class ConsoleOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleOtpSender.class);

    @Override
    public void send(String phone, String code) {
        log.info("[DEV OTP] phone={} code={}", phone, code);
    }
}
