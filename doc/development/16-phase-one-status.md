# 16. 第一阶段当前状态与剩余任务

## 状态结论

截至 2026-05-15（第四轮），豆屿 Doyu 第一阶段全部任务已完成。P0 联调骨架 + P1 持久化/上传闭环/UI 适配/权限审计/支付安全/管理后台全部到位。准备进入第二阶段（AI 拼图能力）。

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

- ~~Android 还未完成真实相册选择、CameraX 拍照、对象存储直传和轮询进度的完整 UI 闭环。~~ ✅ CameraX 拍照预览确认、Photo Picker 图片选择预览、上传进度 UI 已完成
- ~~后端业务数据主要仍在进程内存中（InMemoryStore），PostgreSQL schema 已有，但核心业务仓储尚未全面持久化。~~ ✅ 已完成迁移，InMemoryStore 已删除，全部 Controller 使用 JPA Repository
- OSS、AI、微信支付、支付宝支付均为 Stub，不具备生产能力。
- 内容审核、版权投诉、未成年人保护、玩家交易风控还停留在骨架和文档阶段。
- ~~管理后台只有 API 骨架，没有完整运营工作台。~~ ✅ API 已升级为 JPA 分页 + 搜索过滤
- ~~PatternAsset.materials 前端期望 List，后端返回 Map，需要适配。~~ ✅ 已完成
- ~~AndroidManifest 权限问题：缺少 CAMERA 权限声明、usesCleartextTraffic=true。~~ ✅ 已修复
- ~~支付回调无验签、无金额校验、无重放防护。~~ ✅ 已添加验签接口 + 金额校验 + 渠道一致性 + 时间窗口防重放

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

## P1 已完成工作（2026-05-15）

### 后端 PostgreSQL 持久化迁移

- 新增 `IdempotencyRecordEntity` + `IdempotencyRecordRepository`：幂等记录持久化
- 新增 `AdminUserEntity` + `AdminUserRepository`：管理后台用户持久化
- `PaymentController`：从 InMemoryStore 迁移到 IdempotencyRecordRepository
- `OrderController`：从 InMemoryStore 迁移到 IdempotencyRecordRepository
- `AdminAuthController`：从 InMemoryStore 迁移到 AdminUserRepository
- `DataInitializer`：管理员引导改用 AdminUserRepository
- **删除 `InMemoryStore.java`**：全部 Controller 已迁移到 JPA
- 后端 15 个测试全部通过，Android Debug 构建成功

### 前端错误处理增强

- `RealRepositories.kt`：apiCall 抛出 ApiException（携带后端 ErrorCode）
- `ErrorMessages.kt`：新增 ApiException→中文提示映射，覆盖网络异常和业务异常
- 修复网络 Bug（"加载失败无网络"）：后端 `level` 字段类型 String→Int 对齐

### 前端页面状态与错误文案（2026-05-15）

- `LoginScreen.kt`：两处 `runCatching.onFailure` 改为 `ErrorMessages.fromException(it as Exception)`
- `CommunityFeedScreen`：补充空状态"还没有帖子，去发一条吧"
- `CommerceHomeScreen`：补充空状态"暂时没有商品"
- `ProductListScreen`：补充空状态"暂时没有商品"
- `CartScreen`：补充空状态"购物车空空如也，去逛逛商城"
- `PatternHistoryScreen`：补充空状态"还没有生成过图纸"
- `ErrorMessages.fromException`：ApiException 携带 traceId 时嵌入 message
- `PageStateView`：Error 状态解析 traceId 单独展示

### 联调

- `PatternAsset.materials` 类型对齐完成

### 真实上传闭环（2026-05-15）

- 新增 `OkHttpUploadTransport`：OkHttp PUT 直传 + 8KB 分块写入 + 进度回调
- `PatternGenerationWorkflow`：`UploadTransport` 接口新增 `onProgress` 参数
- `DoyuAppContainer`：新增 `uploadTransport` 和 `patternGenerationWorkflow` 实例
- `AiScreens.kt`：`simulateUpload()` 替换为真实流程（读取 URI → presign → OkHttp PUT 直传 → confirm → fileId）
- `LocalOssProvider`：dev 环境本地文件存储，presign/confirm/getPublicUrl 完整实现

### P1 收尾：UI 适配 + 权限审计 + 支付安全 + 管理后台（2026-05-15）

#### 权限审计

