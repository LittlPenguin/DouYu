# 05. API 契约

## 契约来源

本文件按当前真实代码重写：

- 后端来源：`doyu-server/src/main/java/cn/edu/app/douyu/server/**Controller.java`。
- Android 来源：`DouYu/app/src/main/java/cn/edu/app/douyu/core/network/ApiInterfaces.kt` 与 `Models.kt`。

本文件是文档重构，不代表接口变更。本轮不新增、不删除、不修改任何后端 API，不修改 Android DTO 或 Retrofit 接口。

## 全局规范

| 项 | 规则 |
|---|---|
| API 前缀 | `/api/v1` |
| 响应包裹 | `{ code, message, data, traceId }` |
| 分页入参 | `page` 从 1 开始，`size` 默认 20 |
| 分页响应 | `items`、`page`、`size`、`total`、`hasMore` |
| 鉴权 | 除公开读取、登录、回调、后台登录等白名单外，默认需要 `Authorization: Bearer <accessToken>` |
| 幂等 | 创建订单、创建支付单、退款等写接口使用 `Idempotency-Key` Header |
| 对外 ID | 使用字符串业务 ID，例如 `postId`、`fileId`、`orderId` |

通用错误码：

| code | HTTP | 含义 |
|---|---:|---|
| `OK` | 200 | 成功 |
| `INVALID_ARGUMENT` | 400 | 参数错误、文件类型不支持、评论内容不合法 |
| `UNAUTHORIZED` | 401 | 未登录、Token 无效 |
| `FORBIDDEN` | 403 | 无权访问、非本人资源、未实名等 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `CONFLICT` | 409 | 状态冲突 |
| `RATE_LIMITED` | 429 | 限流 |
| `AUDIT_REJECTED` | 409 | 审核拒绝 |
| `PAYMENT_FAILED` | 409 | 支付失败 |
| `INVENTORY_NOT_ENOUGH` | 409 | 库存不足 |
| `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED` | 409 | 未互关私信超过 3 条 |
| `AI_TASK_FAILED` | 409 | AI 任务失败 |
| `INTERNAL_ERROR` | 500 | 服务端错误 |

