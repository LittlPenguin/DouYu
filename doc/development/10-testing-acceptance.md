# 10. 测试与验收

## 测试目标

当前阶段的测试目标是支撑 **第六轮 UI/品牌与主链路展示收敛**：第三轮商城 UI/API 主体验收已关闭，第四轮登录态持久化和真实图文 seed、第五轮 Aliyun OSS Provider 骨架已作为既有基线保留；本轮重点验证主链路展示、好友/私信边界、作品详情评论工具条和图片评论能力。不可用能力不能以空点击、假成功或误导性文案暴露给用户。

当前已有后端契约测试、图纸算法测试、AI Stub Provider 测试和 Android 单元测试。本文档区分：

- 第一轮登录 + 社区基线验收。
- 第二轮 UI 统一基线验收。
- 第六轮 UI/品牌与主链路展示收敛验收。
- 第四轮登录态持久化与真实图文 seed 验收。
- 第三轮商城 UI/API 收敛回归。
- 后续阶段才需要补齐的 AI、真实支付、退款对账、审核、合规和风控验收。

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

## 第二轮 UI 统一基线验收

在第一轮登录 + 社区基线稳定后，再进入第二轮 UI 统一基线验收：

- 5 个主 Tab 可以正常进入，但第二轮重点检查消息和我的。
- App Shell 视觉一致：顶部品牌栏、底部导航、卡片、按钮、Chip、状态页统一。
- 消息页对齐 Stitch `_4/screen.png`：通知/私信 Tab、列表密度、未读状态、空/错/未登录状态。
- 我的页对齐 Stitch `_3/screen.png`，但必须中文化并降低卡片嵌套。
- 360dp、390dp、430dp 宽度下文字不溢出、底部导航不挤压、卡片不重叠。
- 所有主要页面覆盖加载、空状态、失败、未登录和弱网重试。
- 不存在空 `onClick`、假成功 Toast、可点击但无结果的设置入口。
- 不可用功能必须隐藏、禁用或展示明确开发态说明。
- 消息发送若未闭环，必须表现为禁用、隐藏或明确边界，不得假装强聊天已完成。
- 我的页里的未闭环入口不得包装成生产合规已完成。
- 第一轮社区样板作为回归项保留，但不在本轮扩展新能力。

### 本轮视觉 QA 记录

> 记录日期：2026-05-22。本文只记录第二轮 UI 视觉 QA 真实状态，不替代 Android 构建、设备截图或主 Agent 的手工联调结论。

| QA 对象 | 当前状态 | 验收记录 |
|---|---|---|
| TopBar 统一基线 | 待手工验收 | 纳入本轮视觉 QA 对象；需在 360dp、390dp、430dp 下确认标题、操作区、返回/搜索入口不挤压、不重叠。 |
| Search 组件 | 待手工验收 | 纳入本轮视觉 QA 对象；需确认搜索框高度、占位文案、清除/返回入口和弱状态在主要页面表现一致。 |
| 消息未登录态 | 待手工验收 | 纳入本轮视觉 QA 对象；需确认未登录时显示登录引导，不出现空白页、空点击或假成功反馈。 |
| 退出登录返回 | 待手工验收 | 纳入本轮视觉 QA 对象；需确认退出登录后返回路径明确，受保护页面回到未登录/登录引导状态。 |
| 截图视觉 QA | 以主 Agent 实际设备输出为准 | 已定位 ADB：`D:\AndroidChace\platform-tools\adb.exe`；有在线真机时可安装 debug 包做 smoke，无在线真机时不能写成通过。 |
| Android 构建/单测 | 主 Agent 验证 | 本轮代码验证以主 Agent 实际命令输出为准；文档 Agent 不单独判定 Android 通过。 |

## 第三轮商城 UI/API 收敛验收

第三轮商城验收以已有 `/api/v1` 商品、购物车、订单和支付单接口为准，不新增后端公共 API，不接真实微信/支付宝 SDK，不把 Stub 支付包装成生产能力。

### 商品列表 / 商城首页

