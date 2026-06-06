# 03. Android 客户端实现方案

## 当前定位

Android 客户端负责豆屿的主要用户体验：社区浏览与发布、作品详情评论、AI 图纸生成、商城购买、消息私信、通知、我的资产和设置入口。当前阶段已开始按 Open Design A 方向落地 UI Shell 与核心页面，同时保持已有登录态、Repository、接口契约、商城/支付边界和上传链路不回退。

## 当前技术栈

- Kotlin。
- Jetpack Compose。
- 单 Activity。
- Navigation Compose。
- Retrofit + OkHttp。
- Kotlinx Serialization。
- Coil。
- CameraX。
- Android Photo Picker。
- DataStore 登录态持久化。

## 当前模块结构

| 包 / 文件 | 职责 |
|---|---|
| `app` | `MainActivity`、`DoyuApplication`、应用入口 |
| `core/navigation/DoyuApp.kt` | NavHost、底部 Tab、页面切换动画 |
| `core/navigation/Routes.kt` | 当前 Android 路由常量 |
| `core/network/ApiInterfaces.kt` | Retrofit API 接口 |
| `core/model/Models.kt` | DTO、枚举、序列化模型 |
| `core/data` | Repository、真实实现、Mock、SafeCall、AppContainer |
| `core/ui` | 通用组件、状态页、按钮、卡片和主题 |
| `feature/auth` | 登录 |
| `feature/community` | 社区、发帖、作品详情、评论 |
| `feature/ai` | AI 图纸流程 |
| `feature/commerce` | 商城、购物车、订单、支付状态 |
| `feature/message` | 消息、通知、会话 |
| `feature/profile` | 我的、资产、设置 |

## 当前路由事实

底部主 Tab：

| Tab | route | 当前含义 |
|---|---|---|
| 社区 | `community` | Feed 与社区入口 |
| 商城 | `commerce` | 商品列表与商城入口 |
| AI | `ai` | AI 创作入口；Stage 8 底栏文案已追设计图收敛为 `AI` |
| 消息 | `message` | 通知与会话入口 |
| 我的 | `profile` | 用户资料和个人资产入口 |

当前已注册二级路由：

| 路由 | 页面 |
|---|---|
| `splash` | 启动页 |
| `login` / `login_return?returnTo={returnTo}` | 登录及登录后返回 |
| `post/{postId}` | 作品详情 |
| `post_create` | 发帖 |
| `search` | Search UI-only / 局部能力聚合页 |
| `profile_edit` | 编辑资料，调用现有 `PATCH /api/v1/users/me` |
| `image_select` / `camera_capture` | 选图 / 拍摄 |
| `ai_params/{uploadedFileId}` | AI 参数 |
| `ai_progress/{jobId}` | AI 进度 |
| `pattern/{patternId}` | 图纸结果 |
| `pattern_history` | 图纸历史 |
| `product_list` / `product/{productId}` | 商品列表 / 详情 |
| `cart` | 购物车 |
| `order_confirm` | 订单确认 |
| `payment_result/{orderId}` | 支付状态 |
| `conversation/{conversationId}` | 私信会话 |
| `my_patterns` / `favorites` | 我的图纸 / 收藏图纸 |
| `liked_posts` / `commented_posts` / `favorite_posts` / `followed_posts` | 当前代码已有的个人互动作品页 |
| `my_orders` | 我的订单 |
| `settings` | 设置 |

已注册但仍有边界的设计目标入口：

- 上传帖子新版 UI-only 原型：对应 `post-compose-a.html`，当前代码已有 `post_create`，已补图片、正文、话题和审核前预览结构，但真实多图/视频发布仍未闭环。
- Settings 分区页：当前已注册 `settings_section/{section}`，只表达开发态 / 待补说明，不代表隐私政策、用户协议、备案、SDK 清单等生产合规材料已完成。
- Notification Detail：当前已注册 `notification_detail/{notificationId}`，只展示通知列表已返回的数据，不代表已有独立通知详情后端、已读回写或跳转目标闭环。
- 未来地图、真实支付、大模型生图页面：只作为 UI-only 参考。

## Repository 与 API 依赖

当前真实 Repository：

| Repository | 依赖 API |
|---|---|
| `RealCommunityRepository` | `CommunityApi`、`UploadApi`、用户/话题/贴纸相关接口 |
| `RealPatternRepository` | `PatternApi`、`UploadApi` |
| `RealCommerceRepository` | `ProductApi`、`CartApi`、`OrderApi`、`PaymentApi` |
| `RealMessageRepository` | `MessageApi` |
| `RealProfileRepository` | `UserApi`、`RewardApi`、`PatternApi` |

社区互动规则：

