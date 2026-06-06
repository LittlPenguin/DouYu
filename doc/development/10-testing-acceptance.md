# 10. 测试与验收

## 当前用途

本文件定义豆屿开发阶段的验证命令、设计验收、接口回归、Android/后端测试和真机 QA 规则。当前已进入 Android UI 主链路开发阶段；触碰 `DouYu/` 后必须运行 Android 单元测试、构建，并在真机在线时执行真机 QA。后端未改动时不强制运行 `mvn test`。

## 文档与图谱验证

只改 `doc/development` 文档和 SVG 时运行：

```powershell
git diff --check
git status --short
git diff --name-only -- DouYu doyu-server
```

SVG 可解析：

```powershell
Get-ChildItem doc\development\diagrams -Filter *.svg | ForEach-Object {
  [xml](Get-Content -Raw -Encoding UTF8 $_.FullName) | Out-Null
}
```

阶段口径检查：

```powershell
rg -n "第五轮前置 Aliyun OSS Provider 最小闭环阶[段]|当前阶段是 \*\*第五[轮]|Doyu UI Red[e]sign|doyu-ui-red[e]sign" doc/development
rg -n "doc/[s]titch_document_app_generator" doc/development
```

预期：

- 不得把第五轮写成当前阶段。
- 不得把历史 Stitch 资产写成当前可用入口；如保留历史说明，必须明确历史归档、不恢复、不依赖。

登录契约检查：

```powershell
rg -n '登录请求只传手机号和验证[码]|不再传 `age[G]roup`|不再传 age[G]roup' doc/development
```

预期无结果。当前事实是登录请求仍传 `ageGroup=AGE_18_PLUS`。

## API 文档覆盖验证

列出后端 Controller：

```powershell
rg -n "@(Get|Post|Patch|Delete)Mapping|@RequestMapping" doyu-server\src\main\java
```

列出 Android Retrofit：

```powershell
rg -n "@GET|@POST|@PATCH|@DELETE" DouYu\app\src\main\java\cn\edu\app\douyu\core\network\ApiInterfaces.kt
```

确认 API 文档模块覆盖：

```powershell
rg -n "Auth|User|Upload|Community|Pattern|Product|Cart|Order|Payment|Message|Reward|Admin" doc/development/05-api-contract.md
```

验收标准：

- `05-api-contract.md` 覆盖真实后端模块。
- 标出后端存在但 Android 未接入的接口。
- 不新增地图 API、真实支付 API、大模型生图 API、全局搜索 API。
- 支付、AI、地址管理和合规边界明确。

## Open Design / UI 蓝图验收

检查关键引用：

```powershell
rg -n "profile-edit-a.html|post-detail-comment-toolbar-a.html|stage-development-flow.svg|ui-refactor-flow.svg|获赞 / 作品 / 关注 / 粉丝|图片 -> 内容 -> 评论" doc/development
```

验收标准：

- `README.md`、`diagrams/README.md`、`16-stage-development-roadmap.md`、`13-ui-screen-blueprints.md`、本文件均能找到对应入口或规则。
- 最新我的页统计为 `获赞 / 作品 / 关注 / 粉丝`。
- 作品详情顺序为 `图片 -> 内容 -> 评论区域`。
- Profile Edit、Search、Settings 分区页、通知详情、未来能力页面均标明 UI-only、开发态或当前接口边界。
- 当前 Android 已注册 `search`、`profile_edit`、`settings_section/{section}` 和 `notification_detail/{notificationId}`；Search 必须标注无全局搜索后端，Profile Edit 只能使用现有 `PATCH /api/v1/users/me` 字段，Settings 不得表达为生产合规完成，通知详情只能展示列表已返回数据。

## 设计稿验收清单

### 全局 Shell

- 五个主 Tab 顶部统一为新增 icon、标题、搜索 icon。
- 新增菜单包含 Settings、AI 创作、上传帖子。
- 当前五个主 Tab 已使用 `DoyuMainTopBar`；底部导航仍需继续品牌 Logo 化。
- 不用 emoji 作为正式 UI icon。
- 主页面都有加载、空、错、未登录、禁用状态。

### 社区首页

- 双列瀑布流。
- 图片高度控制在 `120dp-260dp`，超出裁切。
- 卡片包含作者、标题、互动数据、图片 fallback。
- 点击卡片进入作品详情。

### 作品详情与评论

- 顺序为 `图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏`。
- 图片区是明确 carousel / gallery，有页码、滑动提示和缩略图 strip。
- 评论区是独立区域，包含列表、空态、失败态。
- 评论浮动栏展开态为图片在上、输入在下、工具栏在底部。
- 工具按钮为图片、@、#、贴纸，不出现“帖子”入口。
- 评论图片最多 9 张。
- 上传失败保留缩略图并可重试。
- 纯 @/# 不能发送。
- 评论区成功返回空列表时必须显示空态，不得空白。