- 对齐 Stitch `_2/screen.png` 的页面骨架：搜索栏、分类 Chip、Banner、双列商品卡。
- 搜索未接真实后端查询时，不得承诺全站搜索；可以作为本地筛选、输入占位或明确开发态。
- 分类切换有选中态、加载态、空结果态和失败态，不出现空白页。
- Banner 不得作为空点击入口；若仅为开发态素材，应不可点击或标注边界。
- 双列商品卡展示商品图、标题、价格、库存/销量或状态；售罄/下架/无图有明确视觉状态。
- 商品列表接口失败时显示可重试错误，不吞成空列表。

### 商品详情

- 商品详情必须区分 `SELF_OPERATED`、`PLAYER_SECOND_HAND`、`PLAYER_CUSTOM_SERVICE`。
- 自营商品可选择 SKU、数量并加入购物车；数量不能超过服务端返回库存。
- 玩家二手和玩家定制不进入标准购物车，不与自营商品混单；标准加购按钮必须隐藏、禁用或替换为明确的咨询/私信后续边界。
- 库存不足、SKU 下架、商品下架时不得允许继续加购。
- 商品详情中的价格、库存、规格以服务端返回为准，不能本地伪造成功状态。

### 购物车

- 未登录进入购物车显示统一登录引导，不显示空车伪装成已登录无商品。
- 空车、加载失败、弱网、库存不足、数量修改、删除购物车项都有明确状态和反馈。
- 购物车只接受自营商品；玩家商品加入购物车时必须阻断并展示明确原因。
- 结算前需要复查库存边界；库存不足时不能继续创建订单。
- 删除和数量修改失败时保留原状态或重新拉取服务端状态，不做假成功。

### 订单确认

- `POST /orders` 当前需要 `itemIds` 和 `addressId`；地址管理未闭环时，不得伪装“默认地址”。
- 没有真实地址时，创建订单按钮必须禁用或进入明确开发态说明，不得创建订单，不得创建支付单。
- 金额和应付数据以服务端返回或明确联调数据为准；客户端不得传最终订单金额。
- 创建订单提交必须带 `Idempotency-Key`，重复点击不能创建重复订单。
- 创建订单失败要展示可读错误，必要时保留 `traceId`。

### 订单列表

- 我的订单入口可进入订单列表，并展示服务端订单状态。
- 订单状态标签与 `CREATED`、`WAITING_PAYMENT`、`PAID`、`FULFILLING`、`SHIPPED`、`COMPLETED`、`CANCELED`、`REFUNDING`、`REFUNDED` 对齐。
- 空订单、加载失败、未登录、弱网重试状态清楚。
- 订单列表不能仅凭本地支付页状态把订单改写成“已支付”。

### 支付单状态

- 支付状态页必须显式创建联调支付单，展示 `paymentId`、`orderId`、`channel`、`amountCent`、`status`。
- 当前只展示“联调支付单 / 等待服务端确认 / 服务端状态”，不得展示正式微信或支付宝渠道完成态。
- `payParams` 仍是 Stub/占位能力，不作为正式 SDK 参数验收。
- 支付最终状态必须通过 `GET /payments/{paymentId}` 或订单详情查询服务端状态。
- 支付单创建失败、查询失败、支付超时或服务端未确认时，都必须停留在明确状态，不自动伪装成功。

### 第三轮手工真机 smoke

手工 smoke 至少覆盖一台在线真机：

- 从底部 Tab 进入商城，检查搜索、分类、Banner、双列卡在 360dp、390dp、430dp 下不挤压、不重叠。
- 打开自营商品详情，选择 SKU，加入购物车，检查库存边界和成功/失败反馈。
- 打开玩家二手/定制商品详情，确认标准加购不可用且文案不承诺平台担保或真实支付。
- 未登录访问购物车时显示登录引导；登录后空车和有商品状态都可区分。
- 修改购物车数量、删除商品、库存不足时状态清楚。
- 进入订单确认页，确认地址缺口不会伪装默认地址；无真实地址时不能创建订单或支付单。
- 有可用联调订单时，显式创建支付单并查询服务端状态；页面不显示正式渠道完成态。
- 断网/弱网下列表、购物车、订单确认和支付状态显示可重试错误。

