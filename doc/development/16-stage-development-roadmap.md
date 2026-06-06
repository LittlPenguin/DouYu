# 16. 阶段开发路线图

## 目标

本路线图用于下一阶段按设计图重构豆屿 UI、对齐 API 契约、关闭主链路验收。它不是本轮文档重构的开发代码计划；当前阶段只沉淀流程、边界和验收标准。

执行原则：

- 先文档/API/图谱对齐，再改 Android UI。
- 不新增后端公共 API，除非先改 `05-api-contract.md` 并完成后端、Android、测试同步。
- 不把 Open Design、HTML、SVG 写成当前 Android 已实现事实。
- 每个阶段完成后必须更新对应文档和验收记录。

## 总览

| Stage | 名称                              | 主要输出                                                   | 是否改业务代码             |
| ----- | ------------------------------- | ------------------------------------------------------ | ------------------- |
| 0     | 文档/API/图谱对齐                     | 开发分册、API 契约、流程图、验收清单                                   | 否                   |
| 1     | UI Shell 与设计系统落地                | 顶部栏、当前 Android 底部导航容器样式、状态页、禁用态                        | 是，Android           |
| 2     | 社区首页与作品详情重构                     | 瀑布流、图片优先详情、评论区、悬浮评论栏                                   | 是，Android           |
| 3     | 发帖、Search、Settings、Profile Edit | UI-only 与真实接入边界、已接 API 页面                              | 是，Android；原则上不改后端   |
| 4     | 消息/私信/通知                        | 私信/通知分区、会话详情、互关 3 条限制                                  | 是，Android；必要时只修文档契约 |
| 5     | 商城/订单/联调支付边界                    | 商品、购物车、订单确认、支付状态不误导                                    | 是，Android           |
| 6     | AI 开发态和未来能力边界                   | AI 任务状态、地图/真实支付/生图 UI-only 标识                          | 是，Android           |
| 7     | QA 验收关闭                         | 真机 QA、接口回归、文档关闭记录                                      | 不限，按发现问题决定          |
| 8     | UI 视觉对齐重构                       | Android 严格追 Open Design；底部导航样式追现有 Android、icon 与文字追设计图 | 是，Android + 设计资产    |

## Stage 0：文档/API/图谱对齐

目标：只改文档与 SVG，把公开开发文档改成可交接、可执行、可验证。

输入：

- 当前后端 Controller。
- Android `ApiInterfaces.kt`、`Models.kt`、`Routes.kt`。
- Open Design A 方向页面稿。
- `current-status.md` 当前事实。

输出：

- 重写 `README.md`、`00-project-handoff.md`、`02-architecture.md`、`03-android-client.md`、`04-backend-services.md`、`05-api-contract.md`、`10-testing-acceptance.md`、`12-feature-and-flow-map.md`、`13-ui-screen-blueprints.md`。
- 新增本文件。
- 重写 `doc/development/diagrams/*.svg`，新增 `stage-development-flow.svg` 和 `ui-refactor-flow.svg`。

退出标准：

- `05-api-contract.md` 覆盖真实 Controller 与 Retrofit 模块。
- 公开文档不依赖本地私有 Agent 文件。
- 图谱与最新 UI 目标一致。
- `git diff --name-only -- DouYu doyu-server` 无输出。

## Stage 1：UI Shell 与设计系统落地

目标：先统一 App 壳层，避免后续各页面重复造顶部栏、底栏和状态页。

开发范围：

- 新增或收敛 `DoyuTopBar`：左侧新增 icon，中间标题，右侧搜索 icon。
- 新增快捷菜单：Settings、AI 创作、上传帖子。
- 底部导航：社区、商城、AI、消息、我的五项保留；容器样式保持当前 Android 真机 UI，icon 语义和文字以设计图为准。Open Design / SVG 反向同步当前 Android 底栏容器样式，不要求 Android 回退追旧设计图容器。
- 统一加载、空、错、未登录、无权限、弱网、禁用、UI-only 状态组件。
- 保持详情页和流程页隐藏底部导航。

验收：

- 五个主 Tab 首屏都有统一顶部栏和底部导航。
- 无 emoji 作为正式 UI icon。
- 可点击入口必须有真实跳转、禁用态或 UI-only 说明。

