package com.rentivo.backend.media;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3StorageServiceTest {

    @Test
    void keyIsScopedToFolderWithTheRightExtension() {
        String key = S3StorageService.buildKey("listings", ImageType.PNG);
        assertTrue(key.startsWith("listings/"));
        assertTrue(key.endsWith(".png"));
    }

    @Test
    void keysAreUnique() {
        assertNotEquals(S3StorageService.buildKey("listings", ImageType.JPEG),
                S3StorageService.buildKey("listings", ImageType.JPEG));
    }

    @Test
    void contentTypeMatchesDetectedFormat() {
        assertEquals("image/jpeg", S3StorageService.contentTypeOf(ImageType.JPEG));
        assertEquals("image/png", S3StorageService.contentTypeOf(ImageType.PNG));
        assertEquals("image/webp", S3StorageService.contentTypeOf(ImageType.WEBP));
    }
}