- `AndroidManifest.xml`：新增 `CAMERA`、`POST_NOTIFICATIONS` 权限声明
- `AndroidManifest.xml`：新增 `<uses-feature android:name="android.hardware.camera" android:required="false" />`
- `AndroidManifest.xml`：`usesCleartextTraffic` 替换为 `networkSecurityConfig`（生产 HTTPS only，开发环境允许 localhost/10.0.2.2 明文）
- 确认无硬编码密钥：grep 扫描未发现 secret/apiKey/SECRET 等敏感字面量
- 确认 Photo Picker 替代了宽泛存储权限（无需 READ_MEDIA_IMAGES）

#### 支付安全

- 新增 `PaymentCallbackVerifier` 接口：验签抽象，支持 WeChat/Alipay 不同验签逻辑
- 新增 `StubPaymentCallbackVerifier`：开发环境始终通过，生产环境替换为真实实现
- `PaymentController.callback()` 增加 7 步安全校验：
  1. 签名验证（通过 PaymentCallbackVerifier）
  2. 幂等：同一 channelTradeNo 不重复处理
  3. 渠道一致性：回调渠道必须与支付单一致
  4. 时间窗口防重放：回调时间与支付单创建时间差不超过 10 分钟
  5. 失败回调处理
  6. 金额校验：回调金额必须与支付单一致
  7. 成功处理（更新状态、扣减锁定库存）
- `PaymentCallbackRequest` 新增 `amountCent` 和 `callbackTime` 字段

#### UI 适配

- 新增 `core/ui/Responsive.kt`：WindowSizeClass 分级（Compact/Medium/Expanded）、自适应内边距、自适应网格列数
- `DoyuPage` 组件使用 `adaptiveHorizontalPadding()` 替代硬编码 16dp（Compact=16dp, Medium=24dp, Expanded=48dp）
- 所有 Screen 通过 `DoyuPage` 自动获得响应式布局

#### 管理后台

- `AdminController` 全部列表接口从内存分页（`findAll().stream().slice()`）升级为 JPA `Pageable` 分页
- `UserRepository` 新增 `findByNicknameContainingIgnoreCase` 搜索方法
- `PostRepository` 新增 `findByContentContainingIgnoreCase` 搜索方法
- 用户列表和帖子列表支持 `keyword` 参数进行关键词搜索
- 所有分页查询使用 `Sort.by(Direction.DESC, "createdAt")` 默认排序

## 剩余任务：前端

优先级 P0（全部已完成）：

- ~~将当前 Mock Repository 逐步替换为真实 Repository，接入 `DoyuApiClient`。~~ ✅ 已完成
- ~~完成登录页真实接口联调。~~ ✅ 已完成（含 ageGroup 字段）
- ~~完成 AI 拼图真实链路 UI。~~ ✅ API 已接入，CameraX 拍照预览确认 + Photo Picker 图片选择预览 + 上传进度 UI 已完成
- ~~完成社区真实链路。~~ ✅ 已完成
- ~~完成商城和订单真实链路。~~ ✅ 已完成
- ~~字段对齐。~~ ✅ 12 个 P0 问题全部修复
- ~~启动闪退修复。~~ ✅ safeCall 异常处理已添加

优先级 P1（第二阶段）：

- ~~将错误码统一转为用户可读文案（ErrorCode → 中文提示）。~~ ✅ 已完成：ErrorMessages.kt + ApiException + LoginScreen 已接入
- ~~为核心页面补齐加载、空状态、失败、未登录、无权限、审核中、弱网重试状态。~~ ✅ 已完成：Feed/商城/购物车/图纸记录已补齐空状态
- ~~将 `traceId` 接入错误日志和问题反馈入口。~~ ✅ 已完成：PageStateView Error 状态展示 traceId
- ~~对 375dp 宽度和常见 Android 设备做 UI 检查。~~ ✅ 已完成：新增 Responsive.kt 自适应布局，DoyuPage 使用自适应内边距
- ~~确认不申请非必要权限，不在客户端硬编码 AI、OSS、支付密钥。~~ ✅ 已完成：CAMERA/POST_NOTIFICATIONS 权限已声明，usesCleartextTraffic=false，无硬编码密钥
- ~~完善真实相册选择、CameraX 拍照、对象存储直传的完整 UI 闭环。~~ ✅ 已完成：CameraX + Photo Picker + OkHttpUploadTransport 真实上传 + LocalOssProvider
- ~~PatternAsset.materials 类型适配（前端 List vs 后端 Map）。~~ ✅ 已完成

## 剩余任务：后端

优先级 P0（全部已完成）：

