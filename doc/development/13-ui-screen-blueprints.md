# 13. UI 页面蓝图

## 使用方式

本文件把 Open Design A 方向页面稿转成 Android 后续可实现的页面结构、状态、点击去向和验收标准。它是设计落地文档，不代表当前 Android 已经完成这些页面。

配套入口：

- UI 规范：`11-ui-style-guide.md`。
- 阶段路线：`16-stage-development-roadmap.md`。
- 图谱：`diagrams/README.md`。
- Open Design 本地副本：`open-design/index.html`。

## 全局页面框架

适用于五个主 Tab：

- 顶部栏：左侧新增 icon，中间页面标题，右侧搜索 icon。
- 新增菜单：Settings、AI 创作、上传帖子。
- 搜索 icon：目标进入 `search-a.html`；当前 Android 没有全局 Search 路由时必须禁用或标开发态。
- 底部导航：社区、商城、AI、消息、我的。容器样式、圆角、阴影、选中背景、短横、显隐和导航行为使用当前 Android 真机 UI；icon 语义和文字以 Open Design / SVG 为准。
- 不使用 emoji 作为正式 UI icon。

状态要求：

- 加载中。
- 空状态。
- 失败重试。
- 未登录。
- 无权限。
- 禁用态。
- 开发态 / UI-only。

## 社区首页

参考：

- `open-design/community-home-a.html`
- `diagrams/community-home-wireframe.svg`

首屏结构：

1. 统一顶部栏。
2. 频道/推荐 Tab。
3. 双列 masonry 内容流。
4. 内容卡：图片、标题、作者、互动数据、审核中/图片 fallback。
5. 混合规则底部导航：当前 Android 容器样式 + 设计图 icon/文字。

交互规则：

- 点击卡片进入作品详情。
- 点击新增菜单的上传帖子进入发帖目标。
- 切换频道必须保留加载、空、错状态。
- 图片高度建议 `120dp-260dp`；超出裁切。

验收：

- 不出现纯文字底部导航；底部导航容器样式按当前 Android 真机 UI 保持，icon 语义和文字追设计图。
- 图片失败有占位，不出现空白卡。
- Feed 空态和错误态可恢复。

## 作品详情

参考：

- `open-design/post-detail-comment-toolbar-a.html`
- `diagrams/post-detail-comment-toolbar-wireframe.svg`

页面顺序必须是：

```text
图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏
```

图片区：

- 首屏重点展示作品图片。
- 明确 carousel / gallery：页码如 `2/5`、左右切换或滑动提示、缩略图 strip。
- 单图也要保留图片容器稳定尺寸。
- 支持加载中、失败占位、无图 fallback。

内容区：

- 作者信息。
- 关注按钮。
- 标题。
- 正文。
- 话题 Chip。
- 点赞、收藏、评论计数。

评论区：

- 标题和评论数量。
- 评论列表。
- 文字评论。
- 含图片评论。
- 含 @ 用户和 # 话题评论。
- 含贴纸评论。
- 空状态。
- 加载失败。
- 列表只展示公开 `VISIBLE` 评论；提交后等待审核的评论不插入公开列表。
- 当前页只有审核中评论时显示“暂无公开评论”，说明通过审核后才会公开展示。

悬浮评论栏：

- 默认折叠态为底部轻量胶囊输入栏。
- 点击输入或展开按钮后展开。
- 展开态结构：图片/附件预览在上，评论输入在下，工具栏在底部。
- 工具按钮：图片、@、#、贴纸。禁止出现“帖子”入口。
- 发送按钮在无文字、无图片、无贴纸时禁用。

多图状态：

- 无上传图。
- 一张图。
- 多图堆叠。
- 上传中。
- 上传失败可重试。
- 点击多图堆叠进入 carousel 预览。
- 达到 9 图上限后禁用继续添加。

@ 用户选择：

- 搜索输入。
- 用户结果。
- 已选择用户 Chip。
- 空结果。
- 加载失败。
- 如未接真实搜索，显示开发态或禁用，不能空点击。

# 话题选择：

- 话题搜索。
- 热门 / 推荐话题。
- 已选择话题 Chip。
- 空结果。
- 开发态边界。

验收：

- 视觉顺序不是旧的 `内容 -> 图片 -> 评论区域`。
- 评论区是真实区域，不只是输入栏。
- 纯 @/# 不能发送。
- 上传失败不假成功。
- 最多 9 图规则可见。
- 提交成功只提示等待审核，不把审核中评论写成公开列表项。

## 发帖

参考：

- `open-design/post-compose-a.html`

首屏结构：

1. 返回。
2. 标题：上传帖子。
3. 图片选择 / 拍摄入口。
4. 图片预览网格。
5. 正文输入。
6. 话题选择。
7. 预览入口。
8. 发布按钮。

