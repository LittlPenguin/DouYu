# 10. 测试与验收

## 当前用途

本文件定义豆屿开发阶段的验证命令、设计验收、接口回归、Android/后端测试和真机 QA 规则。当前本轮只改文档和 SVG，不运行 Android 构建、后端测试或真机 QA；如果后续阶段触碰 `DouYu/` 或 `doyu-server/`，必须按对应范围补跑。

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
- Profile Edit、Search、Settings 子页、通知详情、未来能力页面均标明 UI-only 或当前未实现边界。

## 设计稿验收清单

### 全局 Shell

- 五个主 Tab 顶部统一为新增 icon、标题、搜索 icon。
- 新增菜单包含 Settings、AI 创作、上传帖子。
- 底部导航使用品牌化 Logo / icon。
- 不用 emoji 作为正式 UI icon。
- 主页面都有加载、空、错、未登录、禁用状态。

### 社区首页

- 双列瀑布流。
- 图片高度控制，超出裁切。
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

### Search

- 有返回、输入、清空/取消、范围 Tab。
- 范围包含全部、作品、图纸、商品、用户、话题。
- 标注当前没有全局搜索后端 API。

### 消息

- 私信和通知分区清晰。
- 通知区域不展示会话输入状态。
- 会话详情展示互关、未互关剩余 3 条、超限禁用、发送失败、加载失败、空会话。

### 我的与编辑资料

- 我的页统计为 `获赞 / 作品 / 关注 / 粉丝`。
- 资产 Tab 为 `我的图纸 / 点赞作品 / 收藏作品`。
- 最新设计稿不展示“我的订单 / 评论作品 / 关注作品”作为首屏入口。
- 编辑资料包含头像、昵称、简介、城市/地区 UI-only、兴趣标签、保存状态。

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

## 后端验证

修改 `doyu-server/` 后至少运行：

```powershell
cd doyu-server
mvn test
```

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
- Profile Edit Android 路由。
- Settings 子页 Android 路由。
- 通知详情 Android 路由。

## 阶段关闭标准

一个阶段关闭必须同时满足：

- 代码改动对应测试或构建通过。
- 文档、API 契约、UI 蓝图、流程图同步。
- 未完成能力被隐藏、禁用或标开发态。
- 真机 QA 有设备在线记录和关键截图/日志；无法执行则明确说明未验证范围。
- `current-status.md` 更新阶段结论、剩余风险和下一步优先级。
