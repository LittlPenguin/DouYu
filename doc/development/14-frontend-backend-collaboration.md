# Frontend Backend Collaboration

## Current API Base

Android calls retained backend APIs under `/api/v1` through Retrofit + Gson.

## Retained Integration Points

- Auth: register/login.
- User/profile/follow/settings.
- Upload: presign, direct PUT, confirm.
- Community: posts, comments, topics, stickers, likes, favorites.
- Search: posts, products, users, topics.
- Commerce: products, categories, cart, create order.
- Notifications: list and mark read.

## Removed Integration Points

Do not add Android calls or backend controllers for:

- Token refresh or account cancellation.
- Private-message or conversation APIs.
- Checkin, reward or badge APIs.
- Report, moderation or admin APIs.
- address book APIs
- order list/detail/cancel APIs
- AI, payment, map/location, SMS or full compliance APIs.

## Local Logout

Android logout is local session cleanup:

1. User taps logout.
2. Android shows confirmation.
3. Android clears `SessionStore`.
4. Protected screens return to login boundaries.

No server logout or refresh-token revoke call is expected.

## Notifications

New registration must create three default `SYSTEM` notifications. The messages tab lists notifications only and opens notification detail without object jumps.

## Orders

Order creation requires user-entered address snapshot fields. Android must not send `addressId` and must not provide order-center navigation.

## Verification

When changing either side, update API contracts, run the focused tests first, then run the full Android/backend gates listed in `10-testing-acceptance.md`.
