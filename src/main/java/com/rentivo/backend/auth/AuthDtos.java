package com.rentivo.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SendOtpRequest(@NotBlank @Size(max = 20) String phone) {
    }

    /** developmentOtp is only ever filled when rentivo.otp.dev-mode is on. */
    public record SendOtpResponse(String message, String developmentOtp) {
    }

    public record VerifyOtpRequest(@NotBlank @Size(max = 20) String phone,
                                   @NotBlank @Size(max = 10) String code,
                                   @Size(max = 100) String name) {
    }

    public record AuthResponse(String token, Long userId, String name, String phone, String role) {
    }
}
