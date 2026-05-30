# 14. 前后端联调手册

本手册记录当前 Android 与后端本地联调口径。登录 + 社区第一轮样板链路的接口字段以 `05-api-contract.md` 为唯一契约源；其他接口以 `05-api-contract.md` 和后端 OpenAPI 为准。当前工程状态以 `current-status.md` 为准。

## 第一轮联调范围

第一轮只治理登录 + 社区 + 文档/契约/UI 基线：

- 登录：短信验证码、短信登录、刷新 token、退出登录。
- 社区：Feed、帖子详情、评论列表、发帖、评论、点赞/取消、收藏/取消。
- 错误：后端 `{ code,message,data,traceId }` 必须被 Android 映射为明确 UI 状态；未登录操作进入登录引导。

第一轮不做真实 AI Provider、真实微信/支付宝支付、退款对账、应用市场上线、备案、隐私政策、SDK 清单、生产审核风控和灰度发布材料。

## 环境

仓库根目录使用本机私有 `.env` 管理本地联调地址；`.env` 是唯一生效文件，提交模板只有 `.env.example`。`.env`、`.env.emulator`、`.env.phone` 和其他 `.env.*` 均被 `.gitignore` 忽略，不提交。

推荐本机维护两个私有模板：

- `.env.emulator`：模拟器模板，当前默认主配置。
- `.env.phone`：真机模板，使用电脑当前 Wi-Fi/LAN IPv4。

切换时只复制目标模板为 `.env`：

```powershell
# 模拟器
Copy-Item .env.emulator .env -Force

# 真机
Copy-Item .env.phone .env -Force
```

切换 `.env` 后必须重新启动后端并重新构建 debug 包。后端 Local OSS URL 在启动时读取环境变量，Android debug 包的 `BuildConfig.API_BASE_URL` 和 HTTP 白名单由 Gradle 构建期写入，不是运行时动态切换。

关键字段：

- `DOUYU_BACKEND_HOST`：当前开发机可被 Android 设备访问的 IP。真机使用电脑 Wi-Fi/LAN IPv4，模拟器通常使用 `10.0.2.2`。
- `DOUYU_BACKEND_PORT`：后端端口，默认 `8081`。
- `DOUYU_ANDROID_API_BASE_URL`：Android debug Retrofit baseUrl，必须以 `/` 结尾。
- `DOUYU_ANDROID_CLEARTEXT_HOSTS`：Android debug HTTP 明文访问白名单，逗号分隔。
- `DOUYU_STORAGE_BASE_URL`：后端 Local OSS 返回给 Android 的上传和资源访问 URL。

模拟器模板示例：

```env
DOUYU_BACKEND_HOST=10.0.2.2
DOUYU_BACKEND_PORT=8081
DOUYU_SERVER_ADDRESS=0.0.0.0

DOUYU_ANDROID_API_BASE_URL=http://10.0.2.2:8081/
DOUYU_ANDROID_CLEARTEXT_HOSTS=10.0.2.2,localhost
DOUYU_STORAGE_BASE_URL=http://10.0.2.2:8081

DOUYU_ANDROID_RELEASE_API_BASE_URL=https://api.example.invalid/
```

真机模板示例：

```env
DOUYU_BACKEND_HOST=10.64.241.153
DOUYU_BACKEND_PORT=8081
DOUYU_SERVER_ADDRESS=0.0.0.0

DOUYU_ANDROID_API_BASE_URL=http://10.64.241.153:8081/
DOUYU_ANDROID_CLEARTEXT_HOSTS=10.64.241.153,10.0.2.2,localhost
DOUYU_STORAGE_BASE_URL=http://10.64.241.153:8081

DOUYU_ANDROID_RELEASE_API_BASE_URL=https://api.example.invalid/
```

后端：

- 工程路径：`doyu-server/`
- 启动命令：`.\start-dev.bat`
- API Base：`http://<host>:8081/api/v1`
- Swagger UI：`http://<host>:8081/swagger-ui/index.html`
- OpenAPI JSON：`http://<host>:8081/v3/api-docs`
- 健康检查：`http://<host>:8081/actuator/health`
- PostgreSQL：宿主机 `localhost:5433`，容器内 `5432`
- Redis：`localhost:6379`

Android：

- 工程路径：`DouYu/`
- Debug 构建：`.\gradlew.bat :app:assembleDebug`
- 单元测试：`.\gradlew.bat :app:testDebugUnitTest`
- 模拟器访问电脑后端：`http://10.0.2.2:8081/`
- 真机访问电脑后端：`http://<电脑 Wi-Fi IP>:8081/`
- Retrofit `baseUrl` 必须以 `/` 结尾。
- Debug 包的 `baseUrl` 和 HTTP 白名单由根目录 `.env` 在构建时生成。
- 当前默认联调目标是模拟器，因此 `.env` 默认应使用 `.env.emulator`。

## ADB 真机调试

本项目允许使用 ADB 进行远程真机调试，默认 ADB 路径为：

```powershell
D:\AndroidChace\platform-tools\adb.exe
```

