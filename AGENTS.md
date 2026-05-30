# AGENTS.md

本仓库用于开发 **豆屿 Doyu**：面向中国大陆 16+ 用户的 Android 拼豆社区、AI 拼豆图纸、商城与玩家交易应用。

本文件是本项目对 AI Agent、Claude Code、Codex 和其他自动化开发工具的 **唯一顶层开发约束入口**。其他工具入口文件只能指向本文件，不得维护另一套重复规则。

## 当前阶段

当前项目处于 **第三轮商城 UI/API 收敛 / UI MVP 验收收口阶段**。

阶段目标：

- 在第一轮登录 + 社区契约样板、第二轮 App Shell / 消息 / 我的 UI 统一基线之后，继续收口第三轮商城 UI/API。
- 按 `doc/stitch_document_app_generator/` 的 Stitch 设计探索稿和 `doc/development/11-ui-style-guide.md` 统一 Android UI 口径。
- 以 `doc/development/11-ui-style-guide.md` 作为唯一 UI 权威规范。
- 以 `doc/development/05-api-contract.md` 作为接口契约源；登录 + 社区第一轮样板链路继续严格追该文档。
- 把商城首页、商品详情、购物车、订单确认、订单列表和联调支付状态收敛为可演示、可联调、边界清楚的 Android UI MVP。
- 优先消除空点击、假成功 Toast、误导性支付、误导性合规入口和半成品功能暴露。

本阶段不做：

- 不接入真实 AI Provider。
- 不增强生产级内容审核、图片审核、版权识别、诈骗识别或交易风控。
- 不接入真实微信支付、支付宝支付、退款、对账或支付 SDK。
- 不补齐备案、隐私政策、用户协议、SDK 清单、版权投诉等生产合规材料。
- 不补完整玩家二手/定制交易闭环、担保、评价、纠纷、提现或卖家资质审核。
- 不新增后端公共 API；后续源码阶段优先接入已有 `/api/v1` 能力。

阶段状态以 `doc/development/current-status.md` 为准。
如果本节与 `doc/development/current-status.md` 冲突，以 `current-status.md` 和当前代码为准，并优先修正文档口径。

## 开发前必读

任何开发、重构、修复、文档更新前，必须先阅读：

1. `AGENTS.md`：本文件，项目顶层开发约束。
2. `doc/development/current-status.md`：当前阶段、完成度、不可用边界和下一步优先级。
3. `doc/development/README.md`：开发文档索引、阅读顺序和同步规则。
4. `doc/豆屿App商业技术执行计划.md`：产品目标、商业闭环和阶段方向。

按任务类型继续阅读：

| 任务 | 必读文档 |
|---|---|
| Android UI / 页面 / 状态 | `doc/development/03-android-client.md`、`doc/development/11-ui-style-guide.md`、`doc/development/10-testing-acceptance.md` |
| 后端接口 / 服务 | `doc/development/04-backend-services.md`、`doc/development/05-api-contract.md` |
| 数据模型 / 状态机 / 枚举 | `doc/development/06-data-model.md` |
| AI 拼豆图纸 | `doc/development/07-ai-pattern-generation.md`、`doc/development/15-ai-pattern-provider-selection.md` |
| 商城 / 订单 / 支付 | `doc/development/08-commerce-payment.md` |
| 权限 / 隐私 / 审核 / 未成年人 | `doc/development/09-security-compliance.md` |
| 前后端联调 | `doc/development/14-frontend-backend-collaboration.md` |

## 文档职责映射

- 任务需求和阶段任务：以 `doc/development/current-status.md` 和 `doc/development/10-testing-acceptance.md` 为准。
- API 文档和字段契约：以 `doc/development/05-api-contract.md` 为准；后端 Controller/DTO、Android DTO、Repository、UI 和测试必须与其对齐。
- 阶段未完成内容和本轮不做内容：以 `doc/development/current-status.md` 为准。
- Android 页面行为和 UI 状态：以 `doc/development/03-android-client.md` 与 `doc/development/11-ui-style-guide.md` 为准。
- 商城、订单、支付和玩家交易边界：以 `doc/development/08-commerce-payment.md` 为准。
- 调试步骤、环境配置、真机/模拟器联调和排障：以 `doc/development/14-frontend-backend-collaboration.md` 为准。
- 测试、验收、真机 QA 记录和未关闭验收项：以 `doc/development/10-testing-acceptance.md` 为准。