## Stage 2：社区首页与作品详情重构

目标：把社区主链路改成内容发现 + 创作工具平衡方向。

开发范围：

- 社区首页双列 masonry / 瀑布流。
- 图片高度控制在 `120dp-260dp`，超出裁切。
- 作品详情顺序固定为 `图片 -> 内容 -> 评论区域 -> 悬浮评论栏`。
- 顶部图片区做明确 carousel：页码、左右切换或滑动提示、缩略图 strip。
- 评论区展示评论数量、列表、文字、图片、@ 用户、# 话题、贴纸、空态、失败态。
- 悬浮评论栏支持图片/附件预览在上、输入在下、工具栏在底部。
- 评论图片上传最多 9 张；失败保留缩略图并重试。

验收：

- 评论成功不假装立即公开，只提示等待审核。
- 纯 @/# 不允许发送。
- 上传失败不得变成成功 Toast。
- `post-detail-comment-toolbar-a.html` 和 `post-detail-comment-toolbar-wireframe.svg` 的关键状态均可对应到实现。

## Stage 3：发帖、Search、Settings、Profile Edit

目标：把新增页面稿转成 Android 可落地任务，并明确哪些只是 UI-only。

发帖：

- 复用当前发帖接口和上传链路。
- 覆盖图片选择、正文、话题、预览、上传中、失败重试、审核中。

Search：

- 当前没有全局搜索 API。
- 可先做 UI-only 或局部已接接口：用户搜索、话题搜索、商品列表筛选。
- 不得宣称“全站搜索”已经接入。

Settings：

- 首页可先落地。
- 合规相关入口必须标注待补/开发态，不得伪造隐私政策、用户协议、备案、SDK 清单已完成。

Profile Edit：

- 后端已有 `PATCH /api/v1/users/me`，Android Retrofit 当前未接。
- 编辑资料页可作为 P0 接入任务；城市/地区仍为 UI-only，不接地图 API。

验收：

- Search、Settings 子页、Profile Edit 的实现事实和 UI-only 边界在文档中同步。
- 不新增地图、全局搜索或合规 API 契约。

## Stage 4：消息/私信/通知

目标：把消息页拆清楚，把互关 3 条限制从服务端语义落实到 Android UI。

开发范围：

- 消息首页拆私信和通知。
- 点击私信进入 `conversation/{conversationId}`。
- 通知详情若没有 Android 路由，先做 UI-only 或列表内详情展示。
- 会话详情展示 `mutualFollow`、`remainingNonMutualMessages`、`canSend`。
- 未互关剩余 3 条提示，超限后输入框禁用。
- 发送失败、加载失败、空会话都有状态。

验收：

- 通知区域不出现会话输入状态。
- 后端返回 `NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED` 时 UI 不允许继续发送。
- 好友第一版仍是关注/互相关注，不设计好友申请审批。

## Stage 5：商城/订单/联调支付边界

目标：复查商城链路，确保不误导真实支付和地址管理。

开发范围：

- 商城首页统一顶部栏和底部导航。
- 商品卡展示图片、标题、价格、库存/状态。
- 玩家二手/定制商品禁用标准购物车。
- 购物车和订单确认处理空态、失败、库存不足、未登录。
- 地址管理未完成时，订单确认必须明确缺口。
- 支付状态页展示联调支付单和服务端状态查询。

验收：

- `payParams.provider=STUB` 不展示为真实支付。
- 不创建缺少真实地址的误导性订单。
- 支付最终状态以服务端查询为准。
- 2026-06-05 Stage 8 真机 smoke 已覆盖玩家商品禁用标准购物车、自营加购、购物车和订单确认地址缺口；创建订单、联调支付单和支付状态页仍未关闭。

## Stage 6：AI 开发态和未来能力 UI-only

目标：保留 AI 主链路开发态可演示，同时把未来能力显式隔离。

开发范围：

- AI 首页统一顶部栏和底部导航。
- 上传、参数、任务、进度、结果、失败、取消、历史状态清楚。
- 真实大模型生图只在 `future-capability-ui-a.html` 中作为 UI-only 页面稿存在。
- 地图 API、真实支付 API、大模型生图 API 均不能写成已接入。

验收：

