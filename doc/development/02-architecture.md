# 02. Architecture

## Architecture Position

SpellBean keeps the completed Java/XML Android client, Spring Boot backend, PostgreSQL, Redis where configured, and OSS-backed image storage. Runtime data must come from backend APIs; empty results render explicit empty states.

## Client Layers

- `MainActivity`: app shell with retained bottom tabs and fragment host.
- Feature screens: community, upload, search, post detail, commerce, cart, notifications, profile, settings, auth.
- Repositories: Retrofit + Gson API calls only.
- UI adapters: RecyclerView lists and grids.
- Upload transport: backend presign URL, direct PUT, backend confirm.

## Backend Layers

- Auth: email/password register and login only.
- User/profile/follow and persisted settings.
- Upload/OSS object storage.
- Community posts, comments, topics and stickers.
- Commerce products, cart and order creation with manual address snapshots.
- Notifications.

## Provider Boundaries

| Provider | Scope |
|---|---|
| OSS Provider | Local / Aliyun OSS retained for object images and upload flow |

Removed from current runtime architecture:

- Refresh-token API and account cancellation workflow.
- Private messages, conversations, message sending and mutual-follow send limits.
- Reward, checkin and badge modules.
- Report, moderation, admin API and admin bootstrap.
- Address book and order center modules.
- AI/pattern generation modules and provider wiring.
- Payment modules, channel SDK/API, callbacks, refunds and reconciliation.
- Map/location modules and provider wiring.
- Real SMS, production rate limiting/risk control, and full compliance pages.

## Upload Flow

1. Android calls `POST /api/v1/uploads/presign` to receive `uploadUrl` and `fileKey`.
2. Android uploads bytes with `PUT uploadUrl`.
3. Android calls `POST /api/v1/uploads/confirm`.
4. Business APIs store and render the returned `fileId` / URL.

Android must not store OSS keys and must not fake successful uploads.

## Commerce / Order

- Product/category data comes from backend.
- Cart uses backend state and inventory validation.
- Order creation consumes cart item ids or immediate purchase items.
- Order creation requires a manual address snapshot entered by the user.
- The current Android product detail and cart pages display created order status only.
- There is no order center, order detail page, order cancellation UI, address book, default address, or address selection module.
- Payment state is not part of current architecture.

## Explicit Non-goals

- Fake runtime data.
- Kotlin/Compose implementation.
- AI, payment, map/location, real SMS, production risk control, full compliance, admin, report, reward, private-message, address-book, order-center, refresh-token, or account-cancellation surfaces.
