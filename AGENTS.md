# AGENTS.md

本仓库用于开发 **豆屿 Doyu**：面向中国大陆 16+ 用户的 Android 拼豆社区、AI 拼豆图纸、商城与玩家交易应用。

本文件是本项目对 AI Agent、Claude Code、Codex 和其他自动化开发工具的 **唯一顶层开发约束入口**。其他工具入口文件只能指向本文件，不得维护另一套重复规则。

## 当前阶段

当前项目处于 **第六轮 UI/品牌与主链路展示收敛阶段**。

阶段目标：

- 在第一轮登录 + 社区契约样板、第二轮 App Shell / 消息 / 我的 UI 统一基线、第三轮商城 UI/API 主体链路验收关闭、第四轮登录态持久化和常驻真实图文数据收敛、第五轮 Aliyun OSS Provider 前置骨架之后，收敛前端观感、品牌识别、主链路展示和基础互动。
- 按 `doc/stitch_document_app_generator/` 的 Stitch 设计探索稿和 `doc/development/11-ui-style-guide.md` 统一 Android UI 口径。
- 以 `doc/development/11-ui-style-guide.md` 作为唯一 UI 权威规范。
- 以 `doc/development/05-api-contract.md` 作为接口契约源；登录 + 社区第一轮样板链路继续严格追该文档。
- 保持 Android 登录态 DataStore 持久化、社区/商城真实图文 seed、第三轮商城订单/支付边界不回退。
- 品牌视觉优先落地“拼豆小岛”方向的 Logo、应用图标和 App 内品牌标识，避免版权风险素材。
- 主链路五页优先统一：社区、商城、AI、消息、我的首屏、空态、加载、错误、未登录和禁用状态必须可演示、边界清楚。
- 好友第一版采用“关注 / 互相关注”语义，不做好友申请审批；未互关私信同一发送者对同一会话最多 3 条，超过后必须后端返回明确错误，Android 清楚提示或禁用发送。
- 继续保持后端 OSS Provider 可通过 `.env` 在 `local`、`stub`、`aliyun` 间切换；Aliyun OSS 只由后端持有 AccessKey，客户端不得保存密钥。
- 继续优先消除空点击、假成功 Toast、误导性支付、误导性合规入口和半成品功能暴露。

本阶段不做：

- 不接入真实 AI Provider。
- 不接入地图 API 或真实地理服务。
- 不增强生产级内容审核、图片审核、版权识别、诈骗识别或交易风控。
- 不接入真实微信支付、支付宝支付、退款、对账或支付 SDK。
- 不补齐备案、隐私政策、用户协议、SDK 清单、版权投诉等生产合规材料。
- 不补完整玩家二手/定制交易闭环、担保、评价、纠纷、提现或卖家资质审核。
- 不把地址管理写成本轮已完成；订单确认仍只能清楚表达地址缺口。
- 不把 Aliyun OSS Provider 骨架写成生产对象存储已完成；单台真机真实 Bucket 上传 smoke 只能证明当前开发环境可联调，CORS 最终收敛、STS/最小权限、CDN、防盗链、图片审核、病毒扫描、缩略图和 seed assets 云迁移仍是后续任务。
- 不新增地图、真实支付、真实 AI Provider 相关公共 API；第六轮只允许为关注关系、互关私信限制、页面展示状态补充必要的非破坏性字段或错误码。

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
- 调试步骤、环境配置、真机联调和排障：以 `doc/development/14-frontend-backend-collaboration.md` 为准。
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
- 本项目从第五轮起默认只维护真机联调配置；不再维护 `.env.emulator` / `.env.phone` 双模板。提交模板只能是 `.env.example`。
- `.env` 使用电脑当前 Wi-Fi/LAN IPv4，例如本机当前可写为：
  - `DOUYU_ANDROID_API_BASE_URL=http://10.64.241.153:8081/`
  - `DOUYU_STORAGE_BASE_URL=http://10.64.241.153:8081`
- 需要重建本机 `.env` 时，从 `.env.example` 复制后手工改成当前 Wi-Fi/LAN IPv4：

```powershell
Copy-Item .env.example .env -Force
```

- 修改 `.env` 后必须重启后端并重新构建 debug 包。后端 Local OSS URL 在启动时读取环境变量，Android `BuildConfig.API_BASE_URL` 和 debug HTTP 白名单在 Gradle 构建期写入，不是运行时动态切换。

## ADB 真机调试