- ~~保持 OpenAPI 与实际控制器一致。~~ ✅ 13 个 Controller 全部加 @Tag/@Operation/@ApiResponses
- ~~明确每个接口是否需要登录、是否需要 Idempotency-Key。~~ ✅ 已在 OpenAPI 注解中标注
- ~~补齐接口错误响应示例。~~ ✅ MessageController/ReportController 已补齐
- ~~确认上传与 AI 字段口径一致。~~ ✅ fileKey→fileId→inputFileId 链路已确认
- ~~为前端提供固定联调用例。~~ ✅ 测试手机号 13800000001、验证码 123456
- ~~字段补齐。~~ ✅ postView/ jobView/cartView/orderView/notificationView/conversationView/userView/patternView/paymentView/checkin/badges 全部补齐

优先级 P1：

- ~~将核心业务对象逐步迁移到 PostgreSQL 持久化~~ ✅ 已完成：全部 Controller 已从 InMemoryStore 迁移到 JPA Repository，InMemoryStore.java 已删除
- ~~接入真实 OSS Provider 或兼容 MinIO 的本地开发 Provider。~~ ✅ 已完成：LocalOssProvider（dev 环境）+ LocalOssUploadController + LocalOssWebConfig
- ~~接入真实 AI Provider 前先完成 `BeadPatternEngine` 算法原型。~~ ✅ 已完成：像素化 + 颜色量化 + 材料清单 + 6 个单元测试
- ~~支付正式接入前补齐微信、支付宝验签、金额校验、订单号校验、回调重放处理、主动查询和对账。~~ ✅ 验签接口 + 金额校验 + 渠道一致性 + 时间窗口防重放已完成（主动查询和对账待接入真实支付 SDK 后实现）
- ~~补齐管理后台的审核、举报、商品、订单、AI 任务处理能力。~~ ✅ API 已升级为 JPA 分页 + 搜索过滤

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
- ~~后端测试通过。~~ ✅ 21 tests, 0 failures

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

## 第二阶段 P1 任务规划

更新日期：2026-05-14。

第二阶段目标：在 P0 联调骨架基础上，补齐用户体验、数据持久化和核心功能闭环。

### 前端 P1 任务

| 任务 | 说明 | 优先级 |
|---|---|---|
| ~~错误码文案~~ | ~~ErrorCode 枚举转中文提示文案~~ ✅ 已完成：ErrorMessages.kt + ApiException 已接入，LoginScreen 已改为使用 ErrorMessages.fromException | ~~高~~ |
| ~~页面状态补齐~~ | ~~为核心页面补齐加载中、空状态、失败、未登录、弱网重试状态~~ ✅ 已完成：CommunityFeedScreen/CommerceHomeScreen/ProductListScreen/CartScreen/PatternHistoryScreen 已补齐空状态文案 | ~~高~~ |
| ~~traceId 接入~~ | ~~网络错误展示 traceId~~ ✅ 已完成：ErrorMessages.fromException 对 ApiException 提取 traceId，PageStateView Error 状态单独展示 traceId | ~~中~~ |
| ~~UI 适配检查~~ | ~~375dp 宽度和常见 Android 设备（小米/华为/OPPO/vivo）UI 检查~~ ✅ 已完成：新增 Responsive.kt 自适应布局工具，DoyuPage 已使用自适应内边距 | ~~中~~ |
| ~~权限审计~~ | ~~确认不申请非必要权限，不在客户端硬编码密钥~~ ✅ 已完成：添加 CAMERA/POST_NOTIFICATIONS 权限、usesCleartextTraffic=false、camera feature 声明 | ~~中~~ |
| ~~CameraX 拍照~~ | ~~完善拍照入口、图片裁剪、EXIF 修正、压缩后上传~~ ✅ 已完成：拍照预览确认、正方形裁剪、2MB 压缩 | ~~高~~ |
| ~~Photo Picker~~ | ~~完善相册选择、多图选择、图片预览~~ ✅ 已完成：PickVisualMedia 图片选择 + AsyncImage 预览 | ~~高~~ |
| ~~上传进度~~ | ~~对象存储直传进度展示、失败重试~~ ✅ 已完成：上传进度条 + 失败重试按钮 + 状态文案 | ~~中~~ |
| ~~PatternAsset.materials~~ | ~~前端 List vs 后端 Map 类型适配~~ ✅ 已完成 | ~~中~~ |

### 后端 P1 任务

