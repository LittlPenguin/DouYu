# 12. 功能与主链路地图

## 目标

本文件描述 Java/XML 迁移后的五个主 Tab、跨模块流程、真实 API 边界、UI-only 边界和后续优先级。所有页面实现必须使用 Java Activity/Fragment + XML，并以 Open Design HTML 为 UI 权威。

## 五个主 Tab

| Tab | Java/XML 首屏目标 | 数据来源 | 不得误导 | 优先级 |
|---|---|---|---|---|
| 社区 | `community-home-a.html`，双列瀑布流、频道、作品卡、搜索/发布入口 | `GET /posts/feed`、帖子详情、评论、话题、贴纸、上传链路 | 不把审核中内容当公开内容；不把上传失败写成功；不显示假帖子 | P0 |
| 商城 | `commerce-home-a.html`，分类、Banner 区、商品卡、购物车入口 | 商品、购物车、订单、支付联调 API | 不伪造商品、地址、订单或真实支付结果 | P1 |
| AI | `ai-home-a.html`，创作入口、任务卡、历史入口、开发态边界 | AI 任务、图纸详情、收藏、上传链路 | 不承诺真实视觉 Provider 或大模型生图已接入 | P2 |
| 消息 | `messages-a.html`，通知在上、消息在下 | 通知、会话、会话详情、私信发送 | 首页消息行不展示互关、额度或禁发状态；不做好友申请审批 | P0/P1 |
| 我的 | `profile-a.html`，资料、统计、资产 Tab、设置入口 | 当前用户、资料更新、收藏/点赞/图纸列表 | 不把未接入字段写成真实统计；不伪造登录态 | P1 |

## 登录态主链路

1. 未登录用户可浏览公开 Feed、作品详情、话题、贴纸、商品列表和商品详情。
2. 点赞、收藏、发布、评论、上传、AI、购物车、订单、支付、消息、我的资产需要登录。
3. 登录请求只提交 `email` 和 `password`；注册请求提交 `email`、`password`、`confirmPassword`、`nickname`、`ageGroup`，注册成功后直接登录。
4. Android 使用 Java `SharedPreferences` 持久化 token，启动时读取会话。
5. 401 或 refresh 失败必须清理本地会话，并展示统一登录引导。

禁止：

- 把 401 包装成普通空态。
- 登录失败后继续展示“已完成操作”。
- 把登录请求写成仍需提交 `ageGroup`。
- 使用本地 mock 用户补齐“我的”页面。

## 社区与作品详情

### 已有能力

- `GET /posts/feed`
- `GET /posts/following`
- `POST /posts`
- `GET /posts/{postId}`
- `POST/DELETE /posts/{postId}/like`
- `POST/DELETE /posts/{postId}/favorite`
- `GET/POST /posts/{postId}/comments`
- `GET /topics`
- `GET /sticker-packs`
- `GET /users/search`
- 上传链路 `/uploads/presign -> PUT -> /uploads/confirm`

### Java/XML 页面

- `CommunityFragment`：社区首页。
- `SearchActivity`：搜索页。
- `PostCreateActivity`：发帖页。
- `PostDetailActivity`：作品详情和评论栏。

### 设计目标

- 社区首页双列瀑布流，卡片图片高度控制在 `120dp-260dp`。
- 作品详情顺序固定为 `图片 -> 内容 -> 评论区域 -> 悬浮评论栏`。
- 图片区必须有 carousel/gallery 语义：页码、滑动提示、缩略图 strip。
- 评论区展示真实列表；空列表显示空态，不只显示输入框。
- 评论浮动栏展开态为图片/附件预览在上，评论输入在中，工具栏在底部。

### 评论规则

- 评论可含文字、图片、@ 用户、# 话题、贴纸。
- 文本、图片、贴纸至少存在一种。
- @/# 不能单独发送。
- 图片最多 9 张。
- 图片必须上传成功并确认得到 `fileId`。
- 上传失败保留缩略图并提供重试，禁止假成功。

## 发帖与上传

### Java/XML 页面

- `PostCreateActivity` 对照 `post-compose-a.html`。
- 图片选择、CameraX 拍照、正文、话题、预览、上传中、失败重试、审核中提示都必须有明确状态。

### 边界

- 发布成功后进入后端返回状态；不能假装直接公开。
- 不新增后端 API。
- 不把全局话题搜索、图片审核或草稿云同步写成已完成。

## Search

### 当前事实

没有全局搜索后端 API。当前可用相关接口：

- 用户搜索：`GET /users/search`
- 话题搜索：`GET /topics?keyword=...`
- 商品列表：`GET /products`

### Java/XML 页面

`SearchActivity` 对照 `search-a.html`：

- 返回按钮。
- 搜索输入框。
- 清空/取消。
- 范围 Tab：全部、作品、图纸、商品、用户、话题。
- 默认推荐、输入中、结果、空结果、失败、未登录受限。

### 禁止误导

