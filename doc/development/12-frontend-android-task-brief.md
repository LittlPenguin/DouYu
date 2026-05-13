# 12. Android 前端任务书

## 任务目标

你负责开发 **豆屿 Doyu Android 客户端**。第一阶段目标是完成可联调的 MVP 骨架，而不是一次性做完全部业务细节。

默认工程路径：

`D:\Studio\SpellBean\DouYu`

## 开发前必读

- `D:\Studio\SpellBean\AGENTS.md`
- `D:\Studio\SpellBean\doc\豆屿App商业技术执行计划.md`
- `D:\Studio\SpellBean\doc\development\README.md`
- `D:\Studio\SpellBean\doc\development\03-android-client.md`
- `D:\Studio\SpellBean\doc\development\05-api-contract.md`
- `D:\Studio\SpellBean\doc\development\07-ai-pattern-generation.md`
- `D:\Studio\SpellBean\doc\development\08-commerce-payment.md`
- `D:\Studio\SpellBean\doc\development\09-security-compliance.md`
- `D:\Studio\SpellBean\doc\development\11-ui-style-guide.md`

## 技术要求

- Kotlin + Jetpack Compose。
- Android 原生，不使用 Flutter、React Native、WebView 套壳。
- `minSdk = 26`。
- 单 Activity + Compose Navigation + MVVM + Repository。
- Retrofit + OkHttp。
- Room + DataStore。
- Coil。
- WorkManager。
- CameraX。
- Android Photo Picker。
- Paging 3。
- API 请求统一走后端 `/api/v1`。
- 客户端不得直连 AI Provider、OSS Secret、支付私钥或任何服务端密钥。

## 模块结构

第一阶段可以先使用单模块分包，不强制拆多 Gradle Module。建议包结构：

```text
cn.edu.app.douyu
  core
    network
    data
    ui
    model
    navigation
  feature
    auth
    community
    ai
    commerce
    message
    profile
```

## UI 要求

UI 必须符合 `11-ui-style-guide.md`。

开发 UI 前必须使用：

`D:\Studio\SpellBean\.codex\skills\ui-ux-pro-max\SKILL.md`

关键词：

```text
mobile social commerce handmade craft young female cute playful modern community ecommerce
```

stack：

```text
jetpack-compose
```

UI 风格锁定为年轻女性友好、轻卡通可爱、手作拼豆质感、现代社区商城 App。可以使用圆润图形、拼豆颗粒感、小插画、柔和微动效，但不得儿童化、不得高饱和铺满、不得牺牲信息可读性。

## 第一阶段页面范围

底部导航固定为 5 个 Tab：

- 社区。
- AI 拼图。
- 商城。
- 消息。
- 我的。

必须实现页面：

- 启动页或简单初始化页。
- 登录页。
- 社区 Feed 页。
- 帖子详情页。
- 发帖入口页。
- AI 拼图首页。
- 图片选择页。
- AI 参数选择页。
- AI 任务进度页。
- 图纸结果页。
- 生成记录页。
- 商城首页。
- 商品列表页。
- 商品详情页。
- 购物车页。
- 订单确认页。
- 支付结果页。
- 消息列表页。
- 私信会话页。
- 我的页面。
- 我的拼豆页。
- 设置页。

## 页面状态

每个核心页面必须支持：

- 加载中。
- 成功。
- 空状态。
- 失败。
- 未登录。
- 无权限。
- 审核中。
- 弱网重试。

Compose 约束：

- UI 只负责渲染状态。
- ViewModel 处理用户操作。
- Repository 负责网络和缓存。
- 错误统一转成用户可读文案。
- 不在 Composable 内直接写网络请求。

## API Client

