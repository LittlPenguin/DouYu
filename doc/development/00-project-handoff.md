# 豆屿 Doyu 项目接手手册

> 更新日期：2026-06-06
> 用途：让新接手工程师或 Agent 快速理解当前迁移阶段、目录边界、验证方式和不可误导的能力边界。

## 一句话定位

豆屿 Doyu 是面向中国大陆 16+ 用户的 Android 拼豆社区、AI 拼豆图纸、材料商城与玩家交流应用。当前阶段不是生产上线，而是把 Android 客户端从历史 Kotlin + Jetpack Compose 实现重写为传统 Android Java + Activity/Fragment + XML，并同步删除运行期静态填充数据。

Open Design 本地 HTML 是 UI 还原权威。真实数据为空时，客户端必须显示空态、未登录态、错误态、禁用态或 UI-only 边界，不能用 mock/demo 数据撑满页面。

## 本轮硬规则

- 先文档和规则，后代码迁移。
- Android 生产和测试源码全部使用 Java/XML，不保留 Kotlin/Compose。
- UI 结构、视觉层级、文案、状态和交互以 `doc/development/open-design/*.html` 为准。
- 删除 Android 运行态 MockData、mock repository、假列表、假订单、假支付参数。
- 删除后端运行期 seed/demo 填充、`static/seed/**` 和公开 `/seed/**`。
- 保留 SMS、OSS、AI、Payment Stub Provider，因为它们是开发联调能力。
- 包括主线程在内最多 3 个 Codex 线程：主线程 + Android 工作线程 A + 后端工作线程 B。
- 当前工作树有未提交改动，禁止 `git reset`、`git checkout --` 或无差别回滚用户改动。
- 构建、测试、真机、截图结论必须来自实际命令或截图；不可伪造。

## 接手先读

1. `AGENTS.md`：本轮任务硬规则和线程边界。
2. `current-status.md`：当前迁移状态、已完成项和未完成项。
3. `01-tech-stack.md`：Java/XML Android 目标技术栈。
4. `03-android-client.md`：Android 迁移边界、页面组织和残留检查。
5. `04-backend-services.md`：后端 seed/demo 清理边界。
6. `10-testing-acceptance.md`：残留检查、构建、后端测试和 UI 验收门禁。
7. `11-ui-style-guide.md`、`13-ui-screen-blueprints.md`：Open Design 落地规则和页面蓝图。
8. `16-stage-development-roadmap.md`、`18-unfinished-and-blockers.md`：阶段路线和未完成项。

按职责继续读：

