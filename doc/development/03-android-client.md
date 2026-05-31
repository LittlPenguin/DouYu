# 03. Android 客户端实现方案

## 客户端目标

Android 客户端负责用户主要体验：社区浏览、发帖、拍照选图、AI 拼豆图生成、商城购买、玩家私信、订单和个人资产。

当前阶段的 Android 工作重心是 **第六轮 UI/品牌与主链路展示收敛**：在第三轮商城 UI/API 主体验收关闭、第四轮登录态持久化和真实图文 seed 收敛、第五轮 Aliyun OSS Provider 骨架补齐之后，继续打磨社区、商城、AI、消息、我的五个主链路的可演示产品感。本轮新增作品详情底部横向评论工具条和图片评论能力，但仍不新增真实 AI Provider、真实支付、地址管理、生产审核、合规风控和完整玩家交易闭环。

## 模块结构

当前工程采用单模块分包，包名按业务域划分：

| 包 | 职责 |
|---|---|
| `app` | 应用入口（`MainActivity`）、导航、依赖装配 |
| `core/model` | 领域数据类和枚举（`Models.kt`，Kotlinx Serializable） |
| `core/data` | Repository 接口 + Mock 实现 + 真实实现 + 依赖容器 |
| `core/network` | Retrofit API 接口、ApiClient 工厂、Auth 拦截器、Token 管理 |
| `core/navigation` | `DoyuApp.kt`（NavHost + 底部栏）、`Routes.kt`（路由常量） |
| `core/ui` | 设计系统组件（`DoyuCard`、`DoyuPrimaryButton` 等）、`UiState` 状态模型 |
| `feature/auth` | 登录页 |
| `feature/ai` | AI 拼豆页面 |
| `feature/commerce` | 商城页面 |
| `feature/community` | 社区页面 |
| `feature/message` | 消息页面 |
| `feature/profile` | 我的页面 |

## 当前实现状态

**依赖注入**：使用 `DoyuAppContainer`（object 单例）作为服务定位器，持有 `DoyuApiClient`、可切换 TokenStore、`AuthSessionManager` 和 5 个真实 Repository 实例。真实 App 启动时由 `DoyuApplication` hydrate `DataStoreTokenStore`，登录、刷新、退出登录和 401 过期清理共用同一个 TokenStore；无 Context 的 Preview / 单元测试场景默认回退内存实现。Debug 包的 Retrofit `baseUrl` 从仓库根目录 `.env` 的 `DOUYU_ANDROID_API_BASE_URL` 在 Gradle 构建期生成到 `BuildConfig.API_BASE_URL`；当前默认只维护真机联调配置，使用电脑当前 Wi-Fi/LAN IPv4，例如 `http://10.64.241.153:8081/`；Retrofit `baseUrl` 必须以 `/` 结尾。

**环境配置**：`.env` 是唯一生效文件，提交模板只有 `.env.example`。本阶段不再维护模拟器/真机双模板；如电脑局域网 IP 变化，直接修改本机 `.env` 后，必须重启后端并重新构建、安装 debug 包，Android 侧的 `BuildConfig.API_BASE_URL` 和 debug HTTP 白名单才会更新。

**真机 HTTP 联调**：Android main 配置保持 HTTPS only；debug 包通过 Gradle 从 `.env` 的 `DOUYU_ANDROID_CLEARTEXT_HOSTS` 生成 `network_security_config.xml`，对当前开发机 IP 添加 `domain-config cleartextTrafficPermitted="true"`。如果真机浏览器能访问后端，但 App 显示网络异常，优先检查 `baseUrl`、debug 包、logcat 中的 cleartext 配置错误。

**屏幕方向**：当前手机端 MVP 固定竖屏，`.MainActivity` 在 Manifest 中使用 `android:screenOrientation="portrait"`。真机系统自动旋转不作为 QA 前置要求；横屏、平板和大屏适配属于后期扩展，不纳入当前验收。

**Repository 层**：已从 Mock Repository 切换到真实 Repository：

- `RealCommunityRepository` → CommunityApi
- `RealPatternRepository` → PatternApi
- `RealCommerceRepository` → ProductApi + CartApi + OrderApi + PaymentApi
- `RealMessageRepository` → MessageApi
- `RealProfileRepository` → UserApi + RewardApi + PatternApi

**当前主要缺口**：

