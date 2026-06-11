# 05. API Contract

## Scope

Current `/api/v1` contract covers retained flows only:

- Auth register/login.
- User/profile/follow/settings.
- Upload / OSS.
- Community posts/comments/topics/stickers.
- Search.
- Commerce products/cart/order creation.
- Notifications.

Removed contracts: refresh token, account cancellation, private messages, conversations, reward/checkin/badge, report/moderation/admin, address book, order center, AI/pattern, payment, map/location, real SMS, production risk control and full compliance workflows.

## Auth (`/api/v1/auth`)

- `POST /api/v1/auth/register` requires `email`, `password`, `confirmPassword`, optional `nickname`, and `ageGroup`.
- `POST /api/v1/auth/login` requires `email` and `password`.
- Success responses include `accessToken`, `expiresIn`, and `user`.
- Success responses do not include `refreshToken`.
- Token refresh and account cancellation endpoints are not part of the current contract.
- Android logout is local session cleanup.

Each newly registered user receives three `SYSTEM` notifications:

- `欢迎来到豆屿`
- `上传作品提示`
- `商城下单提示`

## Upload (`/api/v1/uploads`)

Object upload uses a presign -> PUT -> confirm flow:

- `POST /api/v1/uploads/presign` returns an upload URL and a file key for the target object.
- `PUT {uploadUrl}` uploads raw bytes directly to the returned URL.
- `POST /api/v1/uploads/confirm` finalizes the upload and returns a `FileAsset` / `fileId`.

OSS-backed image storage and Aliyun OSS configuration are retained. Android never stores OSS credentials.

## User / Profile (`/api/v1/users`)

- `GET /api/v1/users/me` returns the current user's persisted profile fields: `nickname`, resolved `avatarUrl`, `bio`, `region`, age/account metadata and real profile counts.
- `PATCH /api/v1/users/me` updates `nickname`, `bio`, optional `avatarFileId`, and optional manual `region`.
- `avatarFileId` must refer to an uploaded `AVATAR` asset owned by the current user; user-facing responses expose its OSS/public URL through `avatarUrl`.
- `region` is a manual profile text field. Android may offer common city/region choices or free text, but must not call map/location providers or fake automatic location.

## User Settings (`/api/v1/users/me/settings`)

- `GET /api/v1/users/me/settings` requires login and returns `allowRecommendation`, `allowFavorites`, `notifyInteractions`, `notifyPublish`, and `notifySystem`.
- `PATCH /api/v1/users/me/settings` requires login. Request fields are nullable booleans; only non-null fields are updated.
- All settings default to `true` for existing and new users.
- Notification settings only control app reminder preferences. They do not delete notification records or imply a production push provider.

## Community and Search

- `POST /api/v1/posts` requires login and creates a `VISIBLE` post from real input and uploaded `mediaFileIds`.
- Post reads, comments, likes, favorites, topics, topic posts and sticker packs remain retained community APIs.
- Public community reads stay available without login where already supported.
- `GET /api/v1/search?keyword=&type=all|posts|products|users|topics&page=&size=` returns backend-driven retained search results.
- Empty keyword returns an empty result page. Android must not synthesize hot lists, personalized recommendations or local sample results.

## Commerce / Cart / Order Creation

- `GET /api/v1/products`, `GET /api/v1/product-categories`, and `GET /api/v1/products/{productId}` are retained.
- `GET /api/v1/cart`, `POST /api/v1/cart/items`, `PATCH /api/v1/cart/items/{itemId}`, and `DELETE /api/v1/cart/items/{itemId}` are retained.
- `POST /api/v1/orders` creates an order from selected backend cart item ids or immediate purchase lines.
- Order creation requires a real user-entered `addressSnapshot` with `recipient`, `phone`, `region`, and `detail`.
- Responses include backend status `CREATED`, product lines, total/payable amounts and the submitted address snapshot.
- Responses do not include payment result or payment jump data.
- `GET /api/v1/orders`, `GET /api/v1/orders/{orderId}`, and `POST /api/v1/orders/{orderId}/cancel` are not retained.
- Address list/create/edit/delete/default/selection APIs are not retained.

## Notifications (`/api/v1/notifications`)

- `GET /api/v1/notifications?page=&size=` returns the current user's notifications.
- `POST /api/v1/notifications/read` marks unread notifications as read.
- Notifications expose `notificationId`, `type`, `title`, `content`, `unread`, and `createdAt`.
- Notifications do not expose target-object jump metadata.
- Private messages and conversations are not retained.
