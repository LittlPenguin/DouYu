# 10. 测试与验收

## 测试目标

当前阶段的测试目标是支撑 **第一轮重构基线 / UI MVP 收敛**：先确保登录 + 社区样板链路的契约、后端、Android Repository、UI 状态和文档一致，再复制到其他模块；不可用能力不能以空点击、假成功或误导性文案暴露给用户。

当前已有后端契约测试、图纸算法测试、AI Stub Provider 测试和 Android 单元测试。本文档区分：

- 当前 UI MVP 必须通过的验收。
- 源码执行阶段必须补充的 Android 检查。
- 生产上线前才需要完整补齐的支付、审核、合规和风控验收。

## 第一轮登录 + 社区基线验收

环境和构建不稳定时，不进入 UI 重构验收。先按“环境与构建验收”修正本机 `.env`、Android Studio Gradle JVM 和 debug 构建。

必须满足：

- `05-api-contract.md` 是登录 + 社区链路唯一接口事实源。
- 登录请求仍传 `ageGroup=AGE_18_PLUS`，不得写成客户端已移除该字段。
- 登录响应用户字段使用 `avatarUrl`。
- 社区接口覆盖 Feed、帖子详情、评论列表、发帖、评论、点赞/取消、收藏/取消。
- 发帖和评论提交后展示“审核中”，不假装立即公开。
- 未登录发帖、评论、点赞、收藏统一进入登录引导。
- Android 错误状态按后端 `{ code,message,traceId }` 转换，不吞异常成空白。
- `PostInteractionResult` 对齐后端 `{ liked }` / `{ favorited }` 响应。

第一轮不验收：

- 真实 AI Provider。
- 真实微信/支付宝支付、退款、对账和支付 SDK。
- 应用市场上线、备案、隐私政策、SDK 清单、生产审核风控和灰度发布材料。
- 五个 Tab 全量重构。

## 当前 UI MVP 验收

在第一轮登录 + 社区基线稳定后，再进入更广的 UI MVP 验收：

- 5 个主 Tab 可以正常进入：社区、商城、AI 创作、消息、我的。
- App Shell 视觉一致：顶部品牌栏、底部导航、卡片、按钮、Chip、状态页统一。
- 社区首页对齐 Stitch `_1/screen.png` 的主骨架：顶部品牌栏、频道 Tab、双列内容流、发布 FAB、圆角底栏。
- 商城首页对齐 Stitch `_2/screen.png`：搜索栏、品类 Chip、Banner、双列商品卡。
- AI 首页对齐 Stitch `ai/screen.png`：AI Hero、正在生成、创作历史。
- 消息页对齐 Stitch `_4/screen.png`：通知/私信 Tab、列表密度、未读状态。
- 我的页对齐 Stitch `_3/screen.png`，但必须中文化并降低卡片嵌套。
- 360dp、390dp、430dp 宽度下文字不溢出、底部导航不挤压、卡片不重叠。
- 所有主要页面覆盖加载、空状态、失败、未登录和弱网重试。
- 不存在空 `onClick`、假成功 Toast、可点击但无结果的设置入口。
- 不可用功能必须隐藏、禁用或展示明确开发态说明。
- 支付页只展示联调支付单和服务端确认状态，不显示真实微信/支付宝支付成功。
- AI 页面只表达开发态图纸生成，不承诺真实视觉理解质量。

## 文档检查

文档更新后必须执行：

```powershell
git diff --check
rg -n "17-ui-red[e]sign|12-front[e]nd|13-back[e]nd|16-ph[a]se|18-bug[f]ix|Leaders[P]rompt" doc AGENTS.md CLAUDE.md -g "!doc/development/10-testing-acceptance.md"
rg -n '登录请求只传手机号和验证[码]|不再传 `age[G]roup`|不再传 age[G]roup' doc AGENTS.md
rg -n "doyu_vit[a]lity_craft|doyu_craft_app_navig[a]tion|17_ui_red[e]sign|11_ui_style_g[u]ide" doc AGENTS.md CLAUDE.md
git status --short
```

验收标准：

- `doc/development/README.md` 的阅读顺序只指向存在且有效的文档。
- 文档不再引用已删除的 Stitch 旧路径。
- Stitch 目录只作为视觉参考和设计探索归档，不作为权威规范。
- 权威 UI 规范只指向 `doc/development/11-ui-style-guide.md`。
- `current-status.md` 能独立回答：当前阶段、已完成、未完成、本轮不做、下一步优先级。

## 环境与构建验收

UI MVP 重构前必须先确认本地环境和构建稳定。

环境文件忽略规则：

```powershell
git check-ignore -v .env .env.emulator .env.phone
```

验收标准：

