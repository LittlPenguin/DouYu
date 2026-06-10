# 00. Project Handoff

## 一句话定位

SpellBean 是面向 16+ 用户的 Android 拼豆社区与材料商城应用。当前客户端使用传统 Android Java + Activity/Fragment + XML；历史 Kotlin + Jetpack Compose 实现不再作为开发目标或验收依据。当前不是生产上线阶段，运行期静态填充数据不得恢复。

## 当前范围

保留：

- 登录、注册、会话和用户资料。
- 社区首页、发帖、作品详情、评论、图片上传、@、#。
- 商城首页、商品详情、购物车、订单确认、订单状态展示。
- 地址管理需求和地址字段。
- 消息、通知、关注、粉丝、我的页。
- 设置中的账号安全、隐私权限和通知设置。
- OSS-backed image storage、上传预签名/确认、Alibaba OSS 配置、空态、错误态、登录态和后台联调。

删除或不再作为当前范围：

- AI 页面、图纸生成、视觉 Provider、大模型 Provider。
- 地图 API、定位 Provider、地图选点。
- 支付、真实微信/支付宝 SDK/API、退款、对账、生产回调。
- 真实 SMS Provider、生产限流、生产风控。

- 完整合规材料页面和文档，包括隐私政策、用户协议、SDK 清单、备案、投诉机制等。

地址管理保留，不受地图/定位能力删除影响。
OSS-backed image storage、上传预签名/确认、Alibaba OSS 配置和后端 upload/oss provider 保留。

## 事实优先级

1. 当前代码、构建配置、测试输出和实际截图。
2. 仓库根目录 `AGENTS.md` 和本轮任务计划。
3. `current-status.md`。
4. `README.md` 和领域分册。
5. 历史验证记录，仅作为当时证据，不代表当前范围。

## 文档入口

| 职责 | 文档 |
|---|---|
| 总览 | `README.md`、`current-status.md` |
| Android | `03-android-client.md`、`13-ui-screen-blueprints.md` |
| 后端/API | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md` |
| 商城/订单/地址 | `08-commerce-orders-address.md` |
| 测试验收 | `10-testing-acceptance.md` |
| UI | `11-ui-style-guide.md`、`open-design/index.html`、`diagrams/README.md` |
| 阶段路线 | `16-stage-development-roadmap.md`、`18-unfinished-and-blockers.md` |

## Open Design

Open Design 位于 `doc/development/open-design/`。当前权威页面不包含 AI、支付、地图或完整合规页面。保留页面包括社区、商城、发帖、作品详情、消息、通知、我的、资料编辑、关注/粉丝、设置、登录和注册。

## 验证入口

Android：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

Backend：

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn test
```

Static checks：

```powershell
cd D:\Studio\SpellBean
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|fake payment|假支付|假订单" DouYu/app doyu-server/src doc
```

本轮文档/设计清理 Worker 不运行长服务，不修改 Android 或后端源码。
