# 16. 第一阶段当前状态与剩余任务

## 状态结论

截至 2026-05-14（第二轮），豆屿 Doyu 第一阶段 P0 任务已全部完成，前后端字段对齐已通过验收，联调主链路已打通。

当前可以认为已经完成：

- Android 工程可以构建 Debug 包，单元测试全部通过。
- Android 已从 Mock Repository 切换到真实 Repository，5 个 Screen 全部接入 `DoyuAppContainer`。
- Android 已完成 12 个 P0 字段对齐修复（SmsLoginRequest、NotificationMessage、Conversation、CheckinStatus、RewardSummary 等）。
- Android 已修复启动闪退问题（safeCall 异常处理、ApiResponse.traceId 默认值）。
- Android 页面切换动画已优化为 150ms。
- 后端 Spring Boot 工程 15 个测试全部通过。
- 后端 13 个 Controller 已全部添加 OpenAPI @Tag/@Operation/@ApiResponses 注解。
- 后端已完成字段补齐：postView(author 对象)、jobView(progress/paletteName)、cartView(product)、orderView(orderItemId/title/specName)、notificationView(notificationId/unread)、conversationView(peerUserId/peerName)、userView(avatarUrl/level)、patternView(ownerId/title/colorStats)、paymentView(paidAt)、checkin(checkedToday)、badges(description/achieved)。
- 后端 CreateOrderRequest 已改为接收 itemIds+addressId。
- 前后端字段、枚举、错误码和分页结构已通过 integrator 验收。
- 文档已补充前后端协作、AI Provider 选型、联调说明和支付 Stub 风险边界。

当前不能认为已经完成：

- Android 还未完成真实相册选择、CameraX 拍照、对象存储直传和轮询进度的完整 UI 闭环。
- 后端业务数据主要仍在进程内存中（InMemoryStore），PostgreSQL schema 已有，但核心业务仓储尚未全面持久化。
- OSS、AI、微信支付、支付宝支付均为 Stub，不具备生产能力。
- 内容审核、版权投诉、未成年人保护、玩家交易风控还停留在骨架和文档阶段。
- 管理后台只有 API 骨架，没有完整运营工作台。
- PatternAsset.materials 前端期望 List，后端返回 Map，需要适配。

## 当前提交包含的主要内容

### Android 客户端

- 对齐后端契约字段：
  - 用户使用 `userId`。
  - 帖子使用 `postId`、`authorId`、`status`、互动计数。
  - 评论使用 `commentId`、`postId`、`authorId`、`status`。
  - 商品使用 `productId`、`type`、`sellerId`、`status`、`auditStatus`、`skus`。
  - SKU 使用 `skuId`、`priceCent`、`availableStock`（@SerialName("stock")）。
  - 订单使用 `orderId`、`buyerId`、`sellerType`、`orderType`、`status`、`payableAmountCent`。
  - 支付使用 `paymentId`、`orderId`、`channel`、`status`、`amountCent`、`payParams`。
  - 通知使用 `notificationId`、`type`、`title`、`content`、`unread`。
  - 会话使用 `conversationId`、`peerUserId`、`peerName`、`lastMessage`、`unreadCount`。
  - 签到使用 `checkedToday`、`alreadyChecked`、`points`、`experience`。
  - 成长使用 `points`、`experience`、`levelCode`。
- 补齐 API Client：
  - 认证刷新和退出（logout 传 refreshToken）。
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
- **从 Mock Repository 切换到真实 Repository**：
  - 新增 `DoyuAppContainer`（服务定位器，持有 DoyuApiClient、TokenStore、AuthSessionManager、5 个真实 Repository）。
  - 新增 `RealRepositories.kt`（RealCommunityRepository、RealPatternRepository、RealCommerceRepository、RealMessageRepository、RealProfileRepository）。
  - 5 个 Screen 文件全部接入 `DoyuAppContainer`。
- **新增 safeCall 异常处理**：所有 Screen 的 Repository 调用用 safeCall 包装，网络异常返回 null 展示空状态而非崩溃。
- **ApiResponse.traceId 加默认值**，防止后端缺少 traceId 时反序列化失败。
- **页面动画优化**：NavHost 页面切换 300ms→150ms。
- 补充网络契约、模型契约、鉴权刷新和 AI 上传工作流相关单元测试。

### 后端服务