## Auth

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/sms-code` | 否 | 是 | 是 | 发送短信验证码，开发环境固定 `123456` |
| POST | `/api/v1/auth/login/sms` | 否 | 是 | 是 | 短信登录 |
| POST | `/api/v1/auth/refresh` | 否 | 是 | 是 | refreshToken 换新 token |
| POST | `/api/v1/auth/logout` | 是 | 是 | 是 | 退出并吊销 refreshToken |
| POST | `/api/v1/auth/account/cancel` | 是 | 是 | 否 | 账号注销申请 |

### 请求字段

| 接口 | 请求字段 |
|---|---|
| `/sms-code` | `phone` |
| `/login/sms` | `phone`、`code`、`ageGroup`、`nickname?` |
| `/refresh` | `refreshToken` |
| `/logout` | `refreshToken` |

登录请求当前仍保留 `ageGroup=AGE_18_PLUS`，Android `SmsLoginRequest` 默认值也是 `AGE_18_PLUS`。不得把该字段写成已经移除。

### 响应字段

| 接口 | data |
|---|---|
| `/sms-code` | `sent`、`expiresIn` |
| `/login/sms` | `accessToken`、`refreshToken`、`expiresIn`、`user` |
| `/refresh` | `accessToken`、`refreshToken`、`expiresIn` |
| `/logout` | `loggedOut` |
| `/account/cancel` | 后端返回注销申请状态 |

Android 依赖：登录页、TokenStore、启动 hydrate、401 清理和登录引导。

边界：当前短信为 Stub；未接真实短信 Provider、限流和风控闭环。

## User

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| GET | `/api/v1/users/me` | 是 | 是 | 是 | 当前用户资料 |
| GET | `/api/v1/users/search` | 否 | 是 | 是 | 用户搜索，当前主要给 @ 用户选择使用 |
| GET | `/api/v1/users/me/liked-posts` | 是 | 是 | 是 | 我点赞过的作品 |
| GET | `/api/v1/users/me/commented-posts` | 是 | 是 | 是 | 我评论过的作品 |
| GET | `/api/v1/users/me/favorite-posts` | 是 | 是 | 是 | 我收藏过的作品 |
| GET | `/api/v1/users/me/followed-posts` | 是 | 是 | 是 | 我关注作者的作品 |
| PATCH | `/api/v1/users/me` | 是 | 是 | 否 | 更新资料；Profile Edit 后续可接 |
| GET | `/api/v1/users/{userId}` | 否 | 是 | 否 | 用户公开资料 |
| POST | `/api/v1/users/{userId}/follow` | 是 | 是 | 是 | 关注用户 |
| DELETE | `/api/v1/users/{userId}/follow` | 是 | 是 | 是 | 取消关注 |
| POST | `/api/v1/users/real-name` | 是 | 是 | 否 | 实名提交骨架 |

### 字段

`UserProfile` 当前 Android 字段：

- `userId`
- `nickname`
- `avatarUrl?`
- `bio`
- `level`
- `isMinor`
- `followingCount`
- `followerCount`

`PATCH /me` 请求字段：

- `nickname?`
- `avatarFileId?`
- `bio?`

`FollowResult` 响应字段：

- `followed`
- `followedByMe`
- `followsMe`
- `mutualFollow`

Android 页面依赖：

- 我的页资料展示。
- 评论 @ 用户选择。
- 关注/取关按钮。
- 互关私信限制展示。
- 当前代码已有四个独立个人互动作品页；最新设计目标只在我的页首屏展示 `我的图纸 / 点赞作品 / 收藏作品` 同组 Tab。

边界：

- `users/search` 不是全局搜索 API。
- Profile Edit 是 UI 目标；Android 需后续接 `PATCH /me`。
- 城市/地区资料仍是 UI-only，未接地图 API。

## Upload

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/uploads/presign` | 是 | 是 | 是 | 获取上传 URL |
| POST | `/api/v1/uploads/confirm` | 是 | 是 | 是 | 确认上传并生成 `fileId` |

### 请求字段

`PresignRequest`：

- `usage`
- `mimeType`
- `sizeBytes`
- `fileName`

`ConfirmRequest`：

- `fileKey`
- `usage`
- `mimeType`
- `sizeBytes`
- `width?`
- `height?`

支持用途：

- `AVATAR`
- `POST_IMAGE`
- `POST_VIDEO`
- `AI_INPUT`
- `PATTERN_OUTPUT`
- `PRODUCT_IMAGE`
- `TRADE_IMAGE`

响应字段：

- `uploadUrl`
- `fileKey`
- `headers`
- `expiresIn`
- `fileId`
- `ownerId`
- `storageKey`
- `auditStatus`
- `publicUrl`

边界：

- 当前大小上限 20MB。
- 评论图片必须使用 `POST_IMAGE` 且 MIME 为 image。
- Aliyun OSS Provider 是后端切换骨架，不代表生产对象存储已完成。
- 客户端不得持有 OSS Secret。

## Community

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| GET | `/api/v1/posts/feed` | 否 | 是 | 是 | 推荐 Feed |
| GET | `/api/v1/posts/following` | 是 | 是 | 是 | 关注 Feed |
| POST | `/api/v1/posts` | 是 | 是 | 是 | 发布帖子，进入审核 |
| GET | `/api/v1/posts/{postId}` | 可选 | 是 | 是 | 作品详情 |
| PATCH | `/api/v1/posts/{postId}` | 是 | 是 | 否 | 编辑帖子 |
| DELETE | `/api/v1/posts/{postId}` | 是 | 是 | 否 | 删除帖子 |
| POST | `/api/v1/posts/{postId}/like` | 是 | 是 | 是 | 点赞 |
| DELETE | `/api/v1/posts/{postId}/like` | 是 | 是 | 是 | 取消点赞 |
| POST | `/api/v1/posts/{postId}/favorite` | 是 | 是 | 是 | 收藏 |
| DELETE | `/api/v1/posts/{postId}/favorite` | 是 | 是 | 是 | 取消收藏 |
| GET | `/api/v1/posts/{postId}/comments` | 否 | 是 | 是 | 评论列表 |
| POST | `/api/v1/posts/{postId}/comments` | 是 | 是 | 是 | 发表评论 |
| DELETE | `/api/v1/comments/{commentId}` | 是 | 是 | 否 | 删除评论 |
| GET | `/api/v1/topics` | 否 | 是 | 是 | 话题列表 / 话题搜索 |
| GET | `/api/v1/topics/{topicId}/posts` | 否 | 是 | 是 | 话题作品列表 |
| GET | `/api/v1/sticker-packs` | 否 | 是 | 是 | 内置贴纸包 |

