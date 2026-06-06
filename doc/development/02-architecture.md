# 02. 总体架构

## 架构定位

豆屿 Doyu 当前采用 Android 单客户端 + Spring Boot 单体后端 + PostgreSQL + Redis + 对象存储 Provider 的开发态架构。它服务五条主链路：

1. 社区内容发现、发布、作品详情、评论、点赞、收藏、关注。
2. AI 拼豆图纸任务、图纸资产和收藏。
3. 自营商城、购物车、订单和联调支付单。
4. 消息通知、私信会话和互关私信限制。
5. 我的资产、资料、签到、徽章和设置入口。

当前重点不是拆微服务，而是把接口契约、UI 目标、业务边界和验收流程整理清楚，后续按 `16-stage-development-roadmap.md` 分阶段落地。

## 分层结构

```text
Android App
  - Compose UI / Navigation
  - Repository / ApiClient / TokenStore
  - 本地状态与 DataStore 登录态

Spring Boot API /api/v1
  - Auth / User / Upload
  - Community / Pattern
  - Commerce / Order / Payment
  - Message / Reward / Report
  - Admin

Persistence
  - PostgreSQL + Flyway
  - Redis 基础设施
  - FileAsset 元数据

Providers
  - SMS Stub
  - Local / Stub / Aliyun OSS Provider
  - AI Stub + 自研拼豆算法
  - Payment Stub / 回调骨架

Design & Docs
  - Open Design HTML 原型
  - doc/development 分册
  - diagrams SVG 流程图
```

## 组件边界

| 组件 | 当前职责 | 当前边界 |
|---|---|---|
| Android App | 展示五个主 Tab、登录、社区、AI、商城、消息、我的等用户流程 | Search、Profile Edit、Settings 分区页和通知列表内详情已注册 Android 路由；全局搜索后端、生产合规配置、独立通知详情接口和未来能力仍未接入。 |
| Spring Boot API | 提供 `/api/v1` REST 接口、鉴权、业务规则、Provider 编排和后台 API | 不新增地图、真实支付、大模型生图、全局搜索公共 API。 |
| PostgreSQL | 存储用户、帖子、评论、商品、订单、支付、消息、AI 任务等核心数据 | 表结构变更必须走 Flyway；当前文档重构不改迁移。 |
| Redis | 作为缓存、限流、异步能力扩展基础设施 | 当前 MVP 不依赖 Redis 才能完成全部链路。 |
| OSS Provider | `/uploads/presign -> PUT -> /uploads/confirm` 上传链路 | Aliyun Provider 是骨架和开发联调能力，生产仍缺 STS、CORS、CDN、防盗链、审核、缩略图。 |
| AI Provider | 任务编排、自研拼豆算法、开发态结果 | 真实视觉模型 Provider 未接入，客户端不得直连模型。 |
| Payment Provider | 创建联调支付单、查询状态、回调/退款骨架 | 真实微信/支付宝 SDK/API、退款、对账未完成。 |
| Open Design / SVG | 表达 UI 目标、流程图和开发路线 | 不代表 Android/后端已经实现。 |

## 关键主流程

### 登录与会话

1. Android 调用 `POST /api/v1/auth/sms-code` 获取 Stub 验证码。
2. Android 调用 `POST /api/v1/auth/login/sms`，请求仍包含 `ageGroup=AGE_18_PLUS`。
3. 后端返回 access token、refresh token、用户摘要。
4. Android 使用 DataStore 持久化 Token，启动时 hydrate。
5. 401 或 refresh 失败时清理本地会话并展示登录引导。

### 上传与内容发布

1. Android 调用 `POST /api/v1/uploads/presign` 获取上传 URL 和 fileKey。
2. Android PUT 文件到 Provider 返回的 `uploadUrl`。
3. Android 调用 `POST /api/v1/uploads/confirm` 获取 `fileId`。
4. 业务提交只使用 `fileId`，不能把 `fileKey` 当业务 ID。
5. 发布帖子进入 `REVIEWING`；评论成功后提示等待审核，不假装立即公开。

### 作品详情与评论

目标 UI 顺序为 `图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏`。当前后端支持评论文字、图片、@ 用户、# 话题和贴纸，单条评论图片最多 9 张。Android 后续实现必须区分：

- 图片上传失败：保留缩略图并提供重试，不允许假成功。
- @/# 面板未接真实搜索时：展示开发态或禁用，不允许空点击。
- 纯 @/# 不能单独提交，至少要有文字、图片或贴纸。

### 商城与联调支付

1. 商品列表和详情来自 `GET /products`、`GET /products/{productId}`。
2. 自营商品可进入购物车；玩家二手/定制商品不能走标准购物车。
3. 创建订单需要 `addressId`，地址管理未闭环时 UI 不得伪造默认地址。
4. 创建支付单必须带 `Idempotency-Key`。
5. 支付最终状态以服务端查询为准，`payParams.provider=STUB` 不能展示为真实支付。

### 消息与互关私信

1. 通知与私信在消息页分区展示。
2. 会话详情返回 `mutualFollow`、`remainingNonMutualMessages`、`canSend`。
3. 未互关时同一发送者对同一会话最多发送 3 条。
4. 超限后后端返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`，Android 必须禁用输入或展示明确提示。

## API 与模块图

API 契约以 `05-api-contract.md` 为准，模块关系见 `diagrams/api-module-map.svg`。当前接口模块：

- Auth
- User
- Upload
- Community
- Pattern
- Product / Cart
- Order
- Payment
- Message
- Reward
- Report
- Admin

## 部署边界

开发态推荐：

- Android 真机 debug 包。
- Spring Boot API 本地 `8081`。
- Docker PostgreSQL 宿主端口 `5433`。
- Redis `6379`。
- Local OSS Provider 暴露 `/uploads/**` 本地文件访问。

生产化前必须补齐：

- 密钥管理、STS、最小权限和 CDN。
- 短信真实 Provider、限流和风控。
- 真实 AI Provider、熔断、限流、成本统计和内容安全。
- 真实微信/支付宝支付、退款、对账、异常账务处理。
- 备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场材料。

## 观测与失败兜底

后续实现必须保留或补齐以下观测字段：

- `traceId`
- 用户 ID
- 请求路径
- 接口耗时
- 错误码
- 上传 `fileKey` / `fileId`
- AI `jobId`
- 订单 `orderId`
- 支付 `paymentId`
- 审核/举报记录 ID

失败兜底原则：

- 客户端不能用本地状态覆盖服务端订单或支付状态。
- 上传、评论、支付、AI 任务失败必须有可读原因和重试/退出路径。
- 审核中内容不能进入公开推荐。
- 幂等写接口必须使用 `Idempotency-Key`。
- 服务端不能信任客户端传入金额、库存、用户等级、实名状态或商家身份。
