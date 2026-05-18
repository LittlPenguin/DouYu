# 04. 后端服务实现方案

## 后端目标

后端负责业务规则、数据一致性、支付安全、AI 编排、内容审核、风控和管理后台能力。第一版采用 Spring Boot 模块化单体。

## 当前实现状态

更新日期：2026-05-14。

### OpenAPI 注解

13 个 Controller 全部已补充 `@Tag`、`@Operation`、`@ApiResponses` 注解，Swagger UI 可浏览所有接口。已注解的 Controller：

AuthController、AdminController、AdminAuthController、CommunityController、CommerceController、OrderController、PaymentController、MessageController、RewardController、PatternController、UploadController、UserController、ReportController。

### 字段补齐

以下响应字段已在控制器中补齐，与前端 Models.kt 对齐：

| 接口 | 补齐字段 |
|---|---|
| 帖子详情/列表 | `author` 对象（含 `userId`、`nickname`、`avatarUrl`、`bio`、`level`、`isMinor`） |
| AI 任务详情 | `userId`、`paletteName`、`progress`、`inputName` |
| 购物车列表 | `productId`、`product` 商品摘要 |
| 订单详情 | `orderItemId`、`title`、`specName`、`sellerId`、`addressSnapshot` |
| 通知列表 | `notificationId`、`unread`（`readAt == null`） |
| 会话列表 | `peerUserId`、`peerName`、`lastMessage`、`unreadCount` |
| 用户信息 | `avatarUrl`、`level`、`followingCount`、`followerCount` |
| 图纸详情 | `ownerId`、`title`、`paletteName`、`colorStats` |
| 支付查询 | `paidAt` |
| 签到 | `checkedToday` |
| 徽章 | `description`、`achieved` |

### CreateOrderRequest

创建订单接口改为接收 `itemIds`（购物车项 ID 列表）+ `addressId`（收货地址 ID）+ `remark`（可选备注）。

### InMemoryStore 当前状态

核心业务数据仍使用 `InMemoryStore` 内存存储，PostgreSQL schema 通过 Flyway 定义但核心业务 Repository 尚未全面接入数据库。唯一已接入数据库的 Mapper 为 `DatabaseHealthMapper`。

### 测试

15 个测试全部通过，包括 7 个新增联调测试：社区点赞收藏评论、关注取关、签到成长、消息通知会话、错误场景、Feed 分页、OpenAPI 文档覆盖。

## 模块划分

| 模块 | 职责 |
|---|---|
| Auth | 登录、注册、token、验证码、账号注销 |
| User | 用户资料、主页、实名状态、年龄状态 |
| Community | 帖子、评论、点赞、收藏、关注、话题 |
| Upload | 上传凭证、文件元数据、缩略图、审核触发 |
| Pattern | AI 拼豆任务、图纸资产、色号清单 |
| Commerce | 商品、SKU、购物车、库存 |
| Order | 订单、履约、取消、售后 |
| Payment | 微信支付、支付宝支付、回调、退款 |
| Message | 通知、私信、系统消息 |
| Reward | 签到、等级、经验、徽章 |
| Moderation | 内容审核、举报、处罚 |
| Admin | 管理后台接口 |

## 本地开发启动

后端工程路径：`D:\Studio\SpellBean\doyu-server`。

Windows 本地推荐使用启动脚本：

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

脚本等价于：