### 帖子字段

`Post` 关键字段：

- `postId`
- `authorId`
- `author`
- `title`
- `content`
- `mediaFileIds`
- `coverImageUrl`
- `topicIds`
- `topicNames`
- `linkedPatternId?`
- `status`
- `likeCount`
- `favoriteCount`
- `commentCount`
- `likedByMe`
- `favoritedByMe`
- `followedAuthorByMe`

`CreatePostRequest`：

- `title`
- `content`
- `mediaFileIds`
- `topicIds`
- `linkedPatternId?`

### 评论字段

`CreateCommentRequest`：

- `content`
- `parentId?`
- `mediaFileIds`
- `mentionUserIds`
- `topicIds`
- `stickerIds`

`Comment` 响应：

- `commentId`
- `postId`
- `author`
- `parentId?`
- `content`
- `status`
- `mediaFileIds`
- `mediaAssets`
- `mentions`
- `topics`
- `stickers`

评论规则：

- 文本、图片、贴纸至少存在一种。
- @ 用户和 # 话题不能单独提交。
- 单条评论最多 9 张图片。
- 图片必须归当前用户所有，`usage=POST_IMAGE`，MIME 为 image。
- 话题、贴纸和提及用户必须存在。
- 评论提交后为 `REVIEWING`。

Android 页面依赖：

- 社区瀑布流。
- 作品详情图片优先布局。
- 评论区、悬浮评论栏、@/# 面板、贴纸面板、图片上传状态。

边界：

- 当前不做视频评论。
- 当前不做用户自定义贴纸。
- 生产级内容审核和图片审核未完成。

