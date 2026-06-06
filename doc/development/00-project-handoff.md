# 豆屿 Doyu 项目接手手册

> 更新日期：2026-06-04
> 用途：让新接手工程师或主 Agent 在不依赖本地私有规则文件的情况下，快速理解项目当前事实、边界、目录、验证方式和下一步开发顺序。

## 一句话定位

豆屿 Doyu 是面向中国大陆 16+ 用户的 Android 拼豆社区、AI 拼豆图纸、材料商城与玩家直连交易应用。当前不是生产上线阶段，而是把开发态 MVP 收敛到主链路可演示、UI 规则可落地、不可用能力不误导用户。

当前权威阶段是 **第六轮 UI/品牌与主链路展示收敛阶段**。本轮之后新增 Stage 8 UI 视觉对齐重构：除底部导航容器样式外，Android 页面严格按 Open Design A 方向重构；底部导航样式保持当前 Android 真机 UI，底栏 icon 语义和文字追设计图。

## 接手先读

1. `current-status.md`：当前代码事实、阶段状态和不能宣称完成的能力。
2. `README.md`：公开文档索引和职责映射。
3. `16-stage-development-roadmap.md`：下一阶段 Stage 0-7 开发流程。
4. `05-api-contract.md`：真实 Controller 和 Retrofit 对齐后的接口契约。
5. `13-ui-screen-blueprints.md`：页面结构、状态、禁用态和点击去向。
6. `diagrams/README.md`：开发流程图、API 图、导航图和 UI 线框图。

按职责继续读：

| 职责                 | 必读文档                                                                       |
| ------------------ | -------------------------------------------------------------------------- |
| Android 页面 / UI 状态 | `03-android-client.md`、`11-ui-style-guide.md`、`13-ui-screen-blueprints.md` |
| 后端接口 / 服务          | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md`           |
| 主链路理解              | `12-feature-and-flow-map.md`、`diagrams/README.md`                          |
| AI 图纸              | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md`        |
| 商城 / 订单 / 支付       | `08-commerce-payment.md`                                                   |
| 联调 / 真机 QA         | `14-frontend-backend-collaboration.md`、`10-testing-acceptance.md`          |
| 安全 / 合规            | `09-security-compliance.md`                                                |

## 目录地图

| 路径                                                          | 职责                                                                                 | 接手注意                                                                           |
| ----------------------------------------------------------- | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| `DouYu/`                                                    | Android App，Kotlin + Jetpack Compose，单 Activity                                    | 本轮计划不改业务代码；后续实现时遵守现有 `DoyuAppContainer`、Repository、Navigation Compose 结构。      |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/navigation/` | 5 个主 Tab 和当前 Android 路由                                                            | 当前代码已注册 Search、Profile Edit、Settings 分区页和通知详情；未来地图/真实支付/大模型生图仍不是 Android 真实链路。 |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/network/`    | Retrofit 接口、DTO、ApiClient、TokenStore                                               | 登录请求仍传 `ageGroup=AGE_18_PLUS`；debug baseUrl 由 `.env` 在构建期注入。                   |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/data/`       | Repository、真实/Mock 数据源、AppContainer                                                | 真实页面优先走后端接口；Mock 只用于测试或预览。                                                     |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/ui/`         | 通用 Compose 组件、状态页、按钮、卡片                                                            | 后续 UI 重构优先收敛统一组件，不做每页自定义状态。                                                    |
| `DouYu/app/src/main/java/cn/edu/app/douyu/feature/`         | auth、community、ai、commerce、message、profile 页面                                      | 多个页面仍在 Composable 内处理副作用；后续复杂写操作应逐步迁移到 ViewModel。                              |
| `doyu-server/`                                              | Java 21 + Spring Boot 后端                                                           | API 前缀 `/api/v1`；统一响应 `{ code, message, data, traceId }`。                      |
| `doyu-server/src/main/java/cn/edu/app/douyu/server/*`       | Auth、User、Upload、Community、Pattern、Commerce、Order、Payment、Message、Reward、Admin 等模块 | Controller 是当前接口事实源；文档不得新增未实现公共 API。                                           |
| `doc/development/`                                          | 当前公开开发文档和 SVG 流程图                                                                  | 本轮只允许修改这里的文档和设计图，不改业务实现。                                                       |
| `doc/development/open-design/`                              | Open Design HTML 原型副本                                                              | 表达 UI 目标，不代表 Android 当前路由已存在。                                                  |

## 当前 Android 路由事实

底部主 Tab：

- `community`：社区。
- `commerce`：商城。
- `ai`：AI 创作入口；Stage 8 底栏文案已按设计图收敛为 `AI`。
- `message`：消息。
- `profile`：我的。

当前已存在的主要二级路由：

