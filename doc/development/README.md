# 豆屿 Doyu 开发文档索引

本目录是豆屿 Doyu 当前公开开发文档入口，覆盖工程架构、接口契约、Android 落地规则、后端服务边界、阶段开发流程、测试验收、UI 蓝图和流程图。当前代码与本目录文档共同构成公开事实源；本地 Agent 规则文件、个人技能目录和临时任务清单不随仓库发布，不作为公开必读入口。

当前阶段是 **第六轮 UI/品牌与主链路展示收敛阶段内的 Stage 8 Android 主链路收口**。Stage 8 不新增真实 AI、真实支付、地图、全局搜索后端、完整地址或生产合规能力，只把现有 Android 主链路、设计一致性、接口回显和文档口径收敛到可验收状态；剩余未完成和阻塞项集中记录在 `18-unfinished-and-blockers.md`。

## 事实优先级

1. 当前代码和可运行配置。
2. `current-status.md`。
3. 对应领域分册：API 看 `05-api-contract.md`，Android 看 `03-android-client.md`，后端看 `04-backend-services.md`，UI 看 `11-ui-style-guide.md` 与 `13-ui-screen-blueprints.md`。
4. `16-stage-development-roadmap.md`。
5. `../豆屿App商业技术执行计划.md`。

如果文档与代码冲突，先记录当前代码事实，再修正文档或在后续阶段改代码追契约。不得把设计目标写成当前实现事实。

## 必读顺序

1. `current-status.md`：当前阶段、已完成项和下一步真实未关闭项。
2. `00-project-handoff.md`：项目接手手册、目录地图、本地联调和不可宣称完成项。
3. `16-stage-development-roadmap.md`：阶段开发路线。
4. `17-ui-parity-refactor-plan.md`：新增 UI 视觉对齐重构计划，明确严格追设计稿和底部导航例外规则。
5. `18-unfinished-and-blockers.md`：未完成、阻塞、不能写成通过和后续 P0/P1 项的集中清单。
6. `02-architecture.md`：系统分层、模块边界、Provider 状态和主流程。
7. `05-api-contract.md`：以后端 Controller 和 Android Retrofit 为源重写的接口契约。
8. `12-feature-and-flow-map.md`：五个主 Tab 与跨模块链路地图。
9. `13-ui-screen-blueprints.md`：Open Design A 方向落地到页面结构、状态和验收规则。
10. `10-testing-acceptance.md`：文档、接口、设计、Android、后端和真机验收规则。

按职责继续阅读：

