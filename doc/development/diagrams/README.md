# 豆屿 Doyu 设计图索引

> 更新日期：2026-06-09
> 用途：集中保存当前可用的阶段流程图、系统/API 流程图、UI 信息架构图和页面线框图。SVG 只表达结构、状态和边界，不替代 `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` 或 `05-api-contract.md`。

## 使用规则

- 技术流程图表达模块、接口、状态和边界。
- UI 线框图表达页面结构，不是高保真截图，也不代表 Android 当前已经实现。
- Open Design HTML 位于 `../open-design/`，是当前 Java/XML UI 还原权威。
- 如果图与代码或接口契约冲突，以当前代码、`current-status.md` 和对应分册为准，再更新图稿。
- 当前图稿不再保留已移除能力或未来能力占位页面。

## 阶段开发流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `stage-development-flow.svg` | 历史 Java/XML 阶段路线：规则文档、构建设置、核心层、UI Shell、流程页、静态数据清理、集成验收 | `16-stage-development-roadmap.md` |
| `ui-refactor-flow.svg` | 按 Open Design 落地 Android XML UI：读取设计源、抽取状态、实现页面、截图验收 | `16-stage-development-roadmap.md`、`03-android-client.md` |

## 系统 / API / 流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `system-architecture.svg` | Android Java/XML、Spring Boot、PostgreSQL、Redis、上传、本地开发 Provider、Admin API 关系 | `02-architecture.md`、`04-backend-services.md` |
| `android-navigation-map.svg` | Java Activity/Fragment 页面入口、主 Tab、二级 Activity 和 UI-only 边界 | `03-android-client.md`、`13-ui-screen-blueprints.md` |
| `api-module-map.svg` | Auth、User、Upload、Community、Product、Cart、Order、Address、Message、Reward、Report、Admin 模块关系 | `05-api-contract.md` |
| `auth-session-flow.svg` | 邮箱密码注册/登录、SharedPreferences session、401 refresh、logout/clear 流程 | `03-android-client.md`、`05-api-contract.md` |
| `upload-oss-flow.svg` | `/uploads/presign -> PUT uploadUrl -> /uploads/confirm` 的本地开发上传流程 | `04-backend-services.md`、`14-frontend-backend-collaboration.md` |
| `community-comment-flow.svg` | 评论文字/图片/@/#/贴纸、上传校验、最多 9 图、审核中和列表刷新 | `05-api-contract.md`、`13-ui-screen-blueprints.md` |
| `message-mutual-follow-flow.svg` | 关注/互关语义、未互关 3 条私信限制、超限后错误和 Android 禁用提示 | `05-api-contract.md`、`12-feature-and-flow-map.md` |

## UI 信息架构图 / 页面线框图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `ui-information-architecture.svg` | 当前保留主信息架构 | `12-feature-and-flow-map.md`、`13-ui-screen-blueprints.md` |
| `community-home-wireframe.svg` | 社区首页：双列瀑布流、顶部操作、底部导航 | `13-ui-screen-blueprints.md` |
| `community-comment-flow.svg` | 社区评论和上传校验 | `13-ui-screen-blueprints.md` |
| `commerce-home-wireframe.svg` | 商城首页：分类、Banner、商品卡、自营/玩家商品边界 | `13-ui-screen-blueprints.md`、`08-commerce-orders-address.md` |
| `message-profile-wireframes.svg` | 消息和我的页线框 | `13-ui-screen-blueprints.md` |
| `post-detail-comment-toolbar-wireframe.svg` | 作品详情和评论输入栏 | `13-ui-screen-blueprints.md` |

## Open Design 页面

| 页面 | 用途 |
|---|---|
| `../open-design/community-home-a.html` | 社区首页 |
| `../open-design/commerce-home-a.html` | 商城首页 |
| `../open-design/messages-a.html` | 消息首页 |
| `../open-design/profile-a.html` | 我的页 |
| `../open-design/search-a.html` | 搜索页 |
| `../open-design/post-compose-a.html` | 发帖页 |
| `../open-design/post-detail-comment-toolbar-a.html` | 作品详情 |
| `../open-design/message-conversation-a.html` | 私信会话 |
| `../open-design/notification-detail-a.html` | 通知详情 |
| `../open-design/settings-home-a.html` | 设置首页 |
| `../open-design/settings-account-security-a.html` | 账号与安全 |
| `../open-design/settings-privacy-permissions-a.html` | 隐私与权限设置 |
| `../open-design/settings-notifications-a.html` | 通知设置 |