- 登录态已切到 `DataStoreTokenStore` 启动 hydrate；单元测试覆盖保存、恢复和清理，2026-05-30 真机复测已确认登录后强杀重启仍保持登录态、退出登录后强杀重启不会恢复旧登录。
- 多数页面仍在 Composable 内直接管理副作用和 Repository 调用，MVVM 尚未完全落地。
- 多个 Feature 文件体量偏大，页面、子组件、网络状态、副作用混在同一文件中。
- 第一轮已把社区发帖、评论、点赞、收藏补到 `CommunityRepository`，并接入社区样板页。
- 第二轮已把 App Shell、消息页和我的页统一成同一套状态与组件语言。
- 第四轮已把 `Product.imageUrl`、`CartProductSummary.imageUrl` 和 `Post.coverImageUrl` 接到真实 UI：商品卡、商品详情、购物车项、社区 Feed 卡和帖子详情优先显示 Coil 加载的真实图片，图片字段为空或加载失败时回退现有 swatch/拼豆占位，不出现空白卡片。
- 第六轮已把作品详情评论输入收敛为底部固定的横向展开工具条：默认态紧凑，展开态显示图片选择、最多 9 张横向缩略图、单行文字输入、清空/关闭和发送按钮，并使用平滑宽度/内容动画避免突兀跳变。
- 评论支持纯文字、纯图片和图文混合；文字和图片不能同时为空。图片上传复用 `/uploads/presign -> PUT -> /uploads/confirm`，usage 固定为 `POST_IMAGE`；任意图片上传失败时整组评论阻断提交，保留已选缩略图并提示重试。
- 评论列表会渲染后端返回的 `mediaAssets.publicUrl`，图片加载失败时显示拼豆占位，不出现空白卡片。本轮不做视频评论、表情包、@ 用户、图片私信或生产级图片审核闭环。

## 页面导航

底部主导航固定为 5 个 Tab：

- 社区。
- 商城。
- AI 创作。
- 消息。
- 我的。

关键页面：

- 登录/注册。
- 社区 Feed。
- 帖子详情。
- 发帖编辑。
- 图片选择和相机拍摄。
- AI 参数选择。
- AI 任务进度。
- 图纸结果。
- 生成记录。
- 商品列表。
- 商品详情。
- 购物车。
- 订单确认。
- 支付结果。
- 私信会话。
- 我的拼豆。
- 收藏图纸。
- 我的订单。
- 设置。

路由收敛规则：

- 主 Tab 保留底部导航。
- 详情页、拍照页、参数页、订单确认页、支付结果页等流程页隐藏底部导航。
- 不完整的二级入口不得继续暴露为可点击空页面。
- 新增页面前先确认是否属于当前 MVP 范围。

### 页面切换动画

NavHost 页面切换动画时长为 150ms（`tween(TRANSITION_DURATION)`），包括 `enterTransition`、`exitTransition`、`popEnterTransition`、`popExitTransition`。UI MVP 阶段可以保持现有动画，不强制引入 MotionLayout、Lottie 或额外导航动画依赖。

## UI MVP 收敛边界

后续源码重构必须遵守：

- 能真实调用已有后端并返回明确状态的入口，保留并接完整 UI。
- 后端有接口但 Android 未接的入口，优先接入已有 `/api/v1` 接口，不新增后端公共 API。
- 后端/客户端都未闭环的入口，隐藏、禁用或展示明确开发态说明。
- 禁止空 `onClick`、假成功 Toast、可点击但无结果的设置入口。
- 支付只展示联调支付单和服务端确认状态，不包装成真实微信/支付宝支付。
- AI 只展示开发态图纸生成，不承诺真实视觉理解质量。
- 审核、合规、风控、玩家交易只保留必要状态提示，不扩展生产能力。

## 第三轮商城 UI/API 基线

第三轮商城收敛只使用已有 `/api/v1` 能力，不新增后端公共 API，不接真实微信/支付宝 SDK，不补完整玩家交易闭环。

商城首页/商品列表：

- 页面结构对齐 Stitch `_2/screen.png`：顶部搜索栏、分类 Chip、横向 Banner、双列商品卡。
- 搜索如果尚未接后端查询，只能作为本地筛选/占位输入或明确开发态，不得承诺全站搜索。
- 分类 Chip 必须有选中态、空结果态和错误态；切换分类不得造成空白页。
- Banner 只展示真实运营位或开发态素材，不作为可点击空入口。
- 双列商品卡必须展示商品图、标题、价格、库存/销量或状态；库存不足、已售罄、下架要有明确视觉差异。

