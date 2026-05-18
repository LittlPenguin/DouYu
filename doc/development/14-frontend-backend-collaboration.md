# 14. 前后端协作与项目管理规则

## 目标

本分册用于管理豆屿 Doyu 第一阶段前后端分离开发。它面向项目负责人、Android 开发、后端开发、测试和后续 AI Agent。

## 工作边界

- 前端指 Android 客户端，默认工程路径为 `D:\Studio\SpellBean\DouYu`。
- 后端指 Spring Boot API 服务，建议工程路径为 `D:\Studio\SpellBean\doyu-server`。
- 第一阶段优先完成可联调闭环，不追求一次性完成全部运营后台和复杂风控。
- 前端不得绕过后端鉴权、审核、支付状态确认。
- 后端不得把 AI Key、OSS Secret、支付密钥下发给前端。

## 推荐推进顺序

1. 后端建立 Spring Boot 工程骨架、统一响应、错误码、traceId、鉴权占位。
2. 后端提供 OpenAPI 或等价接口契约。
3. 前端建立 Compose 导航、主题、页面状态模型、API Client 骨架。
4. 前端按 Mock 或接口契约完成 5 个主 Tab 和核心页面。
5. 后端补齐登录、社区 Feed、上传、AI 任务、商品、订单等可联调接口。
6. 前后端进行登录、社区、AI 拼图、商城、订单支付占位链路联调。
7. 测试按 `10-testing-acceptance.md` 执行验收。

## 接口冻结规则

Android 与后端联调前必须冻结当前接口版本。冻结内容包括：

- 路径。
- 请求字段。
- 响应字段。
- 错误码。
- 状态枚举。
- 分页结构。
- 鉴权规则。
- 幂等规则。

冻结后如果必须变更，提出方需要同步修改：

- `05-api-contract.md`
- 相关客户端 API model
- 相关后端 DTO 或响应对象
- 相关测试样例

## 文档同步规则

- 修改接口字段、路径、状态枚举时，同步 `05-api-contract.md`。
- 修改核心表、数据对象、状态机时，同步 `06-data-model.md`。
- 修改支付、订单、退款、库存逻辑时，同步 `08-commerce-payment.md`。
- 修改 AI 拼豆流程时，同步 `07-ai-pattern-generation.md`。
- 修改权限、隐私、未成年人、内容审核、账号注销时，同步 `09-security-compliance.md`。
- 修改 Android 页面结构、权限、上传、支付、缓存策略时，同步 `03-android-client.md`。
- 修改后端模块、鉴权、队列、后台、审核策略时，同步 `04-backend-services.md`。

## 联调主链路

第一阶段至少完成以下联调链路：

- 手机号验证码登录到获取当前用户。
- 社区 Feed 到帖子详情。
- 发帖提交到审核中状态展示。
- 图片预签名上传到上传确认。
- AI 任务创建到任务状态轮询。
- AI 任务成功到图纸结果展示。
- 商品列表到商品详情。
- 加入购物车到订单确认。
- 创建订单到创建支付单。
- 支付结果页到后端订单状态查询。
- 消息通知列表展示。
- 我的页面到生成记录和订单入口。

## Mock 策略

- 后端接口未完成前，前端可使用本地 Mock 数据，但数据结构必须贴合 `05-api-contract.md`。
- Mock 数据不得改变真实接口字段名。
- Mock 的错误状态必须覆盖 `UNAUTHORIZED`、`FORBIDDEN`、`AUDIT_REJECTED`、`AI_TASK_FAILED`、`INVENTORY_NOT_ENOUGH`。
- 支付、AI、上传可以先用占位 Provider，但接口形状必须保持真实。

## 风险控制

- AI、支付、OSS、短信均通过后端抽象，客户端只拿业务所需参数。
- 支付成功不得由客户端本地 SDK 返回直接判定。
- 玩家交易第一版不做平台余额，不沉淀资金池。
- 玩家卖家、提现、定制服务发布者必须满足 18+ 实名要求。
- 所有用户上传内容默认按不可信输入处理。
- AI 输入和输出都需要审核记录。

## 验收节奏

### 前端

在 `D:\Studio\SpellBean\DouYu` 执行：

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

### 后端

在后端工程目录执行项目测试命令。若使用 Gradle：

```powershell
.\gradlew test
```

### 联调

联调完成后必须提供：

- 前后端接口版本说明。
- 本地启动方式。
- 测试账号或测试登录方式。
- 已联通链路清单。
- 未接入第三方能力的占位说明。
- 已知风险和下一阶段处理项。

## 第一阶段完成标准

- Android Debug 包可构建。
- 后端服务可本地启动。
- `/api/v1` 接口规范统一。
- 前端 5 个主 Tab 可操作。
- 登录、社区、AI 拼图、商城、订单支付占位、我的页面形成可演示闭环。
- 没有硬编码密钥。
- 文档、接口、数据模型、页面状态保持一致。

