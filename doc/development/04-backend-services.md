# 04. 后端服务实现方案

## 当前定位

后端使用 Java 21 + Spring Boot 单体服务，负责认证、用户、上传、社区、AI 图纸、商城、订单、支付、消息、奖励、举报和管理后台 API。当前文档重构不修改后端代码，只把真实 Controller、Provider 边界和未完成生产能力写清楚。

API 前缀固定为 `/api/v1`，统一响应由后端包装为：

```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "traceId": "..."
}
```

错误码见 `common/ErrorCode.java`，包括 `INVALID_ARGUMENT`、`UNAUTHORIZED`、`FORBIDDEN`、`NOT_FOUND`、`CONFLICT`、`RATE_LIMITED`、`AUDIT_REJECTED`、`PAYMENT_FAILED`、`INVENTORY_NOT_ENOUGH`、`NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`、`AI_TASK_FAILED`、`INTERNAL_ERROR`。

## 模块划分

| 模块 | Controller / 目录 | 职责 |
|---|---|---|
| Auth | `auth/AuthController.java` | 短信验证码、短信登录、Token 刷新、退出登录、账号注销申请 |
| User | `user/UserController.java` | 当前用户、用户搜索、资料更新、关注/取关、实名、个人互动作品 |
| Upload | `upload/UploadController.java` | 预签名上传、上传确认、FileAsset 元数据 |
| Community | `community/CommunityController.java` | Feed、关注 Feed、帖子 CRUD、点赞、收藏、评论、话题、贴纸 |
| Pattern | `pattern/PatternController.java` | AI 图纸任务、任务查询、取消、收藏、详情、配额 |
| Commerce | `commerce/CommerceController.java` | 商品、SKU、购物车 |
| Order | `order/OrderController.java` | 订单创建、列表、详情、取消 |
| Payment | `payment/PaymentController.java` | 联调支付单、支付状态、回调骨架、退款骨架 |
| Message | `message/MessageController.java` | 通知、通知已读、会话、私信发送 |
| Reward | `reward/RewardController.java` | 签到、签到状态、积分、徽章 |
| Report | `moderation/ReportController.java` | 用户举报 |
| Admin | `admin/AdminAuthController.java`、`admin/AdminController.java` | 管理员登录、用户/内容/商品/订单/支付/AI/举报/日志后台 API |
| Common | `common/*` | 统一响应、错误码、鉴权、实体、Repository、TraceId |

## 安全与鉴权

当前安全配置：

- `/actuator/**`、OpenAPI、Swagger 公开。
- `/api/v1/auth/**` 公开。
- `/api/v1/admin/auth/login` 公开。
- `/api/v1/payments/callbacks/**` 公开给支付回调。
- 社区 Feed、帖子详情、话题、贴纸、商品列表和商品详情允许公开读取。
- `/api/v1/admin/**` 要求管理员角色。
- 其他接口默认要求登录。

鉴权事实：

- 短信验证码开发环境固定 `123456`。
- `SmsLoginRequest` 当前字段为 `phone`、`code`、`ageGroup`、`nickname`；Android 仍传 `AGE_18_PLUS`。
- refresh token 必须可吊销；退出登录应吊销当前 refresh token。
- 玩家卖家、提现、定制服务发布者等生产要求仍需 18+ 实名，但相关闭环未完成。

## Controller 覆盖状态

| 模块 | 后端存在 | Android 当前接入 | 备注 |
|---|---|---|---|
| Auth | 是 | 部分接入 | 账号注销申请后端存在，Android 未接入完整入口。 |
| User | 是 | 部分接入 | 资料更新、公开用户、实名后端存在；Profile Edit 仍是设计目标。 |
| Upload | 是 | 接入 | 使用 `local|stub|aliyun` Provider；生产能力未完成。 |
| Community | 是 | 部分接入 | 帖子编辑/删除、评论删除后端存在，Android 当前重点是详情和评论 UI 收敛。 |
| Pattern | 是 | 部分接入 | `GET /quota` 后端存在，Android Retrofit 当前未声明。 |
| Product / Cart | 是 | 接入 | 玩家商品不能走标准购物车。 |
| Order | 是 | 接入 | 地址管理未闭环时不得伪造默认地址。 |
| Payment | 是 | 部分接入 | 回调/退款后端存在，Android 当前只接创建和查询支付单。 |
| Message | 是 | 接入 | 私信用通知表承载消息，互关 3 条限制由后端校验。 |
| Reward | 是 | 接入 | 签到、积分、徽章基础接口存在。 |
| Report | 是 | 未接入 Retrofit | 可作为后续举报入口任务。 |
| Admin | 是 | 未接 Android | 属于后台 API，不是 App 内用户路径。 |

## 上传 Provider

上传链路：