2026-05-23 真机 QA 记录：

- 真机 `AU7KVB4713000875` 已覆盖自营商品详情、短信 Stub 登录回跳、加入购物车、购物车有商品态、数量加减、删除后空车态、订单确认地址缺口和禁用下单按钮。
- 真机 QA 发现 Android 购物车契约偏差：写接口返回 `{ itemId, quantity }` 或 `{ deleted }`，不是完整 `Cart`；`GET /cart` 的 `product` 是商品摘要，不是完整 `Product`。已补 Android DTO / Repository / 单元测试。
- 后端本地 QA 种子数据已补齐自营、玩家二手、玩家定制三类商品；真机商城首页已看到玩家二手 / 定制服务商品卡。
- 后端 API smoke 已确认 `/api/v1/products` 返回三类商品；自营 SKU 可加入购物车，玩家二手/定制 SKU 通过 `409 CONFLICT` 拒绝进入标准购物车。
- 真机详情 smoke 已覆盖玩家二手和玩家定制商品：详情页展示“玩家商品暂不支持标准购物车”，底部“暂不支持加购”按钮为禁用态。
- 商城搜索/分类自动化截图受当前真机导航栈和输入法焦点影响，未作为最终证据；商城首页、购物车、订单确认已有截图证据保存在本地 `.qa-output/`，该目录不提交。

2026-05-24 订单 / 支付边界收口记录：

- Android 契约测试已覆盖 `CreateOrderRequest(itemIds, addressId)` 序列化、`Payment` 响应解析、支付渠道和支付状态枚举。
- 后端契约测试已覆盖订单幂等、支付单创建后 `CREATED` 状态、支付金额等于订单应付金额、`GET /payments/{paymentId}` 返回服务端状态。
- 玩家商品边界测试已覆盖：玩家二手 SKU 通过 `409 CONFLICT` 阻断标准购物车；不存在的玩家购物车项不能进入标准订单。
- Android 订单确认页仍保持缺地址禁用下单策略，不创建伪地址、不创建订单、不创建支付单；支付状态页只允许显式创建联调支付单。

2026-05-30 真机优先视觉 QA 收口记录：

- QA 前必须运行 `D:\AndroidChace\platform-tools\adb.exe devices -l`；无在线设备时不得把真机 smoke 或截图 QA 写成通过。
- 本轮真机优先关闭对象：商城首页、搜索/分类、商品详情、自营加购、玩家商品禁用标准购物车、购物车、订单确认地址缺口、联调支付状态边界。
- 截图和 XML 证据只保存到本地 `.qa-output/`，该目录不提交。
- 360dp、390dp、430dp 多宽度模拟器视觉 QA 不再作为当前默认流程；如后续需要多宽度覆盖，单独开任务准备设备或模拟器。
- 真机 `10.64.241.158:41551` 已在线，debug 包已安装启动到 `cn.edu.app.douyu/.MainActivity`；设备当前宽度约 `sw369dp`。
- 真机 UI 已覆盖商城首页、商品详情、自营商品未登录加购拦截、玩家二手商品禁用标准购物车；截图包括 `commerce-home.png`、`commerce-search.png`、`product-detail-self.png`、`product-detail-player.png`、`cart-items.png`。
- 搜索输入在当前真机上进入输入法组合态，未把筛选结果作为最终证据；分类/列表状态仍需后续在稳定输入条件下复查。
- API smoke 已确认自营 SKU 可加入购物车、服务端联调订单为 `WAITING_PAYMENT`、联调支付单为 `CREATED` 且查询状态来自服务端；玩家二手/定制 SKU 均通过 `409 CONFLICT` 阻断标准购物车。
- 已登录 App 内路径已追加覆盖：短信 Stub 登录、购物车有商品态、订单确认地址缺口、我的订单入口、联调支付状态页创建和查询。截图/XML 包括 `cart-auth-items.png`、`order-confirm-address-gap-auth.png`、`my-orders-with-payment-entry-final2.png`、`payment-status-before-create-final.png`、`payment-status-stub-auth-final.png`。
- 本次 QA 修正了真实联调中暴露的 Android 契约差异：订单列表增加“查看联调支付状态”入口；`SellerType` 接收后端 `SELF_OPERATED`；`addressSnapshot` 按对象解析；`POST /orders` 和 `POST /payments` 发送 `Idempotency-Key`。
- 支付状态 App 内证据：联调订单 `ord_866d2498f0064353928346c8d2405b31` 进入支付页后，显式点击创建联调支付单才出现 `paymentId=pay_1743fe0e77354b5881fc34e908ca7675`，状态为 `CREATED`，渠道为 `WECHAT_APP`，金额 `1200` 分；页面只展示服务端状态和联调边界。
- 竖屏追加记录：`MainActivity` 已锁定 `portrait`，真机即使系统自动旋转开启也按竖屏主流程验收；`dumpsys activity` 已看到 `requestedOrientation=SCREEN_ORIENTATION_PORTRAIT`。
- 真机 `10.64.241.158:42861` 追加覆盖商城搜索/分类：使用英文键盘源输入 `beads` 得到自营豆子结果，输入 `zzzz` 得到“没有找到商品 / 换个关键词试试？”空态；分类切换“材料包”展示自营商品，“成品手作”展示玩家二手商品。截图/XML 包括 `commerce-search-keyboard-source-phone.png`、`commerce-search-empty-phone.png`、`commerce-category-material-phone.png`、`commerce-category-handmade-phone.png`。
- 本轮按用户要求不启动模拟器；后续默认继续使用真机竖屏验收。