- 不写全站搜索已完成。
- 不新增 Search API 字段契约。
- 如实现本地筛选，必须在 UI 和文档中写清范围。

## 消息、通知与私信

### 已有能力

- `GET /messages/notifications`
- `POST /messages/notifications/read`
- `GET /messages/conversations`
- `GET /messages/conversations/{conversationId}`
- `POST /messages/conversations/{conversationId}`

### Java/XML 页面

- `MessagesFragment`：消息首页。
- `ConversationActivity`：会话详情。
- `NotificationDetailActivity`：通知详情 UI。

### 目标状态

- 消息首页固定为通知 section 在上、消息 section 在下。
- 点击消息行进入会话详情。
- 点击通知行进入通知详情。
- 首页消息行只展示头像、昵称和最后一条消息摘要，不展示互关、剩余额度或禁发状态。
- 会话详情展示互关正常聊天、未互关剩余 3 条、超限禁用、发送失败、加载失败、空会话。

### 互关规则

- 第一版好友等同关注/互相关注语义。
- 未互关同一发送者对同一会话最多 3 条。
- 超过后后端返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`。
- Android 必须使用 `canSend` 和 `remainingNonMutualMessages` 控制输入态。

## 我的与资料

### Java/XML 页面

- `ProfileFragment`：我的页。
- `ProfileEditActivity`：编辑资料。
- `SettingsActivity`：Settings 首页和分区页。

### 当前 API

- `GET /users/me`
- `PATCH /users/me`
- `GET /users/{userId}`
- 签到、积分、徽章接口。

### 目标 UI

- 统计项为 `获赞 / 作品 / 关注 / 粉丝`。
- 点击 `获赞` 展示来源说明弹窗；本轮说明真实口径为“你发布作品收到的赞”，不展示逐条来源。
- 点击 `作品` 进入 `GET /users/me/posts` 驱动的当前用户作品列表。
- 点击 `关注` 进入 `GET /users/me/following` 驱动的关注用户列表。
- 点击 `粉丝` 进入 `GET /users/me/followers` 驱动的粉丝用户列表。
- 资产 Tab 为 `我的图纸 / 点赞作品 / 收藏作品`。
- 编辑资料入口进入 `profile-edit-a.html`。
- 我的页首屏不显示历史阶段的“我的订单 / 评论作品 / 关注作品”作为主资产组。

### 边界

- 城市/地区、定位和地图只做 UI-only 展示，不接地图 API。
- Profile Edit 只保存当前后端已有资料字段。
- 未登录时显示登录引导，不用 mock 用户。

## 商城、订单与支付

### 已有能力

- 商品列表、详情。
- 自营商品购物车。
- 创建订单、订单列表、详情、取消。
- 创建支付单、查询支付单。
- 支付回调和退款骨架在后端存在。

### Java/XML 页面

- `CommerceFragment`：商城首页。
- `ProductDetailActivity`：商品详情。
- 购物车、订单确认、支付结果使用二级 Activity 或同一 Activity 分区承载。

### 关键边界

- 玩家二手/定制商品不走标准购物车和标准订单。
- 地址管理未闭环；订单确认不能伪造默认地址。
- 支付是 Stub / 联调骨架。
- `payParams.provider=STUB` 不得展示为真实支付 SDK 参数。
- 支付最终状态以服务端为准。

## AI 图纸

### 已有能力

- 创建任务。
- 查询任务。
- 任务列表。
- 取消任务。
- 图纸详情。
- 图纸收藏。
- 后端配额接口。

### Java/XML 页面

- `AiFragment`：AI 首页。
- `AiFlowActivity`：图片选择、参数、进度、结果、历史。
- `CameraActivity`：CameraX 拍照。

### 边界

- 真实视觉 Provider 未接入。
- 大模型生图 API 未接入。
- AI 首页只能表达开发态、图纸生成状态、排队、失败、取消、历史和结果。
- `future-capability-ui-a.html` 只作为地图、真实支付、大模型生图 UI-only 参考。

## Settings 与合规

Settings 首页包含：

- 账号与安全。
- 隐私与权限。
- 通知设置。
- 内容与互动。
- 帮助与关于。
- 退出登录。
- 账号注销。

边界：

- 隐私政策、用户协议、备案、SDK 清单、版权投诉不能写成已完成。
- 账号注销后端申请接口存在，但完整流程、冷静期、人工处理和合规闭环未完成。
- Settings 分区页只做开发态/待补说明，不代表生产合规配置完成。

## 未来能力 UI-only

只允许作为 UI 参考：

- 地图 API：位置授权、位置选择、无权限、定位失败、手动选择城市/区域。
- 真实支付 API：支付方式选择、支付处理中、支付失败、服务端状态查询、支付结果不确定。
- 大模型生图 API：提示词、参考图、生成中、排队中、失败重试、内容安全拦截、生成结果选择。

必须显著标注：`UI-only / 当前不接后端 / 当前不可用于真实支付或真实 AI 生成`。
