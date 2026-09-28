package com.rentivo.backend.media;

public enum ImageType {
    JPEG(".jpg"), PNG(".png"), WEBP(".webp");

    private final String extension;

    ImageType(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }
}
