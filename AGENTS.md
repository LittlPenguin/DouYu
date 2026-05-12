# AGENTS.md

本仓库用于开发 **豆屿 Doyu**：面向中国大陆 16+ 年轻用户的 Android 拼豆社区、AI 拼豆图纸、商城与玩家交易应用。

## 工作原则

- 开发前先阅读 `doc/豆屿App商业技术执行计划.md` 和 `doc/development/README.md`。
- 本仓库默认使用中文文档和中文提交说明；代码标识符按对应技术栈使用英文。
- 不要把所有说明堆进一个文档；接口、数据模型、Android、后端、AI、支付、安全、测试分别维护在 `doc/development/`。
- 修改接口时必须同步 `doc/development/05-api-contract.md`。
- 修改核心表、状态机或枚举时必须同步 `doc/development/06-data-model.md`。
- 修改支付、订单、退款、库存逻辑时必须同步 `doc/development/08-commerce-payment.md`。
- 修改 AI 拼豆图纸流程时必须同步 `doc/development/07-ai-pattern-generation.md`。
- 修改权限、隐私、内容审核、未成年人、备案相关行为时必须同步 `doc/development/09-security-compliance.md`。

## 技术边界

- Android 第一版使用 Kotlin、Jetpack Compose、CameraX、Photo Picker、Retrofit/OkHttp、Room、WorkManager。
- 后端第一版使用 Spring Boot、PostgreSQL、Redis、对象存储、消息队列。
- API 统一使用 `/api/v1` 前缀，REST + JSON。
- 对外业务 ID 使用字符串。
- 鉴权使用 `Authorization: Bearer <access_token>`。
- 响应格式统一为 `{ code, message, data, traceId }`。
- 上传使用后端签发预签名 URL，客户端直传对象存储。
- AI 服务必须由后端封装，客户端不得直连模型供应商。
- 支付以微信支付和支付宝 App 支付为主，支付结果以服务端回调和主动查询为准。

## 安全和合规

- 不得提交密钥、证书、商户私钥、API Key、访问令牌、真实用户数据。
- 不得在客户端硬编码支付密钥、AI 密钥、OSS Secret。
- 不得绕过内容审核、举报、账号注销、隐私授权、未成年人保护逻辑。
- 玩家交易第一版是直连撮合和订单留痕，不自建余额，不沉淀资金池。
- 玩家卖家、提现、定制服务发布者必须满足 18+ 实名要求。
- AI 输入和输出都需要审核记录。
- 所有用户上传文件默认按不可信输入处理。

## 变更纪律

- 保持改动聚焦，避免无关重构。
- 不要擅自替换已定技术栈。
- 不要新增大框架或基础设施，除非对应开发文档先更新并说明原因。
- 新增状态、错误码、接口字段时，必须说明兼容性影响。
- 涉及订单、支付、退款、库存、实名认证、内容审核的改动必须附带测试说明。
- 如果发现计划文档和实现文档冲突，以 `doc/development/` 中更具体的分册为准，并同步修正文档。

## 验证要求

- Android 改动至少运行对应单元测试、构建或静态检查；无法运行时说明原因。
- 后端改动至少运行对应模块测试；涉及支付回调必须覆盖幂等场景。
- API 契约改动必须给出请求、响应和错误场景。
- 数据模型改动必须说明迁移、默认值和回滚风险。
- 文档改动需要确认没有保留未决占位词。

