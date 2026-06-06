# 当前状态

> 更新日期：2026-06-06
> 用途：记录当前工程事实、UI 重构阶段、功能完成度、不可用边界和下一步优先级。产品目标和商业边界以 `../豆屿App商业技术执行计划.md` 为准。

## 阶段结论

豆屿 Doyu 当前处于 **第六轮 UI/品牌与主链路展示收敛阶段**。真机 smoke 后确认 Android 当前 UI 与 Open Design A 页面稿仍有明显差异，已新增 **Stage 8：UI 视觉对齐重构阶段**，详见 `17-ui-parity-refactor-plan.md`。

已经完成的基础阶段：

- 第一轮：登录 + 社区契约样板链路稳定。
- 第二轮：App Shell、消息、我的 UI 统一基线形成。
- 第三轮：商城 UI/API 主体链路关闭为可联调、不误导支付的 MVP。
- 第四轮：Android 登录态收敛到 DataStore，社区/商城常驻 seed 图文可渲染。
- 第五轮：后端对象存储 Provider 切换骨架完成，可通过 `.env` 在 `local|stub|aliyun` 间切换。
- 第六轮：Open Design A 方向页面稿和本地 HTML 原型已补齐；当前 Android 已按阶段落地 UI Shell、Search、Profile Edit、社区/作品详情、Settings 分区页、通知详情、消息/支付/AI 开发态边界的 MVP 收敛；Stage 8 Android 主链路进入收口，评论富内容、消息未互关 3 条限制、商城地址缺口、AI 相册开发态全链路和登录态持久化已有无线真机主证据。剩余未完成和阻塞项集中维护在 `18-unfinished-and-blockers.md`。
- Stage 8 最新真机 smoke：2026-06-04 在真机 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 上安装启动 debug 包，已复验社区标题 `社区`、社区频道 `推荐 / 关注 / 教程 / 图纸 / 新手`、社区 Banner `今日灵感 / 真实作品图优先展示`、底部导航 `社区 / 商城 / AI / 消息 / 我的`、商城首页 `精选 / 豆子 / 板子 / 工具 / 玩家` 和 `自营精选 / 新手材料补给` Banner、AI 首页 `AI 创作 / 上传图片生成拼豆图纸 / 相册上传 / 拍照 / 开发态工具`、消息页 `私信 / 通知 / 未读对话 / 通知预览`、我的页成功态 `获赞 / 作品 / 关注 / 粉丝` 和 `我的图纸 / 点赞作品 / 收藏作品`、Search `UI-only` 与 `图纸搜索待接入`、Settings `账号与安全 / 隐私与权限 / 通知设置 / 帮助、关于与合规`、上传帖子 `图片 / 正文 / 话题 / 审核前预览`、Profile Edit `头像 / 相册 / 拍照 / 年龄段 / 16+ / 城市 / 地区 / 兴趣标签`、作品详情评论栏展开态 `添加评论图片 / 提及好友 / 添加话题 / 表情 / 提交评论`。截图与 XML 记录保存在本地 `.qa-output/stage8/`，不提交；成功态 smoke 临时启动后端，验证后已停止 Spring Boot。
- 2026-06-05 Stage 8 商城专项 smoke：在真机 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 安装最新 debug 包并临时启动后端后，已复验商城首页分类和 Banner、玩家分类商品 `玩家二手拼豆成品套装` / `宠物头像定制咨询`、玩家商品详情 `玩家商品暂不支持标准购物车` / `暂不支持加购`、自营商品详情 `加入购物车`、购物车、订单确认地址缺口 `收货地址暂未接入` 和 CTA `缺少收货地址 · ¥559.20`；`logcat-stage8-commerce-order-smoke.txt` 未匹配 `FATAL EXCEPTION` 或 `AndroidRuntime`。本次启动的 Spring Boot 后端已停止。
- 2026-06-05 后端回归：执行 `mvn test` 时发现 AI 异步任务 PDF/SVG 生成日志中出现 `PatternPdfGenerator.generateSvg` 的 `Integer cannot be cast to String`。根因是 `BeadPatternEngine` 输出的材料 `hex` 可为整数，而 PDF 生成器按字符串强转；已补 `PatternPdfGeneratorTest` 复现并修复为整数 / 字符串 hex 都统一归一化，随后 `mvn -Dtest=PatternPdfGeneratorTest test` 和 `mvn test` 均通过。
- 2026-06-05 Stage 8 AI 专项 smoke：真机曾以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 在线，已安装旧包并抓取社区首页、AI 首页、AI 选择图片页和生成记录页证据；AI 首页显示 `AI 创作 / 上传图片生成拼豆图纸 / 相册上传 / 拍照 / 开发态 AI 能力`，选择图片页显示 `选择图片 / 从相册选择 / 拍照`。生成记录页在后端返回空列表时暴露正文空白问题，已用 TDD 修复为显示 `还没有生成过图纸 / 选择一张图片，开始生成你的第一张拼豆图纸吧！`，对应 `StageEightUiCopyRuleTest` 和 `assembleDebug` 已通过。随后真机以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 重新在线，已安装最新 debug 包并复验 AI 生成记录空态，证据为 `.qa-output/stage8/ai-history-empty-fixed.xml` 和 `.qa-output/stage8/ai-history-empty-fixed.png`。同日继续在真机 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 使用 Local OSS + 局域网 Base URL 完成 AI 全链路专项：`.qa-output/stage8/ai-full-localoss-photo-picker.xml` 显示系统 Photo Picker；`.qa-output/stage8/ai-full-localoss-after-photo-select.xml` 显示 `已选图片 / 上传并继续`；`.qa-output/stage8/ai-full-localoss-upload-poll.xml` 显示 `上传完成` 和 `fileId`；`.qa-output/stage8/ai-full-localoss-params-scroll2.xml` 显示参数和 `开始生成`；`.qa-output/stage8/ai-full-localoss-progress-poll.xml` 显示 `100% / 查看图纸结果`；`.qa-output/stage8/ai-full-private-fix-result-bottom.xml` 显示 `色号清单 / 材料清单 / 保存到我的图纸 / 材料购买待接入 / 加入购物车 / 去购物车`。结果页首次暴露 Android 不认识后端 `PatternAsset.status=PRIVATE`，已用 `ApiInterfaceContractTest.patternAssetDtoAcceptsPrivateStatusFromGeneratedResult` 复现并补 `ContentStatus.PRIVATE` 兼容，复测后结果页不再加载失败。
- 2026-06-05 Stage 8 登录态专项 smoke：真机以 目标真机 `10.64.241.158`（实际 ADB serial 以 `adb devices -l` 为准） 在线，后端健康检查返回 200。清理 App 数据后，`.qa-output/stage8/login-smoke-profile-guest-2.xml` 显示 `未登录 / 去登录`；登录表单 `.qa-output/stage8/login-smoke-login-form.xml` 显示 `手机号登录 / 手机号 / 验证码 / 获取验证码 / 登录并进入豆屿`；使用开发手机号 `13800138000` 和 Stub 验证码 `123456` 登录后，`.qa-output/stage8/login-smoke-after-login.xml` 显示 `获赞 / 作品 / 关注 / 粉丝 / 编辑资料` 且不再显示 `未登录 / 去登录`。随后强杀重启，`.qa-output/stage8/login-smoke-after-force-stop-restore.xml` 仍保持登录态；通过新增菜单进入 Settings 后，`.qa-output/stage8/login-smoke-logout-dialog.xml` 显示 `退出登录 / 确定要退出登录吗？ / 取消 / 确定`；确认退出后 `.qa-output/stage8/login-smoke-after-logout.xml` 回到未登录态，再次强杀重启后 `.qa-output/stage8/login-smoke-after-logout-force-stop.xml` 仍显示 `未登录 / 去登录` 且无 `编辑资料`。这关闭登录后强杀恢复和退出后强杀不恢复的真机专项。
- 2026-06-05 用户要求后续真机 QA 继续使用无线 ADB 连接；本次先执行 `D:\AndroidChace\platform-tools\adb.exe connect 10.64.241.158`，连接被目标拒绝，随后执行 `D:\AndroidChace\platform-tools\adb.exe devices -l`，已看到目标 IP 对应设备在线，同时存在一条历史 offline 连接。已使用在线无线设备安装最新 debug 包并返回 `Success`；后续真机验收继续以 `10.64.241.158` 对应在线设备为准，实际 ADB serial 以 `adb devices -l` 输出为准，仍不得记录无线端口、配对码或一次性连接信息。
- 2026-06-05 Stage 8 无线真机追加 smoke：临时启动后端后健康检查返回 `UP`；社区首页重启证据为 `.qa-output/stage8/wireless-community-restart-for-comment.xml` / `.png`，作品详情和评论栏展开态证据为 `.qa-output/stage8/wireless-comment-post-detail.xml`、`.qa-output/stage8/wireless-comment-expanded.xml`，系统 Photo Picker 打开证据为 `.qa-output/stage8/wireless-comment-picker-retry.xml` / `.png`，包含 `所有照片 / 相册 / 安全访问图库 / 添加 (0)`；尝试切到相册页时被系统短信弹窗打断，未误选用户相册图片，因此真实选图、9 图上限真选、上传失败重试和富评论提交仍未关闭。消息专项通过本地开发数据构造临时未互关会话并使用现有 `/api/v1/messages/conversations/{conversationId}` 验证：`.qa-output/stage8/wireless-message-limit-api-smoke.json` 显示第 1-3 条发送成功，第 4 条返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED`，刷新详情后 `mutualFollow=false`、`remainingNonMutualMessages=0`、`canSend=false`；Android 证据 `.qa-output/stage8/wireless-message-home-after-seed.xml` 显示 `禁发 / 超过 3 条，互相关注后可继续聊天`，`.qa-output/stage8/wireless-message-conversation-disabled.xml` 显示 3 条消息、`互相关注后可继续聊天。`、`已达未互关私信上限` 且输入框 `enabled=false`。本轮插入的临时会话和消息已从本地 dev 数据库清理。
- 2026-06-05 Stage 8 无线商城回归：`.qa-output/stage8/wireless-commerce-home.xml` 显示商城首页 `精选 / 豆子 / 板子 / 工具 / 玩家`、`自营精选`、`新手材料补给`；`.qa-output/stage8/wireless-commerce-player.xml` 显示 `玩家二手 / 定制服务 / 直连`；`.qa-output/stage8/wireless-commerce-player-detail.xml` 显示 `玩家商品暂不支持标准购物车`、`暂不支持加购`；`.qa-output/stage8/wireless-commerce-self-detail.xml` 显示自营商品 `加入购物车`；`.qa-output/stage8/wireless-commerce-after-add.xml` 显示购物车商品、数量和 `去确认订单`；`.qa-output/stage8/wireless-commerce-order-confirm.xml` 显示 `收货地址暂未接入`、`当前 UI MVP 不伪装默认地址`、`支付规则` 和 `缺少收货地址`。最近 logcat 未匹配 `FATAL EXCEPTION` 或 `AndroidRuntime`。
- 2026-06-06 Stage 8 评论富内容无线专项：继续使用无线 ADB，`adb devices -l` 可见目标真机 IP 对应设备在线；临时后端使用 Local OSS + `DOUYU_STORAGE_BASE_URL=http://10.64.241.153:8081` 启动，健康检查返回 `UP`，presign 探针返回局域网 `uploadUrl`。无线真机证据显示：`.qa-output/stage8/wireless-comment-lan-picker-open.xml` 打开系统 Photo Picker，`.qa-output/stage8/wireless-comment-lan-picker-1-selected.xml` 显示 `添加 (1)`，`.qa-output/stage8/wireless-comment-lan-after-1-selected.xml` 显示已选图片缩略图，`.qa-output/stage8/wireless-comment-lan-picker-9-selected.xml` 和 `.qa-output/stage8/wireless-comment-limit-after-9-selected.xml` 覆盖 9 图上限，`.qa-output/stage8/wireless-comment-retry-button-upload-failed.xml` 覆盖上传失败保留缩略图和 `重试上传 / 重试上传失败图片` 入口，`.qa-output/stage8/wireless-comment-lan-submit-result.xml` 显示富评论提交成功后仅提示 `评论已提交，等待审核`。本轮发现后端评论列表会返回 `REVIEWING` 评论，Android 已补 `publicCommentListItems()` 只渲染 `VISIBLE` 评论，并将全为审核中评论的空态改为 `暂无公开评论 / 评论提交后会等待审核，通过后才会公开展示。`；`.qa-output/stage8/wireless-comment-lan-filtered-comments-v2.xml` 验证提交的 smoke 评论不再出现在公开评论列表。随后通过本地 dev 数据构造一条临时 `VISIBLE` 富评论，不新增公共 API；`.qa-output/stage8/wireless-comment-visible-rich-list.xml` / `.png` 显示公开评论列表中的图片缩略图区、正文、`@系统`、`#配色灵感` 和贴纸 `喜欢`，该临时评论及关联 mention/topic/sticker 已从本地数据库清理。继续补做失败后重试专项：`.qa-output/stage8/wireless-comment-retry-success-upload-failed.xml` / `.png` 显示后端停止时上传失败仍保留缩略图、`失败`、`重试上传`、`重试上传失败图片` 和 `网络异常，请稍后再试`；恢复同一 Local OSS 后端后点击失败缩略图重试，`.qa-output/stage8/wireless-comment-retry-success-after-retry.xml` / `.png` 显示评论栏收起并提示 `评论已提交，等待审核`。未新增后端公共 API，仍使用现有 `CreateCommentRequest` 字段。
- 2026-06-06 Stage 8 主链路代码收口：Android 应用图标资源已由 `assets/TBLogo.png` 生成普通和 round launcher icon；社区点赞/收藏接口已扩展兼容 `PostInteractionResult`，返回 `liked/favorited/likeCount/favoriteCount/likedByMe/favoritedByMe`，Android 使用服务端权威计数，避免 seed 帖子点赞前 40、点赞后跳成 1；推荐 Feed、话题作品列表和作品详情在带登录态时回显 `likedByMe/favoritedByMe/followedAuthorByMe`。AI CameraX 拍照不再二次解码重写横屏图，拍照后进入与相册同一预览状态，支持左转、右转、上传并继续、取消、重拍和失败重试。消息页、上传帖子页、我的页和底部导航 icon 语义已按 Open Design A 的 P0 结构收口。后端 seed 社区图片只使用 Wikimedia Commons 等可授权来源并在 `doyu-server/src/main/resources/static/seed/ATTRIBUTION.md` 记录来源、作者、许可证和用途；未使用小红书、抖音、B 站等未授权用户图。
- 2026-06-06 Stage 8 最新无线真机回归：执行无线 ADB 前置后目标真机在线，最新 debug 包安装返回 `Success`。社区作品详情 `.qa-output/stage8/final-smoke-post-interactions.xml` 显示互动计数 `68 / 9 / 31`；点击已点赞帖子后 `.qa-output/stage8/final-smoke-post-after-like.xml` 显示取消点赞为 `67`，再次点击 `.qa-output/stage8/final-smoke-post-like-restored.xml` 恢复为 `68`，Feed 返回后 `.qa-output/stage8/final-smoke-feed-after-like.xml` 仍显示 `68`，未复现 `40 -> 1`。AI 拍照修复后 `.qa-output/stage8/final-smoke-camera-route-fix-entry.xml` 显示 CameraX 页面，`.qa-output/stage8/final-smoke-camera-route-fix-after-shot.xml` 显示 `已选图片 / 左转 / 右转 / 取消 / 重拍 / 上传并继续`，证明拍照后进入与相册一致的确认流程；物理纵横向 EXIF 对照仍未单独截图。消息页 `.qa-output/stage8/final-smoke-message.xml` 显示 `私信 / 通知 / 未读对话 / 通知预览`；我的页 `.qa-output/stage8/final-smoke-profile.xml` 显示 `获赞 / 作品 / 关注 / 粉丝` 和 `我的图纸 / 点赞作品 / 收藏作品`；上传帖子页 `.qa-output/stage8/final-smoke-post-compose-v2.xml` 显示 `上传帖子 / 发布 / 图片 / 0/9 / 正文 / 标题 / 话题 / 添加话题`。启动器桌面图标视觉仍未单独截图。
- 2026-06-06 本轮自动化收口回归：`git diff --check` 无错误，仅有 CRLF 提示；旧阶段口径搜索、登录 `ageGroup` 误写搜索无命中；`.env` / `.env.*` / `.qa-output` / `do.md` 忽略规则检查和 `doc/development/diagrams/*.svg` XML 解析均通过；`.\gradlew.bat --version` 显示 Gradle `9.3.1`、Launcher JVM `21.0.9`；`.\gradlew.bat :app:testDebugUnitTest --console=plain` 和 `.\gradlew.bat :app:assembleDebug --console=plain` 均为 `BUILD SUCCESSFUL`；`mvn test` 为 `BUILD SUCCESS`，共 54 个测试通过；无线 ADB 前置显示目标真机 `10.64.241.158` 对应设备在线。

