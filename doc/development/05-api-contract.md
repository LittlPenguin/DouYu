# 05. API 契约

## 基础规范

- API 前缀：`/api/v1`。
- 数据格式：JSON。
- 字符编码：UTF-8。
- 鉴权：`Authorization: Bearer <access_token>`。
- 幂等请求头：`Idempotency-Key`，用于订单、支付、退款、重要提交。
- 追踪请求头：客户端可传 `X-Request-Id`，服务端返回 `traceId`。

## 统一响应

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "traceId": "trace_20260511_000001"
}
```

分页响应：

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 100,
  "hasMore": true
}
```

## 通用错误码

| code | 含义 |
|---|---|
| OK | 成功 |
| INVALID_ARGUMENT | 参数错误 |
| UNAUTHORIZED | 未登录或 token 失效 |
| FORBIDDEN | 无权限 |
| NOT_FOUND | 资源不存在 |
| CONFLICT | 状态冲突或重复提交 |
| RATE_LIMITED | 请求过于频繁 |
| AUDIT_REJECTED | 内容不符合要求 |
| PAYMENT_FAILED | 支付失败 |
| INVENTORY_NOT_ENOUGH | 库存不足 |
| AI_TASK_FAILED | AI 任务失败 |
| INTERNAL_ERROR | 服务端错误 |

## 认证接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/sms-code` | 发送验证码 |
| POST | `/auth/login/sms` | 手机号验证码登录 |
| POST | `/auth/refresh` | 刷新 token |
| POST | `/auth/logout` | 退出登录 |
| POST | `/auth/account/cancel` | 申请注销账号 |

登录请求字段：

- `phone`：手机号。
- `code`：验证码，Stub 环境固定 `123456`。
- `ageGroup`：当前后端仍要求该字段，只接受 `AGE_16_17` 或 `AGE_18_PLUS`；Android 现阶段默认传 `AGE_18_PLUS`。长期目标是由实名信息或后端规则判定年龄段，移除该字段前必须同步修改后端接口和客户端模型。

登录响应至少包含：

- `accessToken`
- `refreshToken`
- `expiresIn`
- `user`

退出登录请求字段：

- `refreshToken`：客户端必须传当前 refreshToken，后端用于撤销会话。

## 用户接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/users/me` | 当前用户 |
| PATCH | `/users/me` | 更新资料 |
| GET | `/users/{userId}` | 用户主页 |
| POST | `/users/{userId}/follow` | 关注 |
| DELETE | `/users/{userId}/follow` | 取消关注 |
| POST | `/users/real-name` | 提交实名信息 |

对外 `userId` 使用字符串。

用户响应字段（联调口径）：

- `userId`：用户 ID。
- `nickname`：昵称。
- `avatarUrl`：头像 URL。
- `bio`：简介。
- `level`：用户等级。
- `isMinor`：是否未成年。
- `followingCount`：关注数。
- `followerCount`：粉丝数。

## 上传接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/uploads/presign` | 获取预签名上传 URL |
| POST | `/uploads/confirm` | 确认上传完成 |

`/uploads/presign` 联调返回字段：

- `fileKey`
- `uploadUrl`
- `headers`
- `expiresIn`

`/uploads/confirm` 联调返回字段：

- `fileId`
- `fileKey`
- `auditStatus`

上传用途枚举：

- `AVATAR`
- `POST_IMAGE`
- `POST_VIDEO`
- `AI_INPUT`
- `PATTERN_OUTPUT`
- `PRODUCT_IMAGE`
- `TRADE_IMAGE`