### Search

- 有返回、输入、清空/取消、范围 Tab。
- 范围包含全部、作品、图纸、商品、用户、话题。
- 标注当前没有全局搜索后端 API。
- 当前 Android Search 只聚合已有局部能力，不得写成服务端全站搜索。

### 消息

- 私信和通知分区清晰。
- 通知区域不展示会话输入状态。
- 会话详情展示互关、未互关剩余 3 条、超限禁用、发送失败、加载失败、空会话。
- 服务端返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED` 后，Android 必须显示明确错误并刷新会话禁发状态。

### 我的与编辑资料

- 我的页统计为 `获赞 / 作品 / 关注 / 粉丝`。
- 资产 Tab 为 `我的图纸 / 点赞作品 / 收藏作品`。
- 最新设计稿不展示“我的订单 / 评论作品 / 关注作品”作为首屏入口。
- 编辑资料包含头像、昵称、简介、城市/地区 UI-only、兴趣标签、保存状态。
- Profile Edit 保存昵称为空必须禁用；城市/地区不得接地图 API。

### Settings 与未来能力

- Settings 包含账号与安全、隐私与权限、通知设置、内容与互动、帮助与关于、退出登录、账号注销。
- 合规入口只标待补/开发态。
- 地图、真实支付、大模型生图显著标注 UI-only。

## Android 验证

修改 `DouYu/` 后至少运行：

```powershell
cd DouYu
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Android UI 真机 QA 前置：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
```

如果没有在线设备，必须报告“当前无在线真机，不能执行真机验收”，不得写通过。

Android 验收重点：

- 登录态 DataStore：登录后强杀重启仍保持，退出后强杀重启不恢复旧登录。
- 五个主 Tab 顶部栏和底部导航。
- 社区瀑布流和作品详情。
- 评论图片上传、@/#、贴纸、9 图上限、失败重试。
- Search UI-only 标识。
- 消息私信/通知和互关 3 条限制。
- 我的页新统计、三资产 Tab、编辑资料入口状态。
- 商城、订单确认地址缺口、联调支付状态不误导。
- AI 开发态，不承诺真实视觉 Provider。

本轮已执行的 Android 自动化验证：

```powershell
cd DouYu
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

执行结果：

- 2026-06-04 本地运行均为 `BUILD SUCCESSFUL`。真机 QA 仍需按下方 ADB 前置规则单独记录截图和日志，不能用构建通过替代真机通过。
- 2026-06-05 Stage 8 收口回归：`.\gradlew.bat --version` 显示 Gradle `9.3.1`、JVM `21.0.9`；`.\gradlew.bat :app:testDebugUnitTest --console=plain` 为 `BUILD SUCCESSFUL`；`.\gradlew.bat :app:assembleDebug --console=plain` 为 `BUILD SUCCESSFUL`。
- 2026-06-05 本轮继续收口：`.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.CommunityUiRuleTest --console=plain` 为 `BUILD SUCCESSFUL`；`.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.MessageUiRuleTest --console=plain` 为 `BUILD SUCCESSFUL`；`.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.StageEightUiCopyRuleTest --tests cn.edu.app.douyu.core.ApiInterfaceContractTest --tests cn.edu.app.douyu.core.RouteTest --console=plain` 为 `BUILD SUCCESSFUL`。
- 2026-06-06 评论富内容收口：先新增 `CommunityUiRuleTest.publicCommentListOnlyShowsVisibleComments`，测试因 `publicCommentListItems` 未实现而编译失败；补 Android 公开评论过滤后，`.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.CommunityUiRuleTest --console=plain` 为 `BUILD SUCCESSFUL`。随后追加 `publicCommentEmptyStateExplainsReviewingCommentsWithoutRetry`，覆盖全为审核中评论时显示 `暂无公开评论` 且不展示重试。
- 2026-06-06 Stage 8 最终验证：`git diff --check` 无错误，仅有 CRLF 提示；旧阶段口径搜索和登录 `ageGroup` 误写搜索无命中；`git check-ignore -v .env .env.local .env.phone .qa-output do.md` 确认 `.env`、`.env.*`、`.qa-output/`、`do.md` 被忽略；`doc/development/diagrams/*.svg` XML 解析通过；`.\gradlew.bat --version` 显示 Gradle `9.3.1`、Launcher JVM `21.0.9`；`.\gradlew.bat :app:testDebugUnitTest --console=plain` 为 `BUILD SUCCESSFUL`；`.\gradlew.bat :app:assembleDebug --console=plain` 为 `BUILD SUCCESSFUL`；`mvn test` 为 `BUILD SUCCESS`，共 54 个后端测试通过；`D:\AndroidChace\platform-tools\adb.exe devices -l` 显示目标无线真机 `10.64.241.158` 对应设备在线。