当前公开文档入口是 `doc/development/README.md`。本地私有 Agent 规则文件、个人技能目录和临时清单不随仓库发布，不作为公开开发文档入口。

## 当前设计目标

Open Design 项目：

- 项目名：`SpellBean`
- 项目 ID：`1a79a45f-6c02-4c3f-9433-4b27b325acf5`
- 本地副本：`doc/development/open-design/index.html`

已选方向：**A：内容发现 + 创作工具平衡**。

关键目标：

- 五个主 Tab 顶部统一为“新增 icon - 页面标题 - 搜索 icon”。
- 新增菜单固定包含 Settings、AI 创作、上传帖子。
- 底部导航采用混合权威：容器样式、圆角、阴影、选中背景、短横、显隐和导航行为以当前 Android 真机 UI 为准；icon 语义和文字以 Open Design / SVG 为准，目标文案为 `社区 / 商城 / AI / 消息 / 我的`。
- 社区首页目标为双列瀑布流。
- 作品详情目标顺序为 `图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏`。
- 评论栏展开态为图片/附件预览在上，评论输入在下，工具栏在底部。
- 评论多图状态包含堆叠缩略图、上传中、失败重试、carousel 预览和 9 图上限。
- 消息页拆私信和通知；私信详情展示互关和未互关 3 条限制。
- 我的页统计目标为 `获赞 / 作品 / 关注 / 粉丝`，资产目标为 `我的图纸 / 点赞作品 / 收藏作品` 同组 Tab。
- Search 已有 Android UI-only / 局部能力聚合页；编辑资料已接现有 `PATCH /api/v1/users/me`。Settings 分区页和通知详情已注册 Android 路由，但仍只表达开发态/列表内详情边界；未来地图/真实支付/大模型生图仍为 UI-only 或待接入目标。

