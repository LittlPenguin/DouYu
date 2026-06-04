# 01. 技术栈

## 目标

固定豆屿 Doyu 第一版工程技术选择，降低后续团队和 Agent 的决策成本。第一版优先稳定、可招聘、可维护、适配中国大陆生态。

## Android 客户端

| 类别 | 选择 |
|---|---|
| 开发语言 | Kotlin |
| UI | Jetpack Compose |
| 架构 | 单 Activity + MVVM + Repository |
| 导航 | Jetpack Navigation Compose |
| 网络 | Retrofit + OkHttp |
| 序列化 | Kotlinx Serialization |
| 图片加载 | Coil |
| 本地缓存 | Room + DataStore |
| 后台任务 | WorkManager |
| 分页 | Paging 3 |
| 相机 | CameraX |
| 相册 | Android Photo Picker |
| 推送 | 先预留厂商推送抽象，灰度期可先用站内消息 |
| 支付 | 微信支付 Android SDK、支付宝 App 支付 SDK |

Android 最低版本建议：

- `minSdk`：26。
- `targetSdk`：按应用市场和 Android 最新要求设置，项目创建时使用稳定最新版。
- 第一版重点适配小米、OPPO、vivo、华为、荣耀、三星主流机型。

## 后端服务

| 类别 | 选择 |
|---|---|
| 语言 | Java 21 |
| 框架 | Spring Boot |
| 架构 | 模块化单体优先，按领域拆包 |
| API | REST + JSON |
| 鉴权 | JWT access token + refresh token |
| 数据库 | PostgreSQL |
| 数据库迁移 | Flyway，PostgreSQL 运行时需包含 `flyway-database-postgresql` |
| 缓存 | Redis |
| 异步任务 | 当前使用 Spring `@Async`；AI 任务量增长后再接入云消息队列、RabbitMQ 或 RocketMQ |
| ORM | Spring Data JPA |
| 管理后台 | Web 前端独立工程，接口由后端提供 |
| 日志 | 结构化日志，包含 traceId |
| 监控 | 应用指标、错误日志、接口耗时、AI 任务耗时、支付回调异常 |

第一版采用模块化单体，不拆微服务。原因是业务边界尚在验证期，拆微服务会增加部署、链路追踪、事务和团队协作成本。

## 基础设施

| 能力 | 默认选择 |
|---|---|
| 对象存储 | 开发环境 Local OSS Provider；生产目标为阿里云 OSS |
| CDN | 阿里云 CDN 或对象存储同厂商 CDN |
| 短信 | 当前为 Stub 验证码；生产接国内云短信服务 |
| 内容审核 | 当前为基础关键词过滤；生产接国内云内容安全服务 + 人审后台 |
| AI 模型 | 当前为 Stub Provider + 自研算法；生产通过 `AiVisionProvider` 接国内模型 API |
| 数据库备份 | PostgreSQL 自动备份 + 手动快照 |
| 配置管理 | 环境变量 + 云密钥管理 |
| 部署 | 云服务器或容器服务，先保证可观测和可回滚 |

## AI 与图像处理

| 类别 | 选择 |
|---|---|
| AI 接入 | 后端统一封装，客户端不得直连 |
| 供应商 | 阿里云百炼/通义万相、腾讯混元、火山引擎、百度千帆中选择 |
| 图像处理 | 后端图像处理库 + 自研拼豆算法 |
| 输出 | 预览图、网格图、色号图、豆量清单、材料建议 |
| 审核 | 输入审核 + 输出审核 |

## 支付

第一版接入：

- 微信支付 App 支付。
- 支付宝 App 支付。

当前工程只有支付单、Stub 支付参数和回调安全骨架，尚未接入真实微信/支付宝 SDK 或 API，不能用于生产收款。

支付规则：

- 金额由服务端计算。
- 客户端只负责拉起支付。
- 支付结果以服务端回调和主动查询为准。
- 回调必须验签和幂等。
- 不在第一版做平台自建余额。

## 禁用或暂缓选择

- 不使用跨平台框架作为第一版主客户端。
- 不做微服务化拆分。
- 不训练自有大模型。
- 不把 AI Key、OSS Secret、支付密钥放在客户端。
- 不直接读取全量相册权限作为默认方案。
- 不做 Google Play Billing。
- 不做海外支付。

## 版本策略

- API 第一版统一为 `/api/v1`。
- 文档和实现字段保持同名。
- 废弃字段先保留兼容，再在下一个大版本移除。
- 影响客户端的接口变更必须先更新 `05-api-contract.md`。
