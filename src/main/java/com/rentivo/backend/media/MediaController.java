package com.rentivo.backend.media;

import com.rentivo.backend.common.exception.NotFoundException;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Serves images stored by PostgresStorageService. Registered unconditionally — harmless (always
 * 404s) when a different storage provider is active, since nothing writes rows for it to serve.
 * Content is immutable once uploaded (a new upload gets a new id), so responses cache for a year.
 */
@RestController
public class MediaController {

    private final StoredFileRepository files;

    public MediaController(StoredFileRepository files) {
        this.files = files;
    }

    @GetMapping("/media/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        StoredFileRepository.Projection file = files.findContentById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(file.getData());
    }
}
