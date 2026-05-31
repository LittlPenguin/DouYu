# 豆屿 Doyu 开发文档索引

本目录保存豆屿 Doyu 当前有效的工程规范、接口契约、数据模型、联调手册、测试验收和当前状态。

当前阶段是 **第五轮前置 Aliyun OSS Provider 最小闭环阶段**：第四轮已把 Android 登录态收口到 DataStore，并为社区 Feed、商品列表/详情和购物车补了常驻真实图文 seed 数据；本轮只补后端对象存储 Provider 切换骨架，让上传链路可通过 `.env` 从 Local OSS 切到 Aliyun OSS。第五轮前置不迁移 seed 图片，不接真实 AI Provider，不接真实微信/支付宝支付，不补完整地址管理、玩家交易闭环或上线生产化。

产品目标、商业闭环和阶段方向以 `../豆屿App商业技术执行计划.md` 为总纲；当前工程事实以代码、`../../AGENTS.md` 和 `current-status.md` 为准。`../../CLAUDE.md` 只作为 Claude Code 的转发入口，要求先读 `../../AGENTS.md`。

## 必读顺序

1. `../../AGENTS.md`：仓库级 Agent 工作规则、工程约束和验证要求。
2. `../../CLAUDE.md`：Claude Code 转发入口，要求遵守 `../../AGENTS.md`。
3. `../豆屿App商业技术执行计划.md`：产品目标、商业闭环和阶段方向。
4. `current-status.md`：当前实现状态、未完成项和下一步优先级。
5. `01-tech-stack.md`：固定技术栈和禁用选择。
6. `02-architecture.md`：总体架构、主链路和目标态。
7. `05-api-contract.md`：接口约定、错误码、分页和幂等。
8. `06-data-model.md`：核心数据对象、枚举、状态和关系。

按职责继续阅读：