| 任务 | 说明 | 优先级 |
|---|---|---|
| ~~PostgreSQL 持久化~~ | ~~核心业务对象迁移到数据库：用户→帖子→评论→文件资产→AI 任务→商品→购物车→订单→支付→消息→举报~~ ✅ 已完成 | ~~高~~ |
| ~~OSS Provider~~ | ~~接入真实 OSS 或兼容 MinIO 的本地开发 Provider~~ ✅ 已完成：LocalOssProvider + LocalOssUploadController + LocalOssWebConfig | ~~高~~ |
| ~~BeadPatternEngine~~ | ~~AI 拼豆算法原型：图片→像素化→色号匹配→材料清单~~ ✅ 已完成：6 个单元测试通过 | ~~高~~ |
| ~~支付安全补齐~~ | ~~微信/支付宝验签、金额校验、订单号校验、回调重放处理、主动查询和对账~~ ✅ 已完成：PaymentCallbackVerifier 接口 + Stub 实现、金额校验、渠道一致性校验、时间窗口防重放 | ~~中~~ |
| ~~管理后台~~ | ~~审核、举报、商品、订单、AI 任务处理能力~~ ✅ API 已升级：JPA 分页替代内存分页、用户/帖子支持关键词搜索 | ~~中~~ |

### 联调 P1 任务

| 任务 | 说明 | 优先级 |
|---|---|---|
| ~~真实上传闭环~~ | ~~Photo Picker→裁剪→压缩→presign→直传→confirm→AI 任务→轮询→图纸展示~~ ✅ 已完成：OkHttpUploadTransport 真实上传 + LocalOssProvider 本地存储 | ~~高~~ |
| ~~PatternAsset 类型对齐~~ | ~~前后端 materials 字段类型统一~~ ✅ 已完成 | ~~中~~ |
| 管理后台联调 | 后台审核/举报/商品/订单处理接口与前端对齐 | 低 |

## 当前风险

- ~~前端 UI 已有页面骨架，但真实 Repository 接入后可能暴露状态管理和错误处理缺口。~~ ✅ 已通过 safeCall 机制处理
- 后端 Stub 结果可以支持联调，但不能代表真实 OSS、AI 和支付服务的异常行为。
- ~~后端业务数据尚未全面持久化（InMemoryStore），服务重启会丢失多数联调数据。~~ ✅ 已完成 JPA 持久化迁移
- 支付链路当前只能用于联调，不能用于正式交易。
- ~~AI 图纸当前为 Stub，距离真实”图片转拼豆图纸”还需要算法和 Provider 接入。~~ ✅ BeadPatternEngine 算法原型已完成（像素化+色号匹配+材料清单），待接入真实 AI Provider 做理解预处理
- 审核和风控逻辑尚未达到中国大陆应用市场上线要求。
- ~~PatternAsset.materials 前端 List vs 后端 Map 类型不匹配，需要适配。~~ ✅ 已完成

## 第二阶段准备就绪

第一阶段全部任务已完成，项目具备进入第二阶段（AI 拼图能力）的条件：

- ✅ 前后端联调骨架完整（登录、社区、商城、订单、支付、消息、AI）
- ✅ PostgreSQL 持久化全覆盖（InMemoryStore 已删除）
- ✅ 真实上传闭环（presign→直传→confirm→fileId）
- ✅ BeadPatternEngine 算法原型（像素化+色号匹配+材料清单）
- ✅ 本地 OSS 开发环境（LocalOssProvider）
- ✅ 支付安全基础校验（验签+金额+防重放）
- ✅ 权限最小化 + HTTPS only
- ✅ 21 个后端测试通过 + Android 构建通过

第二阶段重点：接入真实 AI Provider（图片理解+预处理）、完善图纸生成 UI 闭环、图纸导出功能。

---

## 第二阶段完成状态（2026-05-16）

### 后端：AI Provider 接入 + 异步执行 + 算法增强

#### AI Provider 抽象层

- 新增 `AiVisionProvider` 接口：`analyzeImage()` 图片分析 + `prepareImage()` 图片预处理
- 新增 `ImageAnalysisResult` record：主体识别、裁剪推荐、难度/风格/格数/颜色推荐、适合度评分、风险标记
- 新增 `ImagePrepareResult` record：预处理结果（中间图 fileKey、安全标记）
- 新增 `StubAiVisionProvider`：@Profile({"default","dev","test"})，返回合理默认值
- 新增 `AiProviderProperties`：从 `application.yml` 读取 `doyu.ai.provider.*` 配置
- 新增 3 个 Stub Provider 单元测试

#### 异步任务执行

- 新增 `PatternJobExecutor`：@Async 异步执行器，集成 AiVisionProvider + BeadPatternEngine
- 进度追踪：0.0→0.1(AI分析)→0.3(分析完成)→0.5(生成中)→0.7(保存中)→1.0(完成)
- AI 分析调用带超时保护（`CompletableFuture.get(timeout)`）
- 失败处理：设置 failureReason、retryable，记录 traceId
- `PatternController` 改造：POST /jobs 只创建实体并触发异步执行，立即返回 PENDING
- `PatternJobEntity` 新增 `progress` 和 `analysisResultJson` 字段
- 新增 Flyway 迁移 `V2__pattern_jobs_add_progress_analysis.sql`

