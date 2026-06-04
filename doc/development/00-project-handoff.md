# 豆屿 Doyu 项目接手手册

> 更新日期：2026-06-04
> 用途：给新接手工程师、主 Agent 和子任务 Agent 提供最快速的工程入口。本文件只汇总当前事实和工作边界；具体契约仍以对应分册为准。

## 一句话定位

豆屿 Doyu 是面向中国大陆 16+ 用户的 Android 拼豆社区、AI 拼豆图纸、材料商城和玩家直连交易应用。当前工程目标不是上线生产版，而是把开发态 MVP 收敛到主链路可演示、边界清楚、不可用能力不误导用户。

当前权威阶段是 **第六轮 UI/品牌与主链路展示收敛阶段**。核心工作是统一社区、商城、AI、消息、我的五个主 Tab 的首屏展示、状态页、互动边界和品牌识别，同时保留前几轮已关闭的登录态、社区契约、商城订单/支付边界和 OSS Provider 骨架。

## 必读顺序

任何开发、重构、修复或文档更新前，先按这个顺序读：

1. `../../AGENTS.md`：仓库级规则、当前阶段、不做范围、验证要求。
2. `current-status.md`：当前完成度、不可用边界、下一步优先级。
3. `README.md`：文档索引、职责映射、同步规则。
4. `../豆屿App商业技术执行计划.md`：产品目标和商业闭环。
5. `05-api-contract.md`：接口契约、错误码、分页、幂等。
6. `11-ui-style-guide.md`：当前唯一 UI 权威规范。

按职责继续读：

| 职责 | 必读文档 |
|---|---|
| Android 页面 / UI 状态 | `03-android-client.md`、`13-ui-screen-blueprints.md`、`10-testing-acceptance.md` |
| 后端接口 / 服务 | `04-backend-services.md`、`05-api-contract.md`、`06-data-model.md` |
| 主链路理解 | `12-feature-and-flow-map.md`、`diagrams/README.md` |
| AI 图纸 | `07-ai-pattern-generation.md`、`15-ai-pattern-provider-selection.md` |
| 商城 / 订单 / 支付 | `08-commerce-payment.md` |
| 联调 / 真机 QA | `14-frontend-backend-collaboration.md`、`10-testing-acceptance.md` |
| 安全 / 合规 | `09-security-compliance.md` |

## 代码目录地图

| 路径 | 职责 | 接手注意 |
|---|---|---|
| `DouYu/` | Android App，Kotlin + Jetpack Compose 单 Activity | 不要绕过 `DoyuAppContainer`、Repository、现有导航和 UI 组件口径。 |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/navigation/` | 5 个主 Tab 和详情页路由 | 主 Tab 固定为社区、商城、AI 拼图、消息、我的；详情/流程页通常隐藏底栏。 |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/network/` | Retrofit 接口、DTO、TokenStore、会话管理 | 登录请求仍传 `ageGroup=AGE_18_PLUS`；debug API base 在构建期从 `.env` 注入。 |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/data/` | Repository、真实/Mock 数据源、AppContainer | Mock 只用于测试/预览，真实页面优先走后端接口。 |
| `DouYu/app/src/main/java/cn/edu/app/douyu/core/ui/` | 通用 Compose 组件、状态页、格式化 | 第六轮页面必须复用加载、空、错、未登录和禁用状态口径。 |
| `DouYu/app/src/main/java/cn/edu/app/douyu/feature/` | 业务页面：auth/community/ai/commerce/message/profile | 页面仍未全部 ViewModel 化；不要为小改动强行大重构。 |
| `doyu-server/` | Spring Boot 后端，Java 21 | API 前缀 `/api/v1`，统一响应 `{ code, message, data, traceId }`。 |
| `doyu-server/src/main/java/cn/edu/app/douyu/server/common/` | 安全、错误、响应包装、实体、初始化、配置 | 对外业务 ID 使用字符串；写接口涉及幂等时使用 `Idempotency-Key`。 |
| `doyu-server/src/main/java/cn/edu/app/douyu/server/community/` | Feed、帖子、评论、点赞、收藏、话题、贴纸 | 评论支持文字/图片/@/#/贴纸，图片最多 9 张且必须是本人 `POST_IMAGE`。 |
| `doyu-server/src/main/java/cn/edu/app/douyu/server/message/` | 通知、会话、私信 | 未互关同会话同发送者最多 3 条，超过返回明确错误。 |
| `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/` | 上传预签名、确认、OSS Provider | `local|stub|aliyun` 可切换；Aliyun 密钥只在后端持有。 |
| `doc/development/` | 当前有效开发文档 | 改代码必须同步对应分册；不要只在对话里说明新规则。 |

## 当前主链路

### Android App Shell

- 单 Activity + Navigation Compose。
- 底部 5 Tab：社区、商城、AI 拼图、消息、我的。
- 主 Tab 页面显示底部导航；详情页、AI 参数/进度、订单确认、支付状态、会话详情等流程页隐藏底部导航。
- 当前 UI 权威规范是 `11-ui-style-guide.md`，结构蓝图见 `13-ui-screen-blueprints.md`。

### 后端服务

- Java 21 + Spring Boot + Spring Security + JWT + Spring Data JPA + Flyway。
- PostgreSQL 宿主机端口 `5433`，容器内端口 `5432`；后端端口 `8081`。
- Redis 已纳入技术边界，但当前不少 MVP 能力仍是同步接口或基础骨架。
- 文件上传使用后端签发凭证、客户端直传、后端确认资产。

### 接口契约

- API 前缀固定为 `/api/v1`。
- 统一响应由后端包装为 `{ code, message, data, traceId }`。
- Android DTO、Repository、UI 和测试必须追 `05-api-contract.md`。
- 如果代码和文档冲突，先以当前代码与 `current-status.md` 判断事实，再修正文档或提出后续修复任务。

## 本地联调规则

- 仓库根目录 `.env` 是本地联调唯一生效文件，不提交。
- 当前默认只维护真机联调配置，不维护 `.env.emulator` / `.env.phone`。
- 修改 `.env` 后必须重启后端并重新构建 Android debug 包，因为 Android `BuildConfig.API_BASE_URL` 是构建期注入。
- 后端 Local OSS URL 在启动时读取环境变量。

常用本地地址：

| 项 | 地址 |
|---|---|
| API base | `http://localhost:8081/api/v1` |
| Swagger UI | `http://localhost:8081/swagger-ui/index.html` |
| Stub SMS code | `123456` |
| Default admin | `admin / admin123` |

