# 豆屿 Doyu 开发文档索引

本目录保存豆屿 Doyu 的开发分册文档。文档目标是让产品、设计、Android、后端、AI、测试、运营和后续 AI Agent 能在统一边界下开工。

## 阅读顺序

1. `../豆屿App商业技术执行计划.md`：产品目标、商业闭环、阶段计划。
2. `01-tech-stack.md`：固定技术栈和选型边界。
3. `02-architecture.md`：总体架构和主链路。
4. `05-api-contract.md`：接口约定和错误规范。
5. `06-data-model.md`：核心数据对象、状态和关系。
6. 按职责阅读：
   - Android：`03-android-client.md`
   - 后端：`04-backend-services.md`
   - AI：`07-ai-pattern-generation.md`
   - 商城支付：`08-commerce-payment.md`
   - 安全合规：`09-security-compliance.md`
   - 测试验收：`10-testing-acceptance.md`

## 文档边界

- 本目录只定义开发方案、接口草案、数据模型、测试验收和工程规则。
- 本目录不包含 Android 工程、后端工程、数据库迁移脚本或业务代码。
- 文档中的接口和数据模型是第一版开发契约，后续实现时应保持兼容。
- 如果执行过程中必须调整接口或模型，先更新对应文档，再改实现。

## 默认产品决策

- App 名称：豆屿 Doyu。
- 目标平台：Android，中国大陆应用市场。
- 目标用户：16+，年轻女性为主要设计对象。
- 商城模式：自营精选 + 玩家二手/定制直连。
- 玩家卖家：18+ 实名。
- AI 方案：国内模型 API + 自研拼豆转图算法。
- 支付方案：微信支付 + 支付宝 App 支付。
- 文件存储：后端签发上传凭证，客户端直传对象存储。

## 分册清单

| 文档 | 用途 |
|---|---|
| `01-tech-stack.md` | 技术栈、框架、基础设施和禁用选择 |
| `02-architecture.md` | 系统分层、链路、数据流、部署边界 |
| `03-android-client.md` | Android 架构、页面、权限、相机、上传、支付 |
| `04-backend-services.md` | Spring Boot 服务模块、鉴权、队列、后台 |
| `05-api-contract.md` | REST 规范、接口草案、错误码、分页、幂等 |
| `06-data-model.md` | 核心表、枚举、状态机、关系 |
| `07-ai-pattern-generation.md` | AI Provider、图纸算法、色卡、成本和失败处理 |
| `08-commerce-payment.md` | 商品、订单、支付、退款、库存和玩家交易 |
| `09-security-compliance.md` | 隐私、权限、未成年人、审核、备案、版权 |
| `10-testing-acceptance.md` | 测试范围、验收标准、上线检查 |

## 协作规则

- 产品需求变化先影响 PRD，再同步接口、数据模型和验收标准。
- 设计稿应覆盖加载、空状态、失败、审核中、无权限、弱网和未登录状态。
- Android 与后端联调前必须冻结当前接口版本。
- 支付和 AI 任务必须支持服务端追踪，不依赖客户端单点状态。
- 内容审核、举报、账号注销和隐私说明是上线前置条件。