这些设计资产不代表 Android 当前代码已经完成落地。

## 功能完成度矩阵

| 模块            | 当前已有                                                                                                                                                                                                                                                                                                                                                           | 半成品 / 缺口                                                      | 当前策略                           |
| ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- | ------------------------------ |
| App Shell     | 5 个主 Tab、NavHost、底部导航、基础通用组件；五个主 Tab 已统一 `DoyuMainTopBar`，新增菜单接 Settings / AI / 上传帖子，搜索接 `search`；Settings 分区页已注册路由；launcher icon 已由 `assets/TBLogo.png` 生成；底栏文字固定 `社区 / 商城 / AI / 消息 / 我的`，icon 语义已追设计稿                                                                                                                                     | Settings 分区仍是开发态说明页，不代表生产合规材料已完成                           | Stage 1 已完成主顶部栏、基础路由和图标资源收口   |
| 登录            | 短信登录、验证码 Stub、refresh、logout、DataStore token hydrate；`AuthTokenStoreTest` 和 `AuthSessionManagerTest` 已覆盖本地持久化、refresh 和 logout 清理；2026-06-05 真机专项已验证登录后强杀恢复、退出登录后强杀不恢复旧会话                                                                                                                                                                                      | 真实短信、限流、风控未完成                                                 | 继续保留 `ageGroup=AGE_18_PLUS` 契约 |
| 社区            | Feed、关注 Feed、发帖、详情、评论、点赞、收藏、话题、贴纸、评论富内容字段；社区双列瀑布流已稳定 key 和 120dp-260dp 卡图高度；社区首页频道已按 Open Design A 收敛为 `推荐 / 关注 / 教程 / 图纸 / 新手`；社区首页 Banner 已按 Open Design A 收敛为 `今日灵感 / 真实作品图优先展示`；作品详情已按图片优先加入 carousel 页码、左右切换和缩略图 strip；评论区补空态/数量；纯 @/# 已禁发；Photo Picker 选图、9 图上限、Local OSS 评论图片上传提交、审核提示、审核中评论不公开、上传失败后恢复后端并点按重试成功，以及公开 `VISIBLE` 评论中的图片、@、#、贴纸展示已用无线真机复验；点赞/收藏改用服务端权威计数，推荐 Feed、话题作品列表和详情均支持登录态回显 | 后端 Post 仍只提供封面 URL 和 mediaFileIds，非封面媒体在详情 carousel 中只能用占位缩略图 | Stage 2 已完成核心规则、基础 carousel 视觉和互动回显契约 |
| 上传            | presign、PUT、confirm、FileAsset、Local/Stub/Aliyun Provider                                                                                                                                                                                                                                                                                                       | 生产 STS、CDN、防盗链、审核、缩略图未完成                                      | 继续复用现有上传链路，不新增 API             |
| AI            | 任务创建、查询、列表、取消、结果、收藏、自研拼豆算法；AI 首页已显示开发态/真实视觉 Provider 未接入提示；Stage 8 JVM 规则测试已覆盖 AI 开发态 Provider 边界、任务状态标签、历史空态和结果页材料购买待接入文案；后端 PDF/SVG 生成已兼容材料 `hex` 为整数或字符串；AI 历史空态和 `相册 -> 上传 -> 参数 -> 创建任务 -> 进度 -> 结果` 已用最新包真机复验；Android 已兼容后端图纸结果 `status=PRIVATE`；CameraX 拍照后进入相册同款确认页，支持旋转、上传、取消、重拍和失败重试，最新无线真机已覆盖拍照后确认页和操作按钮 | 真实视觉 Provider、大模型生图、生产内容安全未完成；材料自动加购、PDF 导出和带图纸发帖仍为开发态禁用能力；真实手持纵横向拍照的物理方向 / EXIF 对照仍未单独截图 | 保持开发态，不承诺真实识图质量                |
| 商城            | 商品、购物车、订单、联调支付单、支付查询；支付页显式展示 `payParams.provider=STUB` 边界；商城首页已按 Open Design A 收敛为 `精选 / 豆子 / 板子 / 工具 / 玩家` 分类、可解释 Banner 和商品卡首屏；玩家商品标准购物车禁用、订单确认缺地址 CTA、`STUB` 支付边界已有 JVM 规则测试覆盖                                                                                                                                                                              | 地址管理、真实支付、玩家交易闭环未完成；玩家二手/定制仍不进入标准购物车；订单/支付真机全链路仍需专项 QA        | 只表达联调支付、地址缺口和玩家商品边界            |
| 消息            | 通知、会话、会话详情、私信发送、互关 3 条限制；消息页已接统一主顶部栏；通知列表可进入列表内详情页；互关超限错误会刷新并禁发                                                                                                                                                                                                                                                                                                | 当前没有独立通知详情后端接口，通知详情只展示列表已返回数据；通知已读细节仍未落地                      | Stage 4 已完成核心互关限制体验和通知详情 UI 收口 |
| 我的            | 用户资料、图纸、收藏、签到、徽章、历史个人互动作品路由；我的页统计已改 `获赞 / 作品 / 关注 / 粉丝`；首屏资产区已收敛为 `我的图纸 / 点赞作品 / 收藏作品`；Profile Edit 已接已有后端资料更新接口                                                                                                                                                                                                                                               | 统计里的“获赞”暂用现有奖励点数展示，后续若需要真实获赞数需后端字段                            | 区分代码事实和目标 UI                   |
| Settings / 合规 | 设置入口、Settings 分区页、账号注销申请后端接口、合规文档要求                                                                                                                                                                                                                                                                                                                            | 隐私政策、用户协议、备案、SDK 清单、版权投诉生产材料未完成；设置分区只做开发态说明                   | 只标待补/开发态                       |
| Admin         | 后端后台 API                                                                                                                                                                                                                                                                                                                                                       | 完整运营后台前端、权限分级和生产工作台未完成                                        | 不作为 App 当前主链路                  |