- 登录：`splash`、`login`、`login_return?returnTo={returnTo}`。
- 社区：`post/{postId}`、`post_create`、`image_select`、`camera_capture`。
- AI：`ai_params/{uploadedFileId}`、`ai_progress/{jobId}`、`pattern/{patternId}`、`pattern_history`。
- 商城：`product_list`、`product/{productId}`、`cart`、`order_confirm`、`payment_result/{orderId}`。
- 消息：`conversation/{conversationId}`。
- 我的：`my_patterns`、`favorites`、`liked_posts`、`commented_posts`、`favorite_posts`、`followed_posts`、`my_orders`、`settings`。

设计目标中当前已落地的 Android 入口包括：`search-a.html` 对应 Search、`profile-edit-a.html` 对应编辑资料、Settings 分区页和通知列表内详情。未来地图/真实支付/大模型生图仍只允许作为 UI-only 页面或文档说明，不是当前真实链路。

## 当前后端事实

后端模块和 Controller 已覆盖：

- Auth：短信验证码、短信登录、刷新、退出、账号注销申请。
- User：当前用户、公开用户、用户搜索、资料更新、关注/取关、实名提交、个人互动作品列表。
- Upload：预签名上传和上传确认，Provider 支持 `local|stub|aliyun` 切换。
- Community：Feed、关注 Feed、帖子 CRUD、点赞、收藏、评论、话题、贴纸。
- Pattern：AI 图纸任务、任务列表、取消、收藏、详情、配额。
- Commerce：商品、购物车。
- Order：订单创建、列表、详情、取消。
- Payment：联调支付单、支付查询、回调骨架、退款骨架。
- Message：通知、通知已读、会话、会话详情、私信发送。
- Reward：签到、签到状态、积分、徽章。
- Admin：用户、内容、商品、订单、支付、AI 任务、举报、操作日志等后台 API。

Android Retrofit 当前没有接入全部后端接口，例如账号注销、资料更新、实名、举报、退款、Pattern 配额、Admin API 等；`05-api-contract.md` 会分别标注“后端存在”和“Android 已接入”。

## 本地联调

常用地址：

| 项               | 值                                             |
| --------------- | --------------------------------------------- |
| API base        | `http://localhost:8081/api/v1`                |
| Swagger UI      | `http://localhost:8081/swagger-ui/index.html` |
| 后端端口            | `8081`                                        |
| PostgreSQL 宿主端口 | `5433`                                        |
| Redis 端口        | `6379`                                        |
| Stub SMS code   | `123456`                                      |
| Default admin   | `admin / admin123`                            |

联调规则：

- 仓库根目录 `.env` 是本地联调唯一生效文件，不提交。
- Android debug `BuildConfig.API_BASE_URL` 在构建期从 `.env` 注入，修改 `.env` 后需要重启后端并重新构建 debug 包。
- 当前默认只维护真机联调配置，不维护模拟器/真机双模板。
- 客户端不得保存 OSS Secret、支付密钥或 AI 密钥。

## 不可宣称完成

以下能力不能写成已完成或生产可用：

- 真实 AI Provider 和真实视觉理解质量。
- 地图 API、定位服务和真实地理能力。
- 真实微信/支付宝支付、退款、对账和支付 SDK。
- 完整地址管理；订单确认仍必须清楚表达地址缺口。
- 生产级内容审核、图片审核、版权识别、诈骗识别和交易风控。
- 玩家二手/定制交易完整闭环、担保、评价、纠纷、提现和卖家资质审核。
- 备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场上线材料。
- 生产对象存储；Aliyun OSS 只代表后端 Provider 骨架可联调。
- Search 全局后端、Settings 生产配置能力、独立通知详情后端接口和通知已读/跳转闭环。
- Profile Edit Android 路由已接现有 `PATCH /api/v1/users/me`，但只能覆盖当前后端已有资料字段；城市/地区仍是 UI-only，不代表地图或定位能力已接入。

## 接手后先做什么

1. 运行 `git status --short`，识别已有未提交改动，不能回退用户工作。
2. 对照 `current-status.md` 和 `16-stage-development-roadmap.md` 确认任务属于哪个 Stage。
3. 涉及接口时先对照 `05-api-contract.md`、后端 Controller、Android Retrofit。
4. 涉及 UI 时先对照 `11-ui-style-guide.md`、`13-ui-screen-blueprints.md`、`diagrams/README.md` 和 Open Design HTML。
5. 涉及联调时先检查 `.env`、后端端口、设备在线状态和 API baseUrl。
6. 完成修改后按 `10-testing-acceptance.md` 跑对应验证，并把结果写清楚。

## 文档类验证

只改文档和 SVG 时至少运行：

```powershell
git diff --check
git status --short
git diff --name-only -- DouYu doyu-server
Get-ChildItem doc\development\diagrams -Filter *.svg | ForEach-Object {
  [xml](Get-Content -Raw -Encoding UTF8 $_.FullName) | Out-Null
}
```

如果意外触碰 Android 或后端源码，必须补跑对应构建/测试，并在最终说明中报告原因。