商品详情：

- 商品类型必须进入 UI 决策：`SELF_OPERATED`、`PLAYER_SECOND_HAND`、`PLAYER_CUSTOM_SERVICE`。
- 自营商品可以选择 SKU 并加入标准购物车，数量不得超过服务端返回库存。
- 玩家二手和玩家定制只展示商品/服务信息，不走标准购物车，不与自营商品混单；加购按钮必须禁用、隐藏或替换为明确的后续咨询/私信边界。
- 玩家商品不得伪装成平台自营履约，不展示“平台担保”“立即支付”等未完成承诺。

购物车：

- 购物车需要登录；未登录进入购物车必须使用统一登录引导。
- 空车、加载失败、库存不足、数量修改、删除购物车项、结算入口都必须有明确状态。
- 购物车只容纳自营商品；发现玩家商品类型时，应阻断加入并展示明确提示。
- 库存边界以服务端返回为准，客户端不能仅靠本地数量判断最终可买。
- `GET /cart` 返回的 `product` 是购物车商品摘要，只包含标题和图片等展示字段，不按完整 `Product` 解析。
- `POST/PATCH/DELETE /cart/items` 返回的是变更结果；`RealCommerceRepository` 写入成功后必须重新调用 `GET /cart` 刷新完整购物车状态。

订单确认：

- `POST /orders` 当前需要 `itemIds` 和 `addressId`；地址管理未闭环时，订单确认页不得伪装“默认地址”。
- 缺少真实地址时，创建订单按钮必须禁用或进入明确的开发态说明，不得创建订单，也不得继续创建支付单。
- 金额、库存、运费和应付金额只展示服务端返回或可明确计算的联调数据，客户端不得自行决定最终金额。
- 订单创建类提交需要 `Idempotency-Key`，避免重复点击创建重复订单。

订单列表和支付状态：

- 订单列表展示服务端订单状态，不用本地“支付成功”覆盖服务端状态。
- 支付状态页必须显式创建联调支付单，并展示 `paymentId`、`orderId`、`channel`、`amountCent`、`status` 和服务端确认文案。
- 当前 `payParams` 是 Stub/占位能力，不能展示为正式微信/支付宝 App 支付参数。
- 支付最终状态以 `GET /payments/{paymentId}` 或订单详情服务端状态为准；客户端不能自动伪装渠道完成态。
- 支付页文案统一使用“联调支付单”“等待服务端确认”“服务端状态”，不得把微信/支付宝渠道完成态写成当前事实。

## 后续 UI 重构优先级

P0：App Shell 和通用组件

- 统一 `DoyuPage`、`DoyuTopBar`、底部 5 Tab、FAB、卡片、按钮、Chip、加载、空状态、错误、未登录和弱网状态。
- 主视觉以 Stitch `_1/screen.png` 为基准：顶部品牌栏、频道 Tab、双列内容流、发布 FAB、圆角底部导航。
- 所有图标使用 Compose Material Icons 或统一线性图标，不使用 emoji。
- 视觉规范以 `11-ui-style-guide.md` 为准。

P1：消息与我的 MVP

- 消息页按 Stitch `_4/screen.png` 收敛通知/私信 Tab、列表密度、未读状态。
- 私信发送若后端仍半占位，则不做强聊天体验；可保留只读会话或禁用发送。
- 我的页按 Stitch `_3/screen.png` 收敛个人资产中心，但必须中文化：帖子、获赞、收藏、我的工坊、订单、收藏、历史。
- 入口分组为个人资料、创作资产、交易资产、设置/安全，真实可用入口可点击，未闭环入口隐藏、禁用或明确开发态说明。
- 设置页保留结构但不表达为生产合规完成。

P2：登录 + 社区样板回归检查

- 第一轮登录 + 社区样板保持稳定，不因第二轮组件调整退化。
- 保持 Feed、帖子详情、发布入口、评论展示。
- 接入已有后端能力：发帖、点赞/取消、收藏/取消、评论发布。
- 发帖/评论提交后显示“审核中”，不假装立即公开。
- 搜索/标签若未接真实查询，则弱化为频道筛选或本地展示，不作为强功能。
- 图片不能继续空白占位；若无法解析 URL，则显示明确的图片处理中或无预览状态。
- 点赞/收藏接口返回 `{ liked }` / `{ favorited }`，Android 使用 `PostInteractionResult` 解码；详情页需要最新计数时重新读取帖子详情。

