package com.rentivo.backend.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    /** Fetches only id/contentType/data, skipping a redundant read of the other columns. */
    @Query("select f.contentType as contentType, f.data as data from StoredFile f where f.id = :id")
    Optional<Projection> findContentById(@Param("id") UUID id);

    interface Projection {
        String getContentType();
        byte[] getData();
    }
}
