# SpellBean / Doyu 项目上手指南

## Project Overview

- 项目：SpellBean / Doyu
- 定位：面向 16+ 用户的 Android 拼豆社区与材料商城应用。当前客户端使用传统 Android Java + Activity/Fragment + XML，后端提供 /api/v1 Spring Boot API，运行期数据必须来自后端并支持 OSS-backed image storage。
- 主要语言：Java、XML、Markdown、HTML、Kotlin DSL、SQL、YAML
- 主要框架/库：Android Java/XML、AppCompat、AndroidX Fragment、RecyclerView、Retrofit + Gson、OkHttp、Glide、CameraX View、Spring Boot 4、Spring Security JWT、Spring Data JPA、Flyway、MyBatis、PostgreSQL、Redis、Alibaba OSS
- 当前工程红线：Android 端保持 Java + Activity/Fragment + XML；运行期数据来自后端；Open Design HTML 是 UI 权威；不恢复 Kotlin/Compose、mock/demo 数据、AI、支付、地图定位、真实 SMS 或 runtime seed。

## Architecture Layers

### 文档与 Open Design 权威层
项目规则、交接文档、当前状态、API/UI/验收文档和 Open Design HTML。实现前先读这一层，避免把历史目标写成当前事实。
- `doc/development/00-project-handoff.md`：项目交接入口，说明 SpellBean 的定位、当前范围、移除范围、事实优先级和验证入口。
- `doc/development/03-android-client.md`：Android 客户端架构契约，定义 MainActivity、主 Tab、XML 页面映射、导航、数据和 UI 验收规则。
- `doc/development/04-backend-services.md`：后端服务范围文档，覆盖 auth/user/upload/community/commerce/order/message 等保留模块与移除边界。
- `doc/development/05-api-contract.md`：当前 /api/v1 API 契约，描述上传、用户设置、社区、搜索、商城订单等接口边界。
- `doc/development/10-testing-acceptance.md`：测试与验收门槛文档，定义 Android、后端、设备、UI、空数据、搜索和移除功能验收。
- `doc/development/current-status.md`：当前工程事实快照，记录 Android Java/XML 状态、保留业务范围、静态数据政策和验证覆盖。
- `doc/development/open-design/index.html`：Open Design UI 权威文件，定义对应 Android Java/XML 页面应匹配的结构、层级、文案、状态和交互。
- `doc/development/01-tech-stack.md`：文档包含 5 个章节，用于说明 01-tech-stack.md 相关规则或验证记录。

