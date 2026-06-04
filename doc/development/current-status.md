# 当前状态

> 更新日期：2026-06-04
> 用途：记录当前工程事实、UI 重构阶段、功能完成度、不可用边界和下一步优先级。产品目标和商业边界以 `../豆屿App商业技术执行计划.md` 为准。

## 阶段结论

豆屿 Doyu 当前处于 **第六轮 UI/品牌与主链路展示收敛阶段**。

已经完成的基础阶段：

- 第一轮：登录 + 社区契约样板链路稳定。
- 第二轮：App Shell、消息、我的 UI 统一基线形成。
- 第三轮：商城 UI/API 主体链路关闭为可联调、不误导支付的 MVP。
- 第四轮：Android 登录态收敛到 DataStore，社区/商城常驻 seed 图文可渲染。
- 第五轮：后端对象存储 Provider 切换骨架完成，可通过 `.env` 在 `local|stub|aliyun` 间切换。
- 第六轮：Open Design A 方向页面稿和本地 HTML 原型已补齐，当前继续把设计目标、API 契约、阶段开发流程和 SVG 图谱沉淀到 `doc/development`。

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
- 底部导航使用品牌化 Logo / icon。
- 社区首页目标为双列瀑布流。
- 作品详情目标顺序为 `图片 -> 内容 -> 评论区域 -> 底部悬浮评论栏`。
- 评论栏展开态为图片/附件预览在上，评论输入在下，工具栏在底部。
- 评论多图状态包含堆叠缩略图、上传中、失败重试、carousel 预览和 9 图上限。
- 消息页拆私信和通知；私信详情展示互关和未互关 3 条限制。
- 我的页统计目标为 `获赞 / 作品 / 关注 / 粉丝`，资产目标为 `我的图纸 / 点赞作品 / 收藏作品` 同组 Tab。
- 编辑资料、Search、Settings 子页、通知详情和未来地图/真实支付/大模型生图均为 UI-only 或待接入目标。

这些设计资产不代表 Android 当前代码已经完成落地。

## 功能完成度矩阵

| 模块 | 当前已有 | 半成品 / 缺口 | 当前策略 |
|---|---|---|---|
| App Shell | 5 个主 Tab、NavHost、底部导航、基础通用组件 | 新版顶部栏、品牌 Logo 底部导航、全局 Search 路由未落地 | Stage 1 先统一 Shell 与状态组件 |
| 登录 | 短信登录、验证码 Stub、refresh、logout、DataStore token hydrate | 真实短信、限流、风控未完成 | 继续保留 `ageGroup=AGE_18_PLUS` 契约 |
| 社区 | Feed、关注 Feed、发帖、详情、评论、点赞、收藏、话题、贴纸、评论富内容字段 | 新版瀑布流、图片优先详情、评论区和评论栏状态未完全按设计实现；真机富评论仍需复验 | Stage 2 重构社区和作品详情 |
| 上传 | presign、PUT、confirm、FileAsset、Local/Stub/Aliyun Provider | 生产 STS、CDN、防盗链、审核、缩略图未完成 | 继续复用现有上传链路，不新增 API |
| AI | 任务创建、查询、列表、取消、结果、收藏、自研拼豆算法 | 真实视觉 Provider、大模型生图、生产内容安全未完成 | 保持开发态，不承诺真实识图质量 |
| 商城 | 商品、购物车、订单、联调支付单、支付查询 | 地址管理、真实支付、玩家交易闭环未完成 | 只表达联调支付和地址缺口 |
| 消息 | 通知、会话、会话详情、私信发送、互关 3 条限制 | 通知详情路由、消息页新版分区和会话状态 UI 待落地 | Stage 4 收敛私信/通知 |
| 我的 | 用户资料、图纸、收藏、签到、徽章、历史个人互动作品路由 | 最新我的页统计、三资产 Tab、Profile Edit 仍是设计目标 | 区分代码事实和目标 UI |
| Settings / 合规 | 设置入口、账号注销申请后端接口、合规文档要求 | Settings 子页、隐私政策、用户协议、备案、SDK 清单未完成 | 只标待补/开发态 |
| Admin | 后端后台 API | 完整运营后台前端、权限分级和生产工作台未完成 | 不作为 App 当前主链路 |

