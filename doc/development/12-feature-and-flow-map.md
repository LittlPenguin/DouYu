# 豆屿 Doyu 主链路与功能地图

> 更新日期：2026-06-04
> 用途：按功能链路说明当前已有能力、半成品边界、禁止误导展示和后续优先级。接口字段以 `05-api-contract.md` 为准，页面行为以 `03-android-client.md` 和 `13-ui-screen-blueprints.md` 为准。

## 总体闭环

豆屿的产品闭环是：

1. 用户在社区看到拼豆作品或教程。
2. 用户点赞、收藏、评论、关注作者，或进入 AI 拼图生成自己的图纸。
3. 用户通过 AI 图纸获得预览、色号、豆量和材料清单。
4. 用户在商城购买自营材料，或通过玩家直连了解二手/定制服务。
5. 用户完成作品后发布到社区，沉淀个人资产并带动下一轮内容和交易线索。

当前 MVP 只要求主链路可演示、状态清楚。真实 AI、真实支付、生产合规、完整玩家交易、地址履约和生产级审核不在第六轮关闭。

## 五个主 Tab 地图

| 主 Tab | 当前已有 | 半成品 / 风险 | 展示策略 | 后续优先级 |
|---|---|---|---|---|
| 社区 | Feed、关注 Feed、帖子详情、发帖、评论、点赞、收藏、关注状态回显 | 视频评论、生产级图片审核、复杂推荐、话题运营后台未完成 | 保留真实接口；图片/贴纸/@/# 评论进入审核中，不假装立即公开 | P0 |
| 商城 | 商品列表/详情、购物车、订单、联调支付单、玩家商品禁用标准购物车 | 地址管理、真实支付、玩家交易闭环未完成 | 自营商品可走标准购物车；玩家商品只展示信息和直连边界；缺地址不能伪下单 | P0/P3 |
| AI 拼图 | 上传、参数、任务创建、轮询、取消、历史、结果、材料清单、PDF 记录骨架 | 真实视觉 Provider 未完成，部分结果操作仍是开发态 | 只表达开发态图纸生成；加购/PDF/分享未闭环时禁用或说明 | P0/P5 |
| 消息 | 通知列表、会话列表、会话详情、文本私信、未互关 3 条限制 | 图片私信、复杂已读、黑名单、反骚扰风控未完成 | 通知/私信分区；超过限制时禁用输入或提示互相关注后继续 | P0 |
| 我的 | 资料、我的图纸、收藏、订单、点赞/评论/收藏/关注作品资产页 | 成长系统、签到动作、设置合规入口仍不完整 | 已接接口入口保留；未闭环入口隐藏、禁用或标开发态 | P0/P4 |

## 登录态链路

当前已有：

- 短信验证码 Stub，开发环境固定 `123456`。
- Android 登录请求仍包含 `ageGroup=AGE_18_PLUS`。
- 后端提供短信登录、刷新 token、退出登录。
- Android 使用 DataStore 持久化 access/refresh token，启动时 hydrate。
- 401 refresh 成功后更新 TokenStore；退出登录或鉴权过期时清理。

禁止误导：

- 不得写成客户端已移除 `ageGroup`。
- 不得把登录态持久化写成生产级账号安全完成。
- 未登录用户访问受保护页面时，应显示登录引导，不展示技术错误。

回归重点：

- 登录后强杀重启仍保持登录态。
- 退出登录后重启不恢复旧登录态。
- refresh token 更新后可持久化。

## 社区互动链路

当前已有：

- 推荐 Feed：`GET /posts/feed`。
- 关注 Feed：`GET /posts/following`。
- 发帖：`POST /posts`，进入 `REVIEWING`。
- 帖子详情：`GET /posts/{postId}`，返回 `likedByMe`、`favoritedByMe`、`followedAuthorByMe`。
- 点赞/取消：`POST|DELETE /posts/{postId}/like`。
- 收藏/取消：`POST|DELETE /posts/{postId}/favorite`。
- 评论列表与发布：`GET|POST /posts/{postId}/comments`。
- 用户搜索、话题列表、内置贴纸包。
- 我的互动资产页：点赞作品、评论作品、收藏作品、关注作者作品。

评论 MVP 规则：

- 有效评论必须至少包含文字、图片、贴纸三者之一。
- `mentionUserIds` 和 `topicIds` 不能单独构成有效评论。
- 评论图片最多 9 张。
- 图片必须属于当前用户，usage 必须为 `POST_IMAGE`，mimeType 必须是图片。
- 非法 @ 用户、话题或贴纸返回参数错误。
- 发布响应和评论列表都返回 `mediaAssets`、`mentions`、`topics`、`stickers`。