### Android Java/XML 客户端层
MainActivity、Activity/Fragment、XML layout、资源和 UI adapter。负责页面、导航、状态渲染和用户交互。
- `DouYu/app/build.gradle.kts`：Android app 模块构建配置，声明 Java 11、API base URL、网络安全配置生成和 Java/XML 技术栈依赖。
- `DouYu/app/src/main/java/cn/edu/app/douyu/MainActivity.java`：Android 应用主 shell，承载顶部栏、五个底部 Tab 和 Fragment 切换，并处理 section Intent 路由。
- `DouYu/app/proguard-rules.pro`：code 文件，语言为 pro，约 20 行。
- `DouYu/app/src/debug/AndroidManifest.xml`：config 文件，语言为 xml，约 15 行。
- `DouYu/app/src/debug/res/xml/file_paths.xml`：config 文件，语言为 xml，约 6 行。
- `DouYu/app/src/main/AndroidManifest.xml`：config 文件，语言为 xml，约 52 行。
- `DouYu/app/src/main/java/cn/edu/app/douyu/auth/AuthGate.java`：Android Java 源文件，属于 auth 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/auth/LoginActivity.java`：Android Java 源文件，属于 auth 层。

### Android API/Data 契约层
Retrofit API、Repository、DTO model 和 session/auth helpers。负责真实后端调用、上传链路、URL 归一化和本地会话边界。
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`：Android 数据仓库封装，负责真实 API 调用、分页、上传 presign/PUT/confirm、图片 URL 归一化和业务请求组装。
- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`：Retrofit API 接口总表，映射 Android 到后端 /api/v1 的社区、商城、消息、用户、上传和认证接口。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/AddressSnapshot.java`：Android Java 源文件，属于 model 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/AuthSession.java`：Android Java 源文件，属于 model 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/CartItemRequest.java`：Android Java 源文件，属于 model 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/CartResponse.java`：Android Java 源文件，属于 model 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/ChatMessage.java`：Android Java 源文件，属于 model 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/Comment.java`：Android Java 源文件，属于 model 层。

### Spring Boot 后端服务层
Controller、security、service-like helpers、OSS provider 和业务模块，暴露当前保留的 /api/v1 能力。
- `doyu-server/pom.xml`：后端 Maven 构建配置，声明 Spring Boot 4、Java 21、JPA/Flyway/Security/MyBatis/OSS/OpenAPI/H2 test 依赖。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/commerce/CommerceController.java`：商城 API Controller，处理商品、分类、购物车和商品创建相关接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java`：后端安全配置，定义 JWT resource server、公开接口、admin 权限和密码编码器。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`：社区 API Controller，处理 feed、发帖、详情、评论、点赞、收藏、话题和贴纸接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/DouyuServerApplication.java`：Spring Boot 后端启动入口，启用异步能力和配置属性扫描。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/order/OrderController.java`：订单 API Controller，处理创建订单、查询订单和取消订单，并维护库存锁定边界。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/LocalOssUploadController.java`：上传 API Controller，提供 OSS/local presign 与 confirm 流程入口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/UploadController.java`：上传 API Controller，提供 OSS/local presign 与 confirm 流程入口。

### 后端数据持久化层
JPA entities/repositories、Flyway migrations、数据库健康与 schema 演进。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/AdminOperationLogEntity.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/AdminOperationLogRepository.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/AdminUserEntity.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/AdminUserRepository.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/CartItemEntity.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/CartItemRepository.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/CheckinRecordEntity.java`：后端 Java 源文件，属于 common 模块。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity/CheckinRecordRepository.java`：后端 Java 源文件，属于 common 模块。

