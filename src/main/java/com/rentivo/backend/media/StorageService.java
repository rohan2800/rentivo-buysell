package com.rentivo.backend.media;

import org.springframework.web.multipart.MultipartFile;

/**
 * Where uploaded images live. Listing code only talks to this interface, so moving from local
 * disk to S3 / R2 is a new implementation and nothing else.
 */
public interface StorageService {

    /** Validates and stores the image, returning the public URL path. */
    String storeImage(MultipartFile file, String folder);

    /** Removes a previously stored file. Unknown URLs are ignored. */
    void delete(String url);
}
