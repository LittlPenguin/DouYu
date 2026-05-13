# 13. 后端服务任务书

## 任务目标

你负责开发 **豆屿 Doyu 后端服务**。第一阶段目标是完成可联调的 Spring Boot API 服务骨架和核心业务闭环，不做微服务拆分。

建议工程路径：

`D:\Studio\SpellBean\doyu-server`

## 开发前必读

- `D:\Studio\SpellBean\AGENTS.md`
- `D:\Studio\SpellBean\doc\豆屿App商业技术执行计划.md`
- `D:\Studio\SpellBean\doc\development\README.md`
- `D:\Studio\SpellBean\doc\development\04-backend-services.md`
- `D:\Studio\SpellBean\doc\development\05-api-contract.md`
- `D:\Studio\SpellBean\doc\development\06-data-model.md`
- `D:\Studio\SpellBean\doc\development\07-ai-pattern-generation.md`
- `D:\Studio\SpellBean\doc\development\08-commerce-payment.md`
- `D:\Studio\SpellBean\doc\development\09-security-compliance.md`
- `D:\Studio\SpellBean\doc\development\10-testing-acceptance.md`

## 技术要求

- Spring Boot。
- Java 21，环境不支持时使用 Java 17。
- 模块化单体，不拆微服务。
- PostgreSQL。
- Redis。
- REST + JSON。
- API 前缀统一为 `/api/v1`。
- 对外业务 ID 使用字符串。
- JWT access token + refresh token。
- 文件上传使用后端签发预签名 URL，客户端直传对象存储。
- 对象存储默认按阿里云 OSS 抽象设计。
- AI Provider 只能由后端封装，客户端不得直连。
- 支付预留微信支付 App 支付和支付宝 App 支付。
- 不提交任何密钥、证书、商户私钥、API Key 或真实用户数据。

## 模块范围

按领域拆包或模块组织：

```text
auth
user
community
upload
pattern
commerce
order
payment
message
reward
moderation
admin
common
```

## 第一阶段接口范围

所有接口统一使用 `/api/v1` 前缀。

### 认证

- `POST /auth/sms-code`
- `POST /auth/login/sms`
- `POST /auth/refresh`
- `POST /auth/logout`
- `POST /auth/account/cancel`

### 用户

- `GET /users/me`
- `PATCH /users/me`
- `GET /users/{userId}`
- `POST /users/{userId}/follow`
- `DELETE /users/{userId}/follow`
- `POST /users/real-name`

### 上传

- `POST /uploads/presign`
- `POST /uploads/confirm`

### 社区

- `GET /posts/feed`
- `GET /posts/following`
- `POST /posts`
- `GET /posts/{postId}`
- `PATCH /posts/{postId}`
- `DELETE /posts/{postId}`
- `POST /posts/{postId}/like`
- `DELETE /posts/{postId}/like`
- `POST /posts/{postId}/favorite`
- `DELETE /posts/{postId}/favorite`
- `GET /posts/{postId}/comments`
- `POST /posts/{postId}/comments`
- `DELETE /comments/{commentId}`
- `POST /reports`

### AI 拼豆

- `POST /patterns/jobs`
- `GET /patterns/jobs/{jobId}`
- `GET /patterns/jobs`
- `POST /patterns/jobs/{jobId}/cancel`
- `POST /patterns/{patternId}/favorite`
- `GET /patterns/{patternId}`

### 商城

- `GET /products`
- `GET /products/{productId}`
- `GET /cart`
- `POST /cart/items`
- `PATCH /cart/items/{itemId}`
- `DELETE /cart/items/{itemId}`

### 订单支付

- `POST /orders`
- `GET /orders`
- `GET /orders/{orderId}`
- `POST /orders/{orderId}/cancel`
- `POST /payments`
- `GET /payments/{paymentId}`
- `POST /refunds`

### 消息

- `GET /messages/notifications`
- `POST /messages/notifications/read`
- `GET /messages/conversations`
- `GET /messages/conversations/{conversationId}`
- `POST /messages/conversations/{conversationId}`

### 成长

- `POST /checkins`
- `GET /checkins/status`
- `GET /rewards/me`
- `GET /badges/me`

### 后台

- 后台接口统一使用 `/api/v1/admin`。
- 后台账号与普通用户账号隔离。
- 第一阶段至少提供用户、内容、商品、订单、AI 任务、举报的查询和状态处理接口骨架。