- `Post` 进入 Feed、话题作品列表和详情时，Android 以服务端返回的 `likedByMe`、`favoritedByMe`、`followedAuthorByMe`、`likeCount`、`favoriteCount` 初始化 UI。
- 点赞、取消点赞、收藏、取消收藏后，Android 使用 `PostInteractionResult.likeCount` / `favoriteCount` 和 `likedByMe?` / `favoritedByMe?` 作为最终显示，不再用本地 `displayLikeCount + 1/-1` 作为权威结果。
- 如果后端响应缺少可选 `likedByMe?` / `favoritedByMe?`，客户端可临时用 `liked` / `favorited` 兼容旧响应；计数仍优先使用服务端返回字段。
- 从详情退出再进入、Feed 刷新、话题作品列表和个人互动作品页都不得把已点赞 / 已收藏状态重置为未互动。

后续 UI 重构原则：

- 优先复用现有 Repository，不为了 UI 改造新增后端 API。
- 后端已有但 Android 未接的接口，先在文档中标为 P0/P1 接入任务，再实施。
- 后端和客户端均未闭环的能力，只能隐藏、禁用或展示 UI-only / 开发态说明。
- 不允许空 `onClick`、假成功 Toast、可点击但无结果的入口。

## UI Shell 重构目标

Stage 1 当前已落地：

- 主页面顶部栏：左侧新增 icon，中间页面标题，右侧搜索 icon。
- 新增快捷菜单：固定包含 Settings、AI 创作、上传帖子。
- 五个主 Tab：社区、商城、AI、消息、我的均已使用 `DoyuMainTopBar`。

Stage 8 的 UI 视觉对齐规则：

- 底部导航采用混合规则：容器样式、圆角、阴影、选中背景、短横、显隐和导航行为保持当前 Android 真机 UI；icon 语义和文字追 Open Design / SVG。
- 底栏目标五项文案为 `社区 / 商城 / AI / 消息 / 我的`；当前 Android 已按该目标显示 `AI`，不回退底栏容器样式。
- 除底部导航容器样式外，主页面和流程页继续按 Open Design A 页面稿做视觉对齐。
- 主页面加载、空、错、未登录、禁用、弱网状态统一使用 `core/ui` 组件。
- 详情页和流程页通常隐藏底部导航，保留返回和明确标题。

## 核心页面 UI 目标

### 社区首页

- 双列瀑布流，类似内容发现应用。
- 卡片图片高度当前已控制在 `120dp-260dp`，按 `postId` 稳定派生，超出裁切，不撑高整屏。
- 卡片展示作者、标题、互动数据、审核中、图片 fallback。
- Feed、关注 Feed、加载、空、错、未登录状态都必须明确。

### 作品详情

目标顺序为 `图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏`。

- 顶部图片区是首屏重点，需要 carousel 指示：`2/5`、左右切换或滑动提示、缩略图 strip。
- 内容区在图片后，展示作者、标题、正文、话题、点赞、收藏、关注状态。
- 评论区必须是实际区域，不只是输入工具条。
- 评论列表需要支持文字、图片、@ 用户、# 话题、贴纸和图片加载失败占位。
- 评论列表只渲染公开 `VISIBLE` 评论；`REVIEWING`、`SELF_VISIBLE`、`PRIVATE`、`REJECTED` 和 `DELETED` 不得作为公开评论展示。若服务端返回的当前页全是审核中评论，Android 显示 `暂无公开评论 / 评论提交后会等待审核，通过后才会公开展示。`。
- 悬浮评论栏默认轻量胶囊，点击后展开；图片/附件预览在上，评论输入在下，工具栏在底部。
- 评论图片最多 9 张；当前已有上限提示。
- 上传失败必须保留缩略图并提供重试；当前失败图会保留并提供“点按重试”入口。
- 多图上传时，单张图片 confirm 成功后必须立即把 `fileId` 回写到当前草稿状态；如果后续图片失败，已成功图片不得在下一次重试中重复上传。
- 纯 @/# 且无正文、图片、贴纸时禁止发送。
- 评论提交成功后只提示 `评论已提交，等待审核`，不本地插入假公开评论，也不为了刷新计数把审核中评论写成公开。

### 发帖

- 当前代码有发帖路由；新版设计见 `post-compose-a.html`。
- 需要覆盖图片选择、正文、话题、预览、上传中、上传失败、审核中提示。
- 未接后端能力不能做空点击；提交成功后展示审核中。
- 当前上传帖子页已补顶部返回、标题、发布按钮、标题/正文输入、图片上传网格、话题 chips、审核前预览、上传中/失败和 9 图上限状态；发布成功只能表达等待审核，不插入假公开帖子。

### Search

- `search` 已注册 Android 路由，是 UI-only / 局部能力聚合页，不代表已有全局搜索后端。
- 当前聚合社区作品、商品本地筛选、用户搜索和话题搜索，不宣称全站搜索。
- 搜索范围 Tab：全部、作品、图纸、商品、用户、话题。