状态：

- 未登录。
- 无图片/无正文提示。
- 上传中。
- 上传失败可重试。
- 发布中。
- 发布失败。
- 发布成功后审核中。

边界：

- 不新增草稿云同步。
- 不新增全局话题 API。
- 不把审核中写成已公开。

## Search

参考：

- `open-design/search-a.html`

页面结构：

1. 返回按钮。
2. 搜索输入框。
3. 清空 / 取消。
4. 搜索范围 Tab：全部、作品、图纸、商品、用户、话题。
5. 默认推荐。
6. 输入中建议。
7. 结果列表。

结果卡片：

- 作品卡。
- 图纸卡。
- 商品卡。
- 用户卡。
- 话题卡。

状态：

- 默认推荐。
- 输入中。
- 加载中。
- 空结果。
- 加载失败。
- 未登录受限。
- UI-only 标识。

边界：

- 当前没有全局搜索后端 API。
- 可接用户搜索、话题搜索、商品筛选，但必须写清范围。
- 不得把 Search 写成 Android 已完成路由。

## 商城首页

参考：

- `open-design/commerce-home-a.html`
- `diagrams/commerce-home-wireframe.svg`

首屏结构：

1. 统一顶部栏。
2. 分类 / 推荐 Chip。
3. 运营位或开发态 Banner。
4. 商品双列卡。
5. 混合规则底部导航：当前 Android 容器样式 + 设计图 icon/文字。

商品卡：

- 商品图。
- 标题。
- 价格。
- 库存 / 售罄 / 下架。
- 商品类型标识：自营、玩家二手、玩家定制。

边界：

- 自营商品可加购。
- 玩家二手 / 定制商品不走标准购物车。
- 商品搜索如果只是本地筛选，不能写成全站搜索。

## 订单与支付

订单确认：

- 展示购物车商品、数量、金额。
- 地址管理未完成时，展示地址缺口和禁用提交。
- 不伪造默认地址。
- `Idempotency-Key` 防重复提交。
- 2026-06-05 真机 smoke 已验证购物车进入订单确认后的地址缺口 UI；这不代表创建订单、联调支付单或支付状态页已经通过。

支付状态：

- 展示联调支付单。
- 展示 `paymentId`、`orderId`、`channel`、`amountCent`、`status`。
- 展示服务端状态查询。
- 支付结果不确定时提示继续查询或返回订单。

禁止：

- 不展示真实微信/支付宝完成态。
- 不把 `payParams.provider=STUB` 写成 SDK 参数。

## AI 首页

参考：

- `open-design/ai-home-a.html`
- `diagrams/ai-home-wireframe.svg`

首屏结构：

1. 统一顶部栏。
2. AI 创作入口。
3. 上传入口。
4. 当前任务卡。
5. 历史入口。
6. 开发态提示。

状态：

- 未登录。
- 上传中。
- 参数选择。
- 排队中。
- 处理中。
- 成功。
- 失败重试。
- 已取消。
- 结果页材料区当前只展示 `材料购买待接入`，不开放自动加购、PDF 导出或带图纸发帖。

边界：

- 不承诺真实视觉理解。
- 大模型生图是未来 UI-only，不接当前 API。
- Stage 8 规则测试覆盖 Provider 边界、状态标签和材料购买待接入文案；2026-06-05 无线真机已覆盖 `相册 -> 上传 -> 参数 -> 创建任务 -> 进度 -> 结果` 开发态链路。2026-06-06 新增的拍照后确认、旋转、上传、取消、重拍流程已有 JVM 规则覆盖；最新无线真机 `.qa-output/stage8/final-smoke-camera-route-fix-after-shot.xml` 已覆盖拍照后进入确认页并展示 `左转 / 右转 / 取消 / 重拍 / 上传并继续`。真实手持纵横向拍照的物理方向 / EXIF 对照仍需后续单独截图，不能把这部分写成已验收。

## 消息页

参考：

- `open-design/messages-a.html`
- `open-design/message-conversation-a.html`
- `open-design/notification-detail-a.html`
- `diagrams/message-profile-wireframes.svg`

消息首页结构：

1. 统一顶部栏。
2. 私信 / 通知分区或 Tab。
3. 私信列表。
4. 通知列表。
5. 混合规则底部导航：当前 Android 容器样式 + 设计图 icon/文字。

私信列表：

- 对方头像和昵称。
- 最后一条消息。
- 未读数。
- 互关状态。
- 未互关剩余条数。

通知列表：

- 类型。
- 标题。
- 内容摘要。
- 未读状态。

会话详情：

