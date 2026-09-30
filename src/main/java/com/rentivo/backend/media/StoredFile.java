package com.rentivo.backend.media;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** An uploaded image's bytes, when rentivo.upload.provider=postgres. Served by MediaController. */
@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String folder;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false)
    private int byteSize;

    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public int getByteSize() { return byteSize; }
    public void setByteSize(int byteSize) { this.byteSize = byteSize; }
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
