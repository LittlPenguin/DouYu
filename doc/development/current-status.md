# 当前状态

> 更新日期：2026-05-21
> 用途：记录当前工程事实、UI 重构阶段、功能完成度、不可用边界和下一步优先级。产品目标和商业边界以 `../豆屿App商业技术执行计划.md` 为准。

## 阶段结论

豆屿 Doyu 当前进入 **第二轮 UI 统一基线 / UI MVP 收敛阶段**。

第一轮已经把 **文档契约 + 登录/社区样板链路** 稳住：登录 + 社区接口以 `doc/development/05-api-contract.md` 为唯一契约源，社区 Feed、详情、发布、评论、点赞和收藏已作为可复制样板推进。第二轮不继续扩大接口面，而是把同一套 UI 方法推广到 App Shell、通用组件、消息页和我的页。

本阶段默认策略：

- 采用 **MVP 收敛**，不是全入口保留，也不是重做全部信息架构。
- 第二轮只聚焦 Android UI 基线：App Shell、通用组件、消息页、我的页和对应文档/QA。
- 登录 + 社区接口继续以 `doc/development/05-api-contract.md` 为唯一契约源。
- 视觉参考使用 `doc/stitch_document_app_generator/` 下 Stitch 设计稿。
- 最终 UI 权威规范仍沉淀到 `doc/development/11-ui-style-guide.md`。
- 暂不做真实 AI Provider、真实微信/支付宝支付、生产合规上线、增强审核风控、玩家交易完整闭环。

## 功能完成度矩阵

| 模块 | 当前已有 | 半成品 / 问题 | MVP 收敛策略 |
|---|---|---|---|
| App Shell | 5 个主 Tab：社区、商城、AI 创作、消息、我的；NavHost 和底部导航已存在 | 各页视觉风格仍需统一，底部栏、TopBar、卡片、搜索和状态页体验不完全一致 | 第二轮统一 `DoyuPage`、`DoyuTopBar`、底部 5 Tab、FAB、卡片、按钮、Chip、搜索、加载/空/错/未登录状态 |
| 登录 | 短信登录、验证码 Stub、token 刷新接口、退出登录接口存在；Android DTO 默认传 `AGE_18_PLUS` | Android 使用 `InMemoryTokenStore`，重启后登录态丢失；DataStore 持久化未完成 | 继续保持 `ageGroup=AGE_18_PLUS` 契约不变；DataStore 登录态持久化作为后续 P1 小任务 |
| 社区 | Feed、帖子详情、评论列表、发帖、评论、点赞/取消、收藏/取消后端接口存在；Android 样板页已接 Repository 写操作 | 图片 fileId/媒体预览策略不完整；举报 UI 暂不开放；完整 MVVM 拆分和全页面一致性仍待做 | 保留第一轮样板链路，作为后续页面状态、错误和禁用能力表达的参考 |
| AI 图纸 | 上传、创建任务、轮询、取消、生成记录、图纸详情、收藏、材料清单、PDF 文件记录等开发态能力存在 | 真实视觉 Provider 未完成；部分结果操作如材料加购、PDF 导出、分享到社区在 Android 上仍可能是假成功或开发中 | 第二轮不改 AI 主链路；后续只允许保留开发态图纸生成，不承诺真实视觉理解质量 |
| 商城 | 商品列表/详情、购物车、订单、支付单和支付查询骨架存在 | 地址管理未完成；真实微信/支付宝 SDK 未接；玩家商品不支持标准购物车；支付页容易误导为真实支付 | 第二轮不重构商城深链；支付仍只展示联调支付单/服务端确认 |
| 消息 | 通知列表、会话列表、会话详情和发送接口骨架存在 | 后端私信是半占位；Android 会话输入框/发送/举报存在空点击或假闭环风险；列表状态和未读层级需统一 | 第二轮把消息页做成通知/私信样板：列表密度、未读状态、空/错/未登录状态清楚；发送未闭环时禁用或明确边界 |
| 我的 | 用户资料、图纸、收藏、订单、签到状态、徽章接口部分存在 | 签到动作、成长系统、设置合规入口等 UI/链路不完整；部分入口空点击 | 第二轮把我的页做成个人资产中心样板；入口分组为资料、创作资产、交易资产、设置/安全，未实现入口隐藏、禁用或标注开发态 |
| 设置 / 合规 | 后端有账号注销等接口，文档已有合规要求 | 隐私政策、用户协议、SDK 清单、版权投诉、备案材料不是本阶段目标 | 设置页只保留必要结构和低优先级入口，不包装成生产合规已完成 |

## 已完成基础

- Android 客户端已有 5 个主 Tab：社区、商城、AI 创作、消息、我的。
- Android 页面已接入真实 Repository 和后端 `/api/v1`，保留 Mock 数据用于测试和预览。
- 后端为 Spring Boot 模块化单体，核心业务对象已迁移到 PostgreSQL + JPA Repository。
- 本地开发上传链路已具备：后端签发上传地址，Android 直传，后端确认文件资产。
- AI 拼豆任务支持异步执行、进度追踪、图纸预览、色号图、材料清单和 PDF 文件生成。
- BeadPatternEngine 已实现 CIEDE2000 色差、多色卡、难度和风格参数。
- 管理后台 API、举报处理、基础内容审核、AI 调用额度、支付回调安全骨架已存在。
- `doc/stitch_document_app_generator/` 提供了社区、商城、AI、消息、我的页面视觉参考和 `DESIGN.md` 设计系统探索稿。
- `05-api-contract.md` 已明确登录 + 社区第一轮契约：`avatarUrl`、`ageGroup`、发帖/评论 `REVIEWING`、`PostInteractionResult`、统一错误和 `traceId` 映射。
- 第二轮已把 App Shell、通用组件、消息页和我的页落成 UI 统一基线，不把 AI、支付和上线生产化纳入本轮实现。

