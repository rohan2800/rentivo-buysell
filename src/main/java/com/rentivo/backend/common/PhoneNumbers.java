package com.rentivo.backend.common;

import com.rentivo.backend.common.exception.BadRequestException;

/** Phone number normalisation shared by auth and listings. */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    /** Strips spaces, dashes, brackets and a leading '+'; requires 10-15 digits. */
    public static String normalize(String raw) {
        if (raw == null) {
            throw new BadRequestException("A valid 10-15 digit mobile number is required");
        }
        String digits = raw.replaceAll("[\\s\\-()]", "");
        if (digits.startsWith("+")) {
            digits = digits.substring(1);
        }
        if (!digits.matches("\\d{10,15}")) {
            throw new BadRequestException("A valid 10-15 digit mobile number is required");
        }
        return digits;
    }
}
