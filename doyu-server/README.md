# 豆屿 Doyu 后端服务

第一阶段后端是 Spring Boot 模块化单体，面向 Android 联调提供 `/api/v1` REST API。当前文档范围保留登录、上传、OSS-backed 图片存储、社区、商城、订单、地址管理、消息、成长、举报和后台接口说明；AI、支付、地图/定位 Provider、真实 SMS Provider、生产限流/风控、真实 AI/大模型 Provider、内容安全、成本控制和完整合规材料不属于当前范围。

OSS、上传预签名/确认、后端 upload/oss provider、Alibaba OSS 配置属于保留范围，不在本轮清理目标内。

## 联调入口

- API Base URL: `http://<DOUYU_BACKEND_HOST>:8081/api/v1`
- OpenAPI JSON: `http://<DOUYU_BACKEND_HOST>:8081/v3/api-docs`
- Swagger UI: `http://<DOUYU_BACKEND_HOST>:8081/swagger-ui/index.html`
- 健康检查: `http://<DOUYU_BACKEND_HOST>:8081/actuator/health`
- Swagger 使用 Bearer Auth。前端登录后把 `accessToken` 填入 Swagger Authorize 或 Android `Authorization: Bearer <accessToken>`。

## 本地启动

仓库根目录 `.env` 是当前本地配置入口，`start-dev.bat` 会读取其中的 `DOUYU_BACKEND_HOST`、`DOUYU_BACKEND_PORT`、`DOUYU_SERVER_ADDRESS`、`DOUYU_STORAGE_BASE_URL`、`DOUYU_OSS_PROVIDER` 等变量并传给 Spring Boot。当前默认只维护真机联调配置，不维护 `.env.emulator` / `.env.phone` 双模板。

需要重建本机配置时，从根目录模板复制为 `.env`，再把 host 改成电脑当前 Wi-Fi/LAN IPv4：

```powershell
Copy-Item ..\.env.example ..\.env -Force
```

真机联调时关键字段应保持同一个 LAN IP，例如：

```env
DOUYU_BACKEND_HOST=192.168.1.100
DOUYU_BACKEND_PORT=8081
DOUYU_SERVER_ADDRESS=0.0.0.0
DOUYU_ANDROID_API_BASE_URL=http://192.168.1.100:8081/
DOUYU_ANDROID_CLEARTEXT_HOSTS=192.168.1.100,localhost
DOUYU_STORAGE_BASE_URL=http://192.168.1.100:8081
DOUYU_OSS_PROVIDER=local
```

修改 `.env` 后必须重启后端。

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

等价手动命令：

```powershell
cd D:\Studio\SpellBean\doyu-server
docker compose up -d postgres redis
$env:DOUYU_BACKEND_HOST="<当前开发机IP>"
$env:DOUYU_BACKEND_PORT="8081"
$env:DOUYU_SERVER_ADDRESS="0.0.0.0"
$env:DOUYU_STORAGE_BASE_URL="http://<当前开发机IP>:8081"
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

dev profile 会连接本机 Docker Compose 中的：

- PostgreSQL: `localhost:5433`，数据库 `douyu`，账号 `douyu`
- Redis: `localhost:6379`

如果 Docker Desktop 没启动，`dev` profile 无法完整启动。测试命令不依赖 Docker，会使用 H2 内存数据库。

## 测试

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

## 前端联调说明

### 登录与测试账号

- 测试手机号可使用形如 `13800000001` 的号码。
- 验证码：`123456`。
- 新用户登录必须传 `ageGroup`：
  - `AGE_16_17`：服务端写入 `isMinor=true`。
  - `AGE_18_PLUS`：服务端写入成年用户。
- 后台测试账号：`admin / admin123`，仅 `dev/test` 默认配置使用，可用环境变量覆盖。

### 上传与 OSS

前端上传链路按以下顺序：

1. `POST /api/v1/uploads/presign`
   - 返回：`fileKey`、`uploadUrl`、`headers`、`expiresIn`。
2. 客户端按 `uploadUrl` 和 `headers` 上传文件。
3. `POST /api/v1/uploads/confirm`
   - 请求使用第 1 步返回的 `fileKey`。
   - 返回：`fileId`、`fileKey`、`auditStatus`。

对象图片存储和运行时图片 URL 继续通过 OSS/OSS-backed storage 处理。Android 不保存 OSS 密钥；客户端只使用后端签发的 `uploadUrl` 上传，再调用 confirm 登记文件。

### 鉴权

- 普通用户接口使用登录返回的 user token。
- `/api/v1/admin/**` 使用后台登录返回的 admin token。
- user token 不能访问后台接口，admin token 不作为普通用户 token 联调。

## 保留的开发 Provider / Boundary

| 能力 | 当前用途 | 边界 |
|---|---|---|
| 验证码 | 固定验证码 `123456`，用于本地登录联调 | 不发送真实短信，不接真实 SMS Provider |
| OSS / 对象存储 | `local`、`aliyun` Provider 配置和上传链路 | OSS 保留；Android 不保存密钥，上传由后端签发 URL |
| 后台测试账号 | `admin / admin123` | 仅 `dev/test` 使用 |

## QA empty database profile

Use `qa-empty` when backend QA needs a known-empty database without touching the normal dev database or its Docker volume. It uses separate containers, ports, and volumes:

- PostgreSQL container: `douyu-postgres-qa-empty`
- PostgreSQL port: `55433`
- PostgreSQL volume: `douyu-server_douyu_qa_empty_postgres_data`
- Redis container: `douyu-redis-qa-empty`
- Redis port: `56379`
- Redis volume: `douyu-server_douyu_qa_empty_redis_data`
- Spring profile: `qa-empty`
- Suggested backend port: `8082`

Start infrastructure:

```powershell
cd D:\Studio\SpellBean\doyu-server
docker compose -f docker-compose.qa-empty.yml up -d postgres-qa-empty redis-qa-empty
```

Start the backend against the isolated empty database:

```powershell
$env:DOUYU_BACKEND_PORT="8082"
mvn spring-boot:run -Dspring-boot.run.profiles=qa-empty
```

Verify the public empty-list contract:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-qa-empty.ps1 -BaseUrl http://127.0.0.1:8082
```

Shutdown after QA:

```powershell
docker compose -f docker-compose.qa-empty.yml stop postgres-qa-empty redis-qa-empty
```

Do not use `docker compose down -v` unless you intentionally want to delete the QA empty volume. The default dev volume `douyu_postgres_data` is not used by this profile.

## Dev seed/demo residue diagnostic

The normal dev database may still contain old persisted rows such as `post_seed%` posts or `prod_%` products from earlier runtime seeders. Do not delete those rows as part of QA empty verification. To inspect them read-only:

```powershell
cd D:\Studio\SpellBean\doyu-server
docker compose up -d postgres
powershell -ExecutionPolicy Bypass -File .\scripts\diagnose-dev-seed-residue.ps1
```

This diagnostic only runs `SELECT` statements. It exists to explain old seed/demo residue without modifying the dev persistent database.
