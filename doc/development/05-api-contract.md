# 05. API Contract

## Scope

Current `/api/v1` contract covers retained flows only:

- Auth/session.
- User/profile/follow.
- Upload / OSS.
- Community posts/comments/topics/stickers.
- Commerce products/cart/orders/address.
- Messages/notifications.
- Admin/report/reward where retained.

## Upload (`/api/v1/uploads`)

Object upload uses a presign -> PUT -> confirm flow:

- `POST /api/v1/uploads/presign` returns an upload URL and a file key for the target object. Request carries upload purpose/scene, MIME type, byte size, file name and optional image dimensions.
- `PUT {uploadUrl}` uploads raw bytes directly to the returned URL.
- `POST /api/v1/uploads/confirm` finalizes the upload and returns a `FileAsset` / `fileId`.

OSS-backed image storage and Aliyun OSS configuration are retained. Android never stores OSS credentials.

## Community (`/api/v1`)

Retained community post creation uses the existing backend endpoint:

- `POST /api/v1/posts` requires login.
- Request body: `title`, `content`, `mediaFileIds`, `topicIds`.
- `content` is required by the backend; Android also requires a non-empty title before enabling publish.
- `mediaFileIds` must come from the retained OSS upload flow; Android must not submit fake file ids.
- Successful creation returns a `Post` with backend status `VISIBLE`; new posts are public immediately after a successful upload-backed publish.
- A newly created post is not guaranteed to appear in public feed immediately.

Other retained community reads and interactions include feed, topics, topic posts, post detail, comments, likes and favorites. Removed AI/pattern endpoints are not part of this contract.

## Commerce / Order / Address

- Product categories and product lists are backend-driven.
- Cart applies to retained self-operated product flows.
- Order confirmation and order states are backend-controlled.
- Address management is retained: list, create, edit, delete, default address and order address selection.
- Map/location API is not part of the current contract.
- Payment API is not part of the current contract.

## Removed Contract Areas

Do not document or add current contracts for:

- AI/pattern job creation, visual Provider, content safety or cost controls.
- Payment, payment callbacks, refunds, reconciliation, WeChat or Alipay SDK/API.
- Map/location Provider.
- Real SMS Provider.
- Production rate limiting/risk-control contracts.
- Full compliance document workflows.
