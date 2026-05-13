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
| AUDIT_REJECTED | 内容审核未通过 |
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

登录响应至少包含：

- `accessToken`
- `refreshToken`
- `expiresIn`
- `user`

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

## 上传接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/uploads/presign` | 获取预签名上传 URL |
| POST | `/uploads/confirm` | 确认上传完成 |

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
| GET | `/posts/feed` | 推荐 Feed |
| GET | `/posts/following` | 关注 Feed |
| POST | `/posts` | 发布帖子 |
| GET | `/posts/{postId}` | 帖子详情 |
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

## AI 拼豆接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/patterns/jobs` | 创建 AI 拼豆任务 |
| GET | `/patterns/jobs/{jobId}` | 查询任务详情 |
| GET | `/patterns/jobs` | 生成记录 |
| POST | `/patterns/jobs/{jobId}/cancel` | 取消任务 |
| POST | `/patterns/{patternId}/favorite` | 收藏图纸 |
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

客户端不得传最终订单金额，金额由服务端根据商品、SKU、库存、优惠和运费计算。

## 消息接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/messages/notifications` | 通知列表 |
| POST | `/messages/notifications/read` | 标记已读 |
| GET | `/messages/conversations` | 会话列表 |
| GET | `/messages/conversations/{conversationId}` | 会话详情 |
| POST | `/messages/conversations/{conversationId}` | 发送私信 |

## 成长接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/checkins` | 每日签到 |
| GET | `/checkins/status` | 签到状态 |
| GET | `/rewards/me` | 我的等级和积分 |
| GET | `/badges/me` | 我的徽章 |

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
