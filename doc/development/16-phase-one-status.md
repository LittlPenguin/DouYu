# 16. 第一阶段当前状态与剩余任务

## 状态结论

截至 2026-05-14，豆屿 Doyu 第一阶段已经进入“前后端联调骨架”状态，但尚未达到完整 MVP 交付状态。

当前可以认为已经完成：

- Android 工程可以构建 Debug 包。
- Android 已建立 Compose 页面骨架、底部 5 Tab、主要业务页面和 Mock 数据展示。
- Android 已补齐更接近后端契约的核心数据模型。
- Android 已补齐主要 `/api/v1` Retrofit API Client。
- Android 已建立 Bearer Token 注入、Token 刷新、幂等 Header 和 AI 上传到任务创建的工作流封装。
- 后端 Spring Boot 工程可以运行测试。
- 后端已提供登录、用户、上传、社区、AI 任务、商城、购物车、订单、支付、消息、成长、举报和后台接口骨架。
- 后端已接入 OpenAPI/Swagger，提供接口文档入口。
- 后端已明确短信、OSS、AI、微信支付、支付宝支付均为 Stub Provider。
- 文档已补充前后端协作、AI Provider 选型、联调说明和支付 Stub 风险边界。

当前不能认为已经完成：

- Android 还未完成全部页面与真实后端接口的端到端联调。
- Android 还未完成真实相册选择、CameraX 拍照、对象存储直传和轮询进度的完整 UI 闭环。
- 后端业务数据主要仍在进程内存中，PostgreSQL schema 已有，但核心业务仓储尚未全面持久化。
- OSS、AI、微信支付、支付宝支付均为 Stub，不具备生产能力。
- 内容审核、版权投诉、未成年人保护、玩家交易风控还停留在骨架和文档阶段。
- 管理后台只有 API 骨架，没有完整运营工作台。

## 当前提交包含的主要内容

### Android 客户端

- 对齐后端契约字段：
  - 用户使用 `userId`。
  - 帖子使用 `postId`、`authorId`、`status`、互动计数。
  - 评论使用 `commentId`、`postId`、`authorId`、`status`。
  - 商品使用 `productId`、`type`、`sellerId`、`status`、`auditStatus`、`skus`。
  - SKU 使用 `skuId`、`priceCent`、`availableStock`。
  - 订单使用 `orderId`、`buyerId`、`sellerType`、`orderType`、`status`、`payableAmountCent`。
  - 支付使用 `paymentId`、`orderId`、`channel`、`status`、`amountCent`、`payParams`。
- 补齐 API Client：
  - 认证刷新和退出。
  - 上传确认。
  - 发帖、点赞、收藏、评论。
  - AI 任务创建、查询、列表、取消、收藏和图纸详情。
  - 购物车增删改查。
  - 订单创建、列表、详情、取消。
  - 支付创建和查询。
  - 通知、会话、私信发送。
  - 签到、签到状态、成长信息、徽章。
- 新增网络基础能力：
  - `DoyuApiClient`。
  - `AuthInterceptor`。
  - `TokenRefreshAuthenticator`。
  - `IdempotencyKeyInterceptor`。
  - `AuthSessionManager`。
  - `AuthTokenStore`。
  - `PatternGenerationWorkflow`。
- 调整 Mock Repository，使 Mock 数据结构更贴近真实接口，避免前后端字段口径分裂。
- 补充网络契约、模型契约、鉴权刷新和 AI 上传工作流相关单元测试。

### 后端服务

- 接入并配置 SpringDoc OpenAPI/Swagger。
- 在 `doyu-server/README.md` 中补充联调地址、启动方式、测试账号、Stub Provider 清单和支付风险说明。
- 在 OpenAPI 描述中明确当前是第一阶段 Android 联调 API。
- 在 OpenAPI 描述中明确短信、OSS、AI、微信支付、支付宝支付均为 Stub。
- 补充接口契约测试，覆盖 Swagger、统一响应、鉴权、上传到 AI 任务、订单支付等关键联调口径。
- 新增 `start-dev.bat`，便于本地启动 PostgreSQL、Redis 和后端 dev profile。

### 文档

- 更新技术栈和后端服务文档，明确 OpenAPI/Swagger 与 Stub Provider 边界。
- 更新 API 契约，补充上传确认、AI `inputFileId`、支付查询和联调字段要求。
- 更新 AI 拼豆图纸文档和 AI Provider 选型文档，明确大模型只做理解和预处理，最终图纸由自研算法生成。
- 更新商城支付文档，明确客户端不得单点判定支付成功，支付结果以服务端状态为准。
- 更新前后端协作文档，加入联调冻结、字段对齐、Mock 约束和剩余任务。
- 新增本文档，记录第一阶段当前状态和后续任务。

