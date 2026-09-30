package com.rentivo.backend.auth;

/**
 * Delivers a one-time code. Production needs a real implementation (MSG91, Twilio, ...);
 * with none registered and dev-mode off, the application refuses to start.
 */
public interface OtpSender {

    void send(String phone, String code);
}
