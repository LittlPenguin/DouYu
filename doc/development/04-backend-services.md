# 04. 后端服务实现方案

## 后端目标

后端负责业务规则、数据一致性、支付安全、AI 编排、内容审核、风控和管理后台能力。当前采用 Spring Boot 模块化单体，不拆微服务。

## 当前实现状态

- Java 21 + Spring Boot。
- API 前缀统一为 `/api/v1`。
- Spring Security + JWT access token / refresh token。
- 核心业务对象已迁移到 PostgreSQL + Spring Data JPA Repository。
- Flyway 管理数据库迁移。
- Redis 已作为基础设施接入，当前主要用于后续缓存、限流和异步能力扩展。
- OpenAPI/Swagger 已覆盖主要 Controller。
- 本地开发上传使用 Local OSS Provider。
- AI 拼豆任务已支持异步执行和自研算法生成。
- 关注/取关接口已存在；第六轮好友能力按关注/互相关注收敛，不新增复杂好友申请审批。
- 真实 AI Provider、地图 API、真实微信/支付宝支付、生产级内容审核仍未完成。

## 模块划分

| 模块 | 职责 |
|---|---|
| `auth` | 短信登录、注册、token、刷新、退出、账号注销 |
| `user` | 用户资料、主页、实名状态、年龄状态 |
| `community` | 帖子、评论、点赞、收藏、关注、Feed |
| `upload` | 上传凭证、文件元数据、本地开发对象存储 |
| `pattern` | AI 拼豆任务、图纸资产、算法、额度、PDF |
| `commerce` | 商品、SKU、购物车、库存 |
| `order` | 订单、取消、售后入口 |
| `payment` | 支付单、支付参数、回调、安全校验骨架 |
| `message` | 通知、私信、会话 |
| `reward` | 签到、等级、经验、徽章 |
| `moderation` | 内容审核、举报、处理记录 |
| `admin` | 后台登录、用户、内容、商品、订单、举报、操作日志 |
| `common` | 统一响应、错误码、鉴权、实体、Repository、TraceId |

## 本地开发启动

后端工程路径：`D:\Studio\SpellBean\doyu-server`。

推荐启动：

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

等价手动命令：

