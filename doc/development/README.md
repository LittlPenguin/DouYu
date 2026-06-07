# 豆屿 Doyu 开发文档索引

本目录是豆屿 Doyu 当前公开开发文档入口，覆盖工程架构、接口契约、Android Java/XML 落地规则、后端 seed/demo 清理、阶段路线、测试验收、UI 蓝图和流程图。

当前阶段目标：

- Android 从 Kotlin + Jetpack Compose 全量迁移为传统 Android Java + Activity/Fragment + XML。
- Open Design HTML 是 UI 1:1 还原权威。
- 删除 Android 运行态 mock/demo 数据和后端运行期 seed/demo 填充。
- 保留 Stub Provider 作为开发联调能力。

## 事实优先级

1. 当前代码、构建配置、测试输出和实际截图。
2. `AGENTS.md` 本轮任务规则。
3. `current-status.md` 当前状态。
4. 领域分册：API 看 `05-api-contract.md`，Android 看 `03-android-client.md`，后端看 `04-backend-services.md`，UI 看 `11-ui-style-guide.md` 和 `13-ui-screen-blueprints.md`。
5. `16-stage-development-roadmap.md` 阶段路线。

如果文档与代码冲突，先记录当前代码事实，再修正文档或在后续阶段改代码追契约。不得把设计目标写成当前实现事实。

## 必读顺序

1. `current-status.md`：当前迁移状态、已完成项和未完成项。
2. `00-project-handoff.md`：接手手册、目录地图、联调入口和风险边界。
3. `01-tech-stack.md`：Java/XML Android 目标栈和禁用技术。
4. `02-architecture.md`：系统分层、模块边界和 Provider 状态。
5. `03-android-client.md`：Android Java/XML 迁移规则、页面结构和残留检查。
6. `04-backend-services.md`：后端 seed/demo 清理和 Stub Provider 边界。
7. `05-api-contract.md`：Auth / User / Upload / Community / Pattern / Commerce / Order / Payment / Message / Reward / Admin 接口契约。
8. `10-testing-acceptance.md`：Android/后端残留检查、构建、测试和 UI 验收。
9. `11-ui-style-guide.md`：品牌色、组件、状态、禁用态和 Open Design 规范。
10. `13-ui-screen-blueprints.md`：页面蓝图、状态蓝图、点击去向和 UI-only 标识。
11. `16-stage-development-roadmap.md`：Java/XML 分阶段开发路线。
12. `18-unfinished-and-blockers.md`：未完成、阻塞和不能写成通过的项目。

## 按职责阅读

