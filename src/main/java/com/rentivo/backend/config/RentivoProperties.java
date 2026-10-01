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
        @DefaultValue Listings listings,
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

    /**
     * provider selects the active StorageService: "postgres" (default — image bytes in the
     * database), "local" (disk) or "s3". s3Bucket is required when provider is "s3";
     * s3PublicBaseUrl overrides the default virtual-hosted URL with a CloudFront (or other CDN)
     * domain.
     */
    public record Upload(@DefaultValue("postgres") String provider,
                         @DefaultValue("./uploads") String dir,
                         @DefaultValue("10") int maxImagesPerListing,
                         @DefaultValue("10485760") long maxImageBytes,
                         String s3Bucket,
                         @DefaultValue("ap-south-1") String s3Region,
                         String s3PublicBaseUrl) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Subscriptions(@DefaultValue("false") boolean devActivationEnabled) {
    }

    /** How long an approved listing stays live before it auto-expires and needs a renewal. */
    public record Listings(@DefaultValue("30") int validityDays) {
    }
}
