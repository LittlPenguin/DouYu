# 04. Backend Services

## Scope

The backend remains a Spring Boot service exposing `/api/v1` APIs for retained Android flows:

- Auth and session.
- User/profile/follow.
- Upload and OSS-backed object image storage.
- Community posts, comments, topics and stickers.
- Commerce products, cart, orders and address management.
- Messages and notifications.
- Admin/report/reward where retained.

## Upload / OSS

OSS-backed image storage is retained.

- `POST /api/v1/uploads/presign`
- `PUT uploadUrl`
- `POST /api/v1/uploads/confirm`
- Local / Aliyun OSS Provider configuration.

Android uses backend-signed upload URLs and never stores OSS credentials.

## Removed Backend Surfaces

The current scope does not include:

- AI/pattern generation controllers, providers, usage/cost controls, content safety or large model integration.
- Payment controllers, channel SDK/API, refunds, reconciliation or production callback handling.
- Map/location providers.
- Real SMS Provider.
- Production rate limiting and production risk-control plumbing.
- Full compliance document endpoints/pages.

## Commerce Purchase Runtime

- Product, SKU, cart, and order data are persisted in the database and returned through `/api/v1`.
- Cart create/update validates product type, product visibility, SKU sale status, and available stock.
- Order creation can consume cart item ids or immediate purchase items, requires an address snapshot, locks SKU stock, and returns a `CREATED` order.
- Canceling a created order releases the locked SKU stock for its order items.
- Payment controllers, payment callbacks, refunds, reconciliation, and channel SDK/API integrations must not be reintroduced for this flow.

## Seed / Demo Policy

- Runtime seed/demo content must not be reintroduced.
- Tests may create explicit fixtures.
- Empty API results are valid and should be preserved for Android empty-state verification.
