-- Initial schema. All timestamps are timestamptz (UTC).

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    phone           VARCHAR(15)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    role            VARCHAR(20)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_users_phone UNIQUE (phone)
);

CREATE TABLE otp_challenges (
    id          BIGSERIAL PRIMARY KEY,
    phone       VARCHAR(15)  NOT NULL,
    code_hash   VARCHAR(100) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    attempts    INTEGER      NOT NULL DEFAULT 0,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_otp_phone_id ON otp_challenges (phone, id DESC);
CREATE INDEX idx_otp_expires_at ON otp_challenges (expires_at);

CREATE TABLE categories (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    system_category  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_categories_name UNIQUE (name)
);

CREATE TABLE category_fields (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT        NOT NULL REFERENCES categories (id),
    name         VARCHAR(100)  NOT NULL,
    type         VARCHAR(20)   NOT NULL,
    required     BOOLEAN       NOT NULL DEFAULT FALSE,
    options_csv  VARCHAR(2000),
    sort_order   INTEGER       NOT NULL DEFAULT 0,
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_category_field_name UNIQUE (category_id, name)
);

CREATE TABLE listings (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(200)   NOT NULL,
    category_id       BIGINT         NOT NULL REFERENCES categories (id),
    owner_id          BIGINT         NOT NULL REFERENCES users (id),
    listing_type      VARCHAR(20)    NOT NULL,
    price             NUMERIC(14, 2) NOT NULL,
    price_unit        VARCHAR(20)    NOT NULL,
    description       VARCHAR(3000)  NOT NULL,
    state             VARCHAR(100)   NOT NULL,
    city              VARCHAR(100)   NOT NULL,
    locality          VARCHAR(150)   NOT NULL,
    pincode           VARCHAR(10),
    address           VARCHAR(500),
    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    contact_phone     VARCHAR(15)    NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    rejection_reason  VARCHAR(500),
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ
);
CREATE INDEX idx_listings_status_created ON listings (status, created_at DESC);
CREATE INDEX idx_listings_owner ON listings (owner_id);
CREATE INDEX idx_listings_category ON listings (category_id);
CREATE INDEX idx_listings_city ON listings (LOWER(city));

CREATE TABLE listing_images (
    id           BIGSERIAL PRIMARY KEY,
    listing_id   BIGINT        NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    file_name    VARCHAR(255)  NOT NULL,
    file_url     VARCHAR(1000) NOT NULL,
    sort_order   INTEGER       NOT NULL DEFAULT 0,
    uploaded_at  TIMESTAMPTZ   NOT NULL
);
CREATE INDEX idx_listing_images_listing ON listing_images (listing_id);

CREATE TABLE listing_field_values (
    id          BIGSERIAL PRIMARY KEY,
    listing_id  BIGINT        NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    field_id    BIGINT        NOT NULL REFERENCES category_fields (id),
    value       VARCHAR(4000) NOT NULL,
    CONSTRAINT uk_listing_field UNIQUE (listing_id, field_id)
);

CREATE TABLE subscription_plans (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL,
    price          NUMERIC(12, 2) NOT NULL,
    validity_days  INTEGER        NOT NULL,
    contact_limit  INTEGER        NOT NULL,
    description    VARCHAR(1000),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_subscription_plans_name UNIQUE (name)
);

-- plan_name and contact_limit are snapshots taken at purchase time so that later
-- admin edits to a plan never change what an existing subscriber already bought.
CREATE TABLE user_subscriptions (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users (id),
    plan_id        BIGINT      NOT NULL REFERENCES subscription_plans (id),
    plan_name      VARCHAR(100) NOT NULL,
    contact_limit  INTEGER     NOT NULL,
    start_at       TIMESTAMPTZ NOT NULL,
    end_at         TIMESTAMPTZ NOT NULL,
    contacts_used  INTEGER     NOT NULL DEFAULT 0,
    status         VARCHAR(20) NOT NULL,
    CONSTRAINT ck_contacts_used CHECK (contacts_used >= 0 AND contacts_used <= contact_limit)
);
CREATE INDEX idx_user_subscriptions_user_status ON user_subscriptions (user_id, status, end_at DESC);

CREATE TABLE contact_accesses (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES users (id),
    listing_id       BIGINT      NOT NULL REFERENCES listings (id),
    subscription_id  BIGINT      NOT NULL REFERENCES user_subscriptions (id),
    unlocked_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_contact_access_user_listing UNIQUE (user_id, listing_id)
);

CREATE TABLE payments (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT         NOT NULL REFERENCES users (id),
    plan_id              BIGINT         NOT NULL REFERENCES subscription_plans (id),
    amount               NUMERIC(12, 2) NOT NULL,
    status               VARCHAR(20)    NOT NULL,
    provider             VARCHAR(30)    NOT NULL,
    provider_payment_id  VARCHAR(100),
    created_at           TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uk_payments_provider_payment UNIQUE (provider, provider_payment_id)
);
CREATE INDEX idx_payments_status ON payments (status);
