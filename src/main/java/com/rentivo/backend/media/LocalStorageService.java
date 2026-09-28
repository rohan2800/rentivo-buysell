package com.rentivo.backend.media;

import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.config.RentivoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
    private static final String URL_PREFIX = "/uploads/";

    private final Path root;
    private final long maxBytes;

    public LocalStorageService(RentivoProperties props) {
        this.root = Paths.get(props.upload().dir()).toAbsolutePath().normalize();
        this.maxBytes = props.upload().maxImageBytes();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload directory " + root, e);
        }
    }

    @Override
    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("Image must be at most " + (maxBytes / (1024 * 1024)) + " MB");
        }
        try {
            byte[] header = new byte[ImageSniffer.HEADER_BYTES];
            int read;
            try (InputStream in = file.getInputStream()) {
                read = in.readNBytes(header, 0, header.length);
            }
            ImageType type = ImageSniffer.detect(header, read)
                    .orElseThrow(() -> new BadRequestException("Only JPG, PNG and WEBP images are allowed"));

            Path dir = root.resolve(folder).normalize();
            if (!dir.startsWith(root)) {
                throw new BadRequestException("Invalid upload folder");
            }
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + type.extension();
            Path target = dir.resolve(name).normalize();
            if (!target.startsWith(root)) {
                throw new BadRequestException("Invalid upload path");
            }
            file.transferTo(target);
            return URL_PREFIX + folder + "/" + name;
        } catch (IOException e) {
            throw new IllegalStateException("Image upload failed", e);
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        Path target = root.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete file {}", target, e);
        }
    }
}