```powershell
docker compose up -d postgres redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 使用 Docker Compose 中的 PostgreSQL 16 和 Redis 7。Spring Boot 4 当前使用的 Flyway 需要在 Maven 中包含 `org.flywaydb:flyway-database-postgresql`，否则连接 PostgreSQL 16 时会在启动阶段报 `Unsupported Database: PostgreSQL 16.x`。

端口配置：

- 后端：`server.port: 8081`（`application.yml`）
- PostgreSQL 宿主机端口：`5433:5432`（`docker-compose.yml`，容器内仍为 5432）
- Redis：`6379:6379`

## 安全配置（SecurityConfig）

Spring Security 配置位于 `common/SecurityConfig.java`。

公开接口（permitAll）：

- 认证相关：`/auth/sms-code`、`/auth/login/sms`、`/auth/refresh`
- 后台登录：`/admin/auth/login`
- 支付回调：`/payments/callbacks/**`
- 社区 Feed：`/posts/feed`、`/posts/following`
- 帖子详情：`GET /posts/*`（仅 GET 方法免登录）
- 商品浏览：`/products`、`/products/*`
- 文件访问：`/uploads/**`

管理后台接口（`/admin/**`）要求 `ROLE_ADMIN` 权限，其余接口均需认证。

## 鉴权与账号

认证方式：

- access token：短期有效。
- refresh token：长期有效，可撤销。
- 管理后台账号与普通用户账号分离。

账号状态：

- 正常。
- 待验证。
- 限制发布。
- 限制交易。
- 封禁。
- 注销中。
- 已注销。

年龄策略：

- 用户注册时确认年龄段。
- 16-17 岁标记为未成年人。
- 玩家卖家、提现、定制服务发布必须 18+ 实名。

## 社区服务

社区服务负责：

- Feed 查询。
- 发帖。
- 帖子详情。
- 评论。
- 点赞。
- 收藏。
- 关注。
- 话题。
- 举报。

内容状态：

- 草稿。
- 审核中。
- 可见。
- 仅自己可见。
- 驳回。
- 删除。

Feed 第一版采用规则排序：

- 审核通过。
- 发布时间。
- 点赞数。
- 收藏数。
- 评论数。
- 举报降权。
- 运营置顶。

## AI 图纸任务服务

任务特点：

- 异步。
- 可追踪。
- 可重试。
- 可审核。
- 可计费或消耗次数。

后端职责：

- 校验用户额度。
- 校验输入图片。
- 创建任务。
- 投递队列。
- 执行或调度 AI Provider。
- 保存结果文件。
- 更新任务状态。
- 失败返还次数。

任务状态见 `06-data-model.md`。

## 上传服务

上传服务只签发凭证，不接收大文件作为默认路径。

服务端校验：

- 文件用途。
- 文件类型。
- 文件大小。
- 用户权限。
- 上传频率。

上传完成后：

- 保存文件元数据。
- 触发内容审核。
- 生成缩略图。
- 清理 EXIF。
- 按用途决定是否可公开访问。

## 商城与订单

后端必须保证：

- 商品价格以服务端为准。
- 库存以服务端为准。
- 优惠以服务端为准。
- 订单状态机不可被客户端跳转。
- 支付前锁定库存或下单时校验库存。

自营商城和玩家市场要区分交易责任。第一版玩家直连交易以撮合、留痕、举报、风控为主。

## 支付服务

支付服务负责：

- 创建支付单。
- 调用微信支付。
- 调用支付宝。
- 处理支付回调。
- 主动查询支付状态。
- 退款申请。
- 支付日志。

支付回调要求：

- 验签。
- 幂等。
- 记录原始通知摘要。
- 不信任客户端状态。
- 异常时可重放处理。

## 消息服务

消息类型：

- 评论。
- 点赞收藏。
- 关注。
- 系统通知。
- AI 完成。
- 订单。
- 私信。
- 举报处理。

私信风控：

- 敏感词过滤。
- 频率限制。
- 陌生人限制。
- 举报入口。
- 高风险外部联系方式识别。

## 审核与风控

审核对象：

- 昵称、头像、简介。
- 帖子文本和媒体。
- 评论。
- 私信。
- 商品信息。
- AI 输入和输出。
- 玩家交易内容。

处罚方式：

- 内容驳回。
- 删除。
- 限流。
- 禁言。
- 限制交易。
- 封禁账号。

## 管理后台

后台必须支持：

- 用户查询和处理。
- 内容审核。
- 举报处理。
- 商品和 SKU 管理。
- 订单查询。
- 支付记录查询。
- AI 任务查询。
- 玩家交易风控。
- 等级和签到配置。
- 运营位和话题管理。

后台操作必须记录操作人、时间、对象、前后状态和原因。