## 当前不能认为完成

- App Shell、消息页和我的页已完成第二轮代码基线，TopBar、Search、消息未登录态和退出登录返回已列为本轮视觉 QA 对象；仍需要 360dp、390dp、430dp 手工验收。
- 已定位 ADB 路径 `D:\AndroidChace\platform-tools\adb.exe`，但当前 `adb devices` 无在线模拟器/真机；本轮截图视觉 QA 标记为阻塞，不能写成通过。
- Android debug API Base URL 已支持 `.env` 构建期注入，但本地仍需在 `.env.emulator` / `.env.phone` 模板间切换并重新构建；登录态仍使用内存 TokenStore。
- Android Studio / Gradle JVM 如果误选到 VS Code Red Hat Java 扩展内置精简 JRE，会触发 `jlink.exe does not exist`；本地构建必须按协作规范选择完整 JDK/JBR 21。
- 社区、消息和我的样板链路已开始消除空点击和假成功；商城、AI 仍需继续搜索空点击、假成功 Toast、开发中按钮或弱占位。
- 文件预览 URL 策略不完整，帖子图、图纸预览、头像等 fileId 不一定能直接展示为图片。
- 真实 AI Provider 未接入完成。`AliyunBailianProvider` 仍是占位实现，尚未调用真实阿里云百炼/通义万相 API。
- 微信支付和支付宝支付仍是 Stub。当前实现不能用于真实交易收款、退款或对账。
- 内容审核和交易风控仍是基础能力，未达到中国大陆应用市场上线要求。
- 玩家二手/定制交易仍缺完整闭环，包括真实实名校验、发布限制、评价、纠纷处理和风控策略。
- 管理后台还只有后端 API，没有完整运营工作台前端。
- 合规备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场材料尚未完成。

## 当前技术口径

- Android：Kotlin、Jetpack Compose、单 Activity、Navigation Compose、Retrofit、OkHttp、Kotlinx Serialization、Coil、CameraX、Photo Picker。
- Android 当前依赖装配：`DoyuAppContainer` 服务定位器；MVVM 是目标架构，不代表所有页面已完全 ViewModel 化。
- Android 本地联调默认模拟器使用 `10.0.2.2`；真机使用电脑当前 Wi-Fi/LAN IPv4。debug 包的 Retrofit `baseUrl` 由仓库根目录 `.env` 在 Gradle 构建期写入 `BuildConfig.API_BASE_URL`，切换模拟器/真机模板后必须重新构建。
- 后端：Java 21、Spring Boot、Spring Security、JWT、Spring Data JPA、Flyway、PostgreSQL、Redis。
- 后端端口：`8081`。
- PostgreSQL 宿主机端口：`5433`，容器内端口仍为 `5432`。
- API 前缀：`/api/v1`。
- 本地 OSS：开发环境使用 Local OSS Provider，生产仍需接入真实对象存储。
- AI：自研拼豆算法已落地，视觉理解 Provider 仍需真实接入。
- 支付：支付单和回调骨架已存在，真实微信/支付宝 SDK/API 未接入。

## 本轮明确不做

- 不接入真实 AI Provider，不做供应商 API Key、模型选择、真实视觉理解质量验收。
- 不增强生产级内容审核、图片审核、版权识别、诈骗识别或交易风控。
- 不接入真实微信支付、支付宝支付、退款、对账或支付 SDK。
- 不补齐备案、隐私政策、用户协议、SDK 清单、版权投诉等生产合规材料。
- 不补完整玩家二手/定制交易闭环、担保、评价、纠纷、提现或卖家资质审核。
- 不新增后端公共 API；后续源码阶段优先接入已有 `/api/v1` 能力。
- 不重构 AI 主链路、支付链路、商城深链和后端服务。

## 下一步优先级

P0：第二轮 UI 统一基线验收

- 复查 `DoyuPage`、`DoyuTopBar`、底部 5 Tab、FAB、卡片、按钮、Chip、Search、加载/空/错误/未登录状态在主要页面的表现。
- 手工验收消息页：通知/私信 Tab、列表密度、未读状态、空/错/未登录状态；私信发送未闭环时禁用或明确边界。
- 手工验收退出登录返回：退出后返回路径明确，受保护页面回到未登录态或登录引导。
- 手工验收我的页：个人资产中心结构、中文化、卡片层级、未闭环入口隐藏/禁用/明确开发态。
- 在 360dp、390dp、430dp 下复查顶部栏、底部栏、列表项、按钮文字和卡片不挤压、不重叠。

P1：保留并复查第一轮登录 + 社区基线

- 保持 `05-api-contract.md`、后端 Controller/DTO、Android DTO/Repository、UI 和测试一致。
- 登录继续传 `ageGroup=AGE_18_PLUS`，登录响应用户字段使用 `avatarUrl`。
- 社区 Feed、详情、发布、评论、点赞、收藏均有明确状态和错误映射。
- 避免第二轮组件调整破坏社区样板链路。

P2：开发态主链路联调

- AI 上传、创建任务、轮询、失败、取消、成功结果页可跑，但仍只表达开发态图纸生成。
- 商城商品、购物车、订单、支付单状态边界清楚，不表达真实支付完成。
- 消息、我的、收藏、订单、设置没有空点击和误导性成功文案。

P3：生产化补强

- 真实 AI Provider、真实支付、审核风控、合规材料、玩家交易闭环和运营后台前端放到 UI MVP 之后单独规划。