2026-06-04 追加的专项自动化验证：

- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.AuthTokenStoreTest`：`BUILD SUCCESSFUL`。覆盖 DataStore token hydrate 和 `SwitchableTokenStore` 切换后清理当前持久化 Store。
- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.AuthSessionManagerTest`：`BUILD SUCCESSFUL`。覆盖短信登录保存 token、refresh 更新 token、logout 清理 token；这不能替代真机强杀恢复 smoke。
- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.feature.ai.AiUploadNavigationTest`：`BUILD SUCCESSFUL`。覆盖 AI 上传必须同时满足已登录和已选择图片、未登录上传返回 `image_select`。
- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.CommunityUiRuleTest`：`BUILD SUCCESSFUL`。覆盖纯 @/# 禁发、作品详情 carousel item 规则、失败评论图片可重试条件、上传成功状态保留 `fileId`、上传失败不伪装成功、审核中评论不进入公开评论列表、全为审核中评论时显示 `暂无公开评论`。
- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.MessageUiRuleTest`：`BUILD SUCCESSFUL`。覆盖消息 Tab 顺序、`NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED` 到禁发提示的映射、服务端 `canSend=false` 或本地超限后输入禁用。
- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.StageEightUiCopyRuleTest`：`BUILD SUCCESSFUL`。覆盖 Stage 8 首屏文案规则，并新增验证玩家二手 / 定制服务商品不进入标准购物车、订单确认缺地址 CTA 显示为 `缺少收货地址 · ¥12.80`、`payParams.provider=STUB` 明确不代表真实微信或支付宝收款；2026-06-05 追加覆盖 AI 开发态 Provider 边界文案、状态标签 `排队中 / 处理中 / 已完成 / 失败 / 已取消`、AI 历史空态 `还没有生成过图纸`、以及结果页 `材料购买待接入`，避免把自动加购、PDF 导出或带图纸发帖写成真实能力。
- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.ApiInterfaceContractTest.patternAssetDtoAcceptsPrivateStatusFromGeneratedResult`：先因 `ContentStatus.PRIVATE` 未定义失败，随后补 Android DTO 兼容并通过；覆盖后端生成图纸结果 `PatternAsset.status=PRIVATE` 不再导致结果页反序列化失败。

### 2026-06-04 Stage 8 真机 smoke 记录

设备前置：

- `D:\AndroidChace\platform-tools\adb.exe devices -l` 看到 `10.64.241.158（实际 ADB serial 以 `adb devices -l` 为准） device product:ELI-AN00 model:ELI_AN00`。
- 安装 `DouYu/app/build/outputs/apk/debug/app-debug.apk` 返回 `Success`。
- 启动 `cn.edu.app.douyu/.MainActivity` 成功。

本轮已覆盖的 UI smoke：

- 社区首页：`.qa-output/stage8/community-title-fixed.xml` 和 `.qa-output/stage8/community-title-fixed.png` 显示顶部标题为 `社区`，底部导航文案为 `社区 / 商城 / AI / 消息 / 我的`。
- 社区频道：2026-06-04 再次安装最新 debug 包并临时启动后端，`.qa-output/stage8/community-stage8-channels.xml` 和 `.qa-output/stage8/community-stage8-channels.png` 显示 `社区`、`推荐 / 关注 / 教程 / 图纸 / 新手`。
- 社区 Banner：2026-06-04 真机继续复验，`.qa-output/stage8/community-stage8-hero.xml` 和 `.qa-output/stage8/community-stage8-hero.png` 显示 `今日灵感`、`真实作品图优先展示`、`图片失败时保持卡片宽度与高度上限，回退拼豆色块占位。`；`logcat-stage8-community-hero.txt` 尾部未发现 `FATAL EXCEPTION` 或 `AndroidRuntime`。
- 作品详情评论栏：2026-06-04 安装最新 debug 包并临时启动后端，`.qa-output/stage8/post-detail-comment-stage8.xml` 和 `.qa-output/stage8/post-detail-comment-stage8.png` 显示图片优先作品详情、`作品详情`、`展开评论输入框`、`写下你的评论`；展开后 `.qa-output/stage8/comment-expanded-stage8.xml` 和 `.qa-output/stage8/comment-expanded-stage8.png` 显示 `添加评论图片`、`提及好友`、`添加话题`、`表情`、`提交评论`。这只证明评论栏结构和工具入口可见，不证明真实 Photo Picker 选择、上传失败重试或评论富内容提交已完成。
- AI 首页：`.qa-output/stage8/ai-stage8.xml` 和 `.qa-output/stage8/ai-stage8.png` 显示 `AI 创作`、`上传图片生成拼豆图纸`、`开发态工具`；`.qa-output/stage8/ai-stage8-upload-actions.xml` 和 `.qa-output/stage8/ai-stage8-upload-actions.png` 进一步显示 `相册上传`、`拍照` 和开发态边界。
- Search：`.qa-output/stage8/search-stage8.xml` 和 `.qa-output/stage8/search-stage8.png` 显示 `搜索`、`全部 / 作品 / 图纸 / 商品 / 用户 / 话题` 和 `UI-only`；`.qa-output/stage8/search-pattern-stage8.xml` 和 `.qa-output/stage8/search-pattern-stage8.png` 显示 `图纸搜索待接入`、`当前没有全局图纸搜索后端`。
- 新增菜单：`.qa-output/stage8/add-menu-stage8.xml` 和 `.qa-output/stage8/add-menu-stage8-2.xml` 显示 `设置`、`AI 创作`、`上传帖子`，入口不是空点击。
- Settings：`.qa-output/stage8/settings-stage8-2.xml` 和 `.qa-output/stage8/settings-stage8-2.png` 显示 `账号与安全`、`隐私与权限`、`通知设置`、`帮助、关于与合规`，并保留 `待补`、`UI-only` 等开发态边界。
- 上传帖子：`.qa-output/stage8/post-compose-stage8.xml` 和 `.qa-output/stage8/post-compose-stage8.png` 显示 `上传帖子`、`图片`、`正文`、`话题`；下滑后 `.qa-output/stage8/post-compose-preview-stage8.xml` 和 `.qa-output/stage8/post-compose-preview-stage8.png` 显示 `审核前预览`、`标题预览`、`正文预览会显示在这里`、`提交发布`。
- 消息首页：`.qa-output/stage8/message-stage8.xml` 和 `.qa-output/stage8/message-stage8.png` 显示 `消息`、`私信`、`通知`；后端在线复验 `.qa-output/stage8/message-stage8-latest.xml` 和 `.qa-output/stage8/message-stage8-latest.png` 显示 `未读对话`、`通知预览`。
- 我的页：首次在后端未运行时进入 `网络有点慢` 错误态，不能作为成功态证据；随后按项目服务规则临时启动后端，健康检查 `http://127.0.0.1:8081/actuator/health` 返回 200，再次真机打开我的页，`.qa-output/stage8/profile-online.xml` 和 `.qa-output/stage8/profile-online.png` 显示 `获赞 / 作品 / 关注 / 粉丝` 以及 `我的图纸 / 点赞作品 / 收藏作品`。
- `logcat-stage8-after-title-fix.txt` 和 `logcat-stage8-online-profile.txt` 的尾部检查未发现 `FATAL EXCEPTION` 或 `AndroidRuntime`。
- Profile Edit：后端在线复验 `.qa-output/stage8/profile-edit-stage8-latest.xml` 和 `.qa-output/stage8/profile-edit-stage8-latest.png` 显示 `编辑资料`、`头像`、`相册`、`拍照`、`年龄段`、`16+`、`城市 / 地区`、`兴趣标签`。
- 商城首页：2026-06-04 再次临时启动后端并安装最新 debug 包，`.qa-output/stage8/commerce-stage8.xml`、`.qa-output/stage8/commerce-stage8.png`、`.qa-output/stage8/commerce-stage8-after-community.xml` 和 `.qa-output/stage8/commerce-stage8-after-community.png` 显示 `商城`、`精选 / 豆子 / 板子 / 工具 / 玩家`、`自营精选`、`新手材料补给`、`只展示真实可解释活动，点击路径必须存在。`、`查看购物车`；后端在线复验 `.qa-output/stage8/commerce-stage8-latest.xml` 和 `.qa-output/stage8/commerce-stage8-latest.png` 继续显示 `商城`、`精选 / 豆子 / 板子 / 工具 / 玩家`、`自营精选`、`新手材料补给`；`logcat-stage8-commerce.txt` 和 `logcat-stage8-community-commerce.txt` 尾部未发现 `FATAL EXCEPTION` 或 `AndroidRuntime`。
- 商城边界专项：2026-06-05 安装最新 debug 包并临时启动后端，`.qa-output/stage8/commerce-order-smoke-home.xml` / `.png` 显示商城首页 `商城`、`精选 / 豆子 / 板子 / 工具 / 玩家`、`自营精选`、`新手材料补给`、`查看购物车`；`.qa-output/stage8/commerce-order-smoke-player.xml` / `.png` 显示玩家分类 `玩家二手`、`定制服务`、`玩家二手拼豆成品套装`、`宠物头像定制咨询` 和 `直连`；`.qa-output/stage8/commerce-order-smoke-player-detail.xml` / `.png` 显示 `玩家商品暂不支持标准购物车`、`当前阶段不伪装玩家交易闭环` 和 `暂不支持加购`，未出现 `加入购物车`；`.qa-output/stage8/commerce-order-smoke-self-detail.xml` / `.png` 显示自营商品 `自营商品可加入购物车` 和 `加入购物车`；`.qa-output/stage8/commerce-order-smoke-after-add.xml` / `.png` 显示购物车商品、应付金额和 `去确认订单`；`.qa-output/stage8/commerce-order-smoke-order-confirm.xml` / `.png` 显示 `收货地址暂未接入`、`当前 UI MVP 不伪装默认地址`、`支付规则` 和 `缺少收货地址 · ¥559.20`。`logcat-stage8-commerce-order-smoke.txt` 未匹配 `FATAL EXCEPTION` 或 `AndroidRuntime`。
- AI 专项：2026-06-05 真机先以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 在线，旧包 smoke 已抓取 `.qa-output/stage8/ai-smoke-reconnected-42351-ai.xml` / `.png`、`.qa-output/stage8/ai-smoke-reconnected-42351-image-select.xml` / `.png` 和 `.qa-output/stage8/ai-smoke-reconnected-42351-history.xml` / `.png`。AI 首页显示 `AI 创作`、`上传图片生成拼豆图纸`、`相册上传`、`拍照`、`开发态 AI 能力`，选择图片页显示 `选择图片`、`从相册选择`、`拍照`；生成记录页暴露正文空白问题。修复后真机以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 重新在线，安装最新 debug 包返回 `Success`，`.qa-output/stage8/ai-history-empty-fixed.xml` / `.png` 显示 `生成记录`、`还没有生成过图纸`、`选择一张图片，开始生成你的第一张拼豆图纸吧！`。随后真机以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 在线，临时启动 Local OSS 后端并使用局域网 Base URL，完成 `相册 -> 上传 -> 参数 -> 创建任务 -> 进度 -> 结果` 全链路：`.qa-output/stage8/ai-full-localoss-photo-picker.xml` 显示系统 Photo Picker；`.qa-output/stage8/ai-full-localoss-after-photo-select.xml` 显示 `已选图片 / 上传并继续`；`.qa-output/stage8/ai-full-localoss-upload-poll.xml` 显示 `上传完成` 和 `fileId`；`.qa-output/stage8/ai-full-localoss-params-scroll2.xml` 显示参数区与 `开始生成`；`.qa-output/stage8/ai-full-localoss-progress-poll.xml` 显示 `100% / 查看图纸结果`；修复 `PatternAsset.status=PRIVATE` 后，`.qa-output/stage8/ai-full-private-fix-result.xml` 显示 `拼豆图纸 / 色号清单`，`.qa-output/stage8/ai-full-private-fix-result-bottom.xml` 显示 `材料清单 / 保存到我的图纸 / 材料购买待接入 / 加入购物车 / 去购物车`。这关闭 AI 开发态全链路真机专项，但不代表真实视觉 Provider、大模型生图、自动材料加购、PDF 导出或带图纸发帖已接入。
- 评论区追加 smoke：2026-06-05 在真机 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 进入帖子详情，`.qa-output/stage8/comment-post-detail.xml` 显示作品详情图片优先、正文和底部评论栏；下滑后 `.qa-output/stage8/comment-section.xml` 显示 `评论 / 9 条 · 新评论会按后端审核状态展示` 和评论列表内容。随后准备展开评论栏时 ADB 无线调试变为 offline，无法继续执行真实 Photo Picker、9 图上限、上传失败重试或富评论提交专项；这些仍不得写成通过。
- 登录态专项：2026-06-05 真机以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 在线，后端健康检查返回 200，使用最新 debug 包完成登录态持久化 smoke。清理 App 数据后，`.qa-output/stage8/login-smoke-profile-guest-2.xml` / `.png` 显示 `未登录`、`去登录` 和 `登录态可在重启后恢复`；登录表单 `.qa-output/stage8/login-smoke-login-form.xml` / `.png` 显示 `手机号登录`、`手机号`、`验证码`、`获取验证码`、`登录并进入豆屿`。使用开发手机号 `13800138000` 与 Stub 验证码 `123456` 登录后，`.qa-output/stage8/login-smoke-after-login.xml` / `.png` 显示 `获赞 / 作品 / 关注 / 粉丝`、`编辑资料`、`我的图纸 / 点赞作品 / 收藏作品`，未显示 `未登录 / 去登录`。强杀重启后，`.qa-output/stage8/login-smoke-after-force-stop-restore.xml` / `.png` 仍保持登录态；通过新增菜单进入 Settings，`.qa-output/stage8/login-smoke-plus-menu.xml` 显示 `设置 / AI 创作 / 上传帖子`，`.qa-output/stage8/login-smoke-settings.xml` 和 `.qa-output/stage8/login-smoke-settings-bottom.xml` 显示 `退出登录`，`.qa-output/stage8/login-smoke-logout-dialog.xml` / `.png` 显示 `退出登录 / 确定要退出登录吗？ / 取消 / 确定`。确认退出后，`.qa-output/stage8/login-smoke-after-logout.xml` / `.png` 回到 `未登录 / 去登录` 且无 `编辑资料`；再次强杀重启后，`.qa-output/stage8/login-smoke-after-logout-force-stop.xml` / `.png` 仍是未登录态。这证明 DataStore 登录恢复和退出清理在真机上通过。
- 无线 ADB 恢复记录：2026-06-05 用户要求后续真机 QA 继续使用无线连接。本轮先执行 `D:\AndroidChace\platform-tools\adb.exe connect 10.64.241.158`，连接被目标拒绝；随后执行 `D:\AndroidChace\platform-tools\adb.exe devices -l`，目标 IP 对应设备在线，同时存在一条历史 offline 连接。已使用在线无线设备安装最新 debug 包，安装返回 `Success`。本条只证明无线 ADB 设备链路与 APK 安装可用；无线调试端口、配对码和一次性连接信息不写入文档。
- 评论富内容无线追加：2026-06-05 临时启动后端后健康检查返回 `UP`，无线真机重启 App 并进入社区。`.qa-output/stage8/wireless-community-restart-for-comment.xml` / `.png` 显示社区首页；`.qa-output/stage8/wireless-comment-post-detail.xml` 和 `.qa-output/stage8/wireless-comment-expanded.xml` 显示作品详情、底部评论栏展开态、`添加评论图片 / 提及好友 / 添加话题 / 表情 / 提交评论`。触发评论图片入口后，`.qa-output/stage8/wireless-comment-picker-retry.xml` / `.png` 显示系统 Photo Picker，包含 `所有照片 / 相册 / 安全访问图库 / 添加 (0)`；尝试切到相册页时被系统短信弹窗打断，未误选用户相册图片。因此本轮只覆盖 Photo Picker 打开，不覆盖真实选图、9 图上限真选、上传失败重试或富评论提交。
- 评论富内容无线专项收口：2026-06-06 继续使用无线 ADB；`D:\AndroidChace\platform-tools\adb.exe devices -l` 曾短暂为空，随后 `adb connect` 恢复到目标真机 IP 对应在线设备，仍不切有线或模拟器。临时启动 Local OSS 后端前，PostgreSQL / Redis 容器处于退出状态，已用 `docker compose up -d postgres redis` 恢复开发依赖；Spring Boot 使用 `DOUYU_OSS_PROVIDER=local`、`DOUYU_STORAGE_BASE_URL=http://10.64.241.153:8081` 启动，健康检查返回 `UP`，presign 探针返回 `http://10.64.241.153:8081/uploads/temp/...` 局域网 URL。无线真机证据：`.qa-output/stage8/wireless-comment-lan-picker-open.xml` / `.png` 显示系统 Photo Picker；`.qa-output/stage8/wireless-comment-lan-picker-1-selected.xml` / `.png` 显示 `添加 (1)`；`.qa-output/stage8/wireless-comment-lan-after-1-selected.xml` / `.png` 显示 App 中已选图片缩略图和 `继续添加图片`；`.qa-output/stage8/wireless-comment-lan-picker-9-selected.xml` 显示 `添加 (9)`，`.qa-output/stage8/wireless-comment-limit-after-9-selected.xml` / `.png` 显示 `9/9 已达上限`；`.qa-output/stage8/wireless-comment-retry-button-upload-failed.xml` / `.png` 显示上传失败保留缩略图、`失败`、`重试上传` 和 `重试上传失败图片`；`.qa-output/stage8/wireless-comment-lan-before-submit.xml` / `.png` 显示 1 张图片和评论草稿；`.qa-output/stage8/wireless-comment-lan-submit-result.xml` / `.png` 显示提交后 `评论已提交，等待审核`。提交后滚到评论区曾暴露审核中 smoke 评论被公开列表渲染，修复后 `.qa-output/stage8/wireless-comment-lan-filtered-comments-v2.xml` / `.png` 显示 `暂无公开评论` 和 `评论提交后会等待审核，通过后才会公开展示。`，且 XML 检查不含提交的 `stage/wireless/comment` smoke 文本。随后通过本地 dev 数据库临时构造 `VISIBLE` 富评论用于 seed 展示，不新增公共 API；`.qa-output/stage8/wireless-comment-visible-rich-list.xml` / `.png` 显示公开评论列表中包含评论正文、图片缩略图区、`@系统`、`#配色灵感` 和贴纸 `喜欢`。该临时评论及 mention/topic/sticker 关联已在取证后清理，帖子评论数已回算。继续补做失败后重试专项：`.qa-output/stage8/wireless-comment-retry-success-upload-failed.xml` / `.png` 显示后端停止时上传失败仍保留缩略图、`失败`、`重试上传`、`重试上传失败图片` 和 `网络异常，请稍后再试`；恢复同一 Local OSS 后端后点击失败缩略图重试，`.qa-output/stage8/wireless-comment-retry-success-after-retry.xml` / `.png` 显示评论栏收起并提示 `评论已提交，等待审核`。
- 消息未互关 3 条限制无线专项：2026-06-05 使用本地 dev 数据构造临时未互关会话，但不新增公共 API；通过现有 `/api/v1/messages/conversations/{conversationId}` 接口发送私信。`.qa-output/stage8/wireless-message-limit-api-smoke.json` 显示第 1-3 条返回 `OK`，第 4 条返回 HTTP 409，错误码 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`，刷新详情后 `mutualFollow=false`、`remainingNonMutualMessages=0`、`canSend=false`。Android 证据 `.qa-output/stage8/wireless-message-home-after-seed.xml` / `.png` 显示消息首页私信/通知分区、会话 `禁发` 和 `超过 3 条，互相关注后可继续聊天`；`.qa-output/stage8/wireless-message-conversation-disabled.xml` / `.png` 显示 3 条已发消息、提示 `互相关注后可继续聊天。`、输入框文案 `已达未互关私信上限`，且输入框 `enabled=false`。本轮插入的临时会话和消息已从本地 dev 数据库清理。
- 商城边界无线回归：2026-06-05 `.qa-output/stage8/wireless-commerce-home.xml` / `.png` 显示商城首页 `精选 / 豆子 / 板子 / 工具 / 玩家`、`自营精选`、`新手材料补给`；`.qa-output/stage8/wireless-commerce-player.xml` / `.png` 显示玩家分类 `玩家二手 / 定制服务 / 直连`；`.qa-output/stage8/wireless-commerce-player-detail.xml` / `.png` 显示 `玩家商品暂不支持标准购物车` 和 `暂不支持加购`；`.qa-output/stage8/wireless-commerce-self-detail.xml` / `.png` 显示自营商品 `加入购物车`；`.qa-output/stage8/wireless-commerce-after-add.xml` / `.png` 显示购物车商品、数量和 `去确认订单`；`.qa-output/stage8/wireless-commerce-order-confirm.xml` / `.png` 显示 `收货地址暂未接入`、`当前 UI MVP 不伪装默认地址`、`支付规则` 和 `缺少收货地址`。最近 logcat 未匹配 `FATAL EXCEPTION` 或 `AndroidRuntime`。
- 最新主链路无线回归：2026-06-06 执行无线 ADB 前置后目标真机 IP 对应设备在线，安装 `DouYu/app/build/outputs/apk/debug/app-debug.apk` 返回 `Success`，临时 Spring Boot 后端健康检查为 `UP`。`.qa-output/stage8/final-smoke-launch.xml` / `.png` 显示社区首页、频道 `推荐 / 关注 / 教程 / 图纸 / 新手`、`今日灵感` 和底部导航 `社区 / 商城 / AI / 消息 / 我的`。社区互动回归中，`.qa-output/stage8/final-smoke-post-interactions.xml` / `.png` 显示帖子详情互动计数 `68 / 9 / 31`；点击已点赞帖子后 `.qa-output/stage8/final-smoke-post-after-like.xml` / `.png` 显示取消点赞为 `67`，再次点击 `.qa-output/stage8/final-smoke-post-like-restored.xml` / `.png` 恢复为 `68`，`.qa-output/stage8/final-smoke-feed-after-like.xml` / `.png` 返回 Feed 后仍显示 `68`，未复现点赞前后跳成 `1`。AI 首页 `.qa-output/stage8/final-smoke-ai-home.xml` / `.png` 显示 `相册上传 / 拍照 / 开发态 AI 能力`；修复前 `.qa-output/stage8/final-smoke-camera-after-shot.xml` / `.png` 记录了拍照后误回 AI 首页的问题，修复后 `.qa-output/stage8/final-smoke-camera-route-fix-entry.xml` / `.png` 显示 CameraX 页面，`.qa-output/stage8/final-smoke-camera-route-fix-after-shot.xml` / `.png` 显示拍照后进入 `选择图片` 确认页，并包含 `已选图片 / 左转 / 右转 / 取消 / 重拍 / 上传并继续`。这关闭拍照后确认流程，不等同于真实手持纵横向 EXIF 对照已通过。消息页 `.qa-output/stage8/final-smoke-message.xml` / `.png` 显示 `私信 / 通知 / 新消息进入私信 / 未读对话 / 通知预览`；我的页 `.qa-output/stage8/final-smoke-profile.xml` / `.png` 显示头像区、`获赞 / 作品 / 关注 / 粉丝` 和 `我的图纸 / 点赞作品 / 收藏作品`；上传帖子入口 `.qa-output/stage8/final-smoke-add-menu.xml` / `.png` 显示 `设置 / AI 创作 / 上传帖子`，上传帖子页 `.qa-output/stage8/final-smoke-post-compose-v2.xml` / `.png` 显示 `上传帖子 / 发布 / 图片 / 0/9 / 正文 / 标题 / 话题 / 添加话题`。启动器桌面图标视觉未单独截图，不能写成 launcher 桌面视觉验收通过。

本轮服务处理：

- 后端和 Docker 依赖只为真机 smoke 临时启动。
- 验证完成后已停止本次启动的 Spring Boot 后端；复查 `http://127.0.0.1:8081/actuator/health` 已不可连接。
- Docker PostgreSQL / Redis 容器由 `start-dev.bat` 拉起，保留为本地开发依赖状态；如需清理可在 `doyu-server/` 执行 `docker compose stop postgres redis`。

