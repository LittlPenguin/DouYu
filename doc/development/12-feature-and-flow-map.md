# 12. 功能与主链路地图

## 目标

本文档描述当前 Java/XML Android 客户端的保留主 Tab、跨模块流程、真实 API 边界、UI-only 边界和后续优先级。所有页面实现必须使用 Java Activity/Fragment + XML，并以保留的 Open Design HTML 为 UI 权威。

## 主导航

| Tab | Java/XML 首页目标 | 数据来源 | 不得误导 | 优先级 |
|---|---|---|---|---|
| 社区 | `community-home-a.html`，双列瀑布流、频道、作品卡、搜索/发布入口 | `GET /posts/feed`、帖子详情、评论、话题、贴纸、上传链路 | 新帖发布后直接公开；不把上传失败写成功；不显示假帖子 | P0 |
| 商城 | `commerce-home-a.html`，分类、商品卡、购物车入口 | 商品、购物车、订单、地址接口 | 不伪造商品、地址或订单；不展示支付成功 | P1 |
| 消息 | `messages-a.html`，通知在上、消息在下 | 通知、会话、会话详情、私信发送 | 首页消息行不展示互关、额度或禁发状态；不做好友申请审批 | P0/P1 |
| 我的 | `profile-a.html`，资料、统计、资产 Tab、设置入口 | 当前用户、资料更新、收藏/点赞/作品列表 | 不把未接入字段写成真实统计；不伪造登录态 | P1 |

底部导航视觉顺序固定为 `社区 / 商城 / 上传 / 消息 / 我的`。其中社区、商城、消息、我的是四个内容 Tab；`上传` 是 action item，点击后通过登录门禁进入全屏 `PostCreateActivity`，不替换当前 Fragment。AI 不再是当前主 Tab。

所有底部入口使用同一套 section 导航规则：`MainActivity` 负责
`IntentExtras.SECTION` 到内容 Tab 的映射，并在 `onCreate` 与
`onNewIntent` 中都能切换到目标 section。上传页返回社区、商城、消息或我的时
复用现有 `MainActivity`，不重新定义一套不一致的跳转语义。

## 登录态主链路

1. 未登录用户可浏览公开 Feed、作品详情、话题、贴纸、商品列表和商品详情。
2. 点赞、收藏、发布、评论、上传、购物车、订单、地址、消息和我的资产需要登录。
3. 登录请求提交 `email` 和 `password`；注册请求提交 `email`、`password`、`confirmPassword`、`nickname`、`ageGroup`，注册成功后直接登录。
4. Android 使用 Java `SharedPreferences` 持久化 token，启动时读取会话。
5. 401 或 refresh 失败必须清理本地会话，并展示统一登录引导。

禁止：

- 把 401 包装成普通空态。
- 登录失败后继续展示“已完成操作”。
- 把登录请求写成仍需提交 `ageGroup`。
- 使用本地 mock 用户补齐“我的”页面。

## 社区与作品详情

已有能力：

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

Java/XML 页面：

- `CommunityFragment`：社区首页。
- `SearchActivity`：搜索页。
- `PostCreateActivity`：发帖页。
- `PostDetailActivity`：作品详情和评论栏。

设计目标：

- 社区首页双列瀑布流，卡片图片高度控制在 `120dp-260dp`。
- 作品详情顺序固定为 `图片 -> 内容 -> 评论区域 -> 悬浮评论栏`。
- 图片区必须有 carousel/gallery 语义：页码、滑动提示、缩略图 strip。
- 评论区展示真实列表；空列表显示空态。
- 评论浮动栏展开态为图片/附件预览在上，评论输入在中，工具栏在底部。

## 发帖与上传

- `PostCreateActivity` 对照 `post-compose-a.html`，入口来自底部导航“上传”。
- 图片选择、CameraX 拍照、标题、正文、话题、预览、上传中、失败重试、发布成功提示都必须有明确状态。
- 图片必须先通过 OSS upload presign -> PUT -> confirm 得到真实 `fileId`，再提交发帖请求。
- 发布使用现有 `POST /api/v1/posts`，请求字段为 `title/content/mediaFileIds/topicIds`。
- 发布成功后展示后端返回状态，当前为 `VISIBLE`；新帖应立即进入公开 Feed。
- 不新增后端 API。
- 不把全局话题搜索或云同步写成已经完成。

## Search

`SearchActivity` 对照 `search-a.html`，但不再保留 UI-only 文案。搜索页真实能力范围：

- `GET /search?keyword=&type=all|posts|products|users|topics` 聚合保留业务的搜索结果。
- 全部：展示作品、商品、用户、话题四类混合结果，不做个性化热榜或推荐。
- 作品：搜索公开可见帖子标题、正文和话题名称，结果进入 `PostDetailActivity`。
- 商品：搜索上架且审核通过商品的标题、名称、描述、分类和类型，结果进入 `ProductDetailActivity`。
- 用户：复用 `GET /users/search` 的真实用户资料结果。
- 话题：复用 `GET /topics?keyword=...` 的真实话题结果；点击话题在搜索页内切到作品筛选并以话题名作为关键词查询相关作品。
- 空关键词只展示输入引导，不填充本地假热榜。
- 加载中、空结果、加载失败、清空、取消和范围筛选都必须有明确状态。