| QA 对象 | 本轮关闭标准 | 记录方式 |
|---|---|---|
| 商城首页 | 顶部栏、购物车入口、搜索框、分类 Chip、Banner、双列商品卡、底部导航不遮挡 | 已截图 `commerce-home.png` |
| 搜索 / 分类 | 输入关键词或切换分类后，列表状态清楚，不挤压、不空白遮挡 | 真机竖屏已截图 `commerce-search-keyboard-source-phone.png`、`commerce-search-empty-phone.png`、`commerce-category-material-phone.png`、`commerce-category-handmade-phone.png`；后续默认真机复查 |
| 自营商品详情 | 价格、库存、销量、SKU、数量选择、加入购物车状态正确 | 已截图 `product-detail-self.png` |
| 玩家商品详情 | 展示“不支持标准购物车”边界，加购按钮禁用，无假成功 | 已截图 `product-detail-player.png` |
| 购物车 | 未登录、空车或有商品状态清楚，数量/删除/结算入口可识别 | 已截图 `cart-items.png`、`cart-auth-items.png`；本次覆盖登录后有商品态和结算入口 |
| 订单确认 | 显示地址缺口，暂不能下单，不能创建订单或支付单 | 已截图 `order-confirm-address-gap-auth.png`；缺地址时按钮禁用，不调用创建订单或支付单 |
| 支付状态 | 只展示联调支付单和服务端状态，不展示正式渠道完成态 | 已截图 `payment-status-before-create-final.png`、`payment-status-stub-auth-final.png`；支付单需显式创建，状态来自服务端 |

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
git check-ignore -v .env .env.* .qa-output
```

验收标准：

- `.env`、`.env.*`、`.qa-output/` 均被 `.gitignore` 命中。
- `.env.example` 不应被忽略，且不得写入个人真实 IP、密钥、Token 或本机私有路径。
- 当前默认开发目标是真机，`.env` 直接维护电脑当前 Wi-Fi/LAN IPv4；不再维护 `.env.emulator` / `.env.phone` 双模板。

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
- `DOUYU_STORAGE_BASE_URL` 与当前真机联调 IP 一致。

## 源码执行阶段检查

后续开始 Android 源码重构时，至少运行：

```powershell
cd DouYu
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

第三轮商城 Android 改动仍沿用上述必跑命令；文档 Agent 不单独运行 Android 构建，最终是否通过以主 Agent 或实现 Agent 的实际命令输出为准。

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

第二轮 UI 统一基线手工联调至少覆盖：