### 测试与验收层
Android unit/instrumented tests、backend MockMvc/SpringBootTest、验证记录和 smoke 证据。
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/OpenDesignLayoutMappingTest.java`：测试文件，用于约束 API 映射、移除功能、Open Design 映射、真实后端或 UI smoke 行为。
- `doyu-server/src/test/java/cn/edu/app/douyu/server/DefaultRuntimeNoSeedContractTests.java`：测试文件，用于约束 API 映射、移除功能、Open Design 映射、真实后端或 UI smoke 行为。
- `doyu-server/src/test/java/cn/edu/app/douyu/server/RemovedFeatureContractTests.java`：测试文件，用于约束 API 映射、移除功能、Open Design 映射、真实后端或 UI smoke 行为。
- `doc/development/verification/2026-06-07-community-real-content-repair.md`：文档包含 10 个章节，用于说明 2026-06-07-community-real-content-repair.md 相关规则或验证记录。
- `doc/development/verification/2026-06-07-java-xml-verification.md`：文档包含 3 个章节，用于说明 2026-06-07-java-xml-verification.md 相关规则或验证记录。
- `doc/development/verification/2026-06-07-open-design-parity-matrix.md`：文档包含 4 个章节，用于说明 2026-06-07-open-design-parity-matrix.md 相关规则或验证记录。
- `doc/development/verification/2026-06-07-real-backend-repair-log.md`：文档包含 13 个章节，用于说明 2026-06-07-real-backend-repair-log.md 相关规则或验证记录。
- `doc/development/verification/2026-06-07-repair-stage-plan.md`：文档包含 7 个章节，用于说明 2026-06-07-repair-stage-plan.md 相关规则或验证记录。

### 项目配置与构建层
Gradle、Maven、环境模板、git 配置和工具配置，定义构建与本地运行边界。
- `DouYu/build.gradle.kts`：code 文件，语言为 kotlin，约 4 行。
- `.env.example`：config 文件，语言为 config，约 95 行。
- `.gitattributes`：code 文件，语言为 unknown，约 22 行。
- `.understand-anything/.understandignore`：code 文件，语言为 unknown，约 41 行。
- `.understand-anything/config.json`：config 文件，语言为 json，约 3 行。
- `DouYu/gradle.properties`：config 文件，语言为 properties，约 14 行。
- `DouYu/gradle/gradle-daemon-jvm.properties`：config 文件，语言为 properties，约 13 行。
- `DouYu/gradle/libs.versions.toml`：文档包含 3 个章节，用于说明 libs.versions.toml 相关规则或验证记录。

## Key Concepts

- 事实优先级：当前代码、构建配置、测试输出和实际截图优先；AGENTS.md 与 current-status.md 是工程边界入口。
- Android 主链路：Activity/Fragment/XML 渲染 UI，Repository 调真实 Retrofit API，DTO 与后端 JSON 字段保持一致。
- 后端主链路：Spring Boot Controller 暴露 /api/v1，JWT 保护写接口，公开读取接口由 SecurityConfig 明确放行。
- 上传链路：Android 调 presign，直接 PUT 对象存储，再 confirm；客户端不保存 OSS 密钥，也不伪造上传成功。
- 空数据不是异常：后端空列表必须在 Android 呈现空态、登录边界、错误态或禁用态，不能填 mock 内容。
- UI 验收：doc/development/open-design 下的 HTML 定义结构、层级、文案、状态和交互，Android XML 需要对齐。

## Guided Tour

### 1. 项目边界与硬规则
先读 AGENTS、交接和当前状态，明确 Java/XML、真实后端数据、Open Design 权威，以及 AI/支付/地图/SMS/seed/demo 的当前移除边界。
- `doc/development/00-project-handoff.md`
- `doc/development/current-status.md`
- 学习提示：先建立边界，再读代码；这个仓库里文档不是附属物，而是实现约束。

### 2. Android 主壳与导航
从 MainActivity 理解顶部栏、五个底部 Tab、Fragment 切换和 section Intent，再对照 Android 客户端文档的页面映射。
- `doc/development/03-android-client.md`
- `DouYu/app/src/main/java/cn/edu/app/douyu/MainActivity.java`
- `DouYu/app/src/main/res/layout/activity_main.xml`

### 3. Android 到后端的数据链路
阅读 DoyuApi 和 DoyuRepository，掌握 Retrofit endpoint 映射、Repository 同步调用、上传 presign/PUT/confirm、图片 URL 归一化和 DTO 契约。
- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApiClient.java`

### 4. 后端启动、安全与公开接口
从 Spring Boot 入口和 SecurityConfig 理解服务启动、JWT、公开读取接口、admin 权限和认证边界。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/DouyuServerApplication.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java`
- `doyu-server/src/main/resources/application.yml`

### 5. 核心业务 API：社区、商城、订单、上传
按 Controller 阅读 retained flows，重点看社区发帖/评论、商城商品/购物车、订单创建取消和 OSS 上传。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/commerce/CommerceController.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/order/OrderController.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/UploadController.java`