## 消息、通知与私信

已有能力：

- `GET /messages/notifications`
- `POST /messages/notifications/read`
- `GET /messages/conversations`
- `GET /messages/conversations/{conversationId}`
- `POST /messages/conversations/{conversationId}`

Java/XML 页面：

- `MessagesFragment`：消息首页。
- `ConversationActivity`：会话详情。
- `NotificationDetailActivity`：通知详情 UI。

目标状态：

- 消息首页固定为通知 section 在上、消息 section 在下。
- 点击消息行进入会话详情。
- 点击通知行进入通知详情。
- 首页消息行只展示头像、昵称和最后一条消息摘要。
- 会话详情展示互关正常聊天、未互关剩余 3 条、超限禁用、发送失败、加载失败、空会话。

## 我的与资料

Java/XML 页面：

- `ProfileFragment`：我的页。
- `ProfileEditActivity`：编辑资料。
- `SettingsActivity`：Settings 首页和分区页。

当前 API：

- `GET /users/me`
- `PATCH /users/me`
- `GET /users/{userId}`
- 签到、积分、徽章接口。

目标 UI：

- 统计项为 `获赞 / 作品 / 关注 / 粉丝`。
- 点击 `获赞` 展示来源说明弹窗，不展示逐条来源。
- 点击 `作品` 进入 `GET /users/me/posts` 驱动的当前用户作品列表。
- 点击 `关注` 进入 `GET /users/me/following` 驱动的关注用户列表。
- 点击 `粉丝` 进入 `GET /users/me/followers` 驱动的粉丝用户列表。
- 编辑资料入口进入 `profile-edit-a.html`，保存头像、昵称、简介和手动城市/地区。

边界：

- 城市/地区是手动资料字段：可以从常用地区选择或自行填写，保存到后端，不接地图 API 或定位 Provider。
- Profile Edit 不展示或保存年龄段、兴趣标签。
- 未登录时显示登录引导，不用 mock 用户。

## 商城、订单与地址

已有能力：

- 商品列表和详情。
- 自营商品购物车。
- 订单确认、订单列表、订单详情、取消。
- 地址管理需求和地址字段保留。

Java/XML 页面：

- `CommerceFragment`：商城首页。
- `ProductDetailActivity`：商品详情。
- 购物车、订单确认和地址管理使用二级 Activity 或同一 Activity 分区承载。

关键边界：

- 玩家二手/定制商品不走标准购物车和标准订单。
- 地址管理保留；订单确认不能伪造默认地址。
- 地图选点、定位自动填充和地图 Provider 不属于当前范围。
- 支付、退款、对账、回调、微信/支付宝 SDK/API 不属于当前范围。

## Settings

Settings 首页包含：

- 账号与安全。
- 隐私与权限。
- 通知设置。
- 帮助与关于。
- 退出登录。
- 账号注销。

真实功能：

- 账号与安全读取 `GET /users/me`，展示邮箱、账号状态、本机登录状态；退出登录调用 `/auth/logout`，账号注销申请调用 `/auth/account/cancel`。
- 隐私与权限读取系统相机、通知和相册能力状态；相机和 Android 13+ 通知支持直接发起运行时授权请求；地区资料只跳转手动资料编辑。
- 隐私偏好 `allowRecommendation`、`allowStrangerMessages`、`allowFavorites` 通过 `GET/PATCH /users/me/settings` 持久化。
- 通知偏好 `notifyMessages`、`notifyInteractions`、`notifyPublish`、`notifySystem` 通过 `GET/PATCH /users/me/settings` 持久化。
- 帮助与关于展示真实版本、构建号、API 地址和登录状态，并提供复制诊断信息或打开系统应用信息等可执行动作。

边界：

- 不提供完整合规材料页面。
- 不把隐私政策、用户协议、备案、SDK 清单、版权投诉写成已完成。
- 账号注销后端申请接口存在时仍需把完整流程、冷静期、人工处理和合规闭环标为未完成。
- 通知偏好不删除历史通知、不解除私信限制、不承诺生产推送服务已完成。

## Removed From Current Scope

以下能力已从当前文档/设计范围移除：

- AI 页面、AI 任务、图纸生成、真实视觉 Provider、大模型 Provider。
- 支付、微信/支付宝 SDK/API、退款、对账、生产回调。
- 地图 API、定位 Provider、地图选点。
- 真实 SMS Provider、生产限流、生产风控。

- 完整合规文档页面。

OSS-backed image storage、上传预签名/确认、Alibaba OSS 配置和后端 upload/oss provider 保留。