```powershell
docker compose up -d postgres redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

端口：

- 后端：`8081`
- PostgreSQL：宿主机 `5433`，容器内 `5432`
- Redis：`6379`

测试：

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

## 安全配置

Spring Security 配置位于 `common/SecurityConfig.java`。

公开接口：

- `/actuator/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/api/v1/auth/**`
- `/api/v1/admin/auth/login`
- `/api/v1/payments/callbacks/**`
- `GET /api/v1/posts/feed`
- `GET /api/v1/posts/following`
- `GET /api/v1/posts/{postId}`
- `GET /api/v1/products`
- `GET /api/v1/products/{productId}`
- `/uploads/**`

后台接口：

- `/api/v1/admin/**` 要求 `ROLE_ADMIN`。

其他接口默认要求登录。

## 鉴权与账号

- 普通用户和后台管理员使用不同角色。
- 短信验证码开发环境固定为 `123456`。
- 当前后端 `SmsLoginRequest` 仍要求 `ageGroup`，Android 现阶段继续传默认 `AGE_18_PLUS`。
- 年龄段、未成年人状态和实名状态长期应由后端业务逻辑维护；移除登录 `ageGroup` 需要先修改后端接口和客户端模型。
- 玩家卖家、提现、定制服务发布者必须 18+ 实名。
- refresh token 必须可撤销；退出登录应撤销当前 refresh token。

## 关注、好友和私信限制

第六轮好友语义只做最小闭环：

- “好友”在当前阶段等价于关注关系的产品化表达；双方互相关注时展示为“互相关注”。
- 复用 `POST /api/v1/users/{userId}/follow` 和 `DELETE /api/v1/users/{userId}/follow`，关注/取关必须幂等，不能因为重复点击产生重复关系或错误粉丝数。
- 用户主页、帖子作者、会话对方信息可返回非破坏性关系字段，例如 `followedByMe`、`followsMe`、`mutualFollow`，供 Android 展示“关注 / 已关注 / 互相关注”。
- 私信会话详情必须返回真实消息列表、发送方信息和对方关系状态，不再只返回空 `messages` 占位。
- 未互关时，同一发送者对同一会话最多发送 3 条私信；超过后返回明确业务错误和可读文案，Android 展示“互相关注后可继续聊天”或禁用发送。
- 本轮不做好友申请、同意/拒绝、黑名单、图片私信、撤回、复杂已读回执和生产级反骚扰风控。

## 持久化

- 当前核心业务数据使用 JPA Repository 持久化。
- Flyway 迁移文件位于 `src/main/resources/db/migration/`。
- `ddl-auto` 使用 `validate`，避免运行时隐式改表。
- 对外业务 ID 使用字符串。
- 订单、支付、退款、库存、实名、审核相关改动必须说明迁移和回滚风险。

## 上传与对象存储

当前开发环境：

- 默认使用 Local OSS Provider；`douyu.oss.provider` 默认值为 `local`。
- 后端签发上传地址。
- Android 直传文件。
- 后端 confirm 后生成 `FileAsset`。
- `/uploads/**` 用于本地开发访问文件。
- 第四轮新增本地 seed assets：`src/main/resources/static/seed/` 下提交社区和商城演示图片，`ATTRIBUTION.md` 记录来源和许可说明；这些图片只用于本地 QA/演示，不代表生产对象存储或真实用户上传链路。
- 第四轮商品和帖子 seed 数据通过 Flyway 字段 `products.image_url`、`posts.cover_image_url` 暴露图片 URL。商品列表/详情返回 `imageUrl`，购物车商品摘要返回 `product.imageUrl`，社区 Feed/详情返回 `coverImageUrl`。

第五轮前置能力：

- 后端已提供 Aliyun OSS Provider 骨架，可通过 `.env` 私有配置 `DOUYU_OSS_PROVIDER=aliyun` 启用。
- Aliyun OSS Provider 复用现有 `/api/v1/uploads/presign`、客户端 PUT 直传和 `/api/v1/uploads/confirm` 流程，不新增公共 API。
- Aliyun OSS 必填配置为 `DOUYU_ALIYUN_OSS_ENDPOINT`、`DOUYU_ALIYUN_OSS_REGION`、`DOUYU_ALIYUN_OSS_BUCKET`、`DOUYU_ALIYUN_OSS_ACCESS_KEY_ID`、`DOUYU_ALIYUN_OSS_ACCESS_KEY_SECRET`、`DOUYU_ALIYUN_OSS_PUBLIC_BASE_URL`。
- Local OSS 仍是 dev 默认；test profile 使用 Stub OSS，后端测试不依赖云服务。
- 客户端不得持有 OSS Secret。
- 上传文件默认按不可信输入处理，必须经过类型、大小、用途和审核校验。
- 第四轮 seed assets 暂不迁移到 Aliyun OSS；生产 CDN、防盗链、STS 临时凭证、图片审核和缩略图处理仍是后续生产化任务。

## AI 拼豆

当前已实现：

- AI 任务创建、查询、列表、取消。
- 异步执行器 `PatternJobExecutor`。
- 进度追踪。
- BeadPatternEngine 图纸算法。
- 预览图、色号图、材料清单、PDF 生成。
- AI 调用缓存和每日额度记录。

仍未完成：

- 真实阿里云百炼/通义万相 API 调用。
- 生产级输入/输出审核。
- Provider 限流、熔断、监控和真实成本统计。

## 支付

当前已实现：

- 创建支付单。
- 返回开发态支付参数。
- 支付回调入口。
- 回调签名验证接口。
- 金额校验、渠道一致性、时间窗口防重放和幂等处理骨架。

仍未完成：

- 微信支付 App 支付真实 SDK/API。
- 支付宝 App 支付真实 SDK/API。
- 渠道主动查询。
- 退款真实调用和退款回调。
- 对账和异常账务处理。

客户端不得单点判定支付成功，订单最终状态以服务端为准。

## 管理后台

后端提供后台 API：

- 管理员登录。
- 用户列表和搜索。
- 帖子列表和搜索。
- 举报处理。
- 商品、订单、AI 任务等运营处理入口。
- 操作日志。

当前还没有完整运营后台前端。上线前需要补齐运营可用的审核、举报、订单和风控工作台。

## Provider 边界

当前 Provider 状态：

| 能力 | 当前状态 | 生产要求 |
|---|---|---|
| 短信 | Stub 验证码 `123456` | 接入真实短信供应商、限流、防刷 |
| OSS | Local OSS Provider；Aliyun OSS Provider 骨架可按 `.env` 私有配置启用 | 补齐 STS/权限、CORS、CDN、防盗链、图片审核、缩略图和运维监控 |
| AI | Stub + 自研算法，Aliyun provider 占位 | 接入真实视觉 Provider |
| 微信支付 | Stub 参数和回调骨架 | 官方 SDK/API、验签、查询、退款、对账 |
| 支付宝支付 | Stub 参数和回调骨架 | 官方 SDK/API、验签、查询、退款、对账 |
| 内容审核 | 基础关键词过滤 | 云内容安全 + 人审后台 + 风控策略 |

不得把 Stub 或占位 Provider 当作生产能力交付。