| 职责 | 文档 |
|---|---|
| Android UI / 路由 / 状态 | `03-android-client.md`、`11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| 后端服务 / Controller / Provider | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md` |
| 阶段开发管理 | `16-stage-development-roadmap.md`、`17-ui-parity-refactor-plan.md`、`10-testing-acceptance.md` |
| 主链路理解 | `12-feature-and-flow-map.md`、`diagrams/README.md` |
| AI 拼豆图纸 | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md` |
| 商城 / 订单 / 支付 | `08-commerce-payment.md` |
| 权限 / 隐私 / 审核 / 合规 | `09-security-compliance.md` |
| 本地联调 / ADB / 排障 | `14-frontend-backend-collaboration.md` |

## 分册清单

| 文档 | 用途 |
|---|---|
| `00-project-handoff.md` | 接手手册、目录地图、当前阶段、联调入口、验证命令和风险边界 |
| `01-tech-stack.md` | 固定技术栈、框架选择和禁用选项 |
| `02-architecture.md` | Android、Spring Boot、数据库、对象存储、Provider 和管理后台的总体架构 |
| `03-android-client.md` | 当前 Android 代码事实、现有路由、下一阶段 UI 重构规则 |
| `04-backend-services.md` | 后端 Controller、服务模块、安全配置、Provider 和未完成生产能力 |
| `05-api-contract.md` | Auth / User / Upload / Community / Pattern / Commerce / Order / Payment / Message / Reward / Admin 接口契约 |
| `06-data-model.md` | 核心表、枚举、状态机和关系 |
| `07-ai-pattern-generation.md` | AI 图纸任务、算法、失败处理和 Provider 边界 |
| `08-commerce-payment.md` | 商品、购物车、订单、联调支付、退款和玩家交易边界 |
| `09-security-compliance.md` | 隐私、权限、未成年人、审核、举报、备案和版权边界 |
| `10-testing-acceptance.md` | 文档验证、设计验收、接口回归、Android/后端测试和真机 QA 规则 |
| `11-ui-style-guide.md` | 品牌色、组件、主顶部栏、底部导航混合规则、状态和禁用态规范 |
| `12-feature-and-flow-map.md` | 五个主 Tab、登录态、上传、社区、AI、商城、消息、我的资产等功能地图 |
| `13-ui-screen-blueprints.md` | 页面蓝图、状态蓝图、点击去向、UI-only 标识和验收标准 |
| `14-frontend-backend-collaboration.md` | 本地环境、接口字段冻结、真机联调和排障 |
| `15-ai-pattern-provider-selection.md` | AI Provider 选型、降级策略和后续接入要求 |
| `16-stage-development-roadmap.md` | Stage 0-7 阶段开发路线和退出标准 |
| `17-ui-parity-refactor-plan.md` | Stage 8 UI 视觉对齐重构计划；底部导航样式以当前 Android 真机 UI 为准，icon 与文字以设计图为准 |
| `18-unfinished-and-blockers.md` | 未完成、阻塞、不能写成通过和后续 P0/P1 项的集中清单 |
| `current-status.md` | 当前事实、已完成项和下一步真实未关闭项 |
| `diagrams/README.md` | 阶段流程图、架构图、API 图、UI 信息架构图和页面线框图索引 |

## 设计资产入口

当前设计入口分两类：

- `doc/development/open-design/index.html`：Open Design A 方向页面稿的本地副本，作为视觉和交互参考。
- `doc/development/diagrams/README.md`：开发文档内可引用的 SVG 架构图、流程图、信息架构图和页面线框图。

Open Design 项目名为 `SpellBean`，项目 ID 为 `1a79a45f-6c02-4c3f-9433-4b27b325acf5`。仓库内 HTML 原型只表达 UI 目标，不代表 Android 路由或后端接口已经完成。

当前 Open Design 页面稿包括：

| 文件 | 用途 |
|---|---|
| `community-home-a.html` | 社区双列瀑布流首页 |
| `post-detail-comment-toolbar-a.html` | 作品详情，顺序为 `图片 -> 内容 -> 评论区域`，包含 carousel、评论区、@/# 选择和悬浮评论栏 |
| `post-compose-a.html` | 上传帖子视觉参考；Android 已有 `post_create`，生产审核和更完整多媒体能力仍未闭环 |
| `search-a.html` | Search 搜索页 UI-only 原型 |
| `commerce-home-a.html` | 商城首页 |
| `ai-home-a.html` | AI 创作首页 |
| `messages-a.html` | 消息首页，私信与通知分区 |
| `message-conversation-a.html` | 私信对话详情，互关与 3 条限制状态 |
| `notification-detail-a.html` | 通知详情视觉参考；Android 使用列表内通知数据展示，无独立详情 API |
| `profile-a.html` | 我的页，统计项为 `获赞 / 作品 / 关注 / 粉丝` |
| `profile-edit-a.html` | 编辑资料视觉参考；Android 已接现有资料保存字段，城市/地区仍 UI-only |
| `settings-home-a.html` | Settings 首页 |
| `settings-account-security-a.html` | 账号与安全 |
| `settings-privacy-permissions-a.html` | 隐私与权限 |
| `settings-notifications-a.html` | 通知设置 |
| `settings-about-compliance-a.html` | 关于与合规 |
| `future-capability-ui-a.html` | 未来地图、真实支付、大模型生图 UI-only 占位 |

## 当前默认边界

- 登录请求当前仍传 `ageGroup=AGE_18_PLUS`。
- Search、Profile Edit、Settings 分区页和通知列表内详情已注册 Android 路由；Search 仍不是全局搜索后端，Settings 不代表生产合规完成，通知详情不代表独立详情接口已存在。未来地图、真实支付和大模型生图仍是 UI-only 目标。
- 地图 API、真实微信/支付宝支付 API、大模型生图 API、全局搜索后端 API 均未接入。
- Aliyun OSS Provider 是后端可切换骨架，不代表生产对象存储已完成。
- AI 真实视觉 Provider 未接入；现阶段只允许表达开发态图纸生成。
- 支付只允许表达联调支付单和服务端状态查询，不得展示为真实支付完成。
- 地址管理、生产合规、内容审核生产化、玩家交易闭环仍未完成。

## 文档同步规则

| 改动范围 | 同步文档 |
|---|---|
| API 接口、字段、错误码 | `05-api-contract.md`、必要时同步 `06-data-model.md` |
| Android 路由、页面、状态 | `03-android-client.md`、`13-ui-screen-blueprints.md`、`10-testing-acceptance.md` |
| 后端模块、鉴权、Provider | `04-backend-services.md`、`14-frontend-backend-collaboration.md` |
| UI 风格、设计系统、Open Design 页面稿 | `11-ui-style-guide.md`、`13-ui-screen-blueprints.md`、`diagrams/README.md` |
| 阶段目标、验收标准、未完成项 | `current-status.md`、`16-stage-development-roadmap.md`、`18-unfinished-and-blockers.md`、`10-testing-acceptance.md` |
| AI 图纸 | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md` |
| 商城、订单、支付、退款、库存 | `08-commerce-payment.md` |
| 权限、隐私、审核、未成年人 | `09-security-compliance.md` |

## 最小验证

只改文档和 SVG 时至少运行：

```powershell
git diff --check
git status --short
git diff --name-only -- DouYu doyu-server
Get-ChildItem doc\development\diagrams -Filter *.svg | ForEach-Object {
  [xml](Get-Content -Raw -Encoding UTF8 $_.FullName) | Out-Null
}
```

如果触碰 `DouYu/` 或 `doyu-server/`，必须补跑对应 Android 构建/单测或后端测试，并在验收记录中说明原因。
