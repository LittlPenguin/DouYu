# 03. Android 客户端实现方案

## 客户端目标

Android 客户端负责用户主要体验：社区浏览、发帖、拍照选图、AI 拼豆图生成、商城购买、玩家私信、订单和个人资产。

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

### 依赖容器

`DoyuAppContainer`（`core/data/DoyuAppContainer.kt`）是全局依赖容器，负责装配真实 Repository：

- `RealCommunityRepository`
- `RealPatternRepository`
- `RealCommerceRepository`
- `RealMessageRepository`
- `RealProfileRepository`

所有 Screen 通过 `DoyuAppContainer.xxxRepository` 获取真实 Repository 实例，不再使用 Mock。

### safeCall 异常处理

每个 Screen 定义 `safeCall` 包装函数，统一处理网络异常：

```kotlin
private inline fun <T> safeCall(block: () -> T): T? =
    try { block() } catch (_: Exception) { null }
```

所有 Repository 调用均通过 `safeCall` 包装，异常时返回 null，UI 展示空状态或错误提示。

## 页面导航

底部主导航：

- 社区。
- AI 拼图。
- 商城。
- 消息。
- 我的。

关键页面：

- 登录/注册。
- 社区 Feed。
- 帖子详情。
- 发帖编辑。
- 图片裁剪。
- AI 参数选择。
- AI 任务进度。
- 图纸结果。
- 生成记录。
- 商品列表。
- 商品详情。
- 购物车。
- 订单确认。
- 支付结果。
- 订单详情。
- 私信会话。
- 用户主页。
- 我的拼豆。
- 设置。

### 页面切换动画

NavHost 页面切换动画时长为 150ms（`tween(TRANSITION_DURATION)`），包括 `enterTransition`、`exitTransition`、`popEnterTransition`、`popExitTransition`。

## 状态管理

每个页面至少支持：

- 加载中。
- 成功。
- 空状态。
- 失败。
- 未登录。
- 无权限。
- 审核中。
- 弱网重试。

Compose 页面使用单向数据流：

- UI 只渲染状态。
- ViewModel 处理用户意图。
- Repository 负责网络和缓存。
- 网络错误统一转换为可展示错误。

### 当前实现状态

**依赖注入**：使用 `DoyuAppContainer`（object 单例）作为服务定位器，持有 `DoyuApiClient`、`InMemoryTokenStore`、`AuthSessionManager` 和 5 个真实 Repository 实例。模拟器联调使用 `http://10.0.2.2:8080/`，真机联调必须使用电脑当前 Wi-Fi IP，例如 `http://10.64.241.153:8080/`；Retrofit `baseUrl` 必须以 `/` 结尾。

**真机 HTTP 联调**：Android main 配置保持 HTTPS only；debug 包通过 `app/src/debug/res/xml/network_security_config.xml` 对当前开发机 IP 添加 `domain-config cleartextTrafficPermitted="true"`。如果真机浏览器能访问后端，但 App 显示“加载失败 - 网络异常”，优先检查 `baseUrl` 是否使用电脑 Wi-Fi IP、debug 包是否重装、logcat 是否出现 `CLEARTEXT communication ... not permitted`。

**Repository 层**：已从 Mock Repository 切换到真实 Repository：

- `RealCommunityRepository` → CommunityApi
- `RealPatternRepository` → PatternApi
- `RealCommerceRepository` → ProductApi + CartApi + OrderApi + PaymentApi
- `RealMessageRepository` → MessageApi
- `RealProfileRepository` → UserApi + RewardApi + PatternApi

**异常处理**：所有 Screen 的 Repository 调用使用 `safeCall` 包装，网络异常返回 null 展示空状态而非崩溃。`ApiResponse.traceId` 有默认值，防止后端缺少 traceId 时反序列化失败。

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
- 拍摄后进入裁剪页。
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

上传流程：

1. 客户端请求 `/api/v1/uploads/presign`。
2. 后端返回上传 URL、fileKey、headers、过期时间。
3. 客户端直传对象存储。
4. 客户端调用确认接口或在业务提交时携带 fileKey。
5. 后端触发审核和缩略图处理。

要求：

- 上传支持失败重试。
- 大图上传显示进度。
- AI 输入图和公开帖子图分开标记用途。
- 不在客户端拼接公开 CDN 地址，以后端返回为准。

## AI 任务

AI 生成使用异步任务：

1. 创建任务。
2. 展示排队或处理中状态。
3. 轮询任务详情。
4. 成功后展示图纸。
5. 失败后展示原因和重试入口。

客户端不得直接调用模型供应商。

## 支付

支付流程：

1. 客户端创建订单。
2. 客户端请求支付参数。
3. 拉起微信或支付宝 SDK。
4. SDK 返回后展示“正在确认支付结果”。
5. 客户端查询后端订单状态。
6. 后端状态为准。

客户端不得信任 SDK 本地返回作为最终支付成功依据。

## 缓存与离线

缓存策略：

- Feed 可缓存最近页面。
- 我的资料和配置可缓存。
- AI 任务列表可缓存。
- 订单状态需要每次进入详情刷新。
- 支付状态不得只依赖本地缓存。

## 埋点

第一版建议记录：

- 注册完成。
- 发帖成功。
- 图片上传成功/失败。
- AI 任务创建。
- AI 生成成功/失败。
- 图纸收藏。
- 加入购物车。
- 下单。
- 支付成功/失败。
- 举报。
- 签到。

埋点不得上传用户原图、隐私文本、支付敏感信息。
