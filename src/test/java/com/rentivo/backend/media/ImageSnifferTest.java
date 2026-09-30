package com.rentivo.backend.media;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageSnifferTest {

    @Test
    void detectsJpeg() {
        byte[] h = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
        assertEquals(Optional.of(ImageType.JPEG), ImageSniffer.detect(h, h.length));
    }

    @Test
    void detectsPng() {
        byte[] h = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        assertEquals(Optional.of(ImageType.PNG), ImageSniffer.detect(h, h.length));
    }

    @Test
    void detectsWebp() {
        byte[] h = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P'};
        assertEquals(Optional.of(ImageType.WEBP), ImageSniffer.detect(h, h.length));
    }

    @Test
    void rejectsEverythingElse() {
        byte[] script = "<?php echo 1; ?>".getBytes();
        assertTrue(ImageSniffer.detect(script, ImageSniffer.HEADER_BYTES).isEmpty());
        byte[] wave = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'A', 'V', 'E'};
        assertTrue(ImageSniffer.detect(wave, wave.length).isEmpty());
        assertTrue(ImageSniffer.detect(new byte[]{(byte) 0xFF, (byte) 0xD8}, 2).isEmpty());
        assertTrue(ImageSniffer.detect(null, 0).isEmpty());
    }
}
