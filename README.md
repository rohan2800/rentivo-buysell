# Rentivo (buysell) Backend

Marketplace backend for Property, Furniture, POP and Hardware listings. Posting is free;
seeing a seller's phone number needs a paid subscription. Business rules: see
[BUSINESS_RULES.md](BUSINESS_RULES.md).

**Stack:** Java 21, Spring Boot 4.1, PostgreSQL 16, Flyway, Spring Security (JWT), Maven.

## Run locally

```bash
docker compose up -d          # PostgreSQL on :5432
./mvnw spring-boot:run        # dev profile is the default
```

The `dev` profile enables a fixed OTP (`123456`, also returned in the API response), a free
`/api/subscriptions/activate-dev` endpoint, and creates an admin with phone `9999999999`.
None of these exist in `prod`; the app refuses to start in `prod` if they are on.

> Upgrading from the old version? The schema is now owned by Flyway. Drop the old dev database
> (`docker compose down -v`) and start fresh.

## Run tests

```bash
./mvnw test
```

## Configuration (environment variables)

See [.env.example](.env.example). In `prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`JWT_SECRET` (32+ chars) and `CORS_ALLOWED_ORIGINS` have no defaults and must be set.
Production also needs an `OtpSender` implementation (SMS provider); without one the app will
not start.

## Code layout

```
com.rentivo.backend
  auth/          OTP login (hashed codes, attempt + rate limits)
  user/          users, admin user management
  category/      categories and dynamic fields (fields are deactivated, never deleted)
  listing/       listings, images, search, moderation
  subscription/  plans, contact unlock (atomic), expiry job
  payment/       payment records (gateway integration: Phase 2)
  media/         image validation + storage abstraction
  security/      JWT filter, security rules, JSON 401/403
  admin/         /api/admin/** controllers
  common/        errors (RFC 7807), paging, phone utils
  config/        typed properties, startup safety checks
```

## API summary

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/send-otp`, `POST /api/auth/verify-otp` |
| Public | `GET /api/categories`, `GET /api/categories/{id}`, `GET /api/subscriptions/plans`, `GET /api/listings/approved` (paged, filters: `categoryId, city, type, minPrice, maxPrice, q, page, size`), `GET /api/listings/{id}` |
| Owner | `POST /api/listings`, `GET /api/listings/mine`, `PUT/DELETE /api/listings/{id}`, `POST /api/listings/{id}/images`, `DELETE /api/listings/{id}/images/{imageId}` |
| Subscriber | `GET /api/subscriptions/status`, `POST /api/subscriptions/contact/{listingId}` |
| Admin | `/api/admin/dashboard`, `/api/admin/users`, `/api/admin/listings`, `/api/admin/categories`, `/api/admin/subscriptions/plans` |

Errors are `application/problem+json`. Validation errors include an `errors` map.

## Roadmap

Phase 1 (this branch): correctness, security, structure. Phase 2: real SMS OTP, Razorpay
payments + webhooks, S3 media, refresh tokens, search, reports/audit log. Phase 3: Docker image,
CI/CD, Terraform/AWS, observability.
