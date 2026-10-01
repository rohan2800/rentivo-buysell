-- expires_at is set whenever a listing becomes APPROVED (first approval or a renewal) and left
-- null otherwise. Existing approved rows get null here deliberately: they are not retroactively
-- expired, and will get a real expiry the next time they are approved or renewed.
ALTER TABLE listings ADD COLUMN expires_at TIMESTAMPTZ;
CREATE INDEX idx_listings_expiry ON listings (expires_at) WHERE status = 'APPROVED';