- 本项目允许使用 ADB 进行远程真机调试，默认 ADB 路径为 `D:\AndroidChace\platform-tools\adb.exe`。
- 默认只在用户指定的真机 IP `10.64.241.158` 下执行安装、启动、截图、日志和 App 内 QA；除非用户额外要求，不要运行、安装到或验收其他模拟器/真机。
- 执行任何真机 QA、安装、截图、日志或交互前，必须先运行：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
```

- 如果没有在线设备，必须先告诉用户“当前无在线真机，不能执行真机验收”，不得把设备 QA、截图或真机 smoke 写成通过。
- `.qa-output/` 只用于本地临时截图和日志，按 `.gitignore` 忽略，不提交。
- 无线调试端口、配对码和一次性设备连接信息不得写入文档或提交记录；文档只保留通用命令和排障原则。

## 本地任务清单

- 用户可能在仓库根目录放置 `do.md` 作为本地临时任务清单；该文件不是项目文件，必须按 `.gitignore` 忽略，不提交。
- 当用户说“`do.md` 下有几个任务，去做”或类似指令时，先读取 `do.md`，按清单逐项修复或新增；完成某项后，从 `do.md` 清空对应任务，保留未完成任务。
- `do.md` 中的任务仍必须遵守本文件和 `doc/development` 文档约束；如果任务与当前阶段、API 契约或安全边界冲突，先同步说明冲突并按文档优先级处理。

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

- 开发项目、阶段任务或跨模块任务时，推荐采用团队编排模式：一个主对话 / 主 Agent 扮演项目经理与 Coordinator，负责审核目标、拆分任务、定义边界、安排顺序、集成结果和统一验收。
- 当任务明显可拆分、文件 / 职责边界清楚且并行安全时，主对话可以直接下发多个 thread 进程或 Agent Team 任务：先用一句话告知用户将开启团队编排，然后直接派发，不再等待用户再次说“开”。
- 对复杂阶段任务、UI / 设计收敛、大范围 Android / 后端 / 文档联动任务，优先创建多个独立 thread 对话分工开发，避免在单条对话中积累过多 WindowsContext；如果当前工具环境不支持 thread 管理，再退回 Agent Team。
- 下发 thread 进程或 Agent Team 任务时，必须把子任务的思考等级设置为当前工具支持的最高级；如果工具不支持设置思考等级，必须在主对话中说明限制。
- 多 thread 或 Agent Team 必须由主 thread / 主 Agent 作为 Coordinator 总控：拆分非重叠职责、分发必要上下文、审查 Worker 输出、集成结果、解决冲突、统一验证、提交和推送。
- 子 thread 和 Agent Team Worker 的任务必须文件 / 职责边界清楚，且交付时报告假设、改动范围、验证结果、阻塞项和剩余风险。
- 不得让多个 Agent 同时修改同一文件或同一职责边界；如果必须触碰同一文件，改为主 Agent 串行处理。
- 多 thread / Agent Team 不是跳过验证的理由；最终仍由主 Agent 统一跑构建、测试、真机 QA 和文档同步，不能把子任务结果直接写成已验收。
- 若当前运行模式、工具权限或更高优先级规则限制创建 thread 或派发 Agent，应说明限制并改用主 Agent 串行执行。

## 工作纪律

- 使用中文沟通、中文文档和中文提交说明；代码标识符按技术栈使用英文。
- 先读真实代码和文档，再给判断；不要根据文件名或历史记忆猜测。
- 保持改动聚焦，不做无关重构。
- 不要擅自替换已定技术栈。
- 不要新增大框架或基础设施，除非对应开发文档先更新并说明原因。
- 不要新增计划外能力；如果确实需要，先更新产品总纲或对应分册并说明风险。
- 当任务需要用户协调才能更好完成时，必须直接告诉用户所需资源、原因和最小操作步骤；包括但不限于真机/后端启动、阿里云 OSS Bucket/CORS/AccessKey、账号权限、外部服务控制台配置、人工验收或业务取舍。
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
- 当前问题：debug API Base URL 已支持 `.env` 构建期注入，当前只维护真机联调配置；修改 `.env` 后仍需重新构建 debug 包。TokenStore 已收敛到 DataStore 登录态持久化，商品/帖子真实图片字段已补齐；多个页面仍有半成品 UI 和未接 Repository 的功能。
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
- 开发环境对象存储默认使用 Local OSS Provider；Aliyun OSS Provider 骨架可通过本机私有 `.env` 启用，但客户端不得保存 OSS 密钥，生产仍需补 STS/最小权限、CORS、CDN、防盗链、审核、缩略图和运维监控。
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