- 登录：发送验证码、输入验证码、登录成功、退出登录。
- 退出登录返回：退出后返回路径明确，受保护页面回到未登录态或登录引导。
- 登录态：第四轮需验证登录后重启 App 仍保持登录态，退出登录后重启不恢复登录态，401 / refresh 失败会清理本机 token。
- 社区：Feed、详情、发布、评论、点赞、收藏有明确状态，作为回归项保留。
- 社区提交：发帖和评论提交后展示“审核中”，不假装立即公开。
- 消息：通知列表、私信列表、会话详情无空点击；发送未闭环时禁用或明确边界。
- 消息未登录态：未登录访问消息页时展示登录引导或明确未登录状态，不显示空列表伪装成已登录无消息。
- 我的：个人资产、收藏、订单、设置入口无空点击和假成功。
- App Shell：底部导航、TopBar、Search、卡片、Chip、按钮、状态页在 360dp、390dp、430dp 下不挤压、不重叠。
- 弱网/断网：核心页面能显示重试或明确错误，不直接空白。

第三轮商城手工联调按“第三轮商城 UI/API 收敛验收”执行。

后续阶段再补：

- AI：上传、参数选择、创建任务、进度、失败、取消、成功结果页可跑。
- AI 结果：预览图、色号清单、材料清单、保存入口清楚；加购、PDF、分享未闭环时禁用。
- 真实支付：微信/支付宝 SDK、沙箱或正式渠道、支付后返回 App、退款和对账专项验收。

## 第四轮登录态与真实图文验收

第四轮验收只关闭登录态持久化和常驻真实图文数据，不关闭真实 AI Provider、真实微信/支付宝支付、地址管理、玩家交易闭环或上线生产化。

必跑命令：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain

cd D:\Studio\SpellBean\doyu-server
mvn test
```

后端验收：

- Flyway 包含 `products.image_url` 和 `posts.cover_image_url`。
- `GET /api/v1/products` 至少返回 6 条本地 QA 商品，`imageUrl` 非空。
- `GET /api/v1/posts/feed` 至少返回 4 条常驻帖子，`coverImageUrl` 非空。
- `GET /api/v1/cart` 的 `product.imageUrl` 与商品图一致。
- `src/main/resources/static/seed/ATTRIBUTION.md` 记录图片来源和许可说明。

Android 验收：

- `DataStoreTokenStore` 保存、hydrate、clear 有单元测试。
- 登录、刷新、退出登录和 401 过期清理共用同一个 TokenStore。
- `Product.imageUrl`、`CartProductSummary.imageUrl`、`Post.coverImageUrl` DTO 可解析。
- 商品卡、商品详情、购物车项、社区 Feed 和帖子详情优先渲染真实图片；图片为空或加载失败时回退现有 swatch/拼豆占位。

真机 smoke：

- 先运行 `D:\AndroidChace\platform-tools\adb.exe devices -l`；无在线真机不得写成通过。
- 登录后强制关闭并重启 App，应保持登录态。
- 退出登录后强制关闭并重启 App，应回到未登录态。
- 社区和商城页面能显示真实 seed 图片；后端关闭或图片加载失败时不崩溃、不空白。

2026-05-30 第四轮实际记录：

- 已跑：`.\gradlew.bat --version`、`.\gradlew.bat :app:testDebugUnitTest --console=plain`、`.\gradlew.bat :app:assembleDebug --console=plain`。
- 已跑：`mvn clean test` 和 `mvn test`，后端测试 44 个通过；`/seed/**` 已纳入匿名静态资源访问契约。
- 已跑：真机 `10.64.241.158:42861` 安装启动 debug 包，前台确认为 `cn.edu.app.douyu/.MainActivity`。
- 已跑：开发后端在真机 `.env` 下返回 6 条商品和 4 条帖子，`imageUrl` / `coverImageUrl` 使用 `DOUYU_STORAGE_BASE_URL=http://10.64.241.153:8081` 拼绝对 URL；首个商品图和帖子封面图 HTTP 200 可访问。
- 截图：`.qa-output/fourth-round-after-seed-fix.png` 已保存为本地 QA 证据，不提交。
- 已跑：验证码登录手机号 `13800000088`、验证码 `123456` 成功进入“我的”已登录态；强杀 App 并重启后仍显示“豆友 / 我的工坊 / 等级与奖励”，确认 DataStore 登录恢复生效。
- 已跑：设置页退出登录后回到“我的”未登录态；再次强杀 App 并重启后仍显示“未登录 / 去登录”，确认退出登录会清理持久化 token。
- 截图：`.qa-output/login-persistence-after-restart-logged.png`、`.qa-output/login-persistence-after-restart-guest.png` 已保存为本地 QA 证据，不提交。

## 第六轮 UI / 评论图片验收

第六轮验收只关闭主链路展示收敛和作品详情图片评论，不关闭视频评论、表情包、@ 用户、图片私信、生产级图片审核、真实 AI Provider、真实支付或地址管理。

必跑命令：

```powershell
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain

cd D:\Studio\SpellBean\doyu-server
mvn test
```

作品详情评论工具条验收：

- 默认态为底部紧凑横向工具条，不遮挡帖子正文、评论列表或系统导航。
- 点击输入或图片按钮后，工具条以平滑动画横向展开；缩略图单独横向排列，不挤压文字输入框。
- 支持纯文字、纯图片和图文混合评论；文字和图片不能同时为空。
- 系统 Photo Picker 单次最多选择 9 张图片；第 10 张不能进入提交列表。
- 图片上传复用 `/uploads/presign -> PUT -> /uploads/confirm`，usage 为 `POST_IMAGE`，客户端只提交 `mediaFileIds`。
- 任意图片上传失败时整组评论不提交，已选缩略图保留，并提示用户重试。
- 评论提交后仍按审核中状态展示，不假装立即公开。
- 评论列表渲染 `mediaAssets.publicUrl`；图片加载失败时显示拼豆占位，不出现空白卡片。

后端契约验收：

- 文本评论、纯图评论、图文评论均可创建。
- 空文字 + 空图片返回参数错误。
- 超过 9 张图片返回参数错误。
- 非当前用户文件、非 `POST_IMAGE` 文件、非 `image/*` 文件不能用于评论。
- 评论列表和发布评论响应均返回 `mediaFileIds` 与 `mediaAssets`。

真机 smoke：

- 先运行 `D:\AndroidChace\platform-tools\adb.exe devices -l`；无 `10.64.241.158` 在线真机不得写成通过。
- 只使用 `10.64.241.158:<当前端口>` 安装和截图，不启动模拟器或其他设备。
- 截图保存到 `.qa-output/`，不提交。

## Android 兼容性测试

当前 UI MVP 至少覆盖：

- 360dp、390dp、430dp 宽度。
- 一台在线 Android 真机。
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

## 第二轮 UI 统一基线功能测试

当前 UI 统一基线重点覆盖：

- 注册登录。
- token 刷新。
- Feed 浏览回归。
- 发帖提交回归。
- 评论提交回归。
- 点赞。
- 收藏。
- App Shell 页面切换。
- 消息通知列表。
- 私信列表。
- 我的页入口分组。
- 弱网和断网重试。
- 未登录引导。

第二轮不把以下内容算作验收项：

- 上传图片。
- AI 任务创建、进度、成功和失败。
- 图纸保存、生成记录。
- 商品列表、商品详情、购物车、创建订单、支付单创建与查询。
- 微信支付、支付宝支付、退款申请。
- 账号注销、关注、举报闭环、等级经验。

## 第三轮商城功能测试

当前第三轮商城收敛重点覆盖：

- 商品列表和商品详情。
- 搜索栏、分类 Chip、Banner、双列商品卡。
- 自营商品 SKU 选择和加入购物车。
- 玩家二手/定制商品禁用标准购物车。
- 购物车未登录、空车、加载失败、弱网、库存不足、数量修改、删除。
- 订单确认地址缺口，不伪装默认地址，不在缺地址时创建订单或支付单。
- 订单列表状态展示。
- 显式创建联调支付单。
- 查询支付单服务端状态。
- 支付状态页不展示正式渠道完成态。

当前第三轮不把以下内容算作验收项：

- 真实微信支付或支付宝支付 SDK。
- 真实收款、退款、主动渠道查询和对账。
- 完整地址管理。
- 玩家二手/定制担保交易、评价、纠纷、提现或卖家资质审核。
- AI 材料清单一键转购物车的完整闭环。

## 后续阶段测试

以下章节属于后续阶段测试，不计入第二轮 UI 统一基线验收。

### AI 质量测试

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

### 上传测试

必须覆盖：

- 获取上传凭证。
- 图片直传成功。
- 图片直传失败。
- 上传重试。
- 文件大小超限。
- 文件类型不支持。
- 未登录点击 AI 图片上传时展示登录引导，不显示泛化网络失败。
- 上传后确认返回 `fileId`。
- 业务提交使用 `fileId`，不能把 `fileKey` 当业务 ID。

生产前再补充：

- 上传后审核拒绝。
- 私有文件不可公开访问。
- 真实对象存储验真：第五轮前置已补 Aliyun OSS Provider 单元测试和配置选择测试；2026-05-31 已用当前开发环境完成单台真机 AI_INPUT 上传到真实 Bucket 的 smoke，但生产化对象存储能力仍未关闭。
- 病毒扫描、EXIF 清理和缩略图处理。

### Aliyun OSS Provider 前置验收

第五轮前置只验收后端 Provider 骨架，不迁移 seed 图片，不改变 Android 上传 API。

已覆盖：

- `AliyunOssProviderTest`：生成 PUT 预签名 URL，返回 `Content-Type` 上传头；`publicBaseUrl + fileKey` 会按路径段编码。
- `OssProviderConfigTest`：`douyu.oss.provider` 缺省走 Local OSS，`stub` 可用于测试，`aliyun` 在配置齐全时可创建 Aliyun OSS Provider。
- `DouyuBackendContractTests`：`/api/v1/uploads/confirm` 保留 `fileId/fileKey/auditStatus`，并补齐 Android `FileAsset` 需要的 `ownerId/usage/storageKey/mimeType/sizeBytes/publicUrl` 等字段。
- `mvn test`：后端全量测试必须继续通过，确保 `/api/v1/uploads/presign` 和 `/api/v1/uploads/confirm` 契约不回退。
- 2026-05-31 真机 smoke：登录态下 AI 图片上传已通过 Aliyun OSS 预签名 PUT 和 `/uploads/confirm` 返回 `fileId`；公开 OSS URL `HEAD` 返回 200，数据库 `file_assets` 记录为 `AI_INPUT` / `NEED_MANUAL_REVIEW`。
- 2026-05-31 Android 单元测试：`AiUploadNavigationTest` 覆盖未登录上传先进入登录引导、登录后回到图片选择页、上传成功 `fileId` 可构造参数页路由。
- 2026-05-31 真机 smoke：修复 AI 参数页 `FlowRow` 运行时签名不兼容崩溃后，登录态下从 AI 上传成功页点击“继续设置参数”可进入“图纸参数”页；前台保持 `cn.edu.app.douyu/.MainActivity`，logcat 未再出现 `NoSuchMethodError` / `FATAL EXCEPTION`。

未关闭：

- Aliyun OSS 生产化：Bucket CORS 最终收敛、STS / 最小权限 RAM、CDN、防盗链、图片审核、病毒扫描、EXIF 清理和缩略图处理。
- 第四轮 seed assets 迁移到云对象存储。

### 商城支付测试

当前 UI MVP 覆盖：

- 加入购物车。
- 修改数量。
- 删除购物车项。
- 库存不足。
- 有真实地址时创建订单；地址缺口未闭环时禁止伪创建。
- 订单取消。
- 显式创建联调支付单。
- 查询支付单状态。
- 客户端显示“等待服务端确认”。
- 玩家商品不能走标准购物车。

当前支付仍是 Stub / 占位能力，真实微信/支付宝接入后必须重新执行支付、退款、回调和对账专项测试。

上线前必须覆盖：

- 微信渠道完成态。
- 支付宝渠道完成态。
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

### 安全测试

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

### 合规验收

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

### 后台验收

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

### 上线准入

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