| 职责                 | 必读文档                                                                       |
| ------------------ | -------------------------------------------------------------------------- |
| Android 页面 / UI 状态 | `03-android-client.md`、`11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| 后端接口 / 服务          | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md`           |
| 主链路理解              | `12-feature-and-flow-map.md`、`diagrams/README.md`                          |
| AI 图纸              | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md`        |
| 商城 / 订单 / 支付       | `08-commerce-payment.md`                                                   |
| 联调 / QA            | `14-frontend-backend-collaboration.md`、`10-testing-acceptance.md`          |
| 安全 / 合规            | `09-security-compliance.md`                                                |

## 目录地图

| 路径                                                    | 职责                       | 接手注意                                                                                                 |
| ----------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------- |
| `DouYu/`                                              | Android App              | 当前目标是 Java + Activity/Fragment + XML；构建脚本可继续 Kotlin DSL。                                             |
| `DouYu/app/src/main/java/cn/edu/app/douyu/`           | Java 源码根目录               | 必须保留包结构：`core`、`network`、`model`、`data`、`ui`、`auth`、`community`、`commerce`、`ai`、`message`、`profile`。 |
| `DouYu/app/src/main/res/layout/`                      | XML 页面和列表 item           | 主入口为 `activity_main.xml`；主 Tab 用 Fragment XML；二级流程页用 Activity XML。                                   |
| `DouYu/app/src/main/res/values/`                      | 颜色、主题、文案                 | 对齐 Open Design 品牌色和状态文案。                                                                             |
| `doyu-server/`                                        | Java 21 + Spring Boot 后端 | API 前缀 `/api/v1`；统一响应 `{ code, message, data, traceId }`。                                            |
| `doyu-server/src/main/java/cn/edu/app/douyu/server/*` | 后端业务模块                   | 本轮删除运行期 seed/demo，不改变公开 API 契约。                                                                      |
| `doyu-server/src/main/resources/static/seed/`         | 历史 seed 静态资源             | 本轮必须删除，不能作为线上内容来源。                                                                                   |
| `doc/development/`                                    | 当前公开开发文档                 | 本轮已改为 Java/XML 迁移口径。                                                                                 |
| `doc/development/open-design/`                        | Open Design HTML 权威稿     | UI 1:1 对照源，不代表后端数据一定存在。                                                                              |

## 当前 Android 页面事实

目标主入口：

- `MainActivity.java` 承载顶部栏、Fragment 容器和底部导航。
- 五个主 Tab 固定为：社区、商城、AI、消息、我的。
- 主 Tab 对应 Open Design：
  - 社区：`community-home-a.html`
  - 商城：`commerce-home-a.html`
  - AI：`ai-home-a.html`
  - 消息：`messages-a.html`
  - 我的：`profile-a.html`

二级流程页使用 Java Activity：

- 社区：Search、发帖、作品详情、评论栏。
- AI：图片选择、CameraX 拍照、参数、进度、结果、历史。
- 商城：商品列表、商品详情、购物车、订单确认、支付结果。
- 消息：会话详情、通知详情。
- 我的：资料编辑、Settings 首页和分区页。

每个页面必须覆盖加载、空态、错误、未登录、禁用或 UI-only 状态。没有真实数据时不得出现本地假列表。

## 当前后端事实

后端模块包括：

- Auth：短信验证码、短信登录、刷新、退出、账号注销申请。
- User：当前用户、公开用户、用户搜索、资料更新、关注/取关、实名认证提交、个人互动作品列表。
- Upload：预签名上传和上传确认，Provider 支持 `local|stub|aliyun`。
- Community：Feed、关注 Feed、帖子 CRUD、点赞、收藏、评论、话题、贴纸。
- Pattern：AI 图纸任务、任务列表、取消、收藏、详情、配额。
- Commerce：商品、购物车。
- Order：订单创建、列表、详情、取消。
- Payment：联调支付单、支付查询、回调骨架、退款骨架。
- Message：通知、通知已读、会话、会话详情、私信发送。
- Reward：签到、签到状态、积分、徽章。
- Admin：后台管理 API。

本轮后端只清理运行期 seed/demo 填充和静态 seed 暴露，不删除 Stub Provider，不改变 `/api/v1` 路径。

## 本地联调

| 项               | 默认值                                           |
| --------------- | --------------------------------------------- |
| API base        | `http://localhost:8081/api/v1`                |
| Swagger UI      | `http://localhost:8081/swagger-ui/index.html` |
| 后端端口            | `8081`                                        |
| PostgreSQL 宿主端口 | `5433`                                        |
| Redis 端口        | `6379`                                        |
| Stub SMS code   | `123456`                                      |

联调规则：

- 仓库根目录 `.env` 是本地联调配置文件，不提交。
- Android debug `BuildConfig.API_BASE_URL` 在构建期从 `.env` 或环境变量注入。
- 客户端不得保存 OSS Secret、支付密钥或 AI 密钥。
- 空库启动后列表 API 返回空列表是合法行为，App 必须显示空态。

## 不可宣称完成

以下能力不能写成已完成或生产可用：

- 真实 AI Provider 和真实视觉理解质量。
- 地图 API、定位服务和真实地理能力。
- 真实微信/支付宝支付、退款、对账和支付 SDK。
- 完整地址管理。
- 生产级内容审核、图片审核、版权识别、诈骗识别和交易风控。
- 玩家二手/定制交易完整闭环、担保、评价、纠纷、提现和卖家资质审核。
- 备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场上线材料。
- 生产对象存储；Aliyun OSS 当前只代表后端 Provider 骨架可联调。
- 全局搜索后端、Settings 生产配置、独立通知详情后端接口。

## 接手后先做什么

1. 运行 `git status --short`，识别已有未提交改动，不回滚用户工作。
2. 对照 `current-status.md` 和 `16-stage-development-roadmap.md` 确认当前阶段。
3. 涉及接口先对照 `05-api-contract.md`、后端 Controller、Android Retrofit。
4. 涉及 UI 先对照 `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` 和 Open Design HTML。
5. 完成修改后按 `10-testing-acceptance.md` 运行残留检查、构建、测试和 UI 对照验收。

## 最小验证

文档和源码改动完成后至少运行：

```powershell
git diff --check
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil.compose|paging.compose" DouYu/app
rg -n "DataInitializer|static/seed|/seed/|post_seed|prod_|seedTopic|seedProduct|seedPost" doyu-server/src/main doyu-server/src/test
```

完整验收按 `10-testing-acceptance.md` 执行 Android 构建、后端测试和 UI 截图对照。真机不可用时只能记录“未覆盖”，不能写成通过。
