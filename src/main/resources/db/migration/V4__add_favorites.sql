CREATE TABLE favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    listing_id  BIGINT      NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_favorites_user_listing UNIQUE (user_id, listing_id)
);
CREATE INDEX idx_favorites_user_created ON favorites (user_id, created_at DESC);