## 社区接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/posts/feed` | 推荐 Feed（免登录） |
| GET | `/posts/following` | 关注 Feed（免登录） |
| POST | `/posts` | 发布帖子 |
| GET | `/posts/{postId}` | 帖子详情（免登录） |
| PATCH | `/posts/{postId}` | 编辑帖子 |
| DELETE | `/posts/{postId}` | 删除帖子 |
| POST | `/posts/{postId}/like` | 点赞 |
| DELETE | `/posts/{postId}/like` | 取消点赞 |
| POST | `/posts/{postId}/favorite` | 收藏 |
| DELETE | `/posts/{postId}/favorite` | 取消收藏 |
| GET | `/posts/{postId}/comments` | 评论列表 |
| POST | `/posts/{postId}/comments` | 发表评论 |
| DELETE | `/comments/{commentId}` | 删除评论 |
| POST | `/reports` | 举报 |

帖子状态：

- `REVIEWING`
- `VISIBLE`
- `SELF_VISIBLE`
- `REJECTED`
- `DELETED`

帖子响应字段（联调口径）：

- `postId`：帖子 ID。
- `title`：标题。
- `content`：正文。
- `author`：作者对象，包含 `userId`、`nickname`、`avatarUrl`、`bio`、`level`、`isMinor`、`followingCount`、`followerCount`。
- `status`：帖子状态。
- `likeCount`、`commentCount`、`favoriteCount`：互动计数。
- `createdAt`、`updatedAt`：时间戳。

## AI 拼豆接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/patterns/jobs` | 创建 AI 拼豆任务 |
| GET | `/patterns/jobs/{jobId}` | 查询任务详情 |
| GET | `/patterns/jobs` | 生成记录 |
| POST | `/patterns/jobs/{jobId}/cancel` | 取消任务 |
| POST | `/patterns/{patternId}/favorite` | 收藏图纸 |
| GET | `/patterns/favorites` | 收藏图纸列表（分页） |
| GET | `/patterns/{patternId}` | 图纸详情 |

任务状态：

- `PENDING`
- `PROCESSING`
- `SUCCEEDED`
- `FAILED`
- `REJECTED`
- `CANCELED`

创建任务参数至少包含：

- 输入图片 `inputFileId`，来源于 `/uploads/confirm` 返回的 `fileId`。
- 拼豆规格：`MM_2_6` 或 `MM_5`。
- 输出尺寸。
- 难度。
- 色卡。
- 风格。

> 当前联调口径：客户端先调用 `/uploads/presign` 获取 `fileKey` 并直传对象存储，再调用 `/uploads/confirm` 换取后端文件记录 `fileId`；创建 AI 拼豆任务时传 `inputFileId`，不得直接传预签名阶段的 `fileKey`。

任务响应字段（联调口径）：

- `jobId`：任务 ID。
- `status`：任务状态。
- `progress`：进度（后端返回 0.0-1.0 浮点数，客户端转换为 0-100 整数展示）。
- `userId`：创建者 ID。
- `paletteName`：色卡名称。
- `inputName`：输入文件名。
- `patternId`：生成的图纸 ID（成功时有值）。
- `patternAsset`：图纸资产，包含 `materials`（材料清单，可为 null）。

图纸响应字段（联调口径）：

- `patternId`：图纸 ID。
- `ownerId`：所有者 ID。
- `title`：图纸标题。
- `paletteName`：色卡名称。
- `colorStats`：颜色统计。

## 商城接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/products` | 商品列表 |
| GET | `/products/{productId}` | 商品详情 |
| POST | `/products` | 发布玩家二手或定制商品骨架，要求 18+ 实名 |
| GET | `/cart` | 购物车 |
| POST | `/cart/items` | 加入购物车 |
| PATCH | `/cart/items/{itemId}` | 修改数量 |
| DELETE | `/cart/items/{itemId}` | 移除商品 |

商品类型：

- `SELF_OPERATED`
- `PLAYER_SECOND_HAND`
- `PLAYER_CUSTOM_SERVICE`

购物车响应字段（联调口径）：

- `itemId`：购物车项 ID。
- `productId`：商品 ID。
- `product`：商品摘要对象。
- `skuId`：SKU ID。
- `quantity`：数量。

SKU 字段（联调口径）：