## 当前协同状态

更新日期：2026-05-14。

### P0 协同整改结论（已完成）

前端整改已完成：

- ~~按 `12-frontend-android-task-brief.md` 的”当前联调补充整改项”修正 API model 和 API Client。~~ ✅
- ~~将 Mock 数据结构调整为真实接口结构。~~ ✅ 5 个 Screen 全部接入 `DoyuAppContainer` 真实 Repository
- ~~上传与 AI 任务链路必须改为：`presign -> 直传 -> confirm -> patterns/jobs(inputFileId) -> jobs/{jobId}`。~~ ✅
- ~~订单、支付、退款等重要提交必须生成并携带 `Idempotency-Key`。~~ ✅
- ~~支付结果页必须查询后端订单或支付单状态。~~ ✅

后端整改已完成：

- ~~输出 OpenAPI/Swagger 或等价接口契约。~~ ✅ 13 个 Controller 全部加 @Tag/@Operation/@ApiResponses
- ~~明确所有 Stub Provider。~~ ✅ 短信、OSS、AI、微信支付、支付宝支付
- ~~对前端主链路所需字段保持稳定返回。~~ ✅ 全部字段已补齐
- ~~支付回调标注为 Stub。~~ ✅

### 联调冻结口径（已确认）

以下接口口径已冻结：

- `/uploads/presign` 返回 `fileKey`，用于对象存储直传。
- `/uploads/confirm` 返回 `fileId`，用于后续业务引用。
- `/patterns/jobs` 请求字段使用 `inputFileId`。
- 列表接口统一返回分页结构，不返回裸数组。
- 对外业务 ID 字段统一使用带业务语义的字符串字段，例如 `userId`、`postId`、`jobId`、`patternId`、`productId`、`skuId`、`orderId`、`paymentId`。
- 通知使用 `notificationId`/`unread`（非 `messageId`/`read`）。
- 会话使用 `peerUserId`/`peerName`/`lastMessage`/`unreadCount`。
- 创建订单使用 `itemIds`+`addressId`。
- 签到返回 `checkedToday`。

### 联调主链路验证状态

| 链路 | 状态 |
|---|---|
| 手机号验证码登录→获取当前用户 | ✅ 已联通 |
| 社区 Feed→帖子详情 | ✅ 已联通 |
| 发帖提交→审核中状态展示 | ✅ API 已通 |
| 图片预签名上传→上传确认 | ✅ API 已通 |
| AI 任务创建→任务状态轮询 | ✅ 已联通 |
| AI 任务成功→图纸结果展示 | ✅ Stub 已通 |
| 商品列表→商品详情 | ✅ 已联通 |
| 加入购物车→订单确认 | ✅ 已联通 |
| 创建订单→创建支付单 | ✅ 已联通 |
| 支付结果页→后端订单状态查询 | ✅ 已联通 |
| 消息通知列表展示 | ✅ 已联通 |
| 我的页面→生成记录和订单入口 | ✅ 已联通 |

### 协同验收方式

前后端完成整改后，按以下顺序验收：

1. 后端运行测试并提供 OpenAPI/Swagger 地址。✅
2. 前端基于后端契约完成 API model 和 Mock 结构修正。✅
3. 联通登录、Feed、上传确认、AI 任务创建与查询、商品列表、购物车、创建订单、创建支付单、支付状态查询。✅
4. 前端运行 `.\gradlew.bat :app:assembleDebug` 和 `.\gradlew.bat :app:testDebugUnitTest`。✅
5. 后端运行 `mvn test` 或后端 README 中声明的等价命令。✅
6. 项目负责人确认”已接真实接口清单”和”仍为 Stub/Mock 清单”。✅

### 剩余 P1 协同事项

- PatternAsset.materials 类型适配（前端 List vs 后端 Map）。
- 错误码统一转为用户可读文案。
- 核心页面补齐加载、空状态、失败、未登录、无权限、审核中、弱网重试状态。
- 真实相册选择、CameraX 拍照、对象存储直传的完整 UI 闭环。
- 核心业务对象迁移到 PostgreSQL 持久化。

## 后端第一阶段联调交付说明

更新日期：2026-05-14。

后端工程路径：`D:\Studio\SpellBean\doyu-server`。

### 本地启动

推荐使用 `dev` profile 启动完整后端：

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

`start-dev.bat` 等价于：

```powershell
docker compose up -d postgres redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 需要本机 PostgreSQL 和 Redis。项目已提供 Docker Compose：

- PostgreSQL: `localhost:5433`，数据库 `douyu`，账号 `douyu`。
- Redis: `localhost:6379`。

如果只运行自动化测试，不需要启动 PostgreSQL/Redis：

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

### 前端联调地址

- API Base URL: `http://localhost:8081/api/v1`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- 健康检查: `http://localhost:8081/actuator/health`