## 事实优先级

如果文档、代码和提示互相冲突，按以下顺序判断：

1. 当前代码和可运行配置。
2. `doc/development/current-status.md`。
3. 对应领域分册，例如 API 看 `05-api-contract.md`，Android 看 `03-android-client.md`，UI 看 `11-ui-style-guide.md`。
4. `doc/development/README.md`。
5. `doc/豆屿App商业技术执行计划.md`。

发现冲突时，不要继续扩大实现。先把冲突写清楚，并同步修正文档或向用户确认。

## 第一轮契约规则

- 第一轮只治理登录 + 社区 + 文档/契约/UI 基线。
- 登录 + 社区接口以 `doc/development/05-api-contract.md` 为唯一契约源；后端 Controller/DTO、Android DTO、Repository、UI 和测试若与它冲突，默认改代码追契约。
- 确实需要修改契约时，必须先改 `05-api-contract.md`，再同步后端、Android、测试和 `14-frontend-backend-collaboration.md`。
- 登录请求当前仍传 `ageGroup=AGE_18_PLUS`；不得写成客户端已经移除该字段。
- AI、支付、应用市场上线、备案、生产审核风控、隐私政策、SDK 清单和灰度发布材料均为后期工作，不列入第一轮验收。

## 本地环境配置

- 仓库根目录 `.env` 是本地联调唯一生效文件，由 `doyu-server/start-dev.bat` 和 Android debug Gradle 构建读取；`.env` 不提交。
- `.env.emulator` 和 `.env.phone` 只作为本机私有切换模板，按 `.gitignore` 忽略，不提交；提交模板只能是 `.env.example`。
- 当前默认开发目标是 Android 模拟器，`.env` 应优先使用：
  - `DOUYU_ANDROID_API_BASE_URL=http://10.0.2.2:8081/`
  - `DOUYU_STORAGE_BASE_URL=http://10.0.2.2:8081`
- 真机模板使用电脑当前 Wi-Fi/LAN IPv4，例如本机当前可写为：
  - `DOUYU_ANDROID_API_BASE_URL=http://10.64.241.153:8081/`
  - `DOUYU_STORAGE_BASE_URL=http://10.64.241.153:8081`
- 切换模板时只复制目标模板为 `.env`：

```powershell
Copy-Item .env.emulator .env -Force
Copy-Item .env.phone .env -Force
```

- 切换 `.env` 后必须重启后端并重新构建 debug 包。后端 Local OSS URL 在启动时读取环境变量，Android `BuildConfig.API_BASE_URL` 和 debug HTTP 白名单在 Gradle 构建期写入，不是运行时动态切换。

## ADB 真机调试

