# 豆屿 Doyu 后端服务

第一阶段后端是 Spring Boot 模块化单体，面向 Android 联调提供 `/api/v1` REST API。真实短信、OSS、AI、微信支付和支付宝支付暂以服务端 Stub Provider 实现，不提交任何密钥。

## 第一阶段联调结论

- API Base URL: `http://localhost:8080/api/v1`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- 健康检查: `http://localhost:8080/actuator/health`
- 所有 Controller 暴露的 `/api/v1` 接口会进入 OpenAPI 文档；登录、上传、AI、商城、订单、支付、消息、成长、举报和后台接口均可在 Swagger UI 中检索。
- Swagger 已配置 Bearer Auth。前端登录后把 `accessToken` 填入 Swagger Authorize 或 Android `Authorization: Bearer <accessToken>`。
- 当前所有业务数据用于联调骨架，主要保存在进程内存；Flyway 和 MyBatis 已接入，PostgreSQL schema 已初始化，但业务仓储后续再逐步替换为持久化实现。

## 本地启动

### 推荐：dev profile + PostgreSQL/Redis

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

等价手动命令：

```powershell
cd D:\Studio\SpellBean\doyu-server
docker compose up -d postgres redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

dev profile 会连接本机 Docker Compose 中的：

- PostgreSQL: `localhost:5432`，数据库 `douyu`，账号 `douyu`
- Redis: `localhost:6379`

如果 Docker Desktop 没启动，`dev` profile 无法完整启动。测试命令不依赖 Docker，会使用 H2 内存数据库。

### 测试 profile

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

## 前端联调说明

### 登录与测试账号

- 测试手机号：可使用任意形如 `13800000001` 的手机号，建议前端按场景固定几个号码。
- 验证码：`123456`。
- 新用户短信登录必须传 `ageGroup`：
  - `AGE_16_17`：服务端写入 `isMinor=true`。
  - `AGE_18_PLUS`：服务端写入成年用户。
- 后台测试账号：`admin / admin123`，仅 `dev/test` 默认配置使用，可用环境变量覆盖。

### 上传到 AI 任务字段口径

前端链路必须按以下顺序：

1. `POST /api/v1/uploads/presign`
   - 返回：`fileKey`、`uploadUrl`、`headers`、`expiresIn`。
   - 当前 `uploadUrl` 是 OSS Stub 地址，前端可按真实直传流程组织代码，但联调阶段不需要真实 OSS 密钥。
2. 客户端按 `uploadUrl` 和 `headers` 直传文件。
   - 当前 Stub 不校验真实对象存储结果。
3. `POST /api/v1/uploads/confirm`
   - 请求使用第 1 步返回的 `fileKey`。
   - 返回：`fileId`、`fileKey`、`auditStatus`。
4. `POST /api/v1/patterns/jobs`
   - 必须传 `inputFileId`，值来自第 3 步返回的 `fileId`。
   - 不要把 `fileKey` 当成 `inputFileId`。

### 幂等与鉴权

- 订单、支付、退款等重要提交必须带 `Idempotency-Key`。
- 普通用户接口使用普通登录返回的 user token。
- `/api/v1/admin/**` 使用后台登录返回的 admin token。
- user token 不能访问后台接口，admin token 不作为普通用户 token 联调。

## Stub Provider 清单

| 能力 | 当前实现 | 前端联调口径 |
|---|---|---|
| 短信 | Stub Provider，固定验证码 `123456` | 可以完整联调登录、刷新、登出；不发送真实短信 |
| OSS/对象存储 | Stub Provider，返回 `https://oss-stub.douyu.local/...` | 字段和流程按真实预签名直传设计；不需要真实 OSS Secret |
| AI 拼豆 | Stub Provider，创建任务后生成可读的 `SUCCEEDED` 结果 | 使用真实任务接口轮询；结果、材料清单和图片文件 ID 为占位数据 |
| 微信支付 | Stub Provider，返回 App 拉起参数和回调骨架 | 只能联调订单、支付单、回调幂等状态；不是正式微信支付 |
| 支付宝支付 | Stub Provider，返回 App 拉起参数和回调骨架 | 只能联调订单、支付单、回调幂等状态；不是正式支付宝支付 |

## 支付风险标注

当前支付实现不是正式微信/支付宝支付。正式上线前至少还需要补齐：

- 微信/支付宝官方 SDK 或 API 接入。
- 回调验签和证书/公钥轮换。
- 支付金额与服务端订单应付金额强校验。
- 商户订单号、支付单号、渠道交易号一致性校验。
- 回调重放、重复通知、乱序通知处理。
- 主动查询渠道订单状态和对账差异处理。
- 退款渠道调用、退款回调验签和退款对账。

## 测试

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

## 开发 Stub

- 短信验证码：`123456`
- 测试管理员：`admin / admin123`，仅 `dev/test` 配置使用。
- 上传预签名：返回本地 Stub URL，不暴露 OSS Secret。
- AI 图纸任务：同步 Stub 流转为 `SUCCEEDED`，并生成可联调的图纸和材料清单。
- 支付：创建微信/支付宝 App 支付 Stub 参数，回调接口按支付单和渠道交易号幂等处理。