- AI 文案不承诺真实视觉理解质量。
- 未来能力入口如果可见，必须显著标注 UI-only / 当前不可用于真实交易或真实生成。
- Stage 8 JVM 规则测试已覆盖 AI Provider 边界、任务状态标签和结果页 `材料购买待接入`；2026-06-05 真机专项已覆盖 AI `相册 -> 上传 -> 参数 -> 创建任务 -> 进度 -> 结果` 开发态全链路，并确认结果页继续标注材料购买、自动加购、PDF 导出和带图纸发帖的开发态边界。

## Stage 7：QA 验收关闭

目标：用真机、接口、文档和设计验收关闭阶段。

验证范围：

- 文档验证：`git diff --check`、SVG XML 解析、旧口径搜索。
- Android：`.\gradlew.bat --version`、`:app:testDebugUnitTest`、`:app:assembleDebug`。
- 后端：`mvn test`。
- 真机：先 `adb devices -l`，无在线真机不得写通过。
- API：Swagger 或 curl smoke 覆盖 Auth、Feed、Upload、Comment、Message、Commerce、Payment。

退出标准：

- 五个主 Tab、作品详情、评论、消息、商城、AI、我的页的加载/空/错/未登录/禁用态都有证据。
- 不能完成的能力已隐藏、禁用或明确开发态。
- `current-status.md` 和 `10-testing-acceptance.md` 更新最终结论。

## Stage 8：UI 视觉对齐重构

目标：真机 smoke 后发现当前 Android 页面与 Open Design A 方向页面稿仍有明显视觉差异，因此新增独立 UI 对齐阶段。该阶段不以新增能力为目标，而是把已有页面按设计稿重构到更高视觉一致性。

详细计划：`17-ui-parity-refactor-plan.md`。

核心规则：

- Android 主页面和流程页除底部导航容器样式外，严格按 `doc/development/open-design/`、`13-ui-screen-blueprints.md` 和 `11-ui-style-guide.md` 做视觉对齐。
- 底部导航容器样式保持当前 Android 真机 UI：暖白浮动圆角胶囊、icon + label、选中浅粉容器、粉色图标文字和短横指示。
- 底部导航 icon 语义和文字追 Open Design / SVG，目标文案为 `社区 / 商城 / AI / 消息 / 我的`；当前 Android 已显示 `AI`，不改底栏容器样式。
- Open Design、HTML、SVG 和文档中的底部导航必须反向更新为当前 Android 底栏容器样式，不要求 Android 回退追旧设计图底栏容器。
- 不新增全局搜索、地图、真实支付、真实 AI、生产合规、完整地址或玩家交易闭环。

退出标准：

- 每个主 Tab 和关键流程页都有真机截图与页面稿差异清单。
- P0 视觉差异已关闭，或明确记录不能关闭的技术/接口原因。
- Open Design 与 SVG 底部导航容器样式已改成当前 Android 底栏样式；Android 底栏 icon 语义和文字已追设计图。
- Android 单测、Debug 构建和真机 smoke 结果写回 `current-status.md` 与 `10-testing-acceptance.md`。

2026-06-06 收口结论：

- 已关闭的 P0 自动化/契约项：App 图标使用 `assets/TBLogo.png` 生成 launcher 资源；社区点赞/收藏使用服务端权威计数；推荐 Feed、话题作品列表和详情带登录态时回显互动状态；AI 拍照进入相册同款确认页并保留旋转/上传/取消/重拍/重试；消息页、上传帖子页、我的页和底部导航 icon 语义按 Open Design A 做 P0 结构收口。
- 已关闭的素材边界：后端 seed 社区图片只使用可授权来源，来源和许可记录在 `doyu-server/src/main/resources/static/seed/ATTRIBUTION.md`；不提交小红书、抖音、B 站等未授权用户作品。
- 未关闭和阻塞项不再分散写入路线图，统一记录到 `18-unfinished-and-blockers.md`。其中联调支付状态页受地址管理缺口阻塞；本轮最新无线真机已覆盖社区互动计数和回显、AI 拍照确认流程、消息/上传帖子/我的页 P0 结构与底部导航文案。启动器桌面图标视觉、AI 真实手持纵横向拍照物理方向 / EXIF 对照仍需后续单独截图，不能写成已验收。
