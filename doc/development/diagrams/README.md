# 豆屿 Doyu 设计图索引

> 更新日期：2026-06-04
> 用途：集中保存当前可用的架构图、流程图、信息架构图和 UI 线框图。这里的图用于开发接手和设计沟通，不替代 `11-ui-style-guide.md` 和接口契约。

## 使用规则

- 技术图使用深色背景，表达系统、接口和流程关系。
- UI 线框图使用豆屿品牌色口径：暖白、Petal、Mint、Sky、Coral。
- 图稿只表达结构、状态和边界，不是高保真截图。
- 页面级 Open Design 设计稿是本目录 SVG 线框图的视觉细化配套；当前入口为 Open Design 项目 `SpellBean` / `index.html`，仓库副本位于 `../open-design/index.html`。
- 如果图与代码或契约冲突，以当前代码、`current-status.md` 和对应分册为准，再更新图稿。
- 当前不恢复已删除的 `doc/stitch_document_app_generator/` 资产；本目录是当前可用的文档设计图入口。

## 技术 / 流程图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `system-architecture.svg` | Android、后端、数据、OSS、AI、支付和后台 API 关系 | `00-project-handoff.md`、`02-architecture.md` |
| `android-navigation-map.svg` | 5 个主 Tab 和详情/流程页路由结构 | `03-android-client.md` |
| `api-module-map.svg` | API 模块与主要资源关系 | `05-api-contract.md` |
| `auth-session-flow.svg` | 登录、DataStore hydrate、refresh、logout 流程 | `03-android-client.md`、`05-api-contract.md` |
| `upload-oss-flow.svg` | 预签名上传、直传、确认和 Provider 切换 | `04-backend-services.md`、`14-frontend-backend-collaboration.md` |
| `community-comment-flow.svg` | 评论文字/图片/@/#/贴纸 MVP 流程 | `03-android-client.md`、`05-api-contract.md` |
| `commerce-order-payment-flow.svg` | 商品、购物车、订单、地址缺口和联调支付状态 | `08-commerce-payment.md` |
| `message-mutual-follow-flow.svg` | 关注/互关私信和未互关 3 条限制 | `05-api-contract.md` |

## UI 线框 / 页面蓝图

| 图 | 用途 | 对应文档 |
|---|---|---|
| `ui-information-architecture.svg` | 五个主 Tab 的信息架构 | `13-ui-screen-blueprints.md` |
| `community-home-wireframe.svg` | 社区首页首屏结构 | `13-ui-screen-blueprints.md` |
| `post-detail-comment-toolbar-wireframe.svg` | 帖子详情评论工具条折叠态 / 展开态 | `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| `commerce-home-wireframe.svg` | 商城首页和商品卡层级 | `13-ui-screen-blueprints.md` |
| `ai-home-wireframe.svg` | AI 创作首页、任务卡和历史入口 | `13-ui-screen-blueprints.md` |
| `message-profile-wireframes.svg` | 消息页和我的页首屏结构 | `13-ui-screen-blueprints.md` |

## Open Design 页面稿

| 文件 | 用途 |
|---|---|
| `../open-design/index.html` | Open Design 页面稿总入口，本地副本 |
| `../open-design/doyu-open-design.css` | Open Design 共享视觉样式、图标和组件规则 |
| `../open-design/doyu-design-directions.html` | A 方向选择结果、B/C 备选说明 |
| `../open-design/design-decision.md` | A 方向决策稿、设计系统规则和不可误导边界 |
| `../open-design/community-home-a.html` | 社区瀑布流首页 A 方向视觉稿 |
| `../open-design/post-detail-comment-toolbar-a.html` | 作品详情图片优先结构、评论区、评论浮动栏、多图堆叠、轮播预览和 @/# 选择视觉稿 |
| `../open-design/post-compose-a.html` | 上传帖子 UI-only 视觉稿，覆盖图片、正文、话题、上传失败和审核中 |
| `../open-design/search-a.html` | Search 搜索页 UI-only 视觉稿，覆盖作品、图纸、商品、用户和话题 |
| `../open-design/commerce-home-a.html` | 商城首页 A 方向视觉稿 |
| `../open-design/ai-home-a.html` | AI 创作首页 A 方向视觉稿 |
| `../open-design/messages-a.html` | 消息页 A 方向视觉稿 |
| `../open-design/message-conversation-a.html` | 私信对话详情，互关和未互关 3 条限制 |
| `../open-design/notification-detail-a.html` | 通知详情，通知事件和关联对象结构 |
| `../open-design/profile-a.html` | 我的页获赞/作品/关注/粉丝统计与三资产 Tab A 方向视觉稿 |
| `../open-design/profile-edit-a.html` | 编辑资料 UI-only 视觉稿 |
| `../open-design/settings-home-a.html` | Settings 首页 |
| `../open-design/settings-account-security-a.html` | 账号与安全 |
| `../open-design/settings-privacy-permissions-a.html` | 隐私与权限 |
| `../open-design/settings-notifications-a.html` | 通知设置 |
| `../open-design/settings-about-compliance-a.html` | 关于与合规待补入口 |
| `../open-design/future-capability-ui-a.html` | 地图 API、真实支付 API、大模型生图 API 的 UI-only 占位 |