P3：AI MVP

- 采用 Stitch `ai/screen.png` 页面结构：Hero、正在生成、创作历史。
- 保留上传、参数、创建任务、轮询、失败、取消、结果页、收藏。
- 隐藏或禁用当前假成功项：材料“加入购物车”、PDF 导出、分享到社区，除非后续接入真实链路。
- 结果页必须清楚区分预览图、色号清单、材料清单、开发态提示。

P4：商城 MVP

- 采用 Stitch `_2/screen.png`：搜索栏、品类 Chip、Banner、双列商品卡。
- 保留商品列表/详情、购物车、订单确认、订单列表、支付单状态。
- 自营商品可加购；玩家商品、定制、二手只展示，不走标准购物车。
- 地址管理未完成时，不再伪装“默认地址”，改为开发态占位或禁用下单，不创建订单或支付单。
- 支付页显式创建联调支付单，显示“等待联调回调 / 服务端确认”，不显示正式渠道完成态。

## 可接入接口清单

后续源码重构优先使用已有 API：

| 能力 | 已有 API | Android 后续动作 |
|---|---|---|
| 发帖 | `POST /api/v1/posts` | Repository 暴露 `createPost`，发布页提交后展示审核中 |
| 点赞/取消 | `POST/DELETE /api/v1/posts/{postId}/like` | 帖子卡和详情页接入乐观或确认后更新 |
| 收藏/取消 | `POST/DELETE /api/v1/posts/{postId}/favorite` | 帖子卡和详情页接入 |
| 评论发布 | `POST /api/v1/posts/{postId}/comments` | 详情页评论输入接入，提交后展示审核中 |
| 评论图片 | `/uploads/presign -> PUT -> /uploads/confirm` + `POST /api/v1/posts/{postId}/comments` | 使用 `POST_IMAGE` 上传并提交 `mediaFileIds`，单条评论最多 9 张 |
| 通知已读 | `POST /api/v1/messages/notifications/read` | 消息页切换或点击后标记已读 |
| 私信发送 | `POST /api/v1/messages/conversations/{conversationId}` | 后端仍半占位，UI 不做强聊天体验 |
| 签到 | `POST /api/v1/checkins` | 我的页如接入，必须显示真实成功/已签到状态 |
| 图纸收藏 | `POST /api/v1/patterns/{patternId}/favorite` | 结果页保留收藏 |
| 购物车 | `GET/POST/PATCH/DELETE /api/v1/cart/items` | 自营商品保留，玩家商品禁用标准加购 |
| 订单 | `POST/GET /api/v1/orders` | 下单需处理地址缺口和幂等；无真实地址时禁止伪创建 |
| 支付单 | `POST/GET /api/v1/payments` | 显式创建联调支付单，仅展示服务端确认状态 |

## 状态管理

每个核心页面至少支持：

- 加载中。
- 成功。
- 空状态。
- 失败。
- 未登录。
- 无权限。
- 审核中。
- 弱网重试。

Compose 页面目标是单向数据流：

- UI 只渲染状态。
- ViewModel 处理用户意图。
- Repository 负责网络和缓存。
- 网络错误统一转换为可展示错误。

当前项目仍有页面直接在 Composable 内处理副作用；UI MVP 执行阶段允许渐进式重构，但新增复杂写操作不应继续散落在页面里。

### 通用组件

- `LoginRequiredDialog`（`core/ui/Components.kt`）：统一登录引导弹窗，提示该功能需要登录后使用，按钮为“返回”和“去登录”。
- `PageStateView` / `EmptyContent`：统一加载、空、错误、未登录和弱网状态。
- `DoyuCard` / `DoyuPrimaryButton` / `DoyuOutlinedButton` / `TagChip`：遵守 `11-ui-style-guide.md` 的颜色、圆角、间距和禁用态规则。

### safeCall 异常处理

当前部分 Screen 定义 `safeCall` 包装函数处理网络异常：

```kotlin
private inline fun <T> safeCall(block: () -> T): T? =
    try { block() } catch (_: Exception) { null }
```

后续重构应逐步把错误转换为明确 UI 状态，避免所有异常都退化为空状态。

当前 Android 已在 `core/data/SafeCall.kt` 提供 `exceptionToUiState`：