未覆盖范围：

- 本轮 Stage 8 smoke 覆盖主 Tab 首屏关键文案、底栏文案、Search 图纸 UI-only 边界、Settings 四分区、上传帖子页面结构、作品详情评论栏工具入口、系统 Photo Picker 打开态、消息未互关超限接口与 Android 禁用态、消息未读/通知预览、商城首屏、玩家商品购物车禁用、自营购物车、订单确认地址缺口、我的页成功态、Profile Edit 首屏字段、登录态强杀恢复/退出清理、社区点赞计数不跳变和回显、AI 拍照后确认页、消息/上传帖子/我的页最新 P0 结构。
- 评论图片 Photo Picker 真实选图、9 图上限、Local OSS 上传提交、提交后审核提示、审核中评论不公开、上传失败保留缩略图、恢复后端后点按重试并成功提交、公开评论列表图片、@、#、贴纸展示已用无线真机覆盖。支付状态和支付状态页链路仍需在无线真机在线且有合法联调支付单时按专项 QA 单独验收；这些未覆盖项不应写成已通过。
- Stage 8 商城边界已有 JVM 规则测试和无线真机购物车 -> 订单确认地址缺口 smoke；由于地址管理未闭环会阻止创建订单和支付单，联调支付状态页真机链路仍未关闭，不能写成真实支付或支付状态通过。
- 启动器桌面图标视觉和真实手持纵横向拍照 EXIF / 像素方向仍未单独取证；当前只证明 TBLogo launcher 资源已生成、debug 包可构建并安装成功，以及拍照后会进入可旋转/上传/取消/重拍的确认流程。

