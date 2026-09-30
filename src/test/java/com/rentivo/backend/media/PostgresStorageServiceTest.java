package com.rentivo.backend.media;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresStorageServiceTest {

    @Test
    void publicPathRoundTripsThroughExtractId() {
        UUID id = UUID.randomUUID();
        String path = PostgresStorageService.publicPath(id);

        assertEquals("/media/" + id, path);
        assertEquals(Optional.of(id), PostgresStorageService.extractId(path));
    }

    @Test
    void extractIdRejectsAnythingNotAMediaPath() {
        assertTrue(PostgresStorageService.extractId(null).isEmpty());
        assertTrue(PostgresStorageService.extractId("/uploads/listings/x.jpg").isEmpty());
        assertTrue(PostgresStorageService.extractId("/media/not-a-uuid").isEmpty());
    }
}