- `UNAUTHORIZED` -> `UiState.RequireLogin`
- `FORBIDDEN` -> `UiState.Forbidden`
- `SocketTimeoutException`、`UnknownHostException`、`IOException` -> `UiState.WeakNetwork`
- 其他 `ApiException` -> `UiState.Error`，保留可展示的 `traceId`

社区样板页和后续页面应复用该映射，避免把后端统一响应吞成空白页。

## 权限策略

权限申请遵循最小化原则。

| 能力 | 策略 |
|---|---|
| 相机 | 用户拍照时再申请 CAMERA |
| 相册 | 优先使用 Android Photo Picker |
| 通知 | 用户需要订单、评论、AI 完成提醒时申请 |
| 存储 | 避免申请广泛存储权限 |
| 定位 | 第一版不默认申请 |
| 蓝牙/Wi-Fi | 第一版不作为主功能，不申请 |

权限说明必须清楚解释用途，拒绝权限后提供替代路径。

## 相机与相册

拍照：

- 使用 CameraX。
- 支持前后摄像头切换。
- 设备方向追踪，保证竖拍出竖图、横拍出横图。
- 拍摄后进入预览或裁剪流程。
- 自动修正图片方向。
- 大图压缩后再上传。

相册：

- 使用 Photo Picker。
- 只读取用户选择的图片。
- 不要求访问全部媒体库。

图片处理：

- 限制最大边长。
- 限制文件大小。
- 修正 EXIF 方向。
- 显示压缩和上传进度。

## 上传

上传链路：

1. 客户端请求 `/api/v1/uploads/presign`。
2. 后端返回上传 URL、fileKey、headers、过期时间。
3. 客户端直传对象存储。
4. 客户端调用 `/api/v1/uploads/confirm` 获取 `fileId`。
5. 业务提交使用 `fileId`，不能把 `fileKey` 当成业务文件 ID。

要求：

- 上传支持失败重试。
- 大图上传显示进度。
- AI 输入图和公开帖子图分开标记用途。
- AI 图片上传属于受保护动作；未登录时必须展示登录引导或跳转登录，不能把 401 包装成“网络失败”。
- 不在客户端拼接生产 CDN 地址；公开展示 URL 策略以后端或明确的本地开发 URL 为准。

## 登录

登录页（`feature/auth/LoginScreen.kt`）：

- 手机号 + 验证码登录。
- 手机号格式校验：正则 `^1[3-9]\d{9}$`。
- 验证码通过 Stub 环境展示或开发提示获取。
- 当前后端 `SmsLoginRequest` 仍要求 `ageGroup`，Android 现阶段继续传默认 `AGE_18_PLUS`。
- 不得把“登录不传 ageGroup”写成当前事实，除非后端接口已同步修改。

## AI 任务

AI 生成使用异步任务：

1. 上传并确认图片，拿到 `fileId`。
2. 创建任务。
3. 展示排队或处理中状态。
4. 轮询任务详情。
5. 成功后展示图纸。
6. 失败后展示原因和重试入口。

客户端不得直接调用模型供应商。真实视觉 Provider 未完成前，UI 只表达开发态图纸生成，不承诺真实识图质量。

## 支付

当前支付是 Stub / 联调骨架。

UI MVP 阶段支付流程：

1. 客户端创建订单。
2. 客户端请求支付单。
3. 页面展示联调支付单 ID、渠道、金额和“等待联调回调 / 服务端确认支付状态”。
4. 客户端查询后端支付单或订单状态。
5. 后端状态为准。

不得在当前阶段展示正式微信/支付宝渠道完成态，不得把 Stub payParams 当成正式 SDK 参数。

## 缓存与离线

缓存策略：

- Feed 可缓存最近页面。
- 我的资料和配置可缓存。
- AI 任务列表可缓存。
- 订单状态需要每次进入详情刷新。
- 支付状态不得只依赖本地缓存。
- 登录态使用 DataStore 持久化；退出登录、刷新失败和 401 过期清理必须同步清空本机令牌。

## 埋点

第一版建议记录：

- 注册完成。
- 发帖提交。
- 图片上传成功/失败。
- AI 任务创建。
- AI 生成成功/失败。
- 图纸收藏。
- 加入购物车。
- 下单。
- 支付单创建。
- 举报。
- 签到。

埋点不得上传用户原图、隐私文本、支付敏感信息。
