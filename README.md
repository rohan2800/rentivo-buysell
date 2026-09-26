# Rentivo Backend — Industry-oriented foundation

Java 21 + Spring Boot + PostgreSQL + Spring Security + JWT.

## Final business model
- Posting is free.
- A verified mobile number is mandatory for every post.
- The user's verified login phone is the default listing contact.
- Subscription is only for unlocking owner/provider contact numbers.
- Admin controls plan price, validity, contact limit, and active/inactive state.
- A unique listing contact is counted once per subscription; re-opening the same listing does not consume another contact.
- Expired plans cannot use remaining contacts.
- Admin approves/rejects listings.
- Categories and category-specific fields are dynamic.
- Images are stored locally during development through `StorageService`; an object-storage implementation can be added later without changing listing logic.

## Development
1. Create PostgreSQL database `rentivo`.
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and a strong `JWT_SECRET` (32+ chars).
3. Run with IntelliJ or Maven.
4. Dev OTP is `123456` by default. Do not expose this in production.
5. Seeded development owner/admin phone: `9999999999`. Login through OTP to receive an ADMIN JWT.
6. `/api/subscriptions/activate-dev` is only a development activation shortcut. Replace it with a payment-order + webhook flow before production.

## Important production work before launch
- Connect a real SMS/OTP provider.
- Replace development subscription activation with Razorpay/other provider order creation and server-side signature/webhook verification.
- Move image storage to persistent object storage (S3/R2/Cloudinary/etc.) when deployed.
- Add rate limiting, audit logs, refresh tokens/device sessions, moderation/reporting, observability, backups, and automated integration tests.

# buysell
