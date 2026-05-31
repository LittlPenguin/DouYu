# 当前状态

> 更新日期：2026-05-30
> 用途：记录当前工程事实、UI 重构阶段、功能完成度、不可用边界和下一步优先级。产品目标和商业边界以 `../豆屿App商业技术执行计划.md` 为准。

## 阶段结论

豆屿 Doyu 当前进入 **第五轮前置 Aliyun OSS Provider 最小闭环阶段**。

第一轮已经把 **文档契约 + 登录/社区样板链路** 稳住：登录 + 社区接口以 `doc/development/05-api-contract.md` 为唯一契约源，社区 Feed、详情、发布、评论、点赞和收藏已作为可复制样板推进。第二轮已经把同一套 UI 方法推广到 App Shell、通用组件、消息页和我的页，形成 UI 统一基线。第三轮聚焦商城首页/商品列表、商品详情、购物车、订单确认、订单列表和支付单状态，商城主体验收已关闭为“可联调、边界清楚、不误导支付”的 MVP 链路。第四轮把 Android 登录态从重启即失效的临时令牌状态收敛到 DataStore 持久化，并为社区和商城常驻 seed 数据提供真实可渲染图文。第五轮前置只补后端对象存储 Provider 切换骨架，让现有上传链路可通过 `.env` 从 Local OSS 切到 Aliyun OSS。

本阶段默认策略：

- 采用 **MVP 收敛**，不是全入口保留，也不是重做全部信息架构。
- 第三轮商城 UI/API 主体路径记录为已关闭；后续仅保留多宽度视觉复查和生产化能力缺口。
- 第五轮前置只聚焦 Aliyun OSS Provider 最小闭环：`DOUYU_OSS_PROVIDER=local|stub|aliyun` 配置切换、Aliyun PUT 预签名、公开 URL 映射、配置测试和联调文档。
- 登录 + 社区接口继续以 `doc/development/05-api-contract.md` 为唯一契约源。
- 视觉参考使用 `doc/stitch_document_app_generator/` 下 Stitch 设计稿。
- 最终 UI 权威规范仍沉淀到 `doc/development/11-ui-style-guide.md`。
- 暂不做真实 AI Provider、真实微信/支付宝支付、生产合规上线、增强审核风控、完整地址管理、玩家交易完整闭环；真实 Bucket 上传 smoke、STS/最小权限、CDN、防盗链、图片审核、缩略图和 seed assets 云迁移也不在第五轮前置内关闭。

## 功能完成度矩阵

