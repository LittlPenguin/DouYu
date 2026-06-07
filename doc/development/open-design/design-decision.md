# 豆屿 Doyu Open Design 决策稿

> 更新日期：2026-06-04
> 当前方向：A - 内容发现 + 创作工具平衡
> 项目：SpellBean (`1a79a45f-6c02-4c3f-9433-4b27b325acf5`)

## 当前决策

第六轮页面设计继续采用 **A：内容发现 + 创作工具平衡**：

- 社区承担第一屏内容发现，使用类似小红书的双列瀑布流，但图片高度必须有上限。
- AI 是清楚的创作工具，不承诺真实大模型或视觉理解质量。
- 商城保持可信购买语气，玩家二手/定制不混入标准购物车。
- 消息页明确拆分私信和通知，通知不承载会话输入状态。
- 我的页收敛为轻量资产中心，不保留过多入口。

## 本轮补全

- 主页面框架统一为：左侧新增 icon、中间页面标题、右侧搜索 icon。
- 新增菜单固定包含：Setting、AI 创作、上传帖子。
- 底部导航补齐品牌化 Logo / icon，不使用 emoji 或纯文字占位。
- 社区首页改为双列 masonry，图片视觉高度建议控制在 120-260dp。
- 帖子详情评论栏改为 Google 风格底部悬浮栏，支持折叠、展开、多图堆叠和轮播预览。
- 评论工具全部 icon 化：图片、@、#；删除“帖子”入口，`#` 后不保留旧发帖入口或含义不清的额外 icon。
- 新增 Search 搜索页和上传帖子视觉参考，并为主页面搜索、快捷新增菜单、底部导航、消息列表和 Settings 列表补齐可点击去向；Android 当前已有 `search` 和 `post_create` 路由，但 Search 仍无全局搜索后端，发帖不代表生产审核闭环完成。
- 消息页补齐私信对话详情和通知详情，通知页删除会话输入状态。
- 我的页优化统计区；删除“我的订单”“评论作品”“关注作品”展示；“我的图纸”“点赞作品”“收藏作品”进入同一 Tab。
- 我的页统计项按最新要求调整为“获赞 / 作品 / 关注 / 粉丝”，不再沿用“作品 / 获赞 / 收藏 / 关注”的顺序。
- 新增编辑资料页面 `profile-edit-a.html`，覆盖头像、昵称、简介、年龄段、城市/地区 UI-only、兴趣标签、保存失败和昵称为空禁用保存状态。
- 作品详情页顺序调整为“图片 / 内容 / 评论区域”，首屏先展示作品图片集合，再展示作者、标题、正文、话题和互动状态。
- 作品详情补齐实际评论区，包括评论标题、文字评论、含图片评论、含 @/# 的评论行、空态和加载失败状态；评论提交成功后直接刷新展示，不再要求条数展示。
- 轮播预览改为明确的 gallery / carousel：大图、`2/5` 页码、左右箭头、滑动提示、底部缩略图 strip 和当前选中边框同时出现。
- 评论工具补齐 `@ 用户选择` 和 `# 话题选择` 状态；如 Android 未接真实搜索面板，必须开发态或禁用，不允许空点击。
- 新增 Settings 首页及账号安全、隐私权限、通知设置、关于合规内部页。
- 新增未来地图 API、真实支付 API、大模型生图 API 的 UI-only 占位页。

## 页面清单

- `index.html`：Open Design 总览入口。
- `doyu-design-directions.html`：方向总览。
- `community-home-a.html`：社区首页。
- `post-detail-comment-toolbar-a.html`：作品详情评论区、轮播和评论工具条。
- `post-compose-a.html`：上传帖子视觉参考；Android 已有 `post_create`，不新增后端 API 或生产审核承诺。
- `search-a.html`：Search 搜索页 UI-only 原型。
- `commerce-home-a.html`：商城首页。
- `ai-home-a.html`：AI 创作首页。
- `messages-a.html`：消息首页。
- `message-conversation-a.html`：私信对话详情。
- `notification-detail-a.html`：通知详情。
- `profile-a.html`：我的页。
- `profile-edit-a.html`：编辑资料视觉参考；Android 已接现有资料保存字段，城市/地区仍 UI-only。
- `settings-home-a.html`：Settings 首页。
- `settings-account-security-a.html`：账号与安全。
- `settings-privacy-permissions-a.html`：隐私与权限。
- `settings-notifications-a.html`：通知设置。
- `settings-about-compliance-a.html`：关于与合规。
- `future-capability-ui-a.html`：未来能力 UI-only。

## 不可宣称完成

- 不宣称真实 AI Provider 或大模型生图 API 已完成。
- 不宣称真实微信/支付宝支付 API 或 SDK 已完成。
- 不宣称地图 API、定位服务或真实地理服务已完成。
- 不宣称完整地址管理、玩家交易闭环、资金担保、提现或纠纷处理已完成。
- 不宣称隐私政策、用户协议、SDK 清单、备案、版权投诉和未成年人保护已具备生产上线条件。
- 不宣称 Android 当前代码已实现本轮所有设计稿。

## 设计约束

- 页面背景使用暖白 `#FFFBF7`。
- 主色沿用 Petal / Mint / Sky / Coral。
- 功能图标使用统一线性图标或内联 SVG/CSS 图标，不使用 emoji。
- 卡片圆角和阴影克制，避免卡片套卡片。
- 375dp 宽度下文字不溢出、底部导航不挤压、输入区不遮挡系统导航。
- 所有开发态能力必须隐藏、禁用或明确标注，不允许空点击和假成功 Toast。
