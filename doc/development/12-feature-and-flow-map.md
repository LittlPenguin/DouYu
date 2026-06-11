# 12. 功能与主链路地图

## 目标

本文档描述当前 Java/XML Android 客户端保留的主 Tab、跨模块流程和真实 API 边界。所有页面实现必须使用 Java Activity/Fragment + XML，并以保留的 Open Design HTML 为 UI 权威。

## 主导航

底部导航顺序固定为 `社区 / 商城 / 上传 / 消息 / 我的`。五个入口都是 `MainActivity` 主壳内的 Fragment Tab；`上传` 对应 `PostCreateFragment`，不再打开独立上传 Activity。

| Tab | 页面目标 | 数据来源 | 边界 |
|---|---|---|---|
| 社区 | `community-home-a.html`，双列内容流、话题、作品卡、搜索入口 | feed、帖子详情、评论、话题、贴纸、上传链路 | 不显示假帖子，不把上传失败写成功 |
| 商城 | `commerce-home-a.html`，分类、商品卡、购物车入口 | 商品、SKU、购物车、创建订单 | 不做支付，不做订单中心，不伪造商品/订单 |
| 上传 | `post-compose-a.html`，发帖表单、图片、话题、预览 | topics、OSS 上传、创建帖子 | 不提交假 fileId，不做本地成功 |
| 消息 | `messages-a.html`，通知列表 | notifications | 不做私信、会话、通知对象跳转 |
| 我的 | `profile-a.html`，资料、统计、资产 Tab、设置入口 | 当前用户、资料更新、点赞/收藏/作品列表 | 未登录显示登录边界，不用 mock 用户 |

## 登录态主链路

- 未登录用户可浏览公开 Feed、作品详情、评论列表、话题、商品列表、商品详情和搜索。
- 点赞、收藏、发布、评论、上传、购物车、创建订单、通知和我的资产需要登录。
- 登录请求提交 `email` 和 `password`；注册请求提交 `email`、`password`、`confirmPassword`、`nickname`、`ageGroup`。
- 登录/注册成功返回 access token；Android 使用 Java `SharedPreferences` 持久化 token。
- 当前版本不提供 refresh token API。401 必须清理本地会话并展示登录引导。

## 社区与上传

保留能力：

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

- `CommunityFragment`
- `SearchActivity`
- `PostCreateFragment`
- `PostDetailActivity`

## Search

`SearchActivity` 对照 `search-a.html`，搜索页真实能力范围：

- `GET /search?keyword=&type=all|posts|products|users|topics`
- 全部：展示作品、商品、用户、话题四类混合结果，不做个性化热榜或推荐。
- 作品结果进入 `PostDetailActivity`。
- 商品结果进入 `ProductDetailActivity`。
- 用户结果使用真实用户资料。
- 话题结果在搜索页内切换到作品筛选并以话题名作为关键词查询相关作品。
- 空关键词只展示输入引导，不填充本地假热榜。

## 通知

保留能力：

- `GET /notifications`
- `POST /notifications/read`

Java/XML 页面：

- `MessagesFragment`：通知列表。
- `NotificationDetailActivity`：通知详情。

目标状态：

- 消息 Tab 只展示通知 section。
- 新注册用户默认有 3 条系统通知。
- 点击通知行进入通知详情。
- 通知详情只展示标题、内容、类型和时间。
- 不做私信、会话、消息发送、互关限制或通知对象跳转。

## 我的与资料

Java/XML 页面：

- `ProfileFragment`
- `ProfileEditActivity`
- `SettingsActivity`
- `ProfilePostsActivity`
- `ProfileUsersActivity`

当前 API：

- `GET /users/me`
- `PATCH /users/me`
- `GET /users/{userId}`
- `GET /users/me/posts`
- `GET /users/me/following`
- `GET /users/me/followers`
- `GET /users/me/liked-posts`
- `GET /users/me/favorite-posts`
- `GET/PATCH /users/me/settings`

边界：

- 城市/地区是手动资料字段，不接地图 API 或定位 Provider。
- Profile Edit 不展示或保存年龄段、兴趣标签。
- 设置页保留账号信息、隐私/通知偏好、权限状态、帮助关于和本地退出登录。
- 不提供账号注销、完整合规材料、生产推送服务或私信偏好承诺。

## 商城、购物车与创建订单

保留能力：

- 商品分类、商品列表和商品详情。
- 自营商品购物车。
- 创建订单。
- 商品详情和购物车内手动填写收货人、手机号、地区和详细地址。

Java/XML 页面：

- `CommerceFragment`
- `ProductDetailActivity`
- `CartActivity`

边界：

- 当前版本不做地址簿、默认地址、地址列表、编辑地址或订单地址选择。
- 当前版本不做订单中心、订单详情、订单取消入口或售后。
- 当前版本不做支付、退款、对账、回调、微信/支付宝 SDK/API。
- 创建订单后只展示后端返回的订单 ID、金额和 `CREATED` 状态。

## Removed From Current Scope

以下能力已从当前产品范围移除：

- Refresh token API。
- 账号注销。
- 私信、会话、消息发送、互关消息限制。
- 奖励、签到、徽章。
- 举报、内容审核后台、管理后台、Admin 登录和 Admin bootstrap。
- 地址簿、订单中心、订单详情、订单取消。
- AI 页面、AI 任务、图纸生成、真实视觉 Provider、大模型 Provider。
- 支付、微信/支付宝 SDK/API、退款、对账、生产回调。
- 地图 API、定位 Provider、地图选点。
- 真实 SMS Provider、生产限流、生产风控。
- 完整合规文档页面。

OSS-backed image storage、上传预签名/确认、Alibaba OSS 配置和后端 upload/oss provider 保留。
