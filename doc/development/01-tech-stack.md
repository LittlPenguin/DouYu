# 01. 技术栈

## 目标

本文件固定当前迁移阶段的工程技术选择。Android 客户端目标架构已经从 Kotlin + Jetpack Compose 改为传统 Android：Java + Activity/Fragment + XML。旧 Kotlin/Compose 实现只作为历史背景，不再作为开发目标或验收依据。

## Android 客户端

| 类别    | 当前目标选择                                          |
| ----- | ----------------------------------------------- |
| 开发语言  | Java                                            |
| UI    | XML layout + AppCompat + Material Components    |
| 页面组织  | `MainActivity` + 五个主 Tab Fragment + 二级 Activity |
| 主 Tab | 社区 / 商城 / AI / 消息 / 我的                          |
| 导航    | Fragment tab state + Java `Intent` extras       |
| 网络    | Retrofit + OkHttp                               |
| JSON  | Gson                                            |
| 图片加载  | Glide                                           |
| 本地登录态 | `SharedPreferences`                             |
| 列表    | RecyclerView                                    |
| 布局    | ConstraintLayout、LinearLayout、FrameLayout       |
| 相机    | CameraX View                                    |
| 相册    | Android Photo Picker 或系统媒体选择 Intent             |
| 后台任务  | 仅在真实业务需要时引入 WorkManager                         |
| 支付    | 当前只展示联调支付单和服务端状态；真实微信/支付宝 SDK 未接入               |

Android 版本要求：

- `compileSdk`：36。
- `minSdk`：26。
- `targetSdk`：36。
- 保留 debug `BuildConfig.API_BASE_URL` 和网络安全配置生成能力。

### Android 禁用项

本阶段 Android 生产和测试源码不得使用：

- Kotlin 源文件。
- Jetpack Compose、Navigation Compose、Compose Material、Compose UI test。
- Kotlin serialization、Kotlin coroutine、Kotlin DataStore。
- Coil Compose、Paging Compose。
- 客户端 `MockData`、mock repository、假列表、假订单、假支付参数。

构建脚本可以继续使用 Gradle Kotlin DSL，但 Android 源码必须是 Java/XML。

## 后端服务

| 类别       | 选择                                                  |
| -------- | --------------------------------------------------- |
| 语言       | Java 21                                             |
| 框架       | Spring Boot                                         |
| 架构       | 模块化单体优先，按领域拆包                                       |
| API      | REST + JSON，统一前缀 `/api/v1`                          |
| 鉴权       | JWT access token + refresh token                    |
| 数据库      | PostgreSQL                                          |
| 数据库迁移    | Flyway                                              |
| 缓存       | Redis                                               |
| ORM      | Spring Data JPA                                     |
| 日志       | 结构化日志，包含 traceId                                    |
| Provider | SMS Stub、Local/Stub/Aliyun OSS、AI Stub、Payment Stub |

后端本阶段重点是删除运行期 seed/demo 填充数据，同时保留 Stub Provider 作为开发联调能力。Stub Provider 不是静态填充数据，不能和 seed/demo 混为一谈。

## 基础设施

| 能力    | 默认选择                                      |
| ----- | ----------------------------------------- |
| 对象存储  | 开发环境 Local/Stub Provider，生产目标为 Aliyun OSS |
| 短信    | 当前 Stub 验证码；生产接国内云短信服务                    |
| 内容审核  | 当前基础规则和状态边界；生产需要真实审核 Provider 和人工后台       |
| AI 模型 | 当前 Stub Provider + 后端编排；客户端不得直连模型         |
| 支付    | 当前 Stub/联调 Provider；真实支付 SDK/API 未完成      |
| 配置管理  | 环境变量 + 本地 `.env`                          |
| 部署    | 本地开发优先；生产化前补齐密钥、监控、备份和合规                  |

## 版本策略

- 公共 API 路径保持 `/api/v1`，本次 Android 迁移不得改变后端公开路径。
- Android 页面跳转从 Compose route 改为 Java `Intent` extras 和 Fragment tab state。
- 关键 extras 保持现有语义：`postId`、`productId`、`conversationId`、`notificationId`、`uploadedFileId`、`jobId`、`patternId`、`returnTo`。
- DTO 从 Kotlin data class 改为 Java POJO，字段名兼容后端 JSON。
- 删除 seed/demo 后，空列表是合法返回；客户端必须显示空态、未登录态或错误态，不得用本地假数据补齐。

## 禁止误导

- 不把旧 Kotlin/Compose 截图写成 Java/XML 验收证据。
- 不把真实 AI、真实支付、地图、全局搜索、生产合规写成已完成。
- 不把空库 API 的空列表视为异常，也不允许恢复本地 mock 填充。