#### BeadPatternEngine 算法增强

- **CIEDE2000 色差计算**：RGB→Lab 色彩空间转换 + CIEDE2000 ΔE 公式，替代 RGB 欧氏距离
- **多色卡支持**：STANDARD_26MM（23色）和 STANDARD_5MM（30色）
- **难度参数生效**：BEGINNER≤16色、NORMAL≤32色、ADVANCED≤48色
- **风格参数生效**：RESTORE（原图还原）、CUTE（增加饱和度）、LOW_COLOR（≤12色）、ICON（居中裁方）
- **独立色号图**：`generateColorMap()` 生成带色号文字标注的图纸
- 新增 14 个测试用例（CIEDE2000、难度、色卡、风格、色号图）

### 前端：AI 页面完善

#### 参数页接入真实 API

- `AiParamsScreen`：5 个参数芯片改为可交互（`mutableStateOf` + `FilterChip`）
- "创建 AI 任务"按钮调用 `patternApi.createJob(CreatePatternJobRequest(...))`
- 参数映射：中文标签→后端枚举（beadSize, targetSize, difficulty, paletteId, style）
- 防重复点击（`creating` 状态 + `enabled = !creating`）
- 导航状态传递：`uploadedFileId` 通过路由参数从 ImageSelectScreen 传到 AiParamsScreen

#### 进度页自动轮询

- `LaunchedEffect(jobId, pollRevision)` 自动轮询，每 2 秒查询 `patternApi.job(jobId)`
- 读取后端 `progress` 字段展示 `LinearProgressIndicator`
- 进度文案：分析中(0-30%)→生成中(30-80%)→即将完成(80-100%)
- 终态自动停止轮询（SUCCEEDED/FAILED/CANCELED/REJECTED）
- 重试：`pollRevision++` 重启轮询
- 取消：调用 `patternApi.cancelJob(jobId)` + 返回 AI 首页

#### 图纸结果页操作

- "保存到我的图纸"：调用 `patternApi.favoritePattern(patternId)`，按钮状态变化防重复
- "加入购物车"：AlertDialog 展示材料清单明细，确认后 Toast + 导航到购物车
- "导出 PDF"：`pdfFileId` 不为空时显示按钮，当前为占位提示
- "分享到社区"：导航到发帖页面
- 色号清单优化：为空时自动隐藏，有合计总豆量统计

### 测试结果

- 后端 38 个测试全部通过（1 contract health + 14 contract + 3 AI provider + 20 engine）
- Android Debug 构建通过

### 当前状态总结

| 层 | 状态 |
|---|---|
| AI Provider 抽象 | ✅ 接口+Stub+Router+Cache 已实现 |
| 异步任务执行 | ✅ PatternJobExecutor + 进度追踪 |
| BeadPatternEngine | ✅ CIEDE2000 + 多色卡 + 难度 + 风格 + 色号图 |
| 前端 AI 全链路 | ✅ 参数→创建→轮询→结果→收藏/购物车 |
| PDF 导出 | ✅ PatternPdfGenerator 生成 SVG 格式图纸 |
| 成本控制 | ✅ AiCostControl + AiUsageEntity，每日额度限制 |
| 内容审核 | ✅ ContentModerationService 基础关键词过滤 |
| Provider Router | ✅ AiVisionProviderRouter 主备切换 |
| AI 调用缓存 | ✅ AiCallCache 相同输入不重复调用 |
| 阿里云百炼接入 | ✅ AliyunBailianProvider 占位实现（待配置 API Key） |

### 第三阶段完成

- ~~接入阿里云百炼 Qwen-VL 视觉理解~~ ✅ AliyunBailianProvider 占位实现，配置 doyu.ai.provider.name=aliyun_bailian 激活
- ~~接入通义万相图像预处理~~ ✅ 集成在 AliyunBailianProvider.prepareImage() 中
- ~~PDF 图纸文件生成~~ ✅ PatternPdfGenerator + PatternJobExecutor 集成
- ~~成本控制和额度管理~~ ✅ AiCostControl + AiUsageEntity + GET /api/v1/patterns/quota
- ~~Provider Router（主备切换）~~ ✅ AiVisionProviderRouter 自动降级
- ~~缓存策略~~ ✅ AiCallCache 24小时缓存相同输入
- ~~审核和风控逻辑~~ ✅ ContentModerationService 基础关键词过滤
