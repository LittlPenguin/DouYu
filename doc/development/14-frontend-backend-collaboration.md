# 14. 前后端联调手册

本手册记录当前 Android 与后端本地联调口径。接口字段以 `05-api-contract.md` 和后端 OpenAPI 为准；当前工程状态以 `current-status.md` 为准。

## 环境

仓库根目录使用本机私有 `.env` 管理本地联调地址；`.env` 不提交，提交的是 `.env.example`。换网络、换电脑或从真机改模拟器时，只改 `.env` 后重新启动后端并重新构建 debug 包。

关键字段：

- `DOUYU_BACKEND_HOST`：当前开发机可被 Android 设备访问的 IP。真机使用电脑 Wi-Fi/LAN IPv4，模拟器通常使用 `10.0.2.2`。
- `DOUYU_BACKEND_PORT`：后端端口，默认 `8081`。
- `DOUYU_ANDROID_API_BASE_URL`：Android debug Retrofit baseUrl，必须以 `/` 结尾。
- `DOUYU_ANDROID_CLEARTEXT_HOSTS`：Android debug HTTP 明文访问白名单，逗号分隔。
- `DOUYU_STORAGE_BASE_URL`：后端 Local OSS 返回给 Android 的上传和资源访问 URL。

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
- 登录请求只传手机号和验证码；客户端不再传 `ageGroup`。
- 年龄段、未成年人状态和实名状态由后端逻辑维护。
- 普通用户接口使用普通 user token。
- `/api/v1/admin/**` 使用 admin token。
- user token 不能访问后台接口，admin token 不作为普通用户 token 使用。
- Token 过期后客户端应调用 `/auth/refresh`；刷新失败进入未登录态。

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
- `GET /api/v1/posts/following`
- `GET /api/v1/posts/{postId}`
- `GET /api/v1/products`
- `GET /api/v1/products/{productId}`
- `/uploads/**` 文件访问

需要登录的操作：

- 发帖、评论、点赞、收藏、关注。
- 上传、创建 AI 任务、收藏图纸。
- 购物车、订单、支付、退款。
- 消息、私信、签到、成长、个人资产。

Android 未登录状态应展示登录引导或 guest 占位，不应直接显示技术错误。

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

基础人工链路：

- 短信登录。
- 浏览社区 Feed 和帖子详情。
- 发帖、点赞、收藏、评论。
- Photo Picker 或 CameraX 上传图片。
- 创建 AI 任务、轮询进度、查看图纸结果。
- 查看商品、加入购物车、创建订单、创建支付单、查询支付状态。
- 查看消息、我的页面、收藏图纸、我的订单。