## 统一协议

统一响应：

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

通用错误码：

```text
OK
INVALID_ARGUMENT
UNAUTHORIZED
FORBIDDEN
NOT_FOUND
CONFLICT
RATE_LIMITED
AUDIT_REJECTED
PAYMENT_FAILED
INVENTORY_NOT_ENOUGH
AI_TASK_FAILED
INTERNAL_ERROR
```

## 幂等要求

必须幂等：

- 创建订单。
- 创建支付单。
- 支付回调。
- 退款申请。
- 取消订单。

幂等来源：

- `Idempotency-Key`。
- 业务唯一键。
- 支付渠道交易号。
- 订单 ID + 操作类型。

## 核心数据

核心对象以 `06-data-model.md` 为准，必须覆盖：

- User。
- UserProfile。
- AuthToken。
- Post。
- Comment。
- Like。
- Favorite。
- Follow。
- FileAsset。
- PatternJob。
- PatternAsset。
- Product。
- Sku。
- CartItem。
- Order。
- OrderItem。
- Payment。
- Refund。
- Message。
- Conversation。
- RewardAccount。
- CheckinRecord。
- Report。
- ModerationRecord。
- AdminOperationLog。

状态枚举必须覆盖：

```text
PENDING
PROCESSING
SUCCEEDED
FAILED
REJECTED
CANCELED
CREATED
WAITING_PAYMENT
PAID
FULFILLING
SHIPPED
COMPLETED
CANCELED
REFUNDING
REFUNDED
DRAFT
ON_SALE
OFF_SALE
SOLD_OUT
DELETED
SELF_OPERATED
PLAYER_SECOND_HAND
PLAYER_CUSTOM_SERVICE
WECHAT_APP
ALIPAY_APP
AVATAR
POST_IMAGE
POST_VIDEO
AI_INPUT
PATTERN_OUTPUT
PRODUCT_IMAGE
TRADE_IMAGE
```

## 重点业务规则

### 用户与年龄

- 用户注册时确认年龄段。
- 16-17 岁标记为未成年人。
- 玩家卖家、提现、定制服务发布必须 18+ 实名。
- 账号注销必须可追踪。

### 上传

- 服务端只签发上传凭证，不默认接收大文件。
- 校验文件用途、类型、大小、用户权限、上传频率。
- 上传完成后记录 fileKey，触发审核和缩略图处理。
- 不向客户端暴露 OSS Secret。

### 社区

- 帖子、评论、昵称、头像、简介都要预留审核状态。
- Feed 第一版可使用规则排序。
- 举报必须有记录和后台处理入口。

### AI 拼豆

- 创建任务后异步处理。
- 输入图和输出图都要审核。
- AI Provider 必须通过统一接口封装。
- AI 失败时记录可读失败原因、traceId、是否返还次数、是否允许重试。
- 输出必须包含预览图、网格图、色号图、材料清单。

### 商城

- 自营商品支持购物车。
- 玩家二手/定制第一版以直连撮合和留痕为主。
- 玩家卖家必须 18+ 实名。
- 不做平台余额，不沉淀资金池。
- 商品价格、库存、优惠、运费均以服务端为准。

### 支付

- 客户端只拉起支付，不决定支付成功。
- 支付结果以服务端回调和主动查询为准。
- 回调必须验签、幂等、可重放处理。
- 金额以分为单位。
- 退款金额不得超过可退金额。

### 安全合规

- 不记录明文敏感信息。
- 不提交密钥。
- AI 输入和输出要有审核记录。
- 用户上传内容全部按不可信输入处理。
- 后台操作必须记录操作人、对象、时间、前后状态和原因。

## 验收命令

完成后必须提供并运行 Spring Boot 项目的测试命令。若使用 Gradle：

```powershell
.\gradlew test
```

## 验收标准

- 服务可本地启动。
- OpenAPI 或接口文档可供前端联调。
- `/api/v1` 前缀统一。
- 响应格式统一。
- 对外 ID 为字符串。
- 无密钥硬编码。
- 支付、AI、OSS 均通过服务端抽象，不让客户端直连第三方敏感能力。
- 与 `05-api-contract.md`、`06-data-model.md`、`08-commerce-payment.md` 保持一致。
- 覆盖登录、token 刷新、上传预签名、AI 状态流转、订单幂等、支付幂等、库存不足、举报审核、玩家卖家年龄实名限制测试。