- `skuId`：SKU ID。
- `priceCent`：价格（分）。
- `stock`：库存（`@SerialName("stock")`，客户端字段名为 `availableStock`）。

## 订单与支付接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/orders` | 创建订单 |
| GET | `/orders` | 订单列表 |
| GET | `/orders/{orderId}` | 订单详情 |
| POST | `/orders/{orderId}/cancel` | 取消订单 |
| POST | `/payments` | 创建支付单 |
| GET | `/payments/{paymentId}` | 查询支付状态 |
| POST | `/payments/callbacks/wechat` | 微信支付回调 |
| POST | `/payments/callbacks/alipay` | 支付宝支付回调 |
| POST | `/refunds` | 申请退款 |

支付渠道：

- `WECHAT_APP`
- `ALIPAY_APP`

订单响应字段（联调口径）：

- `orderId`：订单 ID。
- `orderItemId`：订单项 ID。
- `title`：商品标题。
- `specName`：规格名称。
- `sellerId`：卖家 ID。
- `addressSnapshot`：收货地址快照。
- `status`：订单状态。
- `payableAmountCent`：应付金额（分）。

支付响应字段（联调口径）：

- `paymentId`：支付单 ID。
- `orderId`：关联订单 ID。
- `channel`：支付渠道。
- `status`：支付状态。
- `amountCent`：支付金额（分）。
- `payParams`：客户端拉起支付 SDK 的参数。
- `paidAt`：支付完成时间。

客户端不得传最终订单金额，金额由服务端根据商品、SKU、库存、优惠和运费计算。

创建订单请求字段：

- `itemIds`：购物车项 ID 列表（`List<String>`），必填。
- `addressId`：收货地址 ID，必填。
- `remark`：订单备注，可选。

> 当前口径：微信和支付宝支付仍是开发态 Stub/占位能力，不是正式支付能力。正式上线前必须补齐官方渠道接入、验签、支付金额校验、订单号/支付单号/渠道交易号一致性校验、回调重放与重复通知处理、主动查询、退款和对账。

## 消息接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/messages/notifications` | 通知列表 |
| POST | `/messages/notifications/read` | 标记已读 |
| GET | `/messages/conversations` | 会话列表 |
| GET | `/messages/conversations/{conversationId}` | 会话详情 |
| POST | `/messages/conversations/{conversationId}` | 发送私信 |

通知响应字段（联调口径）：

- `notificationId`：通知 ID。
- `type`：通知类型。
- `title`：标题。
- `content`：内容。
- `unread`：是否未读（`readAt == null` 时为 `true`）。

会话响应字段（联调口径）：

- `conversationId`：会话 ID。
- `peerUserId`：对方用户 ID。
- `peerName`：对方昵称。
- `lastMessage`：最后一条消息摘要。
- `unreadCount`：未读消息数。
- `riskHint`：风控提示（可选）。

## 成长接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/checkins` | 每日签到 |
| GET | `/checkins/status` | 签到状态 |
| GET | `/rewards/me` | 我的等级和积分 |
| GET | `/badges/me` | 我的徽章 |

签到响应字段（联调口径）：

- `checkedToday`：今日是否已签到（布尔值）。
- `streakDays`：连续签到天数。
- `rewardPoints`：本次签到获得积分。

成长信息响应字段（联调口径）：

- `points`：当前积分。
- `experience`：当前经验值。
- `levelCode`：当前等级代码。

徽章响应字段（联调口径）：

- `badgeId`：徽章 ID。
- `name`：徽章名称。
- `description`：徽章描述。
- `achieved`：是否已获得（布尔值）。

## 管理后台接口

后台接口使用 `/api/v1/admin` 前缀，必须使用后台账号鉴权。

后台认证：

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/auth/login` | 后台账号登录，返回后台 access token |

后台覆盖：

- 用户管理。
- 内容审核。
- 举报处理。
- 商品管理。
- 订单管理。
- 支付记录。
- AI 任务。
- 风控记录。
- 运营配置。