统一响应格式：

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "traceId": "trace_xxx"
}
```

鉴权 Header：

```http
Authorization: Bearer <access_token>
```

幂等 Header：

```http
Idempotency-Key: <uuid>
```

必须预留 API Client：

- AuthApi。
- UserApi。
- UploadApi。
- CommunityApi。
- PatternApi。
- ProductApi。
- CartApi。
- OrderApi。
- PaymentApi。
- MessageApi。
- RewardApi。

## 业务流程

### 登录

- 手机号验证码登录。
- 保存 access token 和 refresh token。
- token 失效后自动刷新。
- 退出登录清理本地 token。

### 社区

- Feed 列表。
- 帖子详情。
- 点赞、收藏、评论。
- 发帖时支持图片 fileKey。
- 内容发布后可能进入审核中。

### AI 拼图

- Photo Picker 选择图片。
- CameraX 拍照入口预留。
- 图片裁剪和参数选择。
- 请求上传预签名 URL。
- 上传图片到对象存储。
- 创建 `/patterns/jobs` 异步任务。
- 轮询任务状态。
- 成功展示预览图、网格图、色号清单、材料清单。
- 失败展示原因和重试入口。

### 商城

- 商品列表。
- 商品详情。
- 购物车。
- 订单确认。
- 创建订单。
- 创建支付单。
- 支付 SDK 暂以封装占位为第一阶段目标，后续接入微信支付和支付宝 App 支付。
- 支付结果以服务端订单状态为准。

### 我的

- 用户资料。
- 我的拼豆。
- 生成记录。
- 订单入口。
- 签到和等级入口。
- 设置、隐私、注销入口预留。

## 验收命令

完成后必须运行：

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

## 验收标准

- Debug 包可构建成功。
- 底部 5 个 Tab 可正常切换。
- 核心页面无空白崩溃。
- 375dp 宽度下文字不挤压、不重叠。
- 页面包含加载、失败、空状态。
- 不申请非必要权限。
- 不硬编码 AI、OSS、支付密钥。
- 不直接访问 AI 供应商。
- UI 有轻卡通可爱风格，但仍像成熟可用的社区商城 App。
- UI 体现豆屿 Doyu，而不是 Android 默认模板。

## 当前联调补充整改项

以下为 2026-05-13 前后端协同检查后的前端整改要求，优先级高于继续扩展新页面。

### 1. API 数据模型对齐后端契约

当前前端 `ApiInterfaces.kt` 和 `Models.kt` 仍偏 Mock 形态，字段名与后端实际返回不完全一致。请按 `05-api-contract.md` 和后端 OpenAPI/控制器返回修正：

- 用户字段使用后端返回的 `userId`，不要只定义本地 `id`。
- 帖子字段使用 `postId`、`authorId`、`status`、`likeCount`、`favoriteCount`、`commentCount`。
- 评论字段使用 `commentId`、`postId`、`authorId`、`status`。
- 商品字段使用 `productId`、`type`、`sellerId`、`status`、`auditStatus`、`skus`，SKU 使用 `skuId`、`priceCent`、`availableStock`。
- 订单字段使用 `orderId`、`buyerId`、`sellerType`、`orderType`、`status`、`totalAmountCent`、`payableAmountCent`、`items`。
- 支付字段使用 `paymentId`、`orderId`、`channel`、`status`、`amountCent`、`payParams`。
- 消息通知列表和会话列表按分页响应解析，不要按裸数组解析。

### 2. 补齐缺失接口方法

当前 API Client 只覆盖部分 GET/POST，无法完成任务书里的主链路。请补齐并接入调用点：

- `POST /auth/refresh`
- `POST /auth/logout`
- `POST /uploads/confirm`
- `POST /posts`
- `POST /posts/{postId}/like`
- `DELETE /posts/{postId}/like`
- `POST /posts/{postId}/favorite`
- `DELETE /posts/{postId}/favorite`
- `GET /posts/{postId}/comments`
- `POST /posts/{postId}/comments`
- `POST /patterns/jobs`
- `GET /patterns/jobs/{jobId}`
- `POST /patterns/jobs/{jobId}/cancel`
- `POST /cart/items`
- `PATCH /cart/items/{itemId}`
- `DELETE /cart/items/{itemId}`
- `POST /orders`
- `GET /orders`
- `GET /payments/{paymentId}`
- `POST /messages/notifications/read`
- `GET /messages/conversations/{conversationId}`
- `POST /messages/conversations/{conversationId}`
- `POST /checkins`
- `GET /checkins/status`
- `GET /badges/me`

### 3. 上传与 AI 任务链路必须改成真实顺序

AI 拼豆链路按以下顺序实现，不得把 Mock 图纸直接当作后端结果：

1. 用户选择图片或拍照。
2. 调用 `POST /uploads/presign`，请求包含 `usage`、`mimeType`、`sizeBytes`、`fileName`。
3. 使用返回的 `uploadUrl`、`headers`、`fileKey` 直传对象存储。
4. 调用 `POST /uploads/confirm`，请求包含 `fileKey`、`usage`、`mimeType`、`sizeBytes`、`width`、`height`。
5. 使用确认接口返回的 `fileId` 作为 `POST /patterns/jobs` 的 `inputFileId`。
6. 轮询 `GET /patterns/jobs/{jobId}`，按 `PENDING`、`PROCESSING`、`SUCCEEDED`、`FAILED`、`REJECTED`、`CANCELED` 展示状态。

### 4. 幂等和鉴权 Header 接入

- 登录后保存 `accessToken`、`refreshToken`，所有需要登录的接口统一添加 `Authorization: Bearer <access_token>`。
- token 过期时调用 `/auth/refresh`，刷新失败则回到登录态。
- 创建订单、创建支付单、退款、取消订单等重要提交必须添加 `Idempotency-Key`。
- 客户端可生成 `X-Request-Id`，用于问题排查；服务端返回的 `traceId` 要进入错误展示或日志。

### 5. 支付结果不得由客户端单点判定

- 支付 SDK 返回只作为“已拉起/待确认”的本地事件。
- 支付结果页必须调用 `GET /orders/{orderId}` 或 `GET /payments/{paymentId}` 查询服务端状态。
- UI 文案区分 `WAITING_PAYMENT`、`PAID`、`PAYMENT_FAILED`、`CANCELED` 等状态。

### 6. Mock 保留但必须贴近真实接口

如果后端接口尚未全部接入，前端可以继续保留 Mock Repository，但 Mock 数据结构必须与 `05-api-contract.md` 一致，并覆盖以下错误态：

- `UNAUTHORIZED`
- `FORBIDDEN`
- `AUDIT_REJECTED`
- `AI_TASK_FAILED`
- `INVENTORY_NOT_ENOUGH`

### 7. 前端交付说明

整改完成后请提交：

- 本地 API Base URL 配置方式。
- 已接真实后端的接口清单。
- 仍使用 Mock 的接口清单和原因。
- `.\gradlew.bat :app:assembleDebug` 结果。
- `.\gradlew.bat :app:testDebugUnitTest` 结果。