- 本项目允许使用 ADB 进行远程真机调试，默认 ADB 路径为 `D:\AndroidChace\platform-tools\adb.exe`。
- 执行任何真机 QA、安装、截图、日志或交互前，必须先运行：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
```

- 如果没有在线设备，必须先告诉用户“当前无在线真机，不能执行真机验收”，不得把设备 QA、截图或真机 smoke 写成通过。
- `.qa-output/` 只用于本地临时截图和日志，按 `.gitignore` 忽略，不提交。
- 无线调试端口、配对码和一次性设备连接信息不得写入文档或提交记录；文档只保留通用命令和排障原则。

## Android Studio / Gradle JDK

- Android Studio 使用 Gradle Wrapper 构建；Gradle JVM 必须是完整 JDK 21，必须包含 `bin\java.exe`、`bin\javac.exe` 和 `bin\jlink.exe`。
- 推荐使用 Android Studio Embedded JDK / JetBrains JBR，例如 `D:\Program Files\Android\Android Studio\jbr`，或明确选择本机完整 JDK 21。
- Android Studio 设置路径：`File > Settings > Build, Execution, Deployment > Build Tools > Gradle`。
- `Distribution` 使用 `Wrapper`；`Gradle JVM criteria` 使用 `Version 21`，`Vendor` 推荐 `JetBrains`，或直接选择完整 JDK 路径。不要保持容易误选精简 JRE 的 `Vendor: Any vendor`。
- 禁止把 VS Code Red Hat Java 扩展内置 JRE 作为 Gradle JVM。出现 `jlink executable ...\.vscode\extensions\redhat.java...\bin\jlink.exe does not exist` 时，按环境/JDK 选择问题处理，不先改业务代码。
- 标准恢复命令：

```powershell
cd DouYu
.\gradlew.bat --stop
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
```

- `.\gradlew.bat --version` 输出中的 JVM 路径不得指向 `C:\Users\Oya\.vscode\extensions\redhat.java-...`。

## Agent Team 协作规则

- 当任务明显可拆分、文件/职责边界清楚且并行安全时，Agent 可以直接执行 Agent Team：先用一句话告知用户将开启 Agent Team，然后直接派发，不再等待用户再次说“开”。
- Agent Team 必须由主 Agent 总控：拆分非重叠职责、集成结果、解决冲突、统一验证、提交和推送。
- 不得让多个 Agent 同时修改同一文件或同一职责边界；如果必须触碰同一文件，改为主 Agent 串行处理。
- 若当前运行模式、工具权限或更高优先级规则限制派发 Agent，应说明限制并改用单 Agent 执行。

## 工作纪律

- 使用中文沟通、中文文档和中文提交说明；代码标识符按技术栈使用英文。
- 先读真实代码和文档，再给判断；不要根据文件名或历史记忆猜测。
- 保持改动聚焦，不做无关重构。
- 不要擅自替换已定技术栈。
- 不要新增大框架或基础设施，除非对应开发文档先更新并说明原因。
- 不要新增计划外能力；如果确实需要，先更新产品总纲或对应分册并说明风险。
- 不能把开发态 Stub、占位能力或半成品包装成生产能力。
- 不能出现空 `onClick`、假成功 Toast、可点击但无结果的入口。
- 发现未完成能力时，优先隐藏、禁用或展示明确开发态说明。
- 如果用户明确只要求写计划或文档，不要改业务源码。
- 如果用户明确要求实现，除非存在高风险歧义，否则直接执行并完成验证。

## 当前技术边界

### Android

- 目录：`DouYu/`。
- 技术栈：Kotlin、Jetpack Compose、单 Activity、Navigation Compose、Retrofit、OkHttp、Kotlinx Serialization、Coil、CameraX、Photo Picker。
- 当前依赖装配：`DoyuAppContainer` 服务定位器；MVVM 是目标架构，不代表所有页面已完全 ViewModel 化。
- 当前问题：debug API Base URL 已支持 `.env` 构建期注入，但本地仍需在模拟器/真机模板间切换并重新构建；TokenStore 仍是内存实现，多个页面仍有半成品 UI 和未接 Repository 的功能。
- UI 权威规范：`doc/development/11-ui-style-guide.md`。
- Stitch 设计稿只作为视觉参考：`doc/stitch_document_app_generator/`。

### Backend

- 目录：`doyu-server/`。
- 技术栈：Java 21、Spring Boot、Spring Security、JWT、Spring Data JPA、Flyway、PostgreSQL、Redis。
- API 前缀：`/api/v1`。
- 后端端口：`8081`。
- PostgreSQL 宿主机端口：`5433`，容器内端口 `5432`。
- 统一响应：`{ code, message, data, traceId }`。
- 对外业务 ID 使用字符串。
- POST/PATCH/DELETE 涉及幂等时使用 `Idempotency-Key` Header。

### 外部服务状态

- 短信验证码为 Stub，开发环境固定 `123456`。
- 开发环境对象存储使用 Local OSS Provider；生产仍需接真实对象存储。
- AI 真实视觉 Provider 未完成；客户端不得直连模型供应商。
- 微信/支付宝支付仍是 Stub / 联调骨架；不能用于真实收款、退款或对账。

## UI MVP 规则

- 当前采用 MVP 收敛，不是全入口保留，也不是重做全部信息架构。
- 能真实调用已有后端并返回明确状态的入口，保留并接完整 UI。
- 后端有接口但 Android 未接的入口，标为后续 P0/P1 接入任务。
- 后端/客户端都未闭环的入口，隐藏、禁用或展示明确开发态说明。
- 支付只展示联调支付单和服务端确认状态，不包装成真实微信/支付宝支付。
- AI 只展示开发态图纸生成，不承诺真实视觉理解质量。
- 审核、合规、风控、玩家交易只保留必要状态提示，不扩展生产能力。

## 安全与合规底线

- 不得提交密钥、证书、商户私钥、API Key、访问令牌、真实用户数据。
- 不得在客户端硬编码支付密钥、AI 密钥、OSS Secret。
- 不得绕过内容审核、举报、账号注销、隐私授权、未成年人保护逻辑。
- 所有用户上传文件默认按不可信输入处理。
- 玩家交易为直连撮合，不自建余额，不沉淀资金池。
- 玩家卖家、提现、定制服务发布者需要 18+ 实名。
- 涉及订单、支付、退款、库存、实名、审核的改动必须附带测试说明。

## 文档同步

修改实现时必须同步对应文档：

- 任务需求、调试步骤、注意事项、接口变更、阶段目标、阶段未完成项和验收结论都必须同步到对应 `doc/development` 分册。
- 改变计划、新增能力、调整边界或改变验收标准时，必须先更新对应文档；若影响仓库级规则，再同步 `AGENTS.md`。
- 不允许只改代码不改文档，也不允许只在对话里说明新规则而不沉淀到文档。

| 改动范围 | 同步文档 |
|---|---|
| 当前阶段状态 | `doc/development/current-status.md` |
| Android 客户端架构或页面行为 | `doc/development/03-android-client.md` |
| UI 风格和设计系统 | `doc/development/11-ui-style-guide.md` |
| 测试和上线验收 | `doc/development/10-testing-acceptance.md` |
| API 接口 | `doc/development/05-api-contract.md` |
| 核心表、状态机、枚举 | `doc/development/06-data-model.md` |
| 后端模块、鉴权、持久化、Provider | `doc/development/04-backend-services.md` |
| AI 拼豆图纸流程 | `doc/development/07-ai-pattern-generation.md`、必要时同步 `doc/development/15-ai-pattern-provider-selection.md` |
| 支付、订单、退款、库存 | `doc/development/08-commerce-payment.md` |
| 权限、隐私、审核、未成年人 | `doc/development/09-security-compliance.md` |
| 前后端联调规则 | `doc/development/14-frontend-backend-collaboration.md` |

`doc/stitch_document_app_generator/` 是设计探索归档，必须保留，但不作为日常必读或权威规范。

## 验证要求

文档改动至少运行：

```powershell
git diff --check
rg -n "17-ui-red[e]sign|12-front[e]nd|13-back[e]nd|16-ph[a]se|18-bug[f]ix|Leaders[P]rompt" doc AGENTS.md CLAUDE.md -g "!doc/development/10-testing-acceptance.md"
rg -n '登录请求只传手机号和验证[码]|不再传 `age[G]roup`|不再传 age[G]roup' doc AGENTS.md
git check-ignore -v .env .env.emulator .env.phone
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

如果环境缺依赖或命令无法运行，必须说明未验证范围、影响和替代检查。

## 常用命令

Android：

```powershell
cd DouYu
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

后端：

```powershell
cd doyu-server
.\start-dev.bat
mvn test
mvn spring-boot:run
```

本地服务：

- API base：`http://localhost:8081/api/v1`
- Swagger UI：`http://localhost:8081/swagger-ui/index.html`
- Stub SMS code：`123456`
- Default admin：`admin / admin123`
