# 豆屿 Doyu 设计图索引

> 更新日期：2026-06-04
> 用途：集中保存当前可用的阶段开发流程图、系统/API 流程图、UI 信息架构图和页面线框图。SVG 用于开发接手和设计沟通，不替代 `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` 或 `05-api-contract.md`。

## 使用规则

- 技术/流程图使用深色 SVG，表达模块、接口、状态和边界。
- UI 线框图使用豆屿品牌色口径：暖白 `#FFFBF7`、Petal、Mint、Sky、Coral。
- 图稿只表达结构、状态和边界，不是高保真截图，也不代表 Android 当前已实现。
- 页面级 Open Design 视觉稿入口为 `../open-design/index.html`，Open Design 项目为 `SpellBean`。
- 如果图与代码或契约冲突，以当前代码、`current-status.md` 和对应分册为准，再更新图稿。

## 阶段开发流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `stage-development-flow.svg` | Stage 0-7 阶段开发路线：文档/API/图谱、UI Shell、社区、Search/Profile/Settings、消息、商城、AI、QA | `16-stage-development-roadmap.md` |
| `ui-refactor-flow.svg` | 按设计图落地 Android UI 的工程流程：读取设计源、区分代码事实、抽组件、补状态、验收 | `16-stage-development-roadmap.md`、`03-android-client.md` |

## 系统 / API / 流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `system-architecture.svg` | Android、Spring Boot、PostgreSQL、Redis、OSS、AI Stub、Payment Stub、Admin API 关系 | `02-architecture.md`、`04-backend-services.md` |
| `android-navigation-map.svg` | 当前 Android 路由与仍需标注的 UI-only 边界：主 Tab、Search、上传、作品详情、Profile Edit、Settings 子页、通知详情 | `03-android-client.md`、`13-ui-screen-blueprints.md` |
| `api-module-map.svg` | Auth、User、Upload、Community、Pattern、Product、Cart、Order、Payment、Message、Reward、Report、Admin 模块关系 | `05-api-contract.md` |
| `auth-session-flow.svg` | 短信登录、`ageGroup=AGE_18_PLUS`、DataStore hydrate、401 refresh、logout/clear 流程 | `03-android-client.md`、`05-api-contract.md` |
| `upload-oss-flow.svg` | `/uploads/presign -> PUT uploadUrl -> /uploads/confirm`，以及 `local|stub|aliyun` Provider 边界 | `04-backend-services.md`、`14-frontend-backend-collaboration.md` |
| `community-comment-flow.svg` | 评论文字/图片/@/#/贴纸、上传校验、最多 9 图、审核中、列表渲染 | `05-api-contract.md`、`13-ui-screen-blueprints.md` |
| `commerce-order-payment-flow.svg` | 商品、购物车、订单确认、地址缺口、联调支付单、服务端状态查询 | `08-commerce-payment.md` |
| `message-mutual-follow-flow.svg` | 关注/互关语义、未互关 3 条私信限制、超过后错误与 Android 禁用提示 | `05-api-contract.md`、`12-feature-and-flow-map.md` |

## UI 信息架构图 / 页面线框图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `ui-information-architecture.svg` | 五个主 Tab 最新信息架构：社区、商城、AI、消息、我的，含 UI-only 目标 | `13-ui-screen-blueprints.md` |
| `community-home-wireframe.svg` | 社区首页：统一顶部栏、频道、双列瀑布流、卡片状态、底部 Logo 导航 | `13-ui-screen-blueprints.md` |
| `post-detail-comment-toolbar-wireframe.svg` | 作品详情：`图片 -> 内容 -> 评论区 -> 悬浮评论栏`，carousel、@/#、上传失败和 9 图上限 | `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| `commerce-home-wireframe.svg` | 商城首页：分类、Banner、商品卡、自营/玩家商品边界 | `13-ui-screen-blueprints.md`、`08-commerce-payment.md` |
| `ai-home-wireframe.svg` | AI 首页：创作入口、任务卡、历史、真实 Provider 未接入边界 | `13-ui-screen-blueprints.md`、`07-ai-pattern-generation.md` |
| `message-profile-wireframes.svg` | 消息页和我的页：私信/通知、互关限制、新统计、三资产 Tab、Profile Edit 现有字段边界 | `13-ui-screen-blueprints.md` |

## Open Design 页面稿

| 文件 | 用途 |
|---|---|
| `../open-design/index.html` | Open Design 页面稿总入口，本地副本 |
| `../open-design/community-home-a.html` | 社区瀑布流首页 A 方向视觉稿 |
| `../open-design/post-detail-comment-toolbar-a.html` | 作品详情图片优先结构、评论区、评论浮动栏、多图堆叠、轮播预览和 @/# 选择视觉稿 |
| `../open-design/post-compose-a.html` | 上传帖子视觉参考；Android 已有 `post_create`，生产审核和更完整多媒体能力仍未闭环 |
| `../open-design/search-a.html` | Search 搜索页 UI-only 视觉稿 |
| `../open-design/commerce-home-a.html` | 商城首页 A 方向视觉稿 |
| `../open-design/ai-home-a.html` | AI 创作首页 A 方向视觉稿 |
| `../open-design/messages-a.html` | 消息首页 A 方向视觉稿 |
| `../open-design/message-conversation-a.html` | 私信对话详情 |
| `../open-design/notification-detail-a.html` | 通知详情视觉参考；Android 使用列表内通知数据展示，无独立详情 API |
| `../open-design/profile-a.html` | 我的页新统计与三资产 Tab 视觉稿 |
| `../open-design/profile-edit-a.html` | 编辑资料视觉参考；Android 已接现有资料保存字段，城市/地区仍 UI-only |
| `../open-design/settings-home-a.html` | Settings 首页 |
| `../open-design/settings-account-security-a.html` | 账号与安全 |
| `../open-design/settings-privacy-permissions-a.html` | 隐私与权限 |
| `../open-design/settings-notifications-a.html` | 通知设置 |
| `../open-design/settings-about-compliance-a.html` | 关于与合规待补入口 |
| `../open-design/future-capability-ui-a.html` | 地图 API、真实支付 API、大模型生图 API 的 UI-only 占位 |
