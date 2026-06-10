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
- Local / Stub / Aliyun OSS Provider configuration.

Android uses backend-signed upload URLs and never stores OSS credentials.

## Removed Backend Surfaces

The current scope does not include:

- AI/pattern generation controllers, providers, usage/cost controls, content safety or large model integration.
- Payment controllers, channel SDK/API, refunds, reconciliation or production callback handling.
- Map/location providers.
- Real SMS Provider.
- Production rate limiting and production risk-control plumbing.
- Full compliance document endpoints/pages.

## Seed / Demo Policy

- Runtime seed/demo content must not be reintroduced.
- Tests may create explicit fixtures.
- Empty API results are valid and should be preserved for Android empty-state verification.
