package com.rentivo.backend.media;

public enum ImageType {
    JPEG(".jpg", "image/jpeg"), PNG(".png", "image/png"), WEBP(".webp", "image/webp");

    private final String extension;
    private final String mimeType;

    ImageType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String extension() {
        return extension;
    }

    public String mimeType() {
        return mimeType;
    }
}
