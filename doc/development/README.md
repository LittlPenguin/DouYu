# 豆屿 Doyu 开发文档索引

本目录是当前公开开发文档入口，覆盖工程架构、接口契约、Android Java/XML 维护规则、后端 seed/demo 清理边界、阶段路线、测试验收、UI 蓝图和流程图。

当前工程规则：

  Android 客户端使用 Java + Activity/Fragment + XML。
  Open Design HTML 是 UI 结构、视觉层级、文案、状态和交互验收权威。
  Android 运行态不得使用 mock/demo 数据，后端运行期不得恢复 seed/demo 填充。
  当前范围不包含 AI 页面、地图/定位 Provider、支付能力、真实 SMS Provider、生产限流/风控、真实 AI/大模型 Provider 或完整合规材料。OSS-backed 图片存储、上传预签名/确认、Alibaba OSS 配置和后端 upload/oss provider 保持不变。
  地址管理保留；订单确认可继续表达收货地址字段、地址缺口和地址管理需求。

## 事实优先级

1. 当前代码、构建配置、测试输出和实际截图。
2. 仓库根目录 `AGENTS.md` 本轮任务规则。
3. `current-status.md` 当前状态。
4. 领域分册：API 看 `05-api-contract.md`，Android 看 `03-android-client.md`，后端看 `04-backend-services.md`，UI 看 `11-ui-style-guide.md` 和 `13-ui-screen-blueprints.md`。
5. `16-stage-development-roadmap.md` 阶段路线。

如果文档与代码冲突，先记录当前代码事实，再修正文档或在后续阶段改代码追契约。不得把设计目标写成当前实现事实。

## 必读顺序

1. `current-status.md`：当前工程事实、已完成项和未完成项。
2. `00-project-handoff.md`：接手手册、目录地图、联调入口和风险边界。
3. `01-tech-stack.md`：Java/XML Android 目标栈和禁用技术。
4. `02-architecture.md`：系统分层、模块边界和 Provider 状态。
5. `03-android-client.md`：Android Java/XML 架构规则、页面结构和残留检查。
6. `04-backend-services.md`：后端 seed/demo 清理和开发 Stub 边界。
7. `05-api-contract.md`：Auth / User / Upload / Community / Commerce / Order / Message / Reward / Admin 接口契约。
8. `08-commerce-orders-address.md`：商城、订单、收货地址和玩家商品边界。
9. `10-testing-acceptance.md`：Android/后端残留检查、构建、测试和 UI 验收。
10. `11-ui-style-guide.md`：品牌色、组件、状态、禁用态和 Open Design 规范。
11. `13-ui-screen-blueprints.md`：页面蓝图、状态蓝图、点击去向和 UI only 标识。
12. `16-stage-development-roadmap.md`：Java/XML 分阶段开发路线。
13. `18-unfinished-and-blockers.md`：未完成、阻塞和不能写成通过的项目。

## 按职责阅读

| 职责 | 文档 |
|---|---|
| Android UI / 页面 / 状态 | `03-android-client.md`、`11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| 后端服务 / Controller / Provider | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md` |
| 阶段开发管理 | `16-stage-development-roadmap.md`、`17-ui-parity-refactor-plan.md`、`10-testing-acceptance.md` |
| 主链路理解 | `12-feature-and-flow-map.md`、`diagrams/README.md` |
| 商城 / 订单 / 地址管理 | `08-commerce-orders-address.md` |
| 本地联调 / 排障 | `14-frontend-backend-collaboration.md` |

## 分册清单

| 文档 | 用途 |
|---|---|
| `00-project-handoff.md` | 接手手册、目录地图、当前阶段、联调入口、验证命令和风险边界 |
| `01-tech-stack.md` | Java/XML Android 技术栈、后端栈和禁用选项 |
| `02-architecture.md` | Android、Spring Boot、数据库、对象存储、Provider 和文档体系架构 |
| `03-android-client.md` | Android Java/XML 架构规则、页面组织、数据边界 |
| `04-backend-services.md` | 后端 Controller、服务模块、Provider、seed/demo 清理 |
| `05-api-contract.md` | 接口契约 |
| `06-data-model.md` | 核心表、枚举、状态机和 Java POJO 边界 |
| `08-commerce-orders-address.md` | 商品、购物车、订单、收货地址和玩家商品边界 |
| `10-testing-acceptance.md` | 残留检查、设计验收、接口回归、Android/后端测试和真机 QA |
| `11-ui-style-guide.md` | 品牌色、组件、主页顶部栏、底部导航、状态和禁用态规范 |
| `12-feature-and-flow-map.md` | 主 Tab、登录态、上传、社区、商城、消息、我的资产等功能地图 |
| `13-ui-screen-blueprints.md` | 页面蓝图、状态蓝图、点击去向、UI only 标识和验收标准 |
| `14-frontend-backend-collaboration.md` | 本地环境、接口字段冻结、真机联调和排障 |
| `16-stage-development-roadmap.md` | 历史 Java/XML 阶段路线和当前维护参考 |
| `17-ui-parity-refactor-plan.md` | 历史 Open Design UI 还原计划 |
| `18-unfinished-and-blockers.md` | 未完成、阻塞、不能写成通过和后续 P0/P1 项集中清单 |
| `27-remove-ai-map-payment-plan.md` | 本轮移除 AI、地图 API、支付和生产能力描述的计划 |
| `current-status.md` | 当前事实、已完成项和下一步未关闭项 |
| `diagrams/README.md` | 架构图、API 图、UI 信息架构图和页面线框图索引 |

## Open Design 入口

Open Design 本地页面在 `doc/development/open-design/`。当前保留页面只表达当前产品范围内的 UI 结构，不再包含 AI、地图、支付或完整合规材料页面。

| 文件 | 用途 |
|---|---|
| `community-home-a.html` | 社区瀑布流首页 |
| `commerce-home-a.html` | 商城首页 |
| `messages-a.html` | 消息首页 |
| `profile-a.html` | 我的页 |
| `search-a.html` | 搜索页 |
| `post-compose-a.html` | 发帖页 |
| `post-detail-comment-toolbar-a.html` | 作品详情和评论栏 |
| `message-conversation-a.html` | 私信会话详情 |
| `notification-detail-a.html` | 通知详情 |
| `profile-edit-a.html` | 编辑资料 |
| `profile-posts-a.html` | 我的作品 |
| `profile-following-a.html` | 我的关注 |
| `profile-followers-a.html` | 我的粉丝 |
| `settings-home-a.html` | 设置首页 |
| `settings-account-security-a.html` | 账号与安全 |
| `settings-privacy-permissions-a.html` | 隐私与权限设置 |
| `settings-notifications-a.html` | 通知设置 |
| `login-a.html` | 登录 |
| `register-a.html` | 注册 |
| `index.html` | Open Design 入口索引页 |

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

如果触碰 `DouYu/` 或 `doyu-server/` 源码，必须补跑对应 Android 构建/单测或后端测试，并在验收记录中说明结果。
