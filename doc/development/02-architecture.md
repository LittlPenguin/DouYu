# 02. 总体架构

## 架构定位

豆屿 Doyu 当前采用 Android 客户端 + Spring Boot 模块化单体后端 + PostgreSQL + Redis + 对象存储 Provider 的开发态架构。本轮迁移的核心是：

- Android 从 Kotlin + Compose 改为 Java + Activity/Fragment + XML。
- 后端删除运行期 seed/demo 填充，空库返回空列表。
- Open Design HTML 成为 UI 结构、视觉层级、文案、状态和交互的权威。

服务五条主链路：

1. 社区内容发现、发布、作品详情、评论、点赞、收藏、关注。
2. AI 拼豆图纸任务、图纸资产、收藏、历史。
3. 自营商城、购物车、订单和联调支付单。
4. 消息通知、私信会话和互关私信限制。
5. 我的资产、资料、签到、徽章、设置入口。

## 分层结构

```text
Android App
  - Java Activity / Fragment
  - XML layouts / RecyclerView / Material Components
  - Repository / Retrofit / Gson / OkHttp
  - SharedPreferences session state

Spring Boot API /api/v1
  - Auth / User / Upload
  - Community / Pattern
  - Commerce / Order / Payment
  - Message / Reward / Report
  - Admin

Persistence
  - PostgreSQL + Flyway
  - Redis
  - FileAsset metadata

Providers
  - SMS Stub
  - Local / Stub / Aliyun OSS Provider
  - AI Stub + backend orchestration
  - Payment Stub / callback skeleton

Design & Docs
  - Open Design HTML prototypes
  - doc/development manuals
  - diagrams SVG references
```

## Android 包结构

目标包结构：

| 包           | 职责                                 |
| ----------- | ---------------------------------- |
| `core`      | Intent extras、通用格式化、常量、错误边界        |
| `network`   | Retrofit API、Gson 响应、OkHttp client |
| `model`     | Java POJO DTO，字段名兼容后端 JSON         |
| `data`      | Repository，默认调用真实 API              |
| `ui`        | 通用 XML/RecyclerView 辅助组件           |
| `auth`      | 登录态、未登录引导、会话持久化                    |
| `community` | 社区首页、搜索、发帖、作品详情、评论                 |
| `commerce`  | 商城、商品详情、购物车、订单、支付边界                |
| `ai`        | AI 首页、图片选择、CameraX、参数、进度、结果、历史     |
| `message`   | 消息首页、会话详情、通知详情                     |
| `profile`   | 我的、资料编辑、Settings 首页和分区             |

## 组件边界

| 组件                | 当前职责                                               | 当前边界                                            |
| ----------------- | -------------------------------------------------- | ----------------------------------------------- |
| Android App       | 展示五个主 Tab 和二级流程页，调用真实 API，处理空态/错误态/未登录态            | 不使用本地 mock 补齐数据；不直连 OSS/AI/支付密钥；不新增后端契约。        |
| Spring Boot API   | 提供 `/api/v1` REST 接口、鉴权、业务规则、Provider 编排和后台 API    | 本轮删除 seed/demo，不新增地图、真实支付、大模型生图、全局搜索公开 API。     |
| PostgreSQL        | 存储用户、帖子、评论、商品、订单、支付、消息、AI 任务等核心数据                  | 表结构变更必须走 Flyway；本轮不靠运行期 DataInitializer 填充业务内容。 |
| Redis             | 缓存、限流、异步能力扩展基础设施                                   | 当前 MVP 不依赖 Redis 才能展示假数据。                       |
| OSS Provider      | `/uploads/presign -> PUT -> /uploads/confirm` 上传链路 | Aliyun Provider 是骨架和联调能力，不代表生产对象存储完成。           |
| AI Provider       | 任务编排、Stub 结果和后端边界                                  | 真实视觉模型未接入，客户端不得承诺真实识图质量。                        |
| Payment Provider  | 创建联调支付单、查询状态、回调/退款骨架                               | 真实微信/支付宝 SDK/API、退款、对账未完成。                      |
| Open Design / SVG | 表达 UI 目标、流程图和页面蓝图                                  | 不代表 Android/后端已经实现；验收必须看当前源码和截图。                |

## 关键主流程

### 登录与会话

1. Android 调用 `POST /api/v1/auth/sms-code` 获取 Stub 验证码。
2. Android 调用 `POST /api/v1/auth/login/sms`，请求仍包含 `ageGroup=AGE_18_PLUS`。
3. 后端返回 access token、refresh token 和用户摘要。
4. Android 用 Java `SharedPreferences` 持久化 token。
5. 401 或 refresh 失败时清理本地会话并展示统一登录引导。

### 上传与内容发布

1. Android 调用 `POST /api/v1/uploads/presign` 获取 `uploadUrl` 和 `fileKey`。
2. Android PUT 文件到 Provider 返回的 `uploadUrl`。
3. Android 调用 `POST /api/v1/uploads/confirm` 获取 `fileId`。
4. 业务提交只使用 `fileId`，不能把 `fileKey` 当业务 ID。
5. 发布帖子进入审核或后端返回状态；客户端不得假装公开成功。

### 社区与评论

作品详情目标顺序为 `图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏`。评论可含文字、图片、@ 用户、# 话题和贴纸。客户端必须区分：

- 图片上传失败：保留缩略图并提供重试，禁止假成功。
- @/# 面板未接真实搜索时：显示开发态或禁用态，不允许空点击。
- 纯 @/# 不能单独提交，至少要有文字、图片或贴纸。

### 商城与联调支付

1. 商品列表和详情来自真实 API。
2. 自营商品可进入购物车；玩家二手/定制商品不能走标准购物车。
3. 创建订单需要 `addressId`；地址管理未闭环时 UI 不得伪造默认地址。
4. 创建支付单必须使用幂等键。
5. `payParams.provider=STUB` 只能展示为联调支付，不得写成真实支付 SDK。

### 消息与互关私信

1. 消息首页分为私信和通知。
2. 会话详情返回 `mutualFollow`、`remainingNonMutualMessages`、`canSend`。
3. 未互关时同一发送者对同一会话最多发送 3 条。
4. 超限后后端返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`，Android 必须禁用输入或展示明确提示。

## API 与模块图

API 契约以 `05-api-contract.md` 和后端 Controller 为准。当前公开模块：

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

- Android debug 包。
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
- 幂等写接口必须使用幂等键。
- 服务端不能信任客户端传入金额、库存、用户等级、实名状态或商家身份。
