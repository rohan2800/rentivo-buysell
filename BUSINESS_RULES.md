# Business Rules

## Accounts
- Login is by mobile number + OTP. Only verified numbers can post listings.
- A new user's name comes from their first login. Later logins never overwrite it.
- Blocked users lose access immediately (checked on every request). Admin accounts cannot be blocked.

## Listings
- Posting is free. The contact number on a listing must be the poster's own verified login number.
- Every new or edited listing is `PENDING` until an admin approves it. Adding an image to an
  approved listing also sends it back to `PENDING`.
- Statuses: `PENDING` -> `APPROVED` / `REJECTED`; owner can delete (`DELETED`, permanent).
  Rejecting requires a reason, shown to the owner.
- Only `APPROVED` listings are public. Public views never include the phone number or street address.
- Images: JPG, PNG or WEBP (checked by file contents), max 10 MB each, max 10 per listing.

## Categories
- Admins define categories and their extra fields (text, number, decimal, yes/no, date, select,
  multi-select). Values are validated against the field type.
- Removing a field deactivates it; existing listings keep their data.

## Subscriptions and contact unlock
- Buying a plan gives a number of owner contacts valid for a number of days.
- A plan's contact limit and name are copied at purchase: later plan edits do not change what
  an existing subscriber bought.
- Unlocking a listing costs one contact, once per listing per user. Re-opening it is free
  forever, even after the plan expires.
- Unlocking is atomic: the limit can never be exceeded, even with simultaneous requests.
- Cannot unlock your own listing. Only approved listings can be unlocked.
- A user can hold one active subscription at a time (dev activation). Real purchase flow comes with payments in Phase 2.
- Plans are never deleted, only deactivated.