## 当前代码事实

Android：

- 技术栈：Kotlin、Jetpack Compose、Navigation Compose、Retrofit、OkHttp、Kotlinx Serialization、Coil、CameraX、Photo Picker、DataStore。
- 当前底部 Tab：`community`、`commerce`、`ai`、`message`、`profile`。
- 当前已有二级路由包括登录、作品详情、发帖、选图/拍照、AI 参数/进度/结果/历史、商品、购物车、订单确认、支付状态、会话、我的图纸、收藏、个人互动作品页、我的订单和设置。
- 当前没有全局 Search、Profile Edit、Settings 子页、通知详情、未来能力页面的 Android 路由。
- 登录态已从临时内存状态收敛到 DataStore 持久化。

后端：

- 技术栈：Java 21、Spring Boot、Spring Security、JWT、JPA、Flyway、PostgreSQL、Redis。
- API 前缀：`/api/v1`。
- 后端端口：`8081`。
- PostgreSQL 宿主端口：`5433`。
- 统一响应：`{ code, message, data, traceId }`。
- 真实 Controller 覆盖 Auth、User、Upload、Community、Pattern、Commerce、Order、Payment、Message、Reward、Report、Admin。
- 支付、AI、OSS、短信等 Provider 仍有开发态或 Stub 边界。

## 不能认为完成

以下能力不得在文档、UI 或验收中写成已完成：

- 真实 AI Provider。
- 地图 API。
- 真实微信/支付宝支付、退款、对账或支付 SDK。
- 完整地址管理。
- 生产级内容审核、图片审核、版权识别、诈骗识别和交易风控。
- 玩家二手/定制交易完整闭环、担保、评价、纠纷、提现和卖家资质审核。
- 备案、隐私政策、用户协议、SDK 清单、版权投诉和应用市场上线材料。
- 生产对象存储；Aliyun OSS Provider 只是后端切换骨架和开发联调能力。
- Search 全局后端。
- Profile Edit Android 路由。
- Settings 子页 Android 路由。
- 通知详情 Android 路由。
- 大模型生图 API。

## 当前验收风险

没有新的真机证据前，不能写成通过：

- 评论图片 Photo Picker。
- 最多 9 图。
- `/uploads/presign -> PUT -> /uploads/confirm` 评论图片链路。
- 上传失败保留缩略图和重试。
- 评论列表图片、@、#、贴纸渲染。
- 纯贴纸评论。
- 点赞、收藏、关注退出详情后回显。
- 最新我的页统计和三资产 Tab Android 实现。
- Search、Profile Edit、Settings 子页、通知详情 Android 实现。
- 真机 seed/public 图片 URL 不返回 `localhost`。
- 当前轮后端 `mvn test`。

## 下一步优先级

P0：Stage 0 文档/API/图谱对齐。

- 完成 `05-api-contract.md` 的真实接口覆盖。
- 完成 `16-stage-development-roadmap.md`。
- 重写 `diagrams/` 流程图和 UI 线框图。
- 验证未触碰 Android / 后端源码。

P1：Stage 1 UI Shell 与设计系统落地。

- 统一五个主 Tab 顶部栏。
- 落地品牌化底部 Logo 导航。
- 收敛状态页、禁用态、开发态。
- 快捷新增菜单接 Settings、AI 创作、上传帖子。

P2：Stage 2 社区首页与作品详情重构。

- 社区双列瀑布流。
- 作品详情图片优先。
- 评论区、悬浮评论栏、@/#/图片/贴纸和 9 图状态。

P3：Stage 3 发帖、Search、Settings、Profile Edit。

- 发帖按新版原型补状态。
- Search 标 UI-only 或局部筛选范围。
- Profile Edit 接已有后端资料更新接口前先补 Android Retrofit / Repository 计划。
- Settings 子页只标待补/开发态。

P4：Stage 4 消息/私信/通知。

- 私信/通知分区。
- 会话详情互关正常聊天、未互关剩余 3 条、超限禁用。
- 通知详情仍按 UI-only 或后续路由处理。

P5：Stage 5-7。

- 商城/订单/联调支付边界复查。
- AI 开发态和未来能力 UI-only 边界。
- 真机 QA、接口回归和文档验收关闭。
