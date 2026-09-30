package com.rentivo.backend.media;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.config.RentivoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * S3-backed storage, active when rentivo.upload.provider=s3. Credentials are resolved through
 * the default AWS provider chain (env vars, ~/.aws/credentials, or an instance/task role in
 * ECS/EKS) — no access keys are ever read from application properties.
 * <p>
 * Objects are written with a public-read ACL: listing photos are meant to be publicly visible,
 * so there is nothing to protect by hiding them behind presigned URLs, and a plain public URL
 * never expires. Point rentivo.upload.s3-public-base-url at a CloudFront distribution in front
 * of the bucket for a CDN in production; it defaults to the bucket's own virtual-hosted URL.
 */
@Service
@ConditionalOnProperty(name = "rentivo.upload.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3;
    private final String bucket;
    private final String publicBaseUrl;
    private final long maxBytes;

    public S3StorageService(RentivoProperties props) {
        RentivoProperties.Upload cfg = props.upload();
        if (cfg.s3Bucket() == null || cfg.s3Bucket().isBlank()) {
            throw new IllegalStateException(
                    "rentivo.upload.s3-bucket must be set when rentivo.upload.provider=s3");
        }
        this.bucket = cfg.s3Bucket();
        this.maxBytes = cfg.maxImageBytes();
        this.s3 = S3Client.builder()
                .region(Region.of(cfg.s3Region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.publicBaseUrl = (cfg.s3PublicBaseUrl() == null || cfg.s3PublicBaseUrl().isBlank())
                ? "https://" + bucket + ".s3." + cfg.s3Region() + ".amazonaws.com"
                : stripTrailingSlash(cfg.s3PublicBaseUrl());
    }

    @Override
    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("Image must be at most " + (maxBytes / (1024 * 1024)) + " MB");
        }

        ImageType type;
        byte[] bytes;
        try {
            byte[] header = new byte[ImageSniffer.HEADER_BYTES];
            int read;
            try (InputStream in = file.getInputStream()) {
                read = in.readNBytes(header, 0, header.length);
            }
            type = ImageSniffer.detect(header, read)
                    .orElseThrow(() -> new BadRequestException("Only JPG, PNG and WEBP images are allowed"));
            // MultipartFile supports being read more than once (Spring re-opens the underlying
            // part/temp file each call), so reading the header above does not consume this.
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Image upload failed", e);
        }

        String key = buildKey(folder, type);
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentTypeOf(type))
                    .contentLength((long) bytes.length)
                    .build(), RequestBody.fromBytes(bytes));
        } catch (S3Exception e) {
            log.error("S3 upload failed for key {}", key, e);
            throw new IllegalStateException("Image upload failed", e);
        }
        return publicBaseUrl + "/" + key;
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(publicBaseUrl + "/")) {
            return;
        }
        String key = url.substring(publicBaseUrl.length() + 1);
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (S3Exception e) {
            log.warn("Could not delete S3 object {}", key, e);
        }
    }

    static String buildKey(String folder, ImageType type) {
        return folder + "/" + UUID.randomUUID() + type.extension();
    }

    static String contentTypeOf(ImageType type) {
        return switch (type) {
            case JPEG -> "image/jpeg";
            case PNG -> "image/png";
            case WEBP -> "image/webp";
        };
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
