# 04. Backend Services

## Scope

The backend exposes `/api/v1` APIs for retained Android flows:

- Email/password register and login.
- User/profile/follow and settings.
- Upload and OSS-backed object image storage.
- Community posts, comments, topics and stickers.
- Commerce products, cart and order creation.
- Notifications.

## Auth

- `POST /api/v1/auth/register` creates a user and returns an access token.
- `POST /api/v1/auth/login` returns an access token.
- The backend no longer exposes refresh-token or account-cancellation endpoints.
- Android logout is local session cleanup.

New registrations create three default `SYSTEM` notifications for the user: welcome, upload guidance and commerce/order guidance.

## Upload / OSS

OSS-backed image storage is retained.

- `POST /api/v1/uploads/presign`
- `PUT uploadUrl`
- `POST /api/v1/uploads/confirm`
- Local / Aliyun OSS Provider configuration.

Android uses backend-signed upload URLs and never stores OSS credentials.

## Notifications

- `GET /api/v1/notifications` lists the current user's notifications.
- `POST /api/v1/notifications/read` marks unread notifications as read.
- Notifications are not private messages and do not link to target objects.

## Commerce Purchase Runtime

- Product, SKU, cart and created-order data are persisted in the database and returned through `/api/v1`.
- Cart create/update validates product type, product visibility, SKU sale status and available stock.
- Order creation can consume cart item ids or immediate purchase items, requires an address snapshot, locks SKU stock and returns a `CREATED` order.
- Order list/detail/cancel APIs are removed from current scope.
- Payment controllers, payment callbacks, refunds, reconciliation and channel SDK/API integrations must not be reintroduced.

## Removed Backend Surfaces

The current scope does not include:

- Refresh-token API or account cancellation.
- Private messages, conversations or message sending.
- Reward/checkin/badge APIs.
- Report/moderation/admin APIs and admin bootstrap.
- Address book, default address or address selection APIs.
- Order list, order detail or order cancellation APIs.
- AI/pattern generation controllers, providers, usage/cost controls, content safety or large model integration.
- Payment controllers, channel SDK/API, refunds, reconciliation or production callback handling.
- Map/location providers.
- Real SMS provider.
- Production rate limiting and production risk-control plumbing.
- Full compliance document endpoints/pages.

## Seed / Demo Policy

- Runtime seed/demo content must not be reintroduced.
- New user default notifications are product onboarding records, not demo content.
- Tests may create explicit fixtures.
- Empty API results are valid and should be preserved for Android empty-state verification.