## 当前代码事实

Android：

- 技术栈：Kotlin、Jetpack Compose、Navigation Compose、Retrofit、OkHttp、Kotlinx Serialization、Coil、CameraX、Photo Picker、DataStore。
- 当前底部 Tab：`community`、`commerce`、`ai`、`message`、`profile`。
- 当前已有二级路由包括登录、作品详情、发帖、Search、Profile Edit、Settings 分区页、通知详情、选图/拍照、AI 参数/进度/结果/历史、商品、购物车、订单确认、支付状态、会话、我的图纸、收藏、个人互动作品页、我的订单和设置。
- 当前没有未来地图/真实支付/大模型生图页面的 Android 真实链路；这些仍只允许作为 UI-only 说明。
- 登录态已从临时内存状态收敛到 DataStore 持久化。
- 社区主 Tab 顶部标题已按设计稿从历史品牌文案收敛为 `社区`；品牌露出保留在底栏图标、状态页和其他品牌组件中。

后端：

- 技术栈：Java 21、Spring Boot、Spring Security、JWT、JPA、Flyway、PostgreSQL、Redis。
- API 前缀：`/api/v1`。
- 后端端口：`8081`。
- PostgreSQL 宿主端口：`5433`。
- 统一响应：`{ code, message, data, traceId }`。
- 真实 Controller 覆盖 Auth、User、Upload、Community、Pattern、Commerce、Order、Payment、Message、Reward、Report、Admin。
- 支付、AI、OSS、短信等 Provider 仍有开发态或 Stub 边界。
- `PatternPdfGenerator` 已兼容 `BeadPatternEngine` 输出的整数或字符串 hex 色值，避免 AI 异步任务 PDF/SVG 生成阶段因类型不一致退回失败占位。