Swagger/OpenAPI 已暴露当前所有后端 Controller 中的 `/api/v1` 接口，并配置 Bearer Auth。Android 联调时，普通接口使用用户登录返回的 `accessToken`，后台接口使用 `/api/v1/admin/auth/login` 返回的后台 token。

### 真机联调网络排障记录

2026-05-16 真机联调出现过一次典型问题：手机浏览器可以访问后端 `http://10.64.241.153:8080/swagger-ui/index.html`，但 App 内显示“加载失败 - 网络异常”。

最终原因不是后端接口、Swagger 或 Spring Security 拦截，而是 Android 客户端网络安全配置和开发地址配置问题：

- 后端需要监听局域网地址，`doyu-server/src/main/resources/application.yml` 中保留 `server.address: 0.0.0.0`。
- 真机不能使用 `localhost` 或模拟器专用地址 `10.0.2.2` 访问电脑后端，`DoyuAppContainer` 的 `baseUrl` 必须使用电脑当前 Wi-Fi IP，并以 `/` 结尾，例如 `http://10.64.241.153:8080/`。
- Android 默认禁止明文 HTTP。debug 包必须通过 `app/src/debug/res/xml/network_security_config.xml` 对当前开发机 IP 显式放行 `cleartextTrafficPermitted=true`。
- `debug-overrides` 只用于证书信任覆盖，不应作为放行明文 HTTP 的主要方式；明文 HTTP 放行应写在 `domain-config`。
- `app/src/main/res/xml/network_security_config.xml` 继续保持生产默认 HTTPS only，不允许把开发机 IP 的 HTTP 放行写入 main/release 配置。

排障顺序固定如下：

1. 后端启动后，在电脑本机验证 `http://<电脑WiFi-IP>:8080/actuator/health` 返回 `UP`。
2. 在真机浏览器访问同一个 health 地址，确认手机到电脑后端网络可达。
3. 确认 App 的 `baseUrl` 是 `http://<电脑WiFi-IP>:8080/`，不是 `localhost`、`127.0.0.1` 或 `10.0.2.2`。
4. 确认 debug 网络安全配置包含当前电脑 IP 的 `domain-config cleartextTrafficPermitted="true"`。
5. 重新安装 debug 包，避免真机继续运行旧 APK。
6. 若仍显示“网络异常”，优先看 logcat：`CLEARTEXT communication ... not permitted` 表示明文 HTTP 未放行；`ConnectException` 表示后端监听、防火墙或 IP 不通；`JsonDecodingException` 表示网络已通但前后端 JSON 字段不匹配。

### 测试登录规则

- 测试手机号可使用任意手机号格式，建议固定使用 `13800000001`、`13800000002` 等。
- 短信验证码固定为 `123456`。
- ~~新用户登录必须传 `ageGroup`~~：`ageGroup` 已从客户端移除，年龄段由后端根据实名信息自动判定。登录只需手机号 + 验证码。
- 后台测试账号：`admin / admin123`，仅 `dev/test` 默认配置使用。

### 上传与 AI 任务冻结口径

前端必须按以下链路联调：

1. `POST /api/v1/uploads/presign`
   - 返回 `fileKey`、`uploadUrl`、`headers`、`expiresIn`。
2. 客户端按 `uploadUrl` 和 `headers` 执行对象存储直传。
   - 当前后端为 OSS Stub，不校验真实对象存储结果。
3. `POST /api/v1/uploads/confirm`
   - 请求传第 1 步返回的 `fileKey`。
   - 返回 `fileId`、`fileKey`、`auditStatus`。
4. `POST /api/v1/patterns/jobs`
   - 必须传 `inputFileId`，值来自第 3 步返回的 `fileId`。
   - 不得把 `fileKey` 直接传给 `inputFileId`。

### Stub Provider 清单

| 能力 | 当前状态 | 联调说明 |
|---|---|---|
| 短信 | Stub Provider | 验证码固定 `123456`，不发送真实短信 |
| OSS/对象存储 | Stub Provider | 返回占位 `uploadUrl`，字段按真实预签名直传设计 |
| AI 拼豆 | Stub Provider | 创建任务后返回可轮询的占位成功结果、图纸 ID 和材料清单 |
| 微信支付 | Stub Provider | 返回占位 App 拉起参数，回调接口只验证业务幂等骨架 |
| 支付宝支付 | Stub Provider | 返回占位 App 拉起参数，回调接口只验证业务幂等骨架 |

### 支付风险边界

当前后端支付不是正式微信/支付宝支付。前端可以联调订单、支付单、支付状态查询和回调后的状态变化，但不能把当前实现当成生产支付能力。

正式接入前后端至少还需要补齐：

- 微信/支付宝官方渠道接入。
- 支付回调验签。
- 支付金额与服务端订单应付金额校验。
- 商户订单号、支付单号、渠道交易号一致性校验。
- 回调重放、重复通知、乱序通知处理。
- 主动查询支付渠道订单状态。
- 对账、退款渠道调用和退款回调验签。
