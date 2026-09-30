package com.rentivo.backend.media;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageTypeTest {

    @Test
    void everyTypeHasAMatchingExtensionAndMimeType() {
        assertEquals(".jpg", ImageType.JPEG.extension());
        assertEquals("image/jpeg", ImageType.JPEG.mimeType());
        assertEquals(".png", ImageType.PNG.extension());
        assertEquals("image/png", ImageType.PNG.mimeType());
        assertEquals(".webp", ImageType.WEBP.extension());
        assertEquals("image/webp", ImageType.WEBP.mimeType());
    }
}
