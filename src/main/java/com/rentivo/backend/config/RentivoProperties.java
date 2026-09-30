package com.rentivo.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "rentivo")
public record RentivoProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue Otp otp,
        @DefaultValue Upload upload,
        @DefaultValue Cors cors,
        @DefaultValue Subscriptions subscriptions,
        String bootstrapAdminPhone) {

    public record Jwt(String secret,
                      @DefaultValue("1440") long expirationMinutes,
                      @DefaultValue("rentivo") String issuer) {
    }

    public record Otp(@DefaultValue("false") boolean devMode,
                      @DefaultValue("123456") String devCode,
                      @DefaultValue("5") long expirationMinutes,
                      @DefaultValue("5") int maxAttempts,
                      @DefaultValue("60") long resendCooldownSeconds,
                      @DefaultValue("5") int maxPerHour) {
    }

    public record Upload(@DefaultValue("./uploads") String dir,
                         @DefaultValue("10") int maxImagesPerListing,
                         @DefaultValue("10485760") long maxImageBytes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Subscriptions(@DefaultValue("false") boolean devActivationEnabled) {
    }
}
