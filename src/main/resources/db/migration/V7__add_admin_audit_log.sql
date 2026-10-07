CREATE TABLE admin_audit_log (
    id          BIGSERIAL PRIMARY KEY,
    admin_id    BIGINT       NOT NULL,
    admin_phone VARCHAR(15)  NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    target_type VARCHAR(30)  NOT NULL,
    target_id   BIGINT,
    detail      VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_admin_audit_created ON admin_audit_log (created_at DESC);