## 后端验证

修改 `doyu-server/` 后至少运行：

```powershell
cd doyu-server
mvn test
```

2026-06-05 后端回归记录：

- `mvn -Dtest=PatternPdfGeneratorTest test`：先用新增测试复现 `PatternPdfGenerator.generateSvg` 对整数 `hex` 强转字符串导致的失败；修复后为 `BUILD SUCCESS`。
- `mvn test`：修复后为 `BUILD SUCCESS`，共 53 个测试通过；输出中未再出现 AI PDF/SVG 生成的 `Integer cannot be cast to String`。
- 2026-06-05 Stage 8 收口再次执行 `mvn test`：`BUILD SUCCESS`，共 53 个测试通过。
- 2026-06-06 Stage 8 最终验证再次执行 `mvn test`：`BUILD SUCCESS`，共 54 个测试通过。

后端接口 smoke 建议覆盖：

- Auth：短信验证码、登录、刷新、退出。
- Upload：presign、PUT、confirm。
- Community：Feed、发帖、详情、评论、点赞、收藏、话题、贴纸。
- User：me、search、follow、unfollow。
- Message：notifications、conversations、conversation detail、send message、超限错误。
- Commerce：products、cart、orders。
- Payment：create payment、query payment。
- Pattern：create job、job detail、cancel。

## 不能写成通过的能力

除非有新的命令输出、截图或测试记录，否则不能把以下能力写成已验收：

- 真实 AI Provider。
- 地图 API。
- 真实微信/支付宝支付。
- 完整地址管理。
- 生产合规材料。
- 生产对象存储。
- 玩家交易闭环。
- Search 全局后端。
- Settings 生产配置能力和合规材料。
- 独立通知详情后端接口、通知已读回写和通知跳转目标。

## 阶段关闭标准

一个阶段关闭必须同时满足：

- 代码改动对应测试或构建通过。
- 文档、API 契约、UI 蓝图、流程图同步。
- 未完成能力被隐藏、禁用或标开发态。
- 真机 QA 有设备在线记录和关键截图/日志；无法执行则明确说明未验证范围。
- `current-status.md` 更新阶段结论、剩余风险和下一步优先级。