### 6. UI 权威与验收
读 Open Design index、页面 blueprints 和测试验收，理解 Java/XML 页面如何对齐 HTML，以及空态、登录态、错误态和 not-covered 设备证据如何记录。
- `doc/development/open-design/index.html`
- `doc/development/13-ui-screen-blueprints.md`
- `doc/development/10-testing-acceptance.md`
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/OpenDesignLayoutMappingTest.java`

### 7. 测试网络与回归护栏
最后读 Android contract tests 和 backend contract tests，理解无 Kotlin/Compose、无 mock/demo、无 runtime seed、搜索/设置/购买/上传等验收如何被测试约束。
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/RemovedFeatureContractTest.java`
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/SourceMojibakeSpotTest.java`
- `doyu-server/src/test/java/cn/edu/app/douyu/server/DefaultRuntimeNoSeedContractTests.java`
- `doyu-server/src/test/java/cn/edu/app/douyu/server/RemovedFeatureContractTests.java`

## File Map

- `doc/development/00-project-handoff.md`：项目交接入口，说明 SpellBean 的定位、当前范围、移除范围、事实优先级和验证入口。
- `doc/development/current-status.md`：当前工程事实快照，记录 Android Java/XML 状态、保留业务范围、静态数据政策和验证覆盖。
- `doc/development/03-android-client.md`：Android 客户端架构契约，定义 MainActivity、主 Tab、XML 页面映射、导航、数据和 UI 验收规则。
- `doc/development/04-backend-services.md`：后端服务范围文档，覆盖 auth/user/upload/community/commerce/order/message 等保留模块与移除边界。
- `doc/development/05-api-contract.md`：当前 /api/v1 API 契约，描述上传、用户设置、社区、搜索、商城订单等接口边界。
- `DouYu/app/src/main/java/cn/edu/app/douyu/MainActivity.java`：Android 应用主 shell，承载顶部栏、五个底部 Tab 和 Fragment 切换，并处理 section Intent 路由。
- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`：Retrofit API 接口总表，映射 Android 到后端 /api/v1 的社区、商城、消息、用户、上传和认证接口。
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`：Android 数据仓库封装，负责真实 API 调用、分页、上传 presign/PUT/confirm、图片 URL 归一化和业务请求组装。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/DouyuServerApplication.java`：Spring Boot 后端启动入口，启用异步能力和配置属性扫描。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java`：后端安全配置，定义 JWT resource server、公开接口、admin 权限和密码编码器。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`：社区 API Controller，处理 feed、发帖、详情、评论、点赞、收藏、话题和贴纸接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/commerce/CommerceController.java`：商城 API Controller，处理商品、分类、购物车和商品创建相关接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/order/OrderController.java`：订单 API Controller，处理创建订单、查询订单和取消订单，并维护库存锁定边界。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/UploadController.java`：上传 API Controller，提供 OSS/local presign 与 confirm 流程入口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/user/UserController.java`：用户/Profile API Controller，处理当前用户、设置、关注、资料与用户列表。
- `doc/development/open-design/index.html`：Open Design UI 权威文件，定义对应 Android Java/XML 页面应匹配的结构、层级、文案、状态和交互。
- `doc/development/10-testing-acceptance.md`：测试与验收门槛文档，定义 Android、后端、设备、UI、空数据、搜索和移除功能验收。

## Complexity Hotspots

- `doyu-server/src/test/java/cn/edu/app/douyu/server/DouyuBackendContractTests.java`（complexity 32）：测试文件，用于约束 API 映射、移除功能、Open Design 映射、真实后端或 UI smoke 行为。
- `doc/development/open-design/doyu-open-design.css`（complexity 24）：Open Design UI 权威文件，定义对应 Android Java/XML 页面应匹配的结构、层级、文案、状态和交互。
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`（complexity 22）：Android 数据仓库封装，负责真实 API 调用、分页、上传 presign/PUT/confirm、图片 URL 归一化和业务请求组装。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java`（complexity 21）：Android Java 源文件，属于 feature 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java`（complexity 17）：Android Java 源文件，属于 feature 层。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`（complexity 16）：社区 API Controller，处理 feed、发帖、详情、评论、点赞、收藏、话题和贴纸接口。
- `DouYu/app/src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java`（complexity 15）：测试文件，用于约束 API 映射、移除功能、Open Design 映射、真实后端或 UI smoke 行为。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/CartActivity.java`（complexity 14）：Android Java 源文件，属于 feature 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/ProductDetailActivity.java`（complexity 14）：Android Java 源文件，属于 feature 层。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java`（complexity 14）：Android Java 源文件，属于 feature 层。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/commerce/CommerceController.java`（complexity 14）：商城 API Controller，处理商品、分类、购物车和商品创建相关接口。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java`（complexity 13）：Android Java 源文件，属于 feature 层。

## Verification Shortcuts

```powershell
cd D:\Studio\SpellBean
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app/src
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
```

需要改 Android 或 backend 时，再按 doc/development/10-testing-acceptance.md 跑对应 Gradle/Maven/设备验收。