### 消息

- 消息页拆分私信和通知。
- 消息首页按 `messages-a.html` 收敛为私信/通知分区、未读对话区域和通知预览区域；通知详情只使用列表已返回数据，不代表独立详情 API。
- 通知区域不能展示会话输入状态。
- 私信详情需要处理互关正常聊天、未互关剩余 3 条、超过 3 条输入禁用、发送失败、加载失败、空会话。
- 当前发送返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED` 时，Android 会刷新会话并显示互关限制错误。

### 我的

最新设计目标：

- 统计项为 `获赞 / 作品 / 关注 / 粉丝`。
- `我的图纸 / 点赞作品 / 收藏作品` 放在同一 Tab 区域。
- 编辑资料入口已跳转 `profile_edit`，保存调用现有 `PATCH /api/v1/users/me`。
- 最新设计稿不展示“我的订单 / 评论作品 / 关注作品”作为首屏入口；这不代表当前 Android 代码已删除已有路由。
- 当前“获赞”暂无后端真实获赞字段，暂用现有奖励点数展示；后续若要真实获赞数需补服务端字段和契约。

## 登录与会话规则

- 短信登录请求仍传 `ageGroup=AGE_18_PLUS`。
- DataStore 是当前登录态持久化事实。
- 启动时 hydrate Token；退出、refresh 失败和 401 必须清理本地会话。
- 未登录访问受保护入口必须展示统一登录引导，不能把 401 包装成普通网络失败。

## 上传规则

上传链路固定为：

1. `POST /api/v1/uploads/presign`
2. PUT 到返回的 `uploadUrl`
3. `POST /api/v1/uploads/confirm`
4. 业务请求使用 `fileId`

客户端要求：

- 按用途选择 `UploadUsage`，评论图片使用 `POST_IMAGE`。
- 上传失败可重试，不允许假成功；已上传成功的单张图片必须保留 `fileId`，避免批量上传部分失败后重复上传。
- 不在客户端拼接生产 CDN 地址。
- 不保存 OSS Secret。

## AI 拍照与选图规则

- AI 继续使用 CameraX 和 Photo Picker，不接真实 AI Provider。
- CameraX 拍照时通过设备方向更新 `ImageCapture.targetRotation`；拍照后直接返回原始 `Uri` 给 AI 预览流程，不再二次解码、裁切或压缩重写导致 EXIF/方向丢失。
- 相册和拍照共用同一套已选图片预览/上传状态：预览、左转、右转、上传并继续、取消、上传中、上传失败和重试。
- 拍照来源额外显示 `重拍`，相册来源显示 `重新选择`；两者都不得假成功。
- 上传完成后继续走现有 presign、PUT、confirm、AI 参数页、任务进度和结果页链路。

## 支付规则

当前支付是 Stub / 联调骨架：

- 创建订单和支付单必须依赖服务端返回。
- 创建订单和支付单写接口使用 `Idempotency-Key`。
- 玩家二手 / 定制服务商品不能进入标准购物车；当前 Android 使用 `productCanUseStandardCart()` 统一该禁用判断。
- 地址管理未闭环时，订单确认 CTA 必须清楚表达缺口，例如 `缺少收货地址 · ¥12.80`，不能伪造默认地址。
- 支付页只能显示联调支付单、服务端状态、`paymentId`、`orderId`、`channel`、`amountCent`。
- `payParams.provider=STUB` 必须显示为联调边界，不得展示成真实微信/支付宝 App 支付；当前文案为 `payParams.provider=STUB，不代表真实微信或支付宝收款。`。
- 支付最终状态以服务端查询为准。

## 状态管理规则

每个核心页面至少覆盖：

- 加载中。
- 成功。
- 空状态。
- 失败。
- 未登录。
- 无权限。
- 审核中。
- 弱网重试。
- 禁用态和开发态。

后续新写复杂页面应优先使用 ViewModel 管理副作用；已有大文件可以分阶段重构，不要求一次性改完。

## 下一阶段 Android 开发顺序

1. Stage 1：UI Shell、顶部栏、当前 Android 底部导航容器样式、状态页组件。
2. Stage 2：社区首页瀑布流和作品详情重构。
3. Stage 3：发帖、Search、Settings、Profile Edit 的 UI-only / 已接接口边界整理。
4. Stage 4：消息、私信、通知和互关 3 条限制。
5. Stage 5：商城、订单、联调支付边界复查。
6. Stage 6：AI 开发态和未来能力 UI-only 标识。
7. Stage 7：真机 QA、接口回归和文档验收关闭。
8. Stage 8：UI 视觉对齐重构，除底部导航容器样式外严格追 Open Design；底部导航容器样式反向同步到设计图，icon 与文字从设计图同步回 Android。