## 未完成与阻塞入口

以下内容不在 `current-status.md` 分散维护，统一记录在 `18-unfinished-and-blockers.md`：

- 真实 AI Provider、大模型生图、真实微信/支付宝支付、完整地址管理、地图、全局搜索后端和生产合规材料。
- 生产对象存储、生产级审核风控、玩家交易闭环、独立通知详情后端和 Settings 生产配置能力。
- 支付状态页真机链路、启动器桌面图标视觉截图和 AI 真实手持纵横向拍照的物理方向 / EXIF 对照证据。

## 当前验收风险

没有新的真机证据前，不能写成通过：

- 启动器桌面上的 App 图标视觉截图；当前只证明 TBLogo launcher 资源已生成、debug 包可构建并安装成功。
- AI 真实手持纵横向拍照的物理方向 / EXIF 对照；当前已证明拍照后进入相册同款确认页并提供旋转、上传、取消、重拍。
- 联调支付状态页真机链路；当前已补玩家商品不进标准购物车、缺地址 CTA 和 `STUB` 支付边界的 JVM 规则测试，并补了购物车 -> 订单确认地址缺口真机 smoke，但由于地址管理未闭环，不能创建真实联调支付单。
- 纯贴纸评论独立真机截图证据。
- 真机 seed/public 图片 URL 不返回 `localhost` 的最新包回归。