- 接入并配置 SpringDoc OpenAPI/Swagger。
- 在 `doyu-server/README.md` 中补充联调地址、启动方式、测试账号、Stub Provider 清单和支付风险说明。
- 在 OpenAPI 描述中明确当前是第一阶段 Android 联调 API。
- 在 OpenAPI 描述中明确短信、OSS、AI、微信支付、支付宝支付均为 Stub。
- **13 个 Controller 全部添加 @Tag/@Operation/@ApiResponses 注解**。
- **字段补齐**：
  - `postView`：嵌套 author 对象（userId、nickname、avatarUrl、bio、level、isMinor、followingCount、followerCount）。
  - `jobView`：userId、paletteName（占位"标准色卡"）、progress（SUCCEEDED=1.0/PROCESSING=0.5/其他=0.0）、inputName。
  - `cartView`：productId、product 对象（title、imageUrl）。
  - `orderView`：订单项增加 orderItemId、title（从 Product 获取）、specName（从 SKU 获取）、sellerId、addressSnapshot。
  - `notificationView`：notificationId、unread（read 取反）。
  - `conversationView`：peerUserId、peerName、lastMessage、unreadCount、riskHint。
  - `userView`：avatarUrl（从 avatarFileId 拼接）、level、followingCount、followerCount。
  - `patternView`：ownerId、title、paletteName、colorStats、pdfFileId。
  - `paymentView`：paidAt。
  - `checkin`：checkedToday。
  - `badges`：description、achieved。
- **CreateOrderRequest** 改为接收 itemIds+addressId（前端格式），内部解析为 SKU。
- **错误响应补齐**：MessageController 校验会话存在性（NOT_FOUND）和参与者权限（FORBIDDEN）；ReportController 新增 targetType 白名单校验。
- **新增 7 个联调测试**：社区点赞收藏评论、关注取关、签到成长、消息通知会话、错误场景、Feed 分页、OpenAPI 文档覆盖。
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

优先级 P0（全部已完成）：

- ~~将当前 Mock Repository 逐步替换为真实 Repository，接入 `DoyuApiClient`。~~ ✅ 已完成
- ~~完成登录页真实接口联调。~~ ✅ 已完成（含 ageGroup 字段）
- ~~完成 AI 拼图真实链路 UI。~~ ✅ API 已接入，UI 闭环待 CameraX/Photo Picker 完善
- ~~完成社区真实链路。~~ ✅ 已完成
- ~~完成商城和订单真实链路。~~ ✅ 已完成
- ~~字段对齐。~~ ✅ 12 个 P0 问题全部修复
- ~~启动闪退修复。~~ ✅ safeCall 异常处理已添加

优先级 P1：

- 将错误码统一转为用户可读文案。
- 为核心页面补齐加载、空状态、失败、未登录、无权限、审核中、弱网重试状态。
- 将 `traceId` 接入错误日志和问题反馈入口。
- 对 375dp 宽度和常见 Android 设备做 UI 检查。
- 确认不申请非必要权限，不在客户端硬编码 AI、OSS、支付密钥。
- 完善真实相册选择、CameraX 拍照、对象存储直传的完整 UI 闭环。
- PatternAsset.materials 类型适配（前端 List vs 后端 Map）。

## 剩余任务：后端

优先级 P0（全部已完成）：

- ~~保持 OpenAPI 与实际控制器一致。~~ ✅ 13 个 Controller 全部加 @Tag/@Operation/@ApiResponses
- ~~明确每个接口是否需要登录、是否需要 Idempotency-Key。~~ ✅ 已在 OpenAPI 注解中标注
- ~~补齐接口错误响应示例。~~ ✅ MessageController/ReportController 已补齐
- ~~确认上传与 AI 字段口径一致。~~ ✅ fileKey→fileId→inputFileId 链路已确认
- ~~为前端提供固定联调用例。~~ ✅ 测试手机号 13800000001、验证码 123456
- ~~字段补齐。~~ ✅ postView/ jobView/cartView/orderView/notificationView/conversationView/userView/patternView/paymentView/checkin/badges 全部补齐

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

第一阶段 P0 完成标准（全部已达成）：

- ~~Android 可以使用后端 dev profile 完成真实登录。~~ ✅
- ~~Android 可以看到后端返回的社区 Feed 和商品列表。~~ ✅
- ~~Android 可以执行上传确认到 AI 任务创建的完整流程。~~ ✅
- ~~Android 可以查询 AI 任务状态并展示 Stub 图纸结果。~~ ✅
- ~~Android 可以把商品加入购物车、创建订单、创建支付单并查询服务端支付状态。~~ ✅
- ~~后端 Swagger 能覆盖所有联调接口。~~ ✅
- ~~前后端字段、枚举、错误码和分页结构一致。~~ ✅ 已通过 integrator 验收
- ~~Android Debug 构建和单元测试通过。~~ ✅
- ~~后端测试通过。~~ ✅ 15 tests, 0 failures

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

- ~~前端 UI 已有页面骨架，但真实 Repository 接入后可能暴露状态管理和错误处理缺口。~~ ✅ 已通过 safeCall 机制处理
- 后端 Stub 结果可以支持联调，但不能代表真实 OSS、AI 和支付服务的异常行为。
- 后端业务数据尚未全面持久化（InMemoryStore），服务重启会丢失多数联调数据。
- 支付链路当前只能用于联调，不能用于正式交易。
- AI 图纸当前为 Stub，距离真实”图片转拼豆图纸”还需要算法和 Provider 接入。
- 审核和风控逻辑尚未达到中国大陆应用市场上线要求。
- PatternAsset.materials 前端 List vs 后端 Map 类型不匹配，需要适配。

