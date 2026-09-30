package com.rentivo.backend.auth;

import com.rentivo.backend.common.exception.BadRequestException;

/** Kept as its own type so the transaction can commit the failed-attempt counter. */
public class InvalidOtpException extends BadRequestException {

    public InvalidOtpException() {
        super("Invalid or expired OTP");
    }
}