## 剩余任务：前端

优先级 P0：

- 将当前 Mock Repository 逐步替换为真实 Repository，接入 `DoyuApiClient`。
- 完成登录页真实接口联调：
  - `POST /api/v1/auth/sms-code`
  - `POST /api/v1/auth/login/sms`
  - `POST /api/v1/auth/refresh`
  - `POST /api/v1/auth/logout`
- 完成 AI 拼图真实链路 UI：
  - Photo Picker 选择图片。
  - CameraX 拍照入口。
  - `POST /uploads/presign`。
  - 按 `uploadUrl` 和 `headers` 执行上传。
  - `POST /uploads/confirm`。
  - 使用 `fileId` 创建 `POST /patterns/jobs`。
  - 轮询 `GET /patterns/jobs/{jobId}`。
  - 按 `PENDING`、`PROCESSING`、`SUCCEEDED`、`FAILED`、`REJECTED`、`CANCELED` 展示状态。
- 完成社区真实链路：
  - Feed。
  - 帖子详情。
  - 发帖。
  - 评论。
  - 点赞。
  - 收藏。
- 完成商城和订单真实链路：
  - 商品列表。
  - 商品详情。
  - 购物车增删改。
  - 创建订单。
  - 查询订单。
  - 创建支付单。
  - 支付结果页查询服务端状态。

优先级 P1：

- 将错误码统一转为用户可读文案。
- 为核心页面补齐加载、空状态、失败、未登录、无权限、审核中、弱网重试状态。
- 将 `traceId` 接入错误日志和问题反馈入口。
- 对 375dp 宽度和常见 Android 设备做 UI 检查。
- 确认不申请非必要权限，不在客户端硬编码 AI、OSS、支付密钥。

## 剩余任务：后端

优先级 P0：

- 保持 OpenAPI 与实际控制器一致，确保 Android 可直接按 Swagger 联调。
- 明确每个接口是否需要登录、是否需要 `Idempotency-Key`。
- 补齐接口错误响应示例，尤其是：
  - `UNAUTHORIZED`
  - `FORBIDDEN`
  - `AUDIT_REJECTED`
  - `AI_TASK_FAILED`
  - `INVENTORY_NOT_ENOUGH`
  - `PAYMENT_FAILED`
- 确认 `/uploads/presign`、`/uploads/confirm`、`/patterns/jobs` 字段口径保持一致：
  - 预签名阶段返回 `fileKey`。
  - 上传确认阶段返回 `fileId`。
  - AI 任务创建使用 `inputFileId`。
- 为前端提供固定联调用例：
  - 测试手机号。
  - 验证码。
  - 初始商品。
  - 初始帖子。
  - 初始 AI 任务或可创建任务样例。

优先级 P1：

- 将核心业务对象逐步迁移到 PostgreSQL 持久化：
  - 用户。
  - 帖子。
  - 评论。
  - 文件资产。
  - AI 任务。
  - 商品。
  - 购物车。
  - 订单。
  - 支付。
  - 消息。
  - 举报和审核记录。
- 接入真实 OSS Provider 或兼容 MinIO 的本地开发 Provider。
- 接入真实 AI Provider 前先完成 `BeadPatternEngine` 算法原型。
- 支付正式接入前补齐微信、支付宝验签、金额校验、订单号校验、回调重放处理、主动查询和对账。
- 补齐管理后台的审核、举报、商品、订单、AI 任务处理能力。

## 剩余任务：联调与验收

第一阶段完成标准：

- Android 可以使用后端 dev profile 完成真实登录。
- Android 可以看到后端返回的社区 Feed 和商品列表。
- Android 可以执行上传确认到 AI 任务创建的完整流程。
- Android 可以查询 AI 任务状态并展示 Stub 图纸结果。
- Android 可以把商品加入购物车、创建订单、创建支付单并查询服务端支付状态。
- 后端 Swagger 能覆盖所有联调接口。
- 前后端字段、枚举、错误码和分页结构一致。
- Android Debug 构建和单元测试通过。
- 后端测试通过。

联调命令：

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

## 当前风险

- 前端 UI 已有页面骨架，但真实 Repository 接入后可能暴露状态管理和错误处理缺口。
- 后端 Stub 结果可以支持联调，但不能代表真实 OSS、AI 和支付服务的异常行为。
- 后端业务数据尚未全面持久化，服务重启会丢失多数联调数据。
- 支付链路当前只能用于联调，不能用于正式交易。
- AI 图纸当前为 Stub，距离真实“图片转拼豆图纸”还需要算法和 Provider 接入。
- 审核和风控逻辑尚未达到中国大陆应用市场上线要求。