| 职责 | 文档 |
|---|---|
| Android | `03-android-client.md` |
| 后端 | `04-backend-services.md` |
| AI 拼豆 | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md` |
| 商城支付 | `08-commerce-payment.md` |
| 安全合规 | `09-security-compliance.md` |
| 测试验收 | `10-testing-acceptance.md` |
| UI 设计 | `11-ui-style-guide.md` |
| 前后端联调 | `14-frontend-backend-collaboration.md` |

## 文档边界

- 本目录只保留当前有效规范和当前状态，不再保留已完成的一次性任务书或阶段流水账。
- 接口、数据模型、支付、AI、安全合规等变更必须同步更新对应分册。
- 如果文档与代码冲突，先以代码和 `current-status.md` 判断当前事实，再修正文档。
- 新增计划外能力前，先更新产品总纲或对应分册，明确范围、风险和验收标准。
- 任务需求、调试步骤、注意事项、阶段目标、阶段未完成项和验收结论必须沉淀到对应文档，不能只留在对话里。

## 文档职责映射

| 内容 | 权威文档 |
|---|---|
| 任务需求 / 阶段任务 | `current-status.md`、`10-testing-acceptance.md` |
| API 文档 / 字段契约 | `05-api-contract.md` |
| 阶段未完成内容 / 本轮不做 | `current-status.md` |
| Android 页面行为 | `03-android-client.md` |
| UI 风格和组件规则 | `11-ui-style-guide.md` |
| 商城 / 订单 / 支付边界 | `08-commerce-payment.md` |
| 联调 / ADB / 真机排障 | `14-frontend-backend-collaboration.md` |
| 验证命令 / 真机 QA 记录 | `10-testing-acceptance.md` |

## 分册清单

| 文档 | 用途 |
|---|---|
| `current-status.md` | 当前完成内容、UI MVP 收敛状态、生产化缺口和下一步优先级 |
| `01-tech-stack.md` | 技术栈、框架、基础设施和禁用选择 |
| `02-architecture.md` | 系统分层、主链路、部署边界和目标态 |
| `03-android-client.md` | Android 架构、页面、UI MVP 收敛、权限、相机、上传、支付 |
| `04-backend-services.md` | Spring Boot 服务模块、鉴权、持久化、后台和 Provider 边界 |
| `05-api-contract.md` | REST 规范、接口契约、错误码、分页、幂等 |
| `06-data-model.md` | 核心表、枚举、状态机、关系 |
| `07-ai-pattern-generation.md` | AI 图纸算法、任务状态、色卡、成本和失败处理 |
| `08-commerce-payment.md` | 商品、订单、支付、退款、库存和玩家交易 |
| `09-security-compliance.md` | 隐私、权限、未成年人、审核、备案、版权 |
| `10-testing-acceptance.md` | UI MVP 验收、测试范围、上线检查 |
| `11-ui-style-guide.md` | 当前唯一 UI 风格、设计令牌、Stitch 映射和 Compose 设计规范 |
| `14-frontend-backend-collaboration.md` | 本地联调、字段冻结、环境配置和排障规则 |
| `15-ai-pattern-provider-selection.md` | AI Provider 选型结论、接入要求、降级和禁用方向 |

## 设计探索资产

`doc/stitch_document_app_generator/` 是设计探索归档，包含生成过程中的 HTML、截图和 `DESIGN.md`。该目录全部保留，但不属于日常必读路径，也不是权威 UI 规范。

UI 重构可以参考这些资产：

| 资产 | 用途 |
|---|---|
| `_1/screen.png` | 社区首页和 App 主骨架 |
| `_2/screen.png` | 商城首页 |
| `ai/screen.png` | AI 创作首页 |
| `_4/screen.png` | 消息页 |
| `_3/screen.png` | 我的页 |
| `DESIGN.md` | 设计系统探索稿 |

当前权威 UI 文档只有：

- `doc/development/11-ui-style-guide.md`

## 文档同步规则

| 改动范围 | 同步文档 |
|---|---|
| API 接口 | `05-api-contract.md` |
| 核心表、状态机、枚举 | `06-data-model.md` |
| Android 客户端架构或页面行为 | `03-android-client.md` |
| 后端模块、鉴权、持久化、Provider | `04-backend-services.md` |
| AI 拼豆图纸流程 | `07-ai-pattern-generation.md`、必要时同步 `15-ai-pattern-provider-selection.md` |
| 支付、订单、退款、库存 | `08-commerce-payment.md` |
| 权限、隐私、审核、未成年人 | `09-security-compliance.md` |
| 测试和上线验收 | `10-testing-acceptance.md` |
| UI 风格和设计系统 | `11-ui-style-guide.md` |
| 当前阶段状态 | `current-status.md` |

## 当前默认决策

- App 名称：豆屿 Doyu。
- 目标平台：Android，中国大陆应用市场。
- 目标用户：16+，年轻女性为主要设计对象。
- 商城模式：自营精选 + 玩家二手/定制直连。
- 玩家卖家：18+ 实名。
- AI 方案：后端 Provider 抽象 + 自研拼豆算法；客户端不得直连模型供应商。
- 支付方案：微信支付 + 支付宝 App 支付为目标；当前正式渠道未接入，仍不能用于生产交易。
- 文件存储：后端签发上传凭证，客户端直传；开发环境默认使用 Local OSS Provider，Aliyun OSS Provider 可按本机私有 `.env` 启用；生产仍需补 STS/最小权限、CORS、CDN、防盗链、审核、缩略图和运维监控。

## 当前 UI MVP 默认边界

- 采用 MVP 收敛，不是全入口保留，也不是重做全部信息架构。
- 能真实调用已有后端并返回明确状态的入口，保留并接完整 UI。
- 后端有接口但 Android 未接的入口，标为后续 P0/P1 接入任务。
- 后端/客户端都未闭环的入口，隐藏、禁用或展示明确开发态说明。
- 真实 AI Provider、增强审核、真实微信/支付宝支付、生产合规风控和玩家交易完整闭环不属于当前 UI MVP。
