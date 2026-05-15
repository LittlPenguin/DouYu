# AGENTS.md

本仓库用于开发 **豆屿 Doyu**：面向中国大陆 16+ 年轻用户的 Android 拼豆社区、AI 拼豆图纸、商城与玩家交易应用。

## 统一规则

所有开发规则、技术边界、安全合规要求、变更纪律和验证要求已合并至 [`CLAUDE.md`](CLAUDE.md)。

**开发前必须先阅读：**
1. [`CLAUDE.md`](CLAUDE.md) — 工程规则和技术边界
2. [`doc/豆屿App商业技术执行计划.md`](doc/豆屿App商业技术执行计划.md) — 产品目标、商业闭环、阶段计划
3. [`doc/development/README.md`](doc/development/README.md) — 开发文档索引和阅读顺序
4. [`doc/development/LeadersPrompt.md`](doc/development/LeadersPrompt.md) — 团队领导提示词规则

## 文档同步

修改实现时必须同步对应文档：

| 改动范围 | 同步文档 |
|---|---|
| API 接口 | `doc/development/05-api-contract.md` |
| 核心表/状态机/枚举 | `doc/development/06-data-model.md` |
| AI 拼豆图纸流程 | `doc/development/07-ai-pattern-generation.md` |
| 支付/订单/退款/库存 | `doc/development/08-commerce-payment.md` |
| 权限/隐私/审核/未成年人 | `doc/development/09-security-compliance.md` |