## Pattern

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/patterns/jobs` | 是 | 是 | 是 | 创建 AI 拼豆任务 |
| GET | `/api/v1/patterns/jobs/{jobId}` | 是 | 是 | 是 | 任务详情 |
| GET | `/api/v1/patterns/jobs` | 是 | 是 | 是 | 任务列表 |
| POST | `/api/v1/patterns/jobs/{jobId}/cancel` | 是 | 是 | 是 | 取消任务 |
| POST | `/api/v1/patterns/{patternId}/favorite` | 是 | 是 | 是 | 收藏图纸 |
| GET | `/api/v1/patterns/{patternId}` | 是 | 是 | 是 | 图纸详情 |
| GET | `/api/v1/patterns/quota` | 是 | 是 | 否 | AI 配额 |
| GET | `/api/v1/patterns/favorites` | 是 | 否 | 是 | Android Retrofit 当前声明收藏列表；需后续核对后端是否补齐 |

### 请求字段

`CreateJobRequest`：

- `inputFileId`
- `beadSize`
- `targetSize`
- `difficulty`
- `paletteId`
- `style`

后端当前校验：

- `beadSize` 支持 `MM_2_6`、`MM_5`。
- `inputFileId` 必须存在且归当前用户所有。

`PatternJob` 字段：

- `jobId`
- `inputFileId`
- `beadSize`
- `targetSize`
- `difficulty`
- `paletteId`
- `style`
- `status`
- `progress`
- `failureReason?`
- `patternId?`

边界：

- 真实视觉 Provider 未接入。
- 大模型生图 API 未接入。
- AI 质量不能包装成生产可用。

## Product / Cart

### Product 接口

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| GET | `/api/v1/products` | 否 | 是 | 是 | 商品列表 |
| GET | `/api/v1/products/{productId}` | 否 | 是 | 是 | 商品详情 |
| POST | `/api/v1/products` | 是 | 是 | 否 | 玩家商品发布骨架 |

`Product` 字段：

- `productId`
- `type`
- `sellerId?`
- `title`
- `description`
- `categoryId`
- `categoryName`
- `status`
- `auditStatus`
- `skus`
- `imageUrl?`
- `swatchColor`

`ProductType`：

- `SELF_OPERATED`
- `PLAYER_SECOND_HAND`
- `PLAYER_CUSTOM_SERVICE`

### Cart 接口

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| GET | `/api/v1/cart` | 是 | 是 | 是 | 购物车 |
| POST | `/api/v1/cart/items` | 是 | 是 | 是 | 加入购物车 |
| PATCH | `/api/v1/cart/items/{itemId}` | 是 | 是 | 是 | 修改数量 |
| DELETE | `/api/v1/cart/items/{itemId}` | 是 | 是 | 是 | 删除购物车项 |

购物车写请求：

- `skuId`
- `quantity`

边界：

- 玩家二手和玩家定制商品不支持标准购物车。
- 库存、价格和可购买状态以服务端为准。

## Order

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/orders` | 是 | 是 | 是 | 创建订单，需 `Idempotency-Key` |
| GET | `/api/v1/orders` | 是 | 是 | 是 | 订单列表 |
| GET | `/api/v1/orders/{orderId}` | 是 | 是 | 是 | 订单详情 |
| POST | `/api/v1/orders/{orderId}/cancel` | 是 | 是 | 是 | 取消订单 |

`CreateOrderRequest`：

- `itemIds`
- `addressId`
- `remark?`

`Order` 字段：

- `orderId`
- `buyerId`
- `sellerType`
- `sellerId?`
- `orderType`
- `status`
- `totalAmountCent`
- `payableAmountCent`
- `items`
- `addressSnapshot`

边界：

- 地址管理未闭环；UI 不得伪造默认地址。
- 玩家商品不支持标准订单。
- 创建订单要防重；Android 必须提供 `Idempotency-Key`。

## Payment

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/payments` | 是 | 是 | 是 | 创建支付单，需 `Idempotency-Key` |
| GET | `/api/v1/payments/{paymentId}` | 是 | 是 | 是 | 查询支付状态 |
| POST | `/api/v1/payments/callbacks/wechat` | 否 | 是 | 否 | 微信回调骨架 |
| POST | `/api/v1/payments/callbacks/alipay` | 否 | 是 | 否 | 支付宝回调骨架 |
| POST | `/api/v1/refunds` | 是 | 是 | 否 | 退款骨架 |

`CreatePaymentRequest`：

- `orderId`
- `channel`

`Payment` 字段：

- `paymentId`
- `orderId`
- `channel`
- `status`
- `amountCent`
- `payParams`
- `channelTradeNo?`
- `paidAt?`

支付渠道：

- `WECHAT_APP`
- `ALIPAY_APP`

当前 `payParams` 明确是 Stub，例如 `provider=STUB`。不得写成真实微信/支付宝 App 支付已接入。

## Message

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| GET | `/api/v1/messages/notifications` | 是 | 是 | 是 | 通知列表 |
| POST | `/api/v1/messages/notifications/read` | 是 | 是 | 是 | 标记通知已读；后端当前标记全部已读 |
| GET | `/api/v1/messages/conversations` | 是 | 是 | 是 | 会话列表 |
| GET | `/api/v1/messages/conversations/{conversationId}` | 是 | 是 | 是 | 会话详情 |
| POST | `/api/v1/messages/conversations/{conversationId}` | 是 | 是 | 是 | 发送私信 |

`Conversation` 字段：

- `conversationId`
- `peerUserId`
- `peerName`
- `peerAvatarUrl?`
- `lastMessage`
- `unreadCount`
- `mutualFollow`
- `remainingNonMutualMessages`
- `canSend`
- `riskHint?`
- `updatedAt?`

`SendMessageRequest`：

- `content`

互关规则：

- 互相关注可正常聊天。
- 未互关同一发送者对同一会话最多 3 条。
- 超限返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`。
- Android 必须基于 `canSend` 和错误码禁用输入或展示明确提示。

