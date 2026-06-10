# 02. Architecture

## Architecture Position

SpellBean currently uses an Android Java/XML client, Spring Boot backend, PostgreSQL, Redis where configured, and OSS-backed image storage. Runtime data must come from backend APIs; empty results render explicit empty states.

## Client Layers

- `MainActivity`: retained app shell, top actions, retained bottom tabs and fragment host.
- Feature fragments/activities: community, commerce, messages, profile, settings, auth, post compose/detail.
- Repositories: Retrofit + Gson API calls only.
- UI adapters: RecyclerView lists and grids.
- Upload transport: backend presign URL, direct PUT, backend confirm.

## Backend Layers

- Auth/session.
- User/profile/follow.
- Upload/OSS object storage.
- Community posts/comments/topics/stickers.
- Commerce products/cart/order/address.
- Messages/notifications.
- Admin/report/reward where retained.

## Provider Boundaries

| Provider | Scope |
|---|---|
| OSS Provider | Local / Stub / Aliyun OSS retained for object images and upload flow |
| Verification code | Fixed development code retained for local login; no real SMS Provider |

Removed from current runtime architecture:

- AI/pattern generation modules and Provider wiring.
- Payment modules, channel SDK/API, callbacks, refunds and reconciliation.
- Map/location modules and Provider wiring.
- Production rate limiting/risk control modules.
- Full compliance document modules/pages.

## Upload Flow

1. Android calls `POST /api/v1/uploads/presign` to receive `uploadUrl` and `fileKey`.
2. Android uploads bytes with `PUT uploadUrl`.
3. Android calls `POST /api/v1/uploads/confirm`.
4. Business APIs store and render the returned `fileId` / URL.

Android must not store OSS keys and must not fake successful uploads.

## Commerce / Order / Address

- Product/category data comes from backend.
- Cart and order confirmation use backend state.
- Address management and manual address fields are retained.
- Map selection and automatic location fill are not current architecture.
- Payment state is not part of current architecture.

## Explicit Non-goals

- Fake runtime data.
- Kotlin/Compose implementation.
- AI, payment, map/location, real SMS, production risk control, or full compliance surfaces.