## 真机 QA 前置

执行任何安装、截图、日志、真机 QA 或 App 内交互前，必须先运行：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
```

如果没有在线设备，必须明确说明“当前无在线真机，不能执行真机验收”，不能把设备 QA、截图或 smoke 写成通过。默认只在用户指定真机 IP `10.64.241.158` 下验收，除非用户另行要求。

## 验证命令

文档改动至少运行：

```powershell
git diff --check
rg -n "17-ui-red[e]sign|12-front[e]nd|13-back[e]nd|16-ph[a]se|18-bug[f]ix|Leaders[P]rompt" doc AGENTS.md CLAUDE.md -g "!doc/development/10-testing-acceptance.md"
rg -n '登录请求只传手机号和验证[码]|不再传 `age[G]roup`|不再传 age[G]roup' doc AGENTS.md
git check-ignore -v .env .env.* .qa-output
```

Android 改动至少运行：

```powershell
cd DouYu
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

后端改动至少运行：

```powershell
cd doyu-server
mvn test
```

只改文档和 SVG 时，不需要运行 Android/后端构建；如果意外触碰业务源码，必须补跑对应验证。

## 当前不能宣称完成

以下能力不能包装成已完成或生产可用：

- 真实 AI Provider 和真实视觉理解质量。
- 真实微信支付、支付宝支付、退款、对账或支付 SDK。
- 完整地址管理；订单确认仍只能清楚表达地址缺口。
- 生产级内容审核、图片审核、版权识别、诈骗识别和交易风控。
- 玩家二手/定制交易完整闭环、担保、评价、纠纷、提现、卖家资质审核。
- 备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场上线材料。
- 生产对象存储；Aliyun OSS smoke 只证明当前开发环境可联调，STS/最小权限、CDN、防盗链、审核、缩略图和 seed assets 云迁移仍未关闭。

以下第六轮真机 QA 也不能写成已通过，除非补到新的证据：

- 评论图片链路：Photo Picker、最多 9 图、预签名上传、确认、上传失败保留缩略图。
- 评论列表富内容渲染：图片、@、#、贴纸和图片加载失败占位。
- 纯贴纸评论。
- 点赞高亮与计数回显。
- 收藏/关注退出详情后重新进入的持久回显。
- 我的页“评论作品 / 收藏作品 / 关注作品”和互动资产列表卡跳转详情。
- 真机 seed/public 图片 URL 不能返回 `localhost`。
- 后端 `mvn test` 必须有当前轮命令输出。

## 接手后先做什么

1. 看 `git status --short`，识别已有用户改动，不要回退。
2. 读 `current-status.md` 的“下一步优先级”，确认当前任务属于 P0/P1/P2 哪一类。
3. 若任务涉及 API 字段，先对照 `05-api-contract.md`、后端 Controller、Android DTO。
4. 若任务涉及 UI，先对照 `11-ui-style-guide.md`、`13-ui-screen-blueprints.md` 和 `diagrams/README.md`。
5. 若任务涉及联调，先检查 `.env`、后端端口、ADB 在线设备和真机 IP。
6. 完成修改后，按改动范围运行验证命令，并把文档同步到对应分册。
