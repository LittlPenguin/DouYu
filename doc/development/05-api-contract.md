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

## User / Profile (`/api/v1/users`)

- `GET /api/v1/users/me` returns the current user's persisted profile fields: `nickname`, resolved `avatarUrl`, `bio`, `region`, age/account metadata and real profile counts.
- `PATCH /api/v1/users/me` updates `nickname`, `bio`, optional `avatarFileId`, and optional manual `region`.
- `avatarFileId` must refer to an uploaded `AVATAR` asset owned by the current user; user-facing responses expose its OSS/public URL through `avatarUrl`, not the raw file id.
- `region` is a manual profile text field. Android may offer common city/region choices or free text, but must not call map/location providers or fake automatic location.
- Profile edit does not save age group or interest tags.

## User Settings (`/api/v1/users/me/settings`)

Settings are persisted by the backend and returned through both `GET /api/v1/users/me` and the dedicated settings endpoint.

- `GET /api/v1/users/me/settings` requires login and returns `allowRecommendation`, `allowStrangerMessages`, `allowFavorites`, `notifyMessages`, `notifyInteractions`, `notifyPublish`, and `notifySystem`.
- `PATCH /api/v1/users/me/settings` requires login. Request fields are nullable booleans; only non-null fields are updated.
- All settings default to `true` for existing and new users.
- Notification settings only control app reminder preferences. They do not delete notification records, change private-message mutual-follow rules, or imply a production push provider.
- Privacy settings are persisted server-side. Android must not show saved success until the backend returns success.

`POST /api/v1/auth/account/cancel` remains the account cancellation request endpoint. A successful request changes `accountStatus` to `CANCELING`; it is not a full compliance deletion workflow.

## Community (`/api/v1`)

Retained community post creation uses the existing backend endpoint:

- `POST /api/v1/posts` requires login.
- Request body: `title`, `content`, `mediaFileIds`, `topicIds`.
- `content` is required by the backend; Android also requires a non-empty title before enabling publish.
- `mediaFileIds` must come from the retained OSS upload flow; Android must not submit fake file ids.
- Successful creation returns a `Post` with backend status `VISIBLE`; new posts are public immediately after a successful upload-backed publish.
- Post responses include `mediaFileIds`, first-image `coverImageUrl` / dimensions, and `imageUrls` containing all available uploaded image public URLs in `mediaFileIds` order. Android detail uses `imageUrls` for multi-image galleries and falls back to `coverImageUrl` only when `imageUrls` is empty.
- A newly created post is not guaranteed to appear in public feed immediately.

Other retained community reads and interactions include feed, topics, topic posts, post detail, comments, likes and favorites. Removed AI/pattern endpoints are not part of this contract.

Public community reads stay available without login: post detail and comment list may be loaded anonymously, and anonymous post views return interaction counts with `likedByMe`, `favoritedByMe` and `followedAuthorByMe` as `false`. Mutating community interactions, including creating comments, liking, favoriting and following authors, remain login-required and must not be simulated by Android before a successful backend response.

Search reads are backend-driven and public unless an endpoint already requires login for its underlying feature:

- `GET /api/v1/search?keyword=&type=all|posts|products|users|topics&page=&size=` returns grouped search results for the retained searchable surfaces.
- `type=all` returns a bounded mixed list with `resultType` values `POST`, `PRODUCT`, `USER`, and `TOPIC`.
- `type=posts` searches visible community posts by title, content, and topic names and returns post views that can open post detail.
- `type=products` searches on-sale, audit-passed products by title, name, description, category, and type and returns product views that can open product detail.
- `type=users` delegates to the retained user search contract and returns public user profile summaries.
- `type=topics` delegates to the retained topic list query and returns topic summaries.
- Empty keyword returns an empty result page. Android must not synthesize hot lists, personalized recommendations, or local sample results.
- Search does not add AI, pattern-generation, payment, map/location, SMS, or production recommendation-provider contracts.

## 商城 / 订单 / 地址

- 商品分类和商品列表由后端驱动。
- 购物车只适用于保留的自营商品链路。
- 购物车商品创建、更新、删除调用必须由后端处理，并校验 SKU 状态、自营商品类型和可用库存。
- 创建订单由后端控制，支持两类保留输入：选中的后端购物车商品 ID，或携带 `skuId` 与 `quantity` 的立即购买商品。
- 创建订单必须携带来自真实用户输入或保留地址记录的地址快照。Android 不得提交假地址 ID 或静态默认地址来绕过缺失的地址管理。
- 已创建订单返回后端状态 `CREATED`、商品行、总金额/应付金额字段和提交的地址快照。响应中不包含支付结果或支付跳转。
- 取消 `CREATED` 订单由后端控制，并释放已锁定 SKU 库存。
- 地址管理保留：地址列表、新建、编辑、删除、默认地址和订单地址选择。
- 商品详情、购物车、数量、收货信息和订单创建相关 UI 文案必须使用自然中文；API 字段名和后端枚举值保持当前契约。
- 地图/定位 API 不属于当前契约。
- 支付 API 不属于当前契约。

## Removed Contract Areas

Do not document or add current contracts for:

- AI/pattern job creation, visual Provider, content safety or cost controls.
- Payment, payment callbacks, refunds, reconciliation, WeChat or Alipay SDK/API.
- Map/location Provider.
- Real SMS Provider.
- Production rate limiting/risk-control contracts.
- Full compliance document workflows.