| 模块 | 当前已有 | 半成品 / 问题 | MVP 收敛策略 |
|---|---|---|---|
| App Shell | 5 个主 Tab：社区、商城、AI 创作、消息、我的；NavHost 和底部导航已存在；第二轮已统一 App Shell、通用组件、消息页和我的页基线 | 仍需在后续模块扩散时回归顶部栏、底部栏、Search、状态页和响应式表现 | 第四轮改登录态和图文数据时必须复用第二轮 `DoyuPage`、`DoyuTopBar`、底部 5 Tab、卡片、按钮、Chip、搜索、加载/空/错/未登录状态，不另起视觉口径 |
| 登录 | 短信登录、验证码 Stub、token 刷新接口、退出登录接口存在；Android DTO 默认传 `AGE_18_PLUS`；Android 启动时 hydrate `DataStoreTokenStore`，登录、刷新、退出登录和 401 过期清理共用同一个 TokenStore | 仍需后续真机重启路径复测登录保持体验；无 Context 的 Preview / 单元测试场景回退内存实现 | 继续保持 `ageGroup=AGE_18_PLUS` 契约不变；后续重点复测重启后登录保持、过期清理和退出登录返回 |
| 社区 | Feed、帖子详情、评论列表、发帖、评论、点赞/取消、收藏/取消后端接口存在；Android 样板页已接 Repository 写操作 | 图片 fileId/媒体预览策略不完整；帖子列表缺常驻可渲染 `coverImageUrl`；举报 UI 暂不开放；完整 MVVM 拆分和全页面一致性仍待做 | 第四轮补常驻 seed 帖子图和 `coverImageUrl` 契约；保留第一轮样板链路，作为后续页面状态、错误和禁用能力表达的参考 |
| AI 图纸 | 上传、创建任务、轮询、取消、生成记录、图纸详情、收藏、材料清单、PDF 文件记录等开发态能力存在 | 真实视觉 Provider 未完成；部分结果操作如材料加购、PDF 导出、分享到社区在 Android 上仍可能是假成功或开发中 | 第二轮不改 AI 主链路；后续只允许保留开发态图纸生成，不承诺真实视觉理解质量 |
| 商城 | 商品列表/详情、购物车、订单、支付单和支付查询骨架存在；已有 `/products`、`/cart`、`/orders`、`/payments` 联调接口；第三轮主体验收已关闭 | 地址管理未完成；真实微信/支付宝 SDK 未接；玩家二手/定制不支持标准购物车；商品图仍需从色块/空图收敛为 `imageUrl`；seed 商品图片来源记录需可追溯 | 第四轮只补商品 `imageUrl`、购物车商品摘要图片和 seed assets，不改变第三轮支付/地址/玩家商品边界；地址缺口继续明确展示并禁止伪下单；支付状态页仍只展示联调支付单和服务端确认状态 |
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
- 第二轮已把 App Shell、通用组件、消息页和我的页落成 UI 统一基线，不把 AI、支付和上线生产化纳入第二轮实现。
- 第三轮商城 UI/API 主体验收已关闭：商城首页/商品列表对齐 Stitch `_2` 的搜索、分类、Banner 和双列商品卡；自营商品保留标准加购；玩家二手/定制不走标准购物车；订单确认不伪装默认地址；支付状态只展示联调支付单和服务端确认。
- 商城订单 / 支付边界已补契约测试：Android 锁定订单请求和支付响应 DTO，后端锁定订单幂等、支付单 `CREATED` 状态、金额一致和玩家商品不能进入标准购物车/标准订单。
- 第三轮已登录真机 App 内路径已补证：购物车有商品态、订单确认地址缺口、我的订单入口和联调支付状态页均已在真机路径中验证；Android DTO/API 已对齐后端 `SELF_OPERATED`、对象型 `addressSnapshot` 和订单/支付写接口 `Idempotency-Key`。
- Android 已将 `DoyuAppContainer` 切到 DataStore 启动 hydrate；单元测试已覆盖保存、hydrate 和 clear，2026-05-30 真机复测已确认登录后强杀重启仍保持登录态、退出登录后强杀重启不会恢复旧登录。
- `AGENTS.md` 仍是仓库级有效约束入口；已同步第五轮前置阶段标题、ADB 真机调试前置规则、Agent Team 自动派发规则和 doc 职责映射。

## 当前不能认为完成

- App Shell、消息页和我的页已完成第二轮代码基线，TopBar、Search、消息未登录态和退出登录返回已列为视觉 QA 对象；仍需要 360dp、390dp、430dp 手工验收。
- 第三轮商城真机优先 QA 已有 `10-testing-acceptance.md` 记录作为主体验收关闭依据；后续视觉复查以真机为主，不再维护模拟器 `.env` 模板。
- 已定位 ADB 路径 `D:\AndroidChace\platform-tools\adb.exe`；无在线真机时，截图或设备 QA 不能写成通过，必须先告知用户。
- 2026-05-30 真机优先 QA 已覆盖在线真机安装启动、商城首页、搜索有结果和空态、分类切换、玩家商品禁用标准购物车、自营商品详情、未登录加购拦截、已登录购物车、订单确认地址缺口、我的订单入口和联调支付状态页。
- 本地 QA 种子商品已覆盖 `SELF_OPERATED`、`PLAYER_SECOND_HAND`、`PLAYER_CUSTOM_SERVICE`；真机商城 smoke 已验证玩家二手 / 定制商品只展示信息并禁用标准购物车。
- Android debug API Base URL 已支持 `.env` 构建期注入；本地默认只维护真机 `.env`，修改 IP 或 Provider 后仍需重启后端并重新构建 debug 包。登录态已接入 DataStore 启动 hydrate，验证码登录后的真机重启路径已在 2026-05-30 复测通过。
- Android Studio / Gradle JVM 如果误选到 VS Code Red Hat Java 扩展内置精简 JRE，会触发 `jlink.exe does not exist`；本地构建必须按协作规范选择完整 JDK/JBR 21。
- 社区、消息和我的样板链路已开始消除空点击和假成功；第三轮已将商城订单确认和联调支付状态收口到禁用缺地址下单、显式创建联调支付单和服务端状态展示。
- 文件预览 URL 策略不完整，图纸预览、头像等 fileId 不一定能直接展示为图片；第四轮已收敛 Feed 和商城 MVP 所需的 `coverImageUrl` / `imageUrl`，但这不等于完整媒体服务生产化。
- 真实 AI Provider 未接入完成。`AliyunBailianProvider` 仍是占位实现，尚未调用真实阿里云百炼/通义万相 API。
- 微信支付和支付宝支付仍是 Stub。当前实现不能用于真实交易收款、退款或对账。
- 订单确认仍受地址管理缺口限制；在地址接口未闭环前，客户端不能创建伪地址、不能伪装已具备真实收货履约能力；服务端 `POST /orders` 的测试 `addressId` 只用于联调，不代表 Android 已接入地址管理。
- 内容审核和交易风控仍是基础能力，未达到中国大陆应用市场上线要求。
- 玩家二手/定制交易仍缺完整闭环，包括真实实名校验、发布限制、评价、纠纷处理和风控策略。
- 管理后台还只有后端 API，没有完整运营工作台前端。
- 合规备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场材料尚未完成。