- 顶部返回和对方信息。
- 消息列表。
- 互关正常聊天。
- 未互关剩余 3 条提示。
- 超过 3 条后输入禁用。
- 发送失败。
- 消息加载失败。
- 空会话。

边界：

- 通知区域不显示会话输入状态。
- 不设计好友申请审批。
- 不做图片私信和撤回。

## 我的页

参考：

- `open-design/profile-a.html`
- `open-design/profile-edit-a.html`
- `diagrams/message-profile-wireframes.svg`

我的页结构：

1. 统一顶部栏。
2. 头像、昵称、简介。
3. 编辑资料按钮。
4. 统计项：`获赞 / 作品 / 关注 / 粉丝`。
5. 资产 Tab：`我的图纸 / 点赞作品 / 收藏作品`。
6. 内容卡片流。
7. 混合规则底部导航：当前 Android 容器样式 + 设计图 icon/文字。

设计目标不展示：

- 我的订单。
- 评论作品。
- 关注作品。

边界：

- 当前 Android 历史路由仍存在，不得写成已删除。
- 编辑资料 Android 路由已注册，当前通过现有 `PATCH /api/v1/users/me` 保存昵称、头像文件和简介；城市/地区仍是 UI-only 资料字段，不接地图定位。

## 编辑资料

参考：

- `open-design/profile-edit-a.html`

页面结构：

- 返回。
- 头像编辑。
- 昵称。
- 个人简介。
- 性别 / 生日或年龄段展示。
- 城市 / 地区 UI-only。
- 兴趣标签。
- 保存按钮。

状态：

- 默认编辑。
- 头像上传中。
- 头像上传失败可重试。
- 昵称为空禁用保存。
- 保存中。
- 保存失败。
- 保存成功返回我的页。

边界：

- 不新增实名流程。
- 不接地图定位。
- 城市/地区只是 UI-only 资料字段。
- Android 当前只接昵称、头像文件和简介等现有后端字段；兴趣标签、城市/地区和更完整资料结构仍是后续任务。

## Settings

参考：

- `open-design/settings-home-a.html`
- `open-design/settings-account-security-a.html`
- `open-design/settings-privacy-permissions-a.html`
- `open-design/settings-notifications-a.html`
- `open-design/settings-about-compliance-a.html`

Settings 首页：

- 账号与安全。
- 隐私与权限。
- 通知设置。
- 内容与互动。
- 帮助与关于。
- 退出登录。
- 账号注销。

边界：

- 隐私政策、用户协议、备案、SDK 清单、版权投诉只标待补或开发态。
- 账号注销申请接口存在，但完整生产流程未完成。
- Settings 首页和子页路由已注册；子页内容仍是开发态/合规待补展示，不代表生产合规材料已完成。

## 未来能力 UI-only

参考：

- `open-design/future-capability-ui-a.html`

包含：

- 地图 API：授权、定位失败、手动选择城市/区域。
- 真实支付 API：方式选择、处理中、失败、服务端状态查询、不确定结果。
- 大模型生图 API：提示词、参考图、排队、生成中、失败重试、内容安全拦截、结果选择。

强制标识：

- `UI-only`
- `当前不接后端`
- `当前不可用于真实支付或真实 AI 生成`

## 跨页面点击去向

| 起点 | 点击 | 目标 | 当前状态 |
|---|---|---|---|
| 主 Tab 顶部搜索 | 搜索 icon | `search-a.html` / `search` | Android 路由已注册；无全局搜索后端 |
| 主 Tab 顶部新增 | Settings | `settings-home-a.html` / `settings` | 已有首页和子页路由；合规内容待补 |
| 主 Tab 顶部新增 | AI 创作 | `ai` | 已有主 Tab |
| 主 Tab 顶部新增 | 上传帖子 | `post_create` / `post-compose-a.html` | 当前有路由，待按新稿重构 |
| 社区卡片 | 作品卡 | `post/{postId}` | 已有 |
| 作品详情 | @ | @ 用户选择面板 | 接口有，UI 待完善 |
| 作品详情 | # | # 话题选择面板 | 接口有，UI 待完善 |
| 消息私信 | 会话 | `conversation/{conversationId}` | 已有 |
| 消息通知 | 通知 | `notification-detail-a.html` / `notification_detail/{notificationId}` | Android 路由已注册；使用列表内通知信息，无独立详情 API |
| 我的 | 编辑资料 | `profile-edit-a.html` / `profile_edit` | Android 路由已注册；接现有 `PATCH /api/v1/users/me` 字段 |
| 我的 | 我的图纸 | `my_patterns` | 已有 |
| 我的 | 点赞作品 | `liked_posts` | 已有 |
| 我的 | 收藏作品 | `favorite_posts` | 已有 |