边界：

- 当前不做好友申请审批。
- 当前不做图片私信、撤回、复杂已读回执。

## Reward

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/checkins` | 是 | 是 | 是 | 每日签到 |
| GET | `/api/v1/checkins/status` | 是 | 是 | 是 | 签到状态 |
| GET | `/api/v1/rewards/me` | 是 | 是 | 是 | 积分和等级 |
| GET | `/api/v1/badges/me` | 是 | 是 | 是 | 徽章 |

边界：

- 成长系统是基础骨架，不等于完整游戏化体系。
- 我的页最新设计不要求首屏展示全部成长入口。

## Report

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/reports` | 是 | 是 | 否 | 提交举报 |

`ReportRequest`：

- `targetType`：`POST`、`COMMENT`、`USER`
- `targetId`
- `reason`
- `description?`

边界：Android 举报入口未接入完整流程；生产举报处理和后台工作台仍需补齐。

## Admin

### 接口表

| 方法 | 路径 | 鉴权 | 后端 | Android | 说明 |
|---|---|---|---|---|---|
| POST | `/api/v1/admin/auth/login` | 否 | 是 | 否 | 管理员登录 |
| GET | `/api/v1/admin/users` | 管理员 | 是 | 否 | 用户列表 |
| GET | `/api/v1/admin/posts` | 管理员 | 是 | 否 | 帖子列表 |
| GET | `/api/v1/admin/comments` | 管理员 | 是 | 否 | 评论列表 |
| GET | `/api/v1/admin/products` | 管理员 | 是 | 否 | 商品列表 |
| GET | `/api/v1/admin/orders` | 管理员 | 是 | 否 | 订单列表 |
| GET | `/api/v1/admin/payments` | 管理员 | 是 | 否 | 支付列表 |
| GET | `/api/v1/admin/patterns/jobs` | 管理员 | 是 | 否 | AI 任务列表 |
| GET | `/api/v1/admin/reports` | 管理员 | 是 | 否 | 举报列表 |
| POST | `/api/v1/admin/reports/{reportId}/process` | 管理员 | 是 | 否 | 处理举报 |
| POST | `/api/v1/admin/posts/{postId}/audit` | 管理员 | 是 | 否 | 审核帖子 |
| POST | `/api/v1/admin/comments/{commentId}/audit` | 管理员 | 是 | 否 | 审核评论 |
| POST | `/api/v1/admin/products/{productId}/audit` | 管理员 | 是 | 否 | 审核商品 |
| POST | `/api/v1/admin/users/{userId}/status` | 管理员 | 是 | 否 | 变更用户状态 |
| POST | `/api/v1/admin/patterns/jobs/{jobId}/retry` | 管理员 | 是 | 否 | 重试 AI 任务 |
| POST | `/api/v1/admin/patterns/jobs/{jobId}/cancel` | 管理员 | 是 | 否 | 取消 AI 任务 |
| GET | `/api/v1/admin/operation-logs` | 管理员 | 是 | 否 | 操作日志 |

边界：后台 API 存在不代表运营后台前端、权限分级、生产审核和合规流程已完成。

## 明确不存在的公共接口

当前不得在文档或 UI 中写成已接入：

- 地图 API。
- 真实微信/支付宝支付 API。
- 大模型生图 API。
- 全局搜索 API。
- 完整地址管理 API。
- 玩家交易担保、评价、纠纷、提现闭环 API。

如果后续确实要新增这些能力，必须先更新本文件，再同步后端 Controller、Android Retrofit、DTO、Repository、测试和验收文档。
