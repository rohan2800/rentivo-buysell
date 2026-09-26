# Rentivo final business rules

1. Posting is FREE. Users do not pay to create, edit, or delete their own listings.
2. Every post requires a verified mobile contact number. The default contact is the verified login number. Alternate numbers require OTP verification before use.
3. Subscription is only for unlocking owner/provider contact details.
4. The owner/admin controls plan name, price, validity days, contact limit, description, and active/inactive status.
5. A contact unlock consumes one contact only the first time a user unlocks a given listing. Re-opening the same unlocked listing does not consume another contact.
6. A subscription expires at endAt. Remaining contacts from that expired subscription cannot be used.
7. Only approved listings can expose contacts. Owners cannot unlock their own contact.
8. Admin controls listing approval/rejection and can block users.
9. Categories and category-specific fields are dynamic. Adding normal service categories/fields does not require a Java code change.
10. Images are stored locally during development. The storage service is isolated so production object storage (S3/R2/Cloudinary/etc.) can be swapped later without changing listing business logic.
11. The current OTP response returns the development OTP for local testing only. A real SMS provider must be connected before production.
12. Payment activation is currently a development endpoint. A real payment provider and server-side webhook/signature verification must be implemented before production.