## 当前技术口径

- Android：Kotlin、Jetpack Compose、单 Activity、Navigation Compose、Retrofit、OkHttp、Kotlinx Serialization、Coil、CameraX、Photo Picker。
- Android 当前依赖装配：`DoyuAppContainer` 服务定位器；MVVM 是目标架构，不代表所有页面已完全 ViewModel 化。
- Android 登录态：第四轮已切换到 DataStore 持久化 access/refresh token；自动测试覆盖保存、hydrate 和 clear，真机登录重启 smoke 仍按 `10-testing-acceptance.md` 记录为手工复测项。
- Android 本地联调默认使用真机和电脑当前 Wi-Fi/LAN IPv4。debug 包的 Retrofit `baseUrl` 由仓库根目录 `.env` 在 Gradle 构建期写入 `BuildConfig.API_BASE_URL`，修改 `.env` 后必须重新构建。
- 后端：Java 21、Spring Boot、Spring Security、JWT、Spring Data JPA、Flyway、PostgreSQL、Redis。
- 后端端口：`8081`。
- PostgreSQL 宿主机端口：`5433`，容器内端口仍为 `5432`。
- API 前缀：`/api/v1`。
- 本地 OSS：开发环境默认使用 Local OSS Provider；第五轮前置已补 Aliyun OSS Provider 骨架，可通过本机私有 `.env` 启用，但真实 Bucket 端到端上传、CORS、STS/最小权限、CDN、防盗链、图片审核、病毒扫描、缩略图和 seed assets 云迁移仍未关闭。第四轮 seed assets 只用于本地 QA/演示，必须记录图片来源、用途和关联商品/帖子。
- AI：自研拼豆算法已落地，视觉理解 Provider 仍需真实接入。
- 支付：支付单和回调骨架已存在，真实微信/支付宝 SDK/API 未接入。

## 本轮明确不做

- 不接入真实 AI Provider，不做供应商 API Key、模型选择、真实视觉理解质量验收。
- 不增强生产级内容审核、图片审核、版权识别、诈骗识别或交易风控。
- 不接入真实微信支付、支付宝支付、退款、对账或支付 SDK。
- 不补齐备案、隐私政策、用户协议、SDK 清单、版权投诉等生产合规材料。
- 不补完整玩家二手/定制交易闭环、担保、评价、纠纷、提现或卖家资质审核。
- 不把玩家二手/定制商品接入标准购物车混单。
- 不把地址管理写成本轮已完成；订单确认继续以地址缺口和禁用伪下单为边界。
- 不新增后端公共 API；第五轮前置只在已有上传抽象、Provider 配置和联调文档上收敛。

## 下一步优先级

P0：第五轮前置 Aliyun OSS Provider 最小闭环

