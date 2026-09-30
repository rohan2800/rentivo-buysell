package com.rentivo.backend.media;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.config.RentivoProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores image bytes as a database row instead of a disk or object-storage file. The simplest
 * option to get running — no bucket or disk volume to provision — at the cost of growing the
 * database and its backups, and serving images through the app instead of a CDN. Swapping to
 * {@link S3StorageService} later is a one-line config change: nothing outside this class knows
 * where the bytes live.
 * <p>
 * This is the default provider unless rentivo.upload.provider says otherwise.
 */
@Service
@ConditionalOnProperty(name = "rentivo.upload.provider", havingValue = "postgres", matchIfMissing = true)
public class PostgresStorageService implements StorageService {

    static final String PATH_PREFIX = "/media/";

    private final StoredFileRepository files;
    private final long maxBytes;
    private final Clock clock;

    public PostgresStorageService(StoredFileRepository files, RentivoProperties props, Clock clock) {
        this.files = files;
        this.maxBytes = props.upload().maxImageBytes();
        this.clock = clock;
    }

    @Override
    @Transactional
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
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Image upload failed", e);
        }

        StoredFile row = new StoredFile();
        row.setId(UUID.randomUUID());
        row.setFolder(folder);
        row.setContentType(type.mimeType());
        row.setByteSize(bytes.length);
        row.setData(bytes);
        row.setCreatedAt(clock.instant());
        files.save(row);
        return publicPath(row.getId());
    }

    @Override
    @Transactional
    public void delete(String url) {
        extractId(url).ifPresent(files::deleteById);
    }

    static String publicPath(UUID id) {
        return PATH_PREFIX + id;
    }

    static Optional<UUID> extractId(String url) {
        if (url == null || !url.startsWith(PATH_PREFIX)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(url.substring(PATH_PREFIX.length())));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