## 当前联调补充整改项

以下为 2026-05-13 前后端协同检查后的后端整改要求，优先级高于继续扩展非主链路能力。

### 1. 提供可直接交给 Android 的接口契约

后端当前已有控制器和契约测试，但前端仍缺少稳定可消费的字段说明。请补齐以下任一形式：

- 推荐：提供 `/v3/api-docs` 和 Swagger UI，并确认所有 `/api/v1` 接口可见。
- 或者：导出 OpenAPI JSON/YAML，放到 `doc/development/openapi/` 或在后端 README 写明获取方式。

接口契约必须明确：

- 请求字段、必填字段、枚举值。
- 统一响应包裹 `{ code, message, data, traceId }`。
- 分页响应 `{ items, page, size, total, hasMore }`。
- 需要登录的接口。
- 需要 `Idempotency-Key` 的接口。
- 典型错误响应示例。

### 2. 保持 AI 上传字段口径一致

`05-api-contract.md` 已明确：创建 AI 拼豆任务使用 `inputFileId`，不是预签名阶段的 `fileKey`。后端需保持：

- `/uploads/presign` 返回 `fileKey`、`uploadUrl`、`headers`、`expiresIn`。
- `/uploads/confirm` 返回 `fileId`、`fileKey`、`auditStatus`。
- `/patterns/jobs` 请求使用 `inputFileId`。
- 如果后续改回 `fileKey` 或同时支持两者，必须先同步 `05-api-contract.md`、Android API model 和测试样例。

### 3. 补齐前端主链路需要的响应字段

前端 5 个主 Tab 联调至少需要下列字段稳定返回：

- 用户：`userId`、`nickname`、`avatarUrl`、`bio`、`isMinor`、`level` 或 `levelCode`。
- 帖子：`postId`、`authorId`、作者展示信息、`title`、`content`、媒体文件或占位图信息、`status`、互动计数。
- AI 任务：`jobId`、`status`、`failureReason`、`retryable`、`quotaRefunded`、`patternId`、`materials`。
- 图纸：`patternId`、`previewFileId` 或可访问 URL、`gridFileId`、`colorMapFileId`、`beadSize`、`widthCells`、`heightCells`、`totalBeads`、`materials`。
- 商品：`productId`、`type`、`title`、`description`、`status`、`auditStatus`、`skus`。
- SKU：`skuId`、`specName`、`priceCent`、`availableStock`、`status`。
- 订单：`orderId`、`status`、`payableAmountCent`、`items`。
- 支付：`paymentId`、`status`、`payParams`。
- 消息：通知和会话接口统一分页返回。

如果某些字段第一阶段仍是占位值，也要保持字段存在，避免 Android 为同一页面写两套模型。

### 4. 补齐或标注尚未完成的接口

任务书接口范围内尚未完成或仅占位的接口，需要在 OpenAPI/README 中明确状态。第一阶段至少保证以下接口可联调：

- 登录、刷新、退出、当前用户。
- Feed、帖子详情、发帖、评论、点赞、收藏。
- 上传预签名、上传确认。
- AI 任务创建、查询、列表、取消、图纸详情。
- 商品列表、详情、购物车增删改查。
- 创建订单、订单列表、订单详情、取消订单。
- 创建支付单、查询支付单、支付回调占位。
- 通知列表、已读、会话列表、会话详情、发送私信。
- 签到、签到状态、成长信息、徽章。

未实现的真实第三方能力要以 Stub Provider 明确标注，不得把 Stub 当作正式实现交付。

### 5. 支付回调安全不能只保留测试形态

当前支付回调占位可用于联调，但正式接入前必须补齐：

- 微信、支付宝回调验签。
- 回调幂等键，至少使用渠道交易号和支付单 ID。
- 金额、订单号、商户号校验。
- 回调重放处理。
- 失败回调与主动查询补偿。

在验签未完成前，文档和接口说明必须标注为 Stub，避免前端或测试误判为正式支付能力。

### 6. 后端交付说明

整改完成后请提交：

- 本地启动命令和环境变量说明。
- OpenAPI/Swagger 访问地址或导出文件路径。
- 测试账号或短信验证码测试规则。
- Stub Provider 清单：短信、OSS、AI、支付。
- 已通过的测试命令输出，至少包含 `mvn test` 或等价命令。
- 尚未实现但不阻塞第一阶段联调的接口清单。