- 后端：`DOUYU_OSS_PROVIDER=local|stub|aliyun` 可选择对象存储 Provider；Aliyun Provider 只由后端持有 AccessKey，继续复用 `/uploads/presign -> PUT uploadUrl -> /uploads/confirm`。
- 后端：Aliyun Provider 生成 PUT 预签名 URL，返回 Android 直传所需 `headers`，confirm 后返回完整 `FileAsset` 字段，并通过 `publicUrl=publicBaseUrl + fileKey` 暴露公开图片 URL。
- 文档：`.env.example` 只保留 Aliyun 占位变量，不提交真实 AccessKey；联调文档写清广州 Bucket 示例、Bucket CORS、公共读边界和 Provider 切换后重启后端。
- 未完成：真实 Bucket 上传 smoke、STS/最小权限、CDN、防盗链、图片审核、缩略图和 seed assets 云迁移。

P1：第四轮登录态持久化 + 常驻真实图文数据回归

- Android：把 `DoyuAppContainer` 的 tokenStore 接入 DataStore，启动时 hydrate；登录保存 access/refresh token，401 refresh 成功后更新 DataStore，退出登录和鉴权过期时清理 DataStore。
- Android：补登录态单元测试和真机 smoke，覆盖登录后重启 App 仍识别登录态、退出后重启不恢复登录态、refresh token 更新后持久化。
- 后端：为本地 QA seed 数据提供常驻真实图片素材，商品列表/详情和购物车摘要返回 `imageUrl`，社区 Feed 返回 `coverImageUrl`；图片 URL 使用 `DOUYU_STORAGE_BASE_URL` / Local OSS 开发路径，不接真实 OSS。
- 后端：seed assets 必须幂等写入，来源、用途、关联商品/帖子、许可或生成说明必须有记录；不得引入未授权素材或真实用户数据。
- Android：商品卡、商品详情、购物车项和社区 Feed 优先渲染 `imageUrl` / `coverImageUrl`，图片为空或加载失败时才回退到现有 swatch/占位视觉。
- QA：验收清单必须同时包含 Android 单测/构建、后端测试/API smoke、真机重启登录态与图文渲染 smoke；无在线设备时不得把真机 QA 写成通过。

P2：第三轮商城收口回归

- 保留第三轮商城搜索/分类、自营加购、玩家商品禁用标准购物车、订单确认地址缺口、订单列表和联调支付状态回归。
- 后续视觉 QA：以在线真机竖屏为主复查商城列表、详情、购物车、订单确认和支付状态页不挤压、不重叠；如未来需要多宽度覆盖，单独开任务准备设备或模拟器，不再作为当前默认流程。

P3：保留并复查第二轮 UI 统一基线

- 复查 `DoyuPage`、`DoyuTopBar`、底部 5 Tab、FAB、卡片、按钮、Chip、Search、加载/空/错误/未登录状态在主要页面的表现。
- 手工验收消息页：通知/私信 Tab、列表密度、未读状态、空/错/未登录状态；私信发送未闭环时禁用或明确边界。
- 手工验收退出登录返回：退出后返回路径明确，受保护页面回到未登录态或登录引导。
- 手工验收我的页：个人资产中心结构、中文化、卡片层级、未闭环入口隐藏/禁用/明确开发态。

P4：保留并复查第一轮登录 + 社区基线

- 保持 `05-api-contract.md`、后端 Controller/DTO、Android DTO/Repository、UI 和测试一致。
- 登录继续传 `ageGroup=AGE_18_PLUS`，登录响应用户字段使用 `avatarUrl`。
- 社区 Feed、详情、发布、评论、点赞、收藏均有明确状态和错误映射。
- 避免第二轮组件调整破坏社区样板链路。

P4：开发态主链路联调

- AI 上传、创建任务、轮询、失败、取消、成功结果页可跑，但仍只表达开发态图纸生成。
- 商城商品、购物车、订单、支付单状态边界清楚，不表达正式渠道完成态。
- 消息、我的、收藏、订单、设置没有空点击和误导性成功文案。

P5：生产化补强

- 真实 AI Provider、真实支付、审核风控、合规材料、玩家交易闭环和运营后台前端放到 UI MVP 之后单独规划。