| 职责 | 文档 |
|---|---|
| Android UI / 页面 / 状态 | `03-android-client.md`、`11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| 后端服务 / Controller / Provider | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md` |
| 阶段开发管理 | `16-stage-development-roadmap.md`、`17-ui-parity-refactor-plan.md`、`10-testing-acceptance.md` |
| 主链路理解 | `12-feature-and-flow-map.md`、`diagrams/README.md` |
| AI 拼豆图纸 | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md` |
| 商城 / 订单 / 支付 | `08-commerce-payment.md` |
| 权限 / 隐私 / 审核 / 合规 | `09-security-compliance.md` |
| 本地联调 / 排障 | `14-frontend-backend-collaboration.md` |

## 分册清单

| 文档 | 用途 |
|---|---|
| `00-project-handoff.md` | 接手手册、目录地图、当前阶段、联调入口、验证命令和风险边界 |
| `01-tech-stack.md` | Java/XML Android 技术栈、后端栈和禁用选项 |
| `02-architecture.md` | Android、Spring Boot、数据库、对象存储、Provider 和文档体系架构 |
| `03-android-client.md` | Android Java/XML 迁移规则、页面组织、数据边界 |
| `04-backend-services.md` | 后端 Controller、服务模块、Provider、seed/demo 清理 |
| `05-api-contract.md` | 接口契约 |
| `06-data-model.md` | 核心表、枚举、状态机和 Java POJO 边界 |
| `07-ai-pattern-generation.md` | AI 图纸任务、算法、失败处理和 Provider 边界 |
| `08-commerce-payment.md` | 商品、购物车、订单、联调支付、退款和玩家交易边界 |
| `09-security-compliance.md` | 隐私、权限、未成年人、审核、举报、备案和版权边界 |
| `10-testing-acceptance.md` | 残留检查、设计验收、接口回归、Android/后端测试和真机 QA |
| `11-ui-style-guide.md` | 品牌色、组件、主顶部栏、底部导航、状态和禁用态规范 |
| `12-feature-and-flow-map.md` | 五个主 Tab、登录态、上传、社区、AI、商城、消息、我的资产等功能地图 |
| `13-ui-screen-blueprints.md` | 页面蓝图、状态蓝图、点击去向、UI-only 标识和验收标准 |
| `14-frontend-backend-collaboration.md` | 本地环境、接口字段冻结、真机联调和排障 |
| `15-ai-pattern-provider-selection.md` | AI Provider 选型、降级策略和后续接入要求 |
| `16-stage-development-roadmap.md` | Java/XML 分阶段开发路线和退出标准 |
| `17-ui-parity-refactor-plan.md` | Open Design UI 还原计划 |
| `18-unfinished-and-blockers.md` | 未完成、阻塞、不能写成通过和后续 P0/P1 项集中清单 |
| `current-status.md` | 当前事实、已完成项和下一步未关闭项 |
| `diagrams/README.md` | 架构图、API 图、UI 信息架构图和页面线框图索引 |

## Open Design 入口

Open Design 本地页面稿在 `doc/development/open-design/`，共 18 个 HTML，是 UI 结构、视觉层级、文案、状态和交互验收权威：

| 文件 | 用途 |
|---|---|
| `community-home-a.html` | 社区瀑布流首页 |
| `commerce-home-a.html` | 商城首页 |
| `ai-home-a.html` | AI 创作首页 |
| `messages-a.html` | 消息首页 |
| `profile-a.html` | 我的页 |
| `search-a.html` | 搜索页 |
| `post-compose-a.html` | 发帖页 |
| `post-detail-comment-toolbar-a.html` | 作品详情和评论栏 |
| `message-conversation-a.html` | 私信会话详情 |
| `notification-detail-a.html` | 通知详情 |
| `profile-edit-a.html` | 编辑资料 |
| `settings-home-a.html` | 设置首页 |
| `settings-account-security-a.html` | 账号与安全 |
| `settings-privacy-permissions-a.html` | 隐私与权限 |
| `settings-notifications-a.html` | 通知设置 |
| `settings-about-compliance-a.html` | 关于与合规 |
| `future-capability-ui-a.html` | 地图、真实支付、大模型生图 UI-only 占位 |
| `doyu-design-directions.html` | 设计方向说明 |

Open Design 只表达目标 UI，不代表 Android 路由、后端接口或真实数据已经完成。

## 文档同步规则

| 改动范围 | 同步文档 |
|---|---|
| API 接口、字段、错误码 | `05-api-contract.md`，必要时同步 `06-data-model.md` |
| Android 页面、XML、状态 | `03-android-client.md`、`13-ui-screen-blueprints.md`、`10-testing-acceptance.md` |
| 后端模块、鉴权、Provider、seed 清理 | `04-backend-services.md`、`14-frontend-backend-collaboration.md` |
| UI 风格、设计系统、Open Design 页面稿 | `11-ui-style-guide.md`、`13-ui-screen-blueprints.md`、`diagrams/README.md` |
| 阶段目标、验收标准、未完成项 | `current-status.md`、`16-stage-development-roadmap.md`、`18-unfinished-and-blockers.md`、`10-testing-acceptance.md` |

## 最小验证

```powershell
git diff --check
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil.compose|paging.compose" DouYu/app
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
```

如果触碰 `DouYu/` 或 `doyu-server/`，必须补跑对应 Android 构建/单测或后端测试，并在验收记录中说明结果。
