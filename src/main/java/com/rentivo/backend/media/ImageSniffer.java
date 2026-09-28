package com.rentivo.backend.media;

import java.util.Optional;

/**
 * Identifies an image from its first bytes. The client-supplied Content-Type and file name are
 * ignored because an attacker controls both.
 */
public final class ImageSniffer {

    /** Bytes needed to tell the supported formats apart. */
    public static final int HEADER_BYTES = 12;

    private ImageSniffer() {
    }

    public static Optional<ImageType> detect(byte[] h, int length) {
        if (h == null) {
            return Optional.empty();
        }
        if (length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return Optional.of(ImageType.JPEG);
        }
        if (length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
            return Optional.of(ImageType.PNG);
        }
        if (length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return Optional.of(ImageType.WEBP);
        }
        return Optional.empty();
    }
}