禁止误导：

- 不做视频评论、图片私信、自定义贴纸、付费表情包、贴纸上传、热榜或复杂推荐。
- 不把 `REVIEWING` 包装成立即公开。
- 上传失败时必须阻断评论提交并保留已选内容，不能假成功。

## 关注 / 互关 / 私信链路

当前好友语义只做关注关系：

- 关注：`POST /users/{userId}/follow`。
- 取消关注：`DELETE /users/{userId}/follow`。
- 返回 `followedByMe`、`followsMe`、`mutualFollow`。
- 不做好友申请、待通过、同意/拒绝状态机。

私信限制：

- 会话详情返回 `mutualFollow`、`remainingNonMutualMessages`、`canSend`。
- 未互关时，同一发送者对同一会话最多发送 3 条。
- 超过后后端返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED` 和“互相关注后可继续聊天”。
- Android 必须清楚提示或禁用输入，不能假装发送成功。

不做范围：

- 图片私信。
- 复杂已读。
- 消息撤回。
- 黑名单。
- 生产级反骚扰风控。

## AI 图纸链路

当前已有：

- 上传 AI 输入图。
- 创建任务：`POST /patterns/jobs`。
- 任务查询和历史：`GET /patterns/jobs/{jobId}`、`GET /patterns/jobs`。
- 取消任务：`POST /patterns/jobs/{jobId}/cancel`。
- 图纸详情、收藏、材料清单和 PDF 文件记录骨架。
- 自研拼豆算法：色差、多色卡、难度、风格、色号图、材料统计。

开发态边界：

- 真实视觉 Provider 未完成，`AliyunBailianProvider` 仍不能写成真实接入完成。
- AI 只承诺开发态图纸生成，不承诺真实视觉理解质量。
- 材料加购、PDF 导出、分享到社区如果未闭环，必须禁用或展示开发态说明。

后续方向：

- 真实 Provider 接入前需补供应商配置、内容审核、额度、降级、成本和失败处理。
- 客户端不得直连模型供应商，不得保存 AI 密钥。

## 商城 / 订单 / 支付链路

当前已有：

- 商品列表：`GET /products`。
- 商品详情：`GET /products/{productId}`。
- 购物车：`GET /cart`、`POST /cart/items`、`PATCH /cart/items/{itemId}`、`DELETE /cart/items/{itemId}`。
- 订单：`POST /orders`、`GET /orders`、`GET /orders/{orderId}`、`POST /orders/{orderId}/cancel`。
- 支付单：`POST /payments`、`GET /payments/{paymentId}`。
- 自营商品可进入标准购物车；玩家二手/定制商品不能进入标准购物车/标准订单。

边界：

- 地址管理未闭环，订单确认只能展示地址缺口并禁用伪下单。
- 支付只展示联调支付单和服务端确认状态。
- 微信/支付宝真实 SDK、退款、对账未接入。
- 玩家商品只展示直连信息，不沉淀平台资金池。

禁止误导：

- 不显示“微信支付成功”“支付宝支付成功”等正式渠道完成态。
- 不生成伪地址。
- 不把玩家商品混入标准购物车。
- 不把支付 Stub 写成真实收款能力。

## 上传 / OSS 链路

当前已有：

- `POST /uploads/presign`：后端签发上传 URL。
- 客户端 `PUT uploadUrl`：直传对象存储或本地 Provider。
- `POST /uploads/confirm`：后端记录 `FileAsset`。
- Provider 可通过 `.env` 选择 `local|stub|aliyun`。
- Aliyun OSS 只由后端持有 AccessKey。

边界：

- 单台真机真实 Bucket smoke 只证明当前开发环境可联调。
- 生产仍需 STS/最小权限、CORS 最终收敛、CDN、防盗链、图片审核、病毒扫描、缩略图和 seed assets 云迁移。
- 客户端不得保存 OSS Secret。

## 审核 / 合规 / 风控边界

当前已有：

- 基础内容审核服务。
- 举报处理 API。
- 管理后台 API 骨架。
- 支付回调验证骨架。
- 账号注销等基础合规接口。

未完成：

- 生产级内容审核、图片审核、版权识别、诈骗识别和交易风控。
- 隐私政策、用户协议、SDK 清单、备案、版权投诉材料。
- 完整运营工作台前端。

展示策略：

- 设置/合规入口只能保留必要结构或开发态说明。
- 不可用入口隐藏、禁用或明确“开发态不可用”。
- 不能出现空点击、假成功 Toast 或误导性合规完成表达。