```text
POST /uploads/presign
  -> Provider.presign(fileKey, mimeType, expires)
  -> Android PUT uploadUrl
POST /uploads/confirm
  -> Provider.confirm(fileKey)
  -> FileAsset(fileId, ownerId, usage, publicUrl, auditStatus)
```

当前限制：

- 支持用途：`AVATAR`、`POST_IMAGE`、`POST_VIDEO`、`AI_INPUT`、`PATTERN_OUTPUT`、`PRODUCT_IMAGE`、`TRADE_IMAGE`。
- 非图片仅 `POST_VIDEO` 可通过当前类型校验。
- 文件大小限制为 20MB。
- 确认后 `auditStatus` 初始为 `NEED_MANUAL_REVIEW`。
- Aliyun OSS Provider 只代表后端骨架可通过私有环境配置启用；生产仍需 STS、CORS、CDN、防盗链、审核、缩略图和监控。

## 社区与评论

当前后端支持：

- 推荐 Feed、关注 Feed。
- 发布帖子进入 `REVIEWING`。
- 作品详情返回 `likedByMe`、`favoritedByMe`、`followedAuthorByMe`。
- 点赞/取消、收藏/取消。
- 评论列表和发表评论。
- 评论支持 `content`、`parentId`、`mediaFileIds`、`mentionUserIds`、`topicIds`、`stickerIds`。
- 单条评论最多 9 张图片。
- 评论图片必须是当前用户上传、用途为 `POST_IMAGE`、MIME 为 image。
- @ 用户会生成 `MENTION` 通知。
- 话题和贴纸必须存在。

边界：

- 文字、图片、贴纸三者至少存在一种；@/# 不能单独构成评论。
- 评论提交成功后状态为 `REVIEWING`，前端不能假装立即公开。
- 生产级内容审核和图片审核未完成。

## 消息与互关私信

当前后端使用 `ConversationEntity` + `NotificationEntity` 承载会话和消息：

- `GET /messages/notifications` 返回通知列表。
- `POST /messages/notifications/read` 当前后端标记当前用户全部通知已读；Android 请求体目前不影响后端逻辑。
- `GET /messages/conversations` 返回会话列表。
- `GET /messages/conversations/{conversationId}` 返回会话信息和消息列表。
- `POST /messages/conversations/{conversationId}` 发送私信。

互关限制：

- `mutualFollow=true` 时可正常发送。
- 未互关时，同一发送者对同一会话最多发送 3 条。
- 超过后抛出 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`，文案为“互相关注后可继续聊天”。
- 会话视图返回 `remainingNonMutualMessages` 和 `canSend`。

当前不做：好友申请审批、图片私信、撤回、黑名单、复杂已读回执和生产级反骚扰。

## 商城、订单与支付

商城：

- `ProductType` 包括 `SELF_OPERATED`、`PLAYER_SECOND_HAND`、`PLAYER_CUSTOM_SERVICE`。
- 标准购物车只面向自营商品；玩家商品不能混入购物车。
- 库存、价格和订单金额以服务端为准。

订单：

- 创建订单需要 `itemIds`、`addressId`、`remark`。
- 写接口需 `Idempotency-Key`。
- 地址管理未闭环时，客户端不得伪造默认地址。

支付：

- 创建支付单需要 `orderId` 和 `channel`，渠道当前为 `WECHAT_APP` 或 `ALIPAY_APP`。
- `payParams` 返回 `{ provider: "STUB", payload: "stub-pay-payload-..." }`。
- 回调和退款是骨架，不代表真实支付生产能力。
- 订单最终状态以服务端查询为准。

## AI 图纸

后端已提供：

- 创建图纸任务。
- 查询任务。
- 任务列表。
- 取消任务。
- 图纸收藏。
- 图纸详情。
- 配额接口。
- 自研拼豆算法、材料统计和结果资产。

未完成：

- 真实视觉 Provider。
- 输入/输出生产级内容安全。
- Provider 限流、熔断、监控和真实成本统计。

## 管理后台

当前后端提供管理后台 API，但没有完整运营后台前端。已有能力包括：

- 管理员登录。
- 用户、帖子、评论、商品、订单、支付、AI 任务、举报、操作日志列表。
- 举报处理。
- 帖子、评论、商品审核。
- 用户状态变更。
- AI 任务重试和取消。

这些 API 不等于生产运营后台已完成，上线前仍需补操作审计、权限分级、风控工作台和合规材料。

## 本地启动与测试

启动：

```powershell
cd doyu-server
.\start-dev.bat
```

等价手动步骤：

```powershell
docker compose up -d postgres redis
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端测试：

```powershell
cd doyu-server
mvn test
```

本轮只改文档和 SVG，不运行后端测试；若后续阶段修改 `doyu-server/`，必须补跑。