- `.env`、`.env.emulator`、`.env.phone` 均被 `.gitignore` 命中。
- `.env.example` 不应被忽略，且不得写入个人真实 IP、密钥、Token 或本机私有路径。
- 当前默认开发目标是模拟器时，`.env` 应由 `.env.emulator` 复制而来；切换真机时再复制 `.env.phone`。

Android Studio 构建设置：

- 路径：`File > Settings > Build, Execution, Deployment > Build Tools > Gradle`
- `Distribution`：`Wrapper`
- `Gradle JVM criteria`：`Version 21`
- `Vendor`：推荐 `JetBrains`，或直接选择完整 JDK 21 路径，例如 `D:\Program Files\Android\Android Studio\jbr`
- 不要保持可能误选精简 JRE 的 `Vendor: Any vendor`。

Gradle JVM 验证：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat --version
```

验收标准：

- JVM 为完整 JDK/JBR 21。
- JVM 路径不得指向 `.vscode\extensions\redhat.java`。
- 出现 `jlink executable ...\.vscode\extensions\redhat.java...\bin\jlink.exe does not exist` 时，先修 Android Studio Gradle JVM，不先改业务代码。

Debug 构建验收：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:assembleDebug
```

验收标准：

- 构建成功。
- 改 `.env` 后必须重新构建 debug 包，因为 `BuildConfig.API_BASE_URL` 和 debug HTTP 白名单在构建期生成。

后端环境检查：

```powershell
cd D:\Studio\SpellBean\doyu-server
.\start-dev.bat
```

验收标准：

- 后端读取当前 `.env`。
- 服务启动在 `8081`。
- `DOUYU_STORAGE_BASE_URL` 与当前模拟器/真机模板一致。

## 源码执行阶段检查

后续开始 Android 源码重构时，至少运行：

```powershell
cd DouYu
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

后端未改时不强制跑全量后端测试；如果 UI 重构牵引 API 契约、状态机或字段解释变化，必须运行：

```powershell
cd doyu-server
mvn test
```

第一轮登录 + 社区基线必须覆盖：

```powershell
cd D:\Studio\SpellBean\doyu-server
mvn -Dtest=DouyuBackendContractTests test

cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.ApiInterfaceContractTest
.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.CommunityRepositoryContractTest
```

验收标准：

- 后端登录响应、社区发布、评论、互动、未登录、缺失资源、重复点赞/收藏均返回统一响应和 `traceId`。
- Android DTO 能解析 `avatarUrl`、默认编码 `ageGroup=AGE_18_PLUS`，并解析 `{ liked }` / `{ favorited }`。
- Repository 写操作成功返回明确状态；错误保留 `code` 与 `traceId`。

必须搜索空实现和误导性占位：

```powershell
rg -n "onClick = \\{ \\}|开发中|后续接入|TODO|placeholder|Toast" DouYu/app/src/main/java
```

搜索结果需逐条判断：

- 空点击必须删除、禁用或接真实逻辑。
- “开发中”按钮不得作为主 CTA。
- Toast 只能用于真实状态反馈，不能伪装成功。
- placeholder 可以用于骨架屏或明确无预览状态，不能替代真实内容展示。

## 手工联调场景

UI MVP 手工联调至少覆盖：

- 登录：发送验证码、输入验证码、登录成功、退出登录。
- 登录态：后续源码阶段需验证重启 App 后仍保持登录态；当前 `InMemoryTokenStore` 阶段应记录为未完成。
- 社区：Feed、详情、发布、评论、点赞、收藏有明确状态。
- 社区提交：发帖和评论提交后展示“审核中”，不假装立即公开。
- AI：上传、参数选择、创建任务、进度、失败、取消、成功结果页可跑。
- AI 结果：预览图、色号清单、材料清单、保存入口清楚；加购、PDF、分享未闭环时禁用。
- 商城：商品列表、商品详情、购物车、订单确认、订单列表可进入。
- 订单：地址未接入时有明确边界，不能伪装真实收货地址。
- 支付：支付单创建和服务端状态查询清楚，不显示真实支付成功。
- 消息：通知、私信列表、会话详情无空点击；发送未闭环时禁用。
- 我的：个人资产、收藏、订单、设置入口无空点击和假成功。
- 弱网/断网：核心页面能显示重试或明确错误，不直接空白。

## Android 兼容性测试

当前 UI MVP 至少覆盖：

- 360dp、390dp、430dp 宽度。
- Android 模拟器和一台真机。
- 相机权限允许和拒绝。
- Photo Picker 选图。
- 拍照方向修正。
- 大图压缩和上传失败。
- 弱网和断网恢复。
- 深色模式如已开启主题支持，则必须检查文字对比度。

生产前再扩展到：

- 小米、Redmi、OPPO、vivo、华为、荣耀、三星。
- 低端机、中端机、高端机。
- 多个 Android 版本。
- 支付 App 未安装、支付后返回 App 等真实支付场景。

## 功能测试

当前 UI MVP 重点覆盖：

- 注册登录。
- token 刷新。
- Feed 浏览。
- 发帖提交。
- 评论提交。
- 点赞。
- 收藏。
- 上传图片。
- AI 任务创建。
- AI 任务成功。
- AI 任务失败。
- 图纸保存。
- 生成记录。
- 商品列表。
- 商品详情。
- 购物车。
- 创建订单。
- 支付单创建与查询。
- 通知列表。
- 私信列表。
- 签到如已接入。

上线前完整功能测试再覆盖：

- 修改资料。
- 账号注销申请。
- 关注。
- 举报闭环。
- 微信支付。
- 支付宝支付。
- 退款申请。
- 私信发送真实闭环。
- 等级经验。

## AI 质量测试

当前阶段只验收开发态任务链路：

- 上传成功。
- 创建任务成功。
- 任务进度可追踪。
- 成功状态可进入图纸结果。
- 失败原因清晰。
- 取消任务可反馈。
- 额度不足等错误可读。

真实 AI Provider 接入后，再补充质量验收：

- 单人头像。
- 多人照片。
- 宠物。
- 二次元图。
- 表情包。
- 风景。
- 低清图。
- 过暗图。
- 高饱和图。
- 透明背景图。
- 违规图。
- 主体清晰、颜色不过碎、色号可购买或有替代、总豆量可信、图纸可读。

## 上传测试

必须覆盖：

- 获取上传凭证。
- 图片直传成功。
- 图片直传失败。
- 上传重试。
- 文件大小超限。
- 文件类型不支持。
- 上传后确认返回 `fileId`。
- 业务提交使用 `fileId`，不能把 `fileKey` 当业务 ID。

生产前再补充：

- 上传后审核拒绝。
- 私有文件不可公开访问。
- 真实对象存储验真。
- 病毒扫描、EXIF 清理和缩略图处理。

## 商城支付测试

当前 UI MVP 覆盖：

- 加入购物车。
- 修改数量。
- 删除购物车项。
- 库存不足。
- 创建订单。
- 订单取消。
- 创建支付单。
- 查询支付单状态。
- 客户端显示“等待服务端确认”。

当前支付仍是 Stub / 占位能力，真实微信/支付宝接入后必须重新执行支付、退款、回调和对账专项测试。

上线前必须覆盖：

- 微信支付成功。
- 支付宝支付成功。
- 支付取消。
- 支付失败。
- 客户端成功但服务端未确认。
- 回调重复。
- 回调延迟。
- 退款申请。
- 退款成功。
- 对账异常。

验收标准：

- 订单金额不能被客户端篡改。
- 支付回调必须幂等。
- 订单最终状态以服务端为准。
- 对账异常可在后台查询。

## 安全测试

当前阶段至少覆盖开发态边界：

- 未登录访问受保护页面时展示登录引导。
- 普通用户不能访问后台接口。
- 玩家商品不能走标准购物车。
- 支付 Stub 不显示为真实支付能力。

上线前必须覆盖：

- 未登录访问受保护接口。
- 普通用户访问他人订单。
- 篡改用户 ID。
- 篡改订单金额。
- 伪造支付回调。
- 重放支付回调。
- 上传脚本文件。
- 文件 Key 枚举。
- 评论刷屏。
- 私信骚扰。
- 未成年人发布玩家商品。
- 未实名用户发布定制服务。

## 合规验收

当前 UI MVP 不以生产合规完成为目标。设置页可保留入口结构，但不得表达为已经具备上线合规材料。

上线前必须具备：

- 隐私政策。
- 用户协议。
- AI 使用规则。
- 社区规范。
- 交易规则。
- 权限说明。
- 第三方 SDK 清单。
- 举报入口。
- 账号注销入口。
- 内容审核后台。
- 未成年人保护提示。
- APP 备案材料。
- 支付商户资料。
- 版权投诉入口。

## 后台验收

当前已有后台 API 骨架，但没有完整运营工作台前端。UI MVP 不把后台前端作为阻塞项。

上线前后台必须支持：

- 查询用户。
- 处理帖子审核。
- 处理评论审核。
- 处理举报。
- 查看 AI 任务。
- 查看订单。
- 查看支付记录。
- 管理商品。
- 管理 SKU。
- 管理话题。
- 配置签到和等级。
- 查看操作日志。

## 上线准入

当前 UI MVP 不等于上线准入。满足以下条件才允许灰度：

- 核心接口错误率可观测。
- 崩溃率可观测。
- AI 失败率可观测。
- 支付回调异常可告警。
- 内容举报可闭环。
- 账号注销流程可用。
- 支付沙箱或正式联调通过。
- 至少完成一轮主流 Android 机型测试。
- 隐私政策、用户协议、SDK 清单、备案和版权投诉材料完成。