## 下一步优先级

P0：支付状态页前置与剩余真机证据。

- 先按无线 ADB 前置执行 `D:\AndroidChace\platform-tools\adb.exe devices -l`，只使用目标 IP `10.64.241.158` 对应在线设备。
- 已用最新 debug 包补无线真机回归：社区互动计数和回显、AI 拍照确认流程、消息/上传帖子/我的页、底部导航文案。
- 如要进一步关闭剩余证据项，需要补启动器图标视觉截图和 AI 纵横向拍照物理方向 / EXIF 对照截图。
- 支付状态页仍受地址管理缺口阻塞；地址管理未闭环时不得把创建订单、联调支付单或支付状态页写成真机通过。

P1：继续记录 Stage 8 非 P0 视觉尾项。

- Android 主页面和流程页除底部导航容器样式外严格追 Open Design A 页面稿。
- 底部导航容器样式保持当前 Android 真机 UI，不按旧设计图回退。
- Open Design HTML、SVG 和文档中的底部导航容器样式反向改成当前 Android 底栏；Android 底栏 icon 语义和文字追设计图。
- 已关闭第一批 P0 文案/首屏结构差异：社区标题、频道和 Banner、商城分类和 Banner、AI 首页标题/hero 文案、消息页 Tab 顺序、我的页统计和资产 Tab 文案。
- 已关闭第二批 P0 页面结构差异：Search UI-only / 图纸搜索边界、Settings 四分区、上传帖子图片/正文/话题/预览结构、消息未读对话和通知预览、商城 Banner 后商品卡顺序、AI 相册上传/拍照入口、我的页资产卡片流、Profile Edit 头像编辑和基础资料字段。
- 已修复真机 smoke 暴露的 AI 生成记录空白页：后端空列表成功态必须显示空态文案，不得留下正文空白。
- P1/P2 纯视觉细节继续记录到 `18-unfinished-and-blockers.md`，不阻塞本轮可验收状态。

P2：最终验证和文档关闭。

- 每轮收口后重新运行 `git diff --check`、Android 单元测试、Android debug 构建；后端被触碰时运行 `mvn test`。
- 解析 `doc/development/diagrams/*.svg`，检查 Open Design / SVG / 文档仍引用当前底栏混合规则。
- 真机 QA 前必须重新执行 `D:\AndroidChace\platform-tools\adb.exe devices -l`；没有目标 IP 对应无线真机在线时，只能记录“未验收”，不能写通过。
- 关闭阶段时同步 `10-testing-acceptance.md`、`current-status.md` 和必要的 Android/API 分册。