执行真机安装、截图、日志或交互前，必须先确认设备在线：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
```

验收规则：

- 没有在线设备时，必须先告诉用户当前不能执行真机验收，不得把设备 QA 写成通过。
- 无线调试可由用户在手机上开启；配对码、临时端口和一次性连接信息不得写入文档或提交记录。
- 真机截图、XML、logcat 等临时证据保存到 `.qa-output/`，该目录不提交。
- 真机能访问电脑后端但 App 失败时，优先检查 `.env.phone`、debug 包是否重建、HTTP 白名单和 Windows 防火墙。

常用命令：

```powershell
$adb = "D:\AndroidChace\platform-tools\adb.exe"
& $adb devices -l
& $adb install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\debug\app-debug.apk
& $adb shell monkey -p cn.edu.app.douyu -c android.intent.category.LAUNCHER 1
New-Item -ItemType Directory -Force -Path D:\Studio\SpellBean\.qa-output | Out-Null
& $adb exec-out screencap -p > D:\Studio\SpellBean\.qa-output\current-screen.png
```

## Debug HTTP 与 Release HTTPS

- `main` 网络安全配置保持 HTTPS only。
- `debug` 通过 Gradle 从 `.env` 的 `DOUYU_ANDROID_CLEARTEXT_HOSTS` 生成 `network_security_config.xml`，显式放行本机联调用的 IP。
- 不得把开发机 IP 的 HTTP 放行写入 release/main 配置。
- 真机能用浏览器访问后端但 App 网络失败时，优先检查：
  - App 是否安装 debug 包。
  - `baseUrl` 是否使用电脑 Wi-Fi IP，而不是 `localhost` 或 `127.0.0.1`。
  - `baseUrl` 端口是否为 `8081`。
  - debug network security config 是否包含当前 IP。
  - logcat 是否出现 cleartext traffic not permitted。

## 登录和鉴权

- 短信验证码开发环境固定为 `123456`。
- 当前后端 `SmsLoginRequest` 仍要求 `ageGroup`；Android `SmsLoginRequest` 默认传 `AGE_18_PLUS`。不得写成客户端已经移除年龄段字段，除非后端接口和 Android 代码已同步修改。
- 登录响应中的用户头像字段为 `user.avatarUrl`，不得写成 `avatarFileId`。
- 年龄段、未成年人状态和实名状态由后端逻辑维护。
- 普通用户接口使用普通 user token。
- `/api/v1/admin/**` 使用 admin token。
- user token 不能访问后台接口，admin token 不作为普通用户 token 使用。
- Token 过期后客户端应调用 `/auth/refresh`；刷新失败进入未登录态。

## 社区样板链路

社区第一轮按 `05-api-contract.md` 对齐以下接口：

| 场景 | 接口 | Android 行为 |
|---|---|---|
| 推荐 Feed | `GET /api/v1/posts/feed` | 免登录展示双列内容流 |
| 关注 Feed | `GET /api/v1/posts/following` | 需要登录；未登录展示登录引导 |
| 帖子详情 | `GET /api/v1/posts/{postId}` | 免登录可浏览 |
| 评论列表 | `GET /api/v1/posts/{postId}/comments` | 免登录可浏览 |
| 发帖 | `POST /api/v1/posts` | 需要登录；成功后展示“审核中” |
| 评论 | `POST /api/v1/posts/{postId}/comments` | 需要登录；成功后展示“评论已提交，等待审核” |
| 点赞/取消 | `POST/DELETE /api/v1/posts/{postId}/like` | 需要登录；响应 `{ liked }` |
| 收藏/取消 | `POST/DELETE /api/v1/posts/{postId}/favorite` | 需要登录；响应 `{ favorited }` |

联调要求：

- 发帖和评论返回 `REVIEWING` 时，Android 不得假装内容已经公开。
- 重复点赞/收藏是幂等语义，计数不得重复增加。
- 取消点赞/收藏时帖子不存在应返回 `NOT_FOUND`，不得返回假成功。
- Android 使用 `PostInteractionResult` 解码 `{ liked }` / `{ favorited }`；需要最新计数时重新读取帖子详情。
- `UNAUTHORIZED` 映射登录引导；`FORBIDDEN` 映射无权限；网络异常映射弱网；其他 `ApiException` 保留可展示的 `traceId`。

## Android Studio / Gradle JDK 排障

Android Studio 构建必须使用完整 JDK 21，推荐 Android Studio Embedded JDK / JetBrains JBR。

设置路径：

```text
File > Settings > Build, Execution, Deployment > Build Tools > Gradle
```

要求：

- `Distribution`：`Wrapper`
- `Gradle JVM criteria`：`Version 21`
- `Vendor`：推荐 `JetBrains`，或直接选择完整 JDK 路径，例如 `D:\Program Files\Android\Android Studio\jbr`
- 不要使用 VS Code Red Hat Java 扩展内置 JRE，不要保留可能误选精简 JRE 的 `Vendor: Any vendor`

如果出现：

```text
jlink executable C:\Users\Oya\.vscode\extensions\redhat.java-...\bin\jlink.exe does not exist
```

先按环境问题处理：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat --stop
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
```

`--version` 输出中的 JVM 路径不得指向 `.vscode\extensions\redhat.java`。

## 上传和 AI 任务链路

客户端必须按以下顺序执行：

1. `POST /api/v1/uploads/presign`
2. 按返回的 `uploadUrl` 和 `headers` 直传文件
3. `POST /api/v1/uploads/confirm`
4. 使用 confirm 返回的 `fileId` 作为 `inputFileId` 创建 AI 任务
5. `POST /api/v1/patterns/jobs`
6. 轮询 `GET /api/v1/patterns/jobs/{jobId}`
7. 成功后读取 `patternId`，再请求 `GET /api/v1/patterns/{patternId}`

当前状态：

- 开发环境使用 Local OSS Provider，本地存储文件。
- BeadPatternEngine 已能生成预览图、色号图、材料清单和 PDF 文件。
- AI Provider 抽象、Router、Cache 已存在。
- `AliyunBailianProvider` 仍是占位实现，尚未完成真实阿里云百炼/通义万相 API 调用。

## 订单和支付链路

客户端必须按以下规则联调：

- 商品价格和订单金额只信任服务端返回。
- 创建订单使用购物车项 ID 和地址 ID。
- 订单、支付、退款等重要提交必须带 `Idempotency-Key`。
- 客户端支付 SDK 返回只作为“已拉起/已返回 App”的本地事件。
- 支付最终状态必须查询服务端订单或支付单。

当前状态：

- 支付单、回调入口、金额校验、渠道一致性和防重放骨架已存在。
- 微信支付和支付宝支付仍是 Stub，不具备真实收款、退款、主动查询和对账能力。
- 正式支付接入前，不得把当前支付链路用于生产交易。

## 公开接口和登录拦截

当前公开浏览能力：

- `GET /api/v1/posts/feed`
- `GET /api/v1/posts/{postId}`
- `GET /api/v1/posts/{postId}/comments`
- `GET /api/v1/products`
- `GET /api/v1/products/{productId}`
- `/uploads/**` 文件访问

需要登录的操作：

- 发帖、评论、点赞、收藏、关注。
- `GET /api/v1/posts/following`。
- 上传、创建 AI 任务、收藏图纸。
- 购物车、订单、支付、退款。
- 消息、私信、签到、成长、个人资产。

Android 未登录状态应展示登录引导或 guest 占位，不应直接显示技术错误。

## 常见排障

| 现象 | 优先排查 |
|---|---|
| 模拟器连不上后端 | `.env` 是否由 `.env.emulator` 复制而来；`DOUYU_ANDROID_API_BASE_URL` 是否为 `http://10.0.2.2:8081/`；后端是否已启动在 `8081`；改 `.env` 后是否重新构建 debug 包 |
| 真机连不上后端 | `.env` 是否由 `.env.phone` 复制而来；IP 是否为电脑当前 Wi-Fi/LAN IPv4；手机和电脑是否同一局域网；Windows 防火墙是否放行 `8081`；后端 `DOUYU_SERVER_ADDRESS` 是否为 `0.0.0.0` |
| 图片、上传或预览 URL 不通 | `DOUYU_STORAGE_BASE_URL` 是否与当前运行目标一致：模拟器用 `http://10.0.2.2:8081`，真机用 `http://<电脑 Wi-Fi IP>:8081`；后端是否在切换 `.env` 后重启 |
| `cleartext traffic not permitted` | 是否安装 debug 包；`DOUYU_ANDROID_CLEARTEXT_HOSTS` 是否包含当前 host，且只写 host，不写协议和端口；改 `.env` 后是否重新构建 debug 包 |
| 改 `.env` 后 App 没生效 | Android `BuildConfig.API_BASE_URL` 是构建期写入；必须重新构建并安装 debug 包。后端 Local OSS URL 也要重启后端才会更新 |
| `jlink.exe does not exist` | Gradle JVM 选到了 VS Code Red Hat Java 扩展内置 JRE；改为 Android Studio JBR 或完整 JDK 21 后重新运行 `.\gradlew.bat --version` 和 `:app:assembleDebug` |

## 字段冻结和同步

- API 字段变更必须先更新 `05-api-contract.md`。
- 数据模型、枚举、状态机变更必须先更新 `06-data-model.md`。
- Android `Models.kt`、Retrofit 接口和后端 DTO/Controller 返回必须同名同义。
- 废弃字段先保留兼容，再在明确版本点移除。
- 涉及订单、支付、退款、库存、实名、审核的变更必须给测试说明。

## 联调检查

后端：

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
mvn test
```

Android：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

第一轮基础人工链路：

- 短信登录。
- 浏览社区 Feed 和帖子详情。
- 发帖、点赞、收藏、评论。
- 未登录发帖、点赞、收藏、评论时进入登录引导。
- 后端关闭或弱网时展示可理解错误，不直接空白。

后续 UI MVP 再覆盖：

- Photo Picker 或 CameraX 上传图片。
- 创建 AI 任务、轮询进度、查看图纸结果。
- 查看商品、加入购物车、创建订单、创建支付单、查询支付状态。
- 查看消息、我的页面、收藏图纸、我的订单。
