# 豆屿 Doyu 设计图索引

> 更新日期：2026-06-06
> 用途：集中保存当前可用的阶段流程图、系统/API 流程图、UI 信息架构图和页面线框图。SVG 只表达结构、状态和边界，不替代 `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` 或 `05-api-contract.md`。

## 使用规则

- 技术流程图表达模块、接口、状态和边界。
- UI 线框图表达页面结构，不是高保真截图，也不代表 Android 当前已经实现。
- Open Design HTML 位于 `../open-design/`，是本轮 Java/XML UI 还原权威。
- 如果图与代码或接口契约冲突，以当前代码、`current-status.md` 和对应分册为准，再更新图稿。
- 旧 Kotlin/Compose 截图或流程不能作为 Java/XML 验收证据。

## 阶段开发流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `stage-development-flow.svg` | Java/XML 迁移阶段路线：规则文档、构建迁移、核心层、UI Shell、流程页、静态数据清理、集成验收 | `16-stage-development-roadmap.md` |
| `ui-refactor-flow.svg` | 按 Open Design 落地 Android XML UI：读取设计源、抽取状态、实现页面、截图验收 | `16-stage-development-roadmap.md`、`03-android-client.md` |

## 系统 / API / 流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `system-architecture.svg` | Android Java/XML、Spring Boot、PostgreSQL、Redis、OSS、AI Stub、Payment Stub、Admin API 关系 | `02-architecture.md`、`04-backend-services.md` |
| `android-navigation-map.svg` | Java Activity/Fragment 页面入口、主 Tab、二级 Activity 和 UI-only 边界 | `03-android-client.md`、`13-ui-screen-blueprints.md` |
| `api-module-map.svg` | Auth、User、Upload、Community、Pattern、Product、Cart、Order、Payment、Message、Reward、Report、Admin 模块关系 | `05-api-contract.md` |
| `auth-session-flow.svg` | 短信登录、`ageGroup=AGE_18_PLUS`、SharedPreferences session、401 refresh、logout/clear 流程 | `03-android-client.md`、`05-api-contract.md` |
| `upload-oss-flow.svg` | `/uploads/presign -> PUT uploadUrl -> /uploads/confirm`，以及 `local|stub|aliyun` Provider 边界 | `04-backend-services.md`、`14-frontend-backend-collaboration.md` |
| `community-comment-flow.svg` | 评论文字/图片/@/#/贴纸、上传校验、最多 9 图、审核中和列表刷新 | `05-api-contract.md`、`13-ui-screen-blueprints.md` |
| `commerce-order-payment-flow.svg` | 商品、购物车、订单确认、地址缺口、联调支付单、服务端状态查询 | `08-commerce-payment.md` |
| `message-mutual-follow-flow.svg` | 关注/互关语义、未互关 3 条私信限制、超限后错误和 Android 禁用提示 | `05-api-contract.md`、`12-feature-and-flow-map.md` |

## UI 信息架构图 / 页面线框图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `ui-information-architecture.svg` | 五个主 Tab 信息架构：社区、商城、AI、消息、我的，含 UI-only 目标 | `13-ui-screen-blueprints.md` |
| `community-home-wireframe.svg` | 社区首页：统一顶部栏、频道、双列瀑布流、卡片状态、底部导航 | `13-ui-screen-blueprints.md` |
| `post-detail-comment-toolbar-wireframe.svg` | 作品详情：图片 -> 内容 -> 评论区 -> 悬浮评论栏，含 carousel、@/#、上传失败和 9 图上限 | `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| `commerce-home-wireframe.svg` | 商城首页：分类、Banner、商品卡、自营/玩家商品边界 | `13-ui-screen-blueprints.md`、`08-commerce-payment.md` |
| `ai-home-wireframe.svg` | AI 首页：创作入口、任务卡、历史、真实 Provider 未接入边界 | `13-ui-screen-blueprints.md`、`07-ai-pattern-generation.md` |
| `message-profile-wireframes.svg` | 消息页和我的页：私信/通知、互关限制、新统计、三资产 Tab、Profile Edit 字段边界 | `13-ui-screen-blueprints.md` |

## Open Design 页面稿

| 文件 | 用途 |
|---|---|
| `../open-design/index.html` | Open Design 页面稿总入口 |
| `../open-design/community-home-a.html` | 社区瀑布流首页 |
| `../open-design/commerce-home-a.html` | 商城首页 |
| `../open-design/ai-home-a.html` | AI 创作首页 |
| `../open-design/messages-a.html` | 消息首页 |
| `../open-design/profile-a.html` | 我的页 |
| `../open-design/search-a.html` | 搜索页 |
| `../open-design/post-compose-a.html` | 发帖页 |
| `../open-design/post-detail-comment-toolbar-a.html` | 作品详情和评论栏 |
| `../open-design/message-conversation-a.html` | 私信会话详情 |
| `../open-design/notification-detail-a.html` | 通知详情 |
| `../open-design/profile-edit-a.html` | 编辑资料 |
| `../open-design/settings-home-a.html` | 设置首页 |
| `../open-design/settings-account-security-a.html` | 账号与安全 |
| `../open-design/settings-privacy-permissions-a.html` | 隐私与权限 |
| `../open-design/settings-notifications-a.html` | 通知设置 |
| `../open-design/settings-about-compliance-a.html` | 关于与合规 |
| `../open-design/future-capability-ui-a.html` | 地图、真实支付、大模型生图 UI-only 占位 |
| `../open-design/doyu-design-directions.html` | 设计方向说明 |
