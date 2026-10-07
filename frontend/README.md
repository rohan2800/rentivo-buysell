# Rentivo Frontend

React web app for the [Rentivo backend](https://github.com/rohan2800/rentivo-buysell) — browse,
post, favorite, get notified, and unlock seller contacts.

**Stack:** React 19, TypeScript, Vite, Tailwind CSS v4, React Router.

## Run locally

The backend must be running first (see its README). Then:

```bash
npm install
cp .env.example .env.development   # already present with a sensible default
npm run dev
```

Opens on `http://localhost:5173`. Set `VITE_API_BASE_URL` in `.env.development` if your backend
runs on a different port than `8081`.

**Backend CORS:** the backend only allows origins it's told about. Make sure it's running with
`CORS_ALLOWED_ORIGINS` including `http://localhost:5173` — the backend's `dev` profile already
does this by default.

## Build

```bash
npm run build    # type-checks with tsc, then bundles with Vite
npm run lint      # oxlint
```

## Structure

```
src/
  lib/
    types.ts     TypeScript types mirroring every backend DTO — kept in sync by hand
    api.ts        Typed fetch client, one method per backend endpoint
    auth.tsx      Auth context: JWT in localStorage, phone+OTP login
    format.ts     Price/date formatting helpers
  components/     Shared UI: Layout, ListingCard, StatusBadge, Pagination, CategoryTabs...
  pages/          One file per route (see App.tsx for the route list)
```

## Pages / flows covered

- Browse (search, category, city, type filters, pagination) and listing detail with image
  gallery and dynamic category fields
- Phone + OTP login (shows the dev OTP inline when the backend's dev mode returns one)
- Post / edit a listing, including the category's dynamic fields and photo upload
- My listings: edit, delete, renew an approved/expired listing
- Favorites: save and browse saved listings
- Notifications: list, mark read, mark all read, with an unread badge in the header
- Plans: view plans and current subscription status; activation calls the backend's dev-only
  `activate-dev` endpoint and shows a friendly message if that's disabled (real payments aren't
  built yet — see the backend's Phase 2 roadmap)
- **Admin panel** (`/admin/*`, only visible/reachable with an `ADMIN`-role account):
  dashboard stats, listing moderation (approve/reject with a required reason), user
  block/unblock, category management (including the dynamic field editor), subscription plan
  management, and a read-only audit log of every admin action

## Known gaps (next steps)

- No real payment flow yet (depends on the backend's Razorpay integration, not yet built)
- No image cropping — reordering and delete are supported, cropping is not
- Mobile app (React Native) not started; this is web-first per the agreed scope
- The "can't reach the server" banner (`lib/connectivity.ts`) only detects total network
  failure (backend down, wrong URL, CORS block) — it doesn't distinguish those cases from
  each other, since the browser's fetch error looks the same for all three
