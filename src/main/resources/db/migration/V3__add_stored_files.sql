-- Backs the "postgres" StorageService: image bytes stored as a row instead of a disk/S3 object.
CREATE TABLE stored_files (
    id           UUID PRIMARY KEY,
    folder       VARCHAR(50)  NOT NULL,
    content_type VARCHAR(50)  NOT NULL,
    byte_size    INTEGER      NOT NULL,
    data         BYTEA        NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_stored_files_folder ON stored_files (folder);
