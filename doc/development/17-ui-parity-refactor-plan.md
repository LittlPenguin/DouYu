# 17. UI 视觉对齐重构计划

> **For agentic workers:** 执行本计划时按任务逐项推进。开发前先读 `current-status.md`、`11-ui-style-guide.md`、`13-ui-screen-blueprints.md`、`16-stage-development-roadmap.md` 和本文件；每个任务完成后运行对应验证，并同步验收文档。

**Goal:** 让 Android 主要页面严格追齐 Open Design A 页面稿，同时把底部导航收敛为“样式沿用当前 Android，icon 语义与文字追设计图”的混合规则。

**Architecture:** 本阶段不新增业务能力，不新增后端 API。先建立真机截图与设计稿差异清单，再同步设计资产底部导航样式，最后按页面组重构 Compose UI。底部导航是唯一预设例外：Android 保持当前真机底栏的容器、动效和选中态；底栏 icon 语义与文字文案追 Open Design / SVG。

**Tech Stack:** Kotlin、Jetpack Compose、Navigation Compose、Retrofit、Coil、Open Design HTML/CSS、SVG、PowerShell、ADB。

---

## 背景结论

真机 smoke 后确认：当前 Android 页面与 Open Design A 方向页面稿在视觉密度、布局层级、页面组件形态、内容节奏和部分状态表达上仍有明显差异。现有 `16-stage-development-roadmap.md` 的 Stage 1-7 覆盖了 UI Shell、社区、作品详情、Search、Settings、Profile Edit、消息、商城和 AI 的功能收敛，但没有把“严格按设计稿做视觉对齐”作为独立阶段，也没有定义底部导航的例外规则。

因此新增 **Stage 8：UI 视觉对齐重构阶段**。

## 权威顺序

视觉和交互冲突时按以下顺序处理：

1. **底部导航**：采用混合权威。容器样式、阴影、圆角、选中背景、短横、显隐规则以当前 Android 真机 UI 为准；icon 语义和文字文案以 Open Design / SVG 为准，即 `社区 / 商城 / AI / 消息 / 我的`。Open Design、SVG 和文档必须反向同步当前 Android 底栏样式，但 Android 后续仍需把底栏 icon 语义和 label 调整到设计图。
2. **主页面布局与内容结构**：Open Design A 方向 HTML 页面稿、`13-ui-screen-blueprints.md` 和 `11-ui-style-guide.md` 为准。
3. **可用能力边界**：当前代码、`current-status.md`、`05-api-contract.md` 为准，不因视觉稿新增真实能力。
4. **实现细节**：优先复用现有 Compose 组件、Repository、Route、DTO，不为视觉重构新增后端 API。

## 底部导航例外规则

当前 Android 真机底部导航的**样式层**是本阶段权威：

- 底部固定五项，文案按设计图：社区、商城、AI、消息、我的。
- 外层为暖白浮动圆角胶囊，带轻描边和阴影。
- 每项为 icon + label。
- 选中项有浅粉圆角容器，图标和文字为粉色，底部有短横选中指示。
- 未选中项为深灰图标和文字。
- 主 Tab 页面显示底部导航；详情页和流程页隐藏。

底部导航的**语义层**追设计图：

- icon 语义使用设计图中的 `community / shop / ai / message / profile` 五类，不用 emoji，不用空框。
- label 使用 `社区 / 商城 / AI / 消息 / 我的`；当前 Android 已显示 `AI`，不改底栏容器样式。
- Open Design、HTML、SVG 和文档必须同步当前 Android 的底栏容器样式，同时保留设计图的 icon 语义与文字。Android 不按旧设计稿底栏容器样式回退。

## 本阶段不做

- 不新增全局搜索后端。
- 不接地图 API、定位服务或城市选择真实能力。
- 不接真实微信/支付宝支付、退款、对账或支付 SDK。
- 不接真实 AI Provider 或大模型生图 API。
- 不补生产隐私政策、用户协议、备案、SDK 清单、版权投诉等合规材料。
- 不补完整地址管理、玩家交易闭环、担保、评价、纠纷或提现。
- 不把 Open Design 的 UI-only 页面写成当前真实后端能力。

## 文件触达地图

Android：

- `DouYu/app/src/main/java/cn/edu/app/douyu/core/ui/Components.kt`：通用顶部栏、状态页、按钮、卡片、底部相关通用组件。
- `DouYu/app/src/main/java/cn/edu/app/douyu/core/navigation/DoyuApp.kt`：主 NavHost、底部导航、路由注册。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/CommunityScreens.kt`：社区首页、作品详情、发帖、评论栏。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/SearchScreen.kt`：Search 页面。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce/CommerceScreens.kt`：商城首页、商品、购物车、订单、支付。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/ai/AiScreens.kt`：AI 首页、任务和开发态。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/message/MessageScreens.kt`：消息、通知、会话。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/profile/ProfileScreens.kt`：我的、Profile Edit、Settings。

设计和文档：

- `doc/development/open-design/*.html`：Open Design 本地页面稿。
- `doc/development/open-design/doyu-open-design.css`：Open Design 页面通用样式。
- `doc/development/diagrams/*.svg`：UI 信息架构和线框图。
- `doc/development/11-ui-style-guide.md`：UI 规范。
- `doc/development/13-ui-screen-blueprints.md`：页面蓝图。
- `doc/development/current-status.md`：当前事实和下一步。
- `doc/development/10-testing-acceptance.md`：验收记录。

## Task 1：视觉差异盘点

**Files:**

- Modify: `doc/development/current-status.md`
- Modify: `doc/development/10-testing-acceptance.md`
- Create or update: `.qa-output/` 截图和本地差异记录，忽略不提交。

步骤：

1. 运行真机设备检查：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
```

期望：看到用户指定真机 IP `10.64.241.158` 对应的无线 ADB 设备在线，并记录实际 ADB serial。若无在线设备，继续做文档和静态检查，但不得写真机通过。

2. 构建并安装最新 APK：

```powershell
cd DouYu
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
cd ..
D:\AndroidChace\platform-tools\adb.exe -s <adb-serial> install -r DouYu\app\build\outputs\apk\debug\app-debug.apk
```

3. 截图五个主 Tab 和核心二级页，文件名使用：

```text
.qa-output/ui8-community.png
.qa-output/ui8-commerce.png
.qa-output/ui8-ai.png
.qa-output/ui8-message.png
.qa-output/ui8-profile.png
.qa-output/ui8-search.png
.qa-output/ui8-post-detail.png
.qa-output/ui8-post-compose.png
.qa-output/ui8-profile-edit.png
.qa-output/ui8-settings.png
```

4. 对照 `doc/development/open-design/*.html` 建立 P0/P1/P2 差异清单，写入 `10-testing-acceptance.md` 的 UI 视觉对齐验收章节：

```markdown
| 页面 | P0 差异 | P1 差异 | 允许保留差异 |
|---|---|---|---|
| 社区 | 除底部导航容器样式外，列出必须追设计稿的结构差异 | 可后续优化的视觉差异 | 底部导航容器样式保持 Android 当前样式，icon 与文字追设计图 |
```

退出标准：

- 每个主页面至少有一张真机截图和一行差异记录。
- 差异清单不把真实支付、真实 AI、地图、全局搜索列为本阶段开发项。

## Task 2：反向同步设计稿底部导航样式

**Files:**

- Modify: `doc/development/open-design/doyu-open-design.css`
- Modify: `doc/development/open-design/community-home-a.html`
- Modify: `doc/development/open-design/commerce-home-a.html`
- Modify: `doc/development/open-design/ai-home-a.html`
- Modify: `doc/development/open-design/messages-a.html`
- Modify: `doc/development/open-design/profile-a.html`
- Modify: `doc/development/diagrams/ui-information-architecture.svg`
- Modify: `doc/development/diagrams/community-home-wireframe.svg`
- Modify: `doc/development/diagrams/commerce-home-wireframe.svg`
- Modify: `doc/development/diagrams/ai-home-wireframe.svg`
- Modify: `doc/development/diagrams/message-profile-wireframes.svg`

步骤：

0. 保留 Open Design 的底栏语义和文案：

```html
<a class="nav-item on" href="community-home-a.html"><span class="nav-logo community"></span><span>社区</span></a>
<a class="nav-item" href="commerce-home-a.html"><span class="nav-logo shop"></span><span>商城</span></a>
<a class="nav-item" href="ai-home-a.html"><span class="nav-logo ai"></span><span>AI</span></a>
<a class="nav-item" href="messages-a.html"><span class="nav-logo message"></span><span>消息</span></a>
<a class="nav-item" href="profile-a.html"><span class="nav-logo profile"></span><span>我的</span></a>
```

1. 在 CSS 中定义当前 Android 底栏等价样式：

```css
.bottom-nav {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 18px;
  min-height: 78px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
  padding: 6px;
  border: 1px solid rgba(222, 196, 204, 0.72);
  border-radius: 28px;
  background: #fffaf7;
  box-shadow: 0 12px 28px rgba(64, 42, 48, 0.16);
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #4d4045;
  border-radius: 20px;
}

.bottom-nav-item.active {
  background: #fde8ef;
  color: #d86f8b;
}

.bottom-nav-item.active::after {
  content: "";
  width: 34px;
  height: 4px;
  border-radius: 999px;
  background: currentColor;
}
```

2. 把五个主页面的底栏 HTML 改成统一五项：社区、商城、AI、消息、我的；只同步容器样式，不引入历史错误 AI Tab 文案。

3. SVG 线框图里的底栏改成相同结构：暖白圆角胶囊、五个 icon+label、选中浅粉容器和短横；五项文案使用 `社区 / 商城 / AI / 消息 / 我的`。

4. 运行 HTML doctype 和 SVG XML 检查：

```powershell
Get-ChildItem doc\development\open-design -Filter *.html | ForEach-Object {
  $html = Get-Content -Raw -Encoding UTF8 $_.FullName
  if (-not $html.Contains("<!doctype html>")) { throw "missing doctype: $($_.Name)" }
}

Get-ChildItem doc\development\diagrams -Filter *.svg | ForEach-Object {
  [xml](Get-Content -Raw -Encoding UTF8 $_.FullName) | Out-Null
}
```

退出标准：

- Open Design 主页面底栏容器样式与当前 Android 真机截图一致。
- Open Design 主页面底栏 icon 语义与文字仍与设计图一致。
- 文档不再要求 Android 底栏追旧设计图容器样式。

## Task 3：通用视觉组件对齐

**Files:**

- Modify: `DouYu/app/src/main/java/cn/edu/app/douyu/core/ui/Components.kt`
- Modify: `DouYu/app/src/main/java/cn/edu/app/douyu/core/navigation/DoyuApp.kt`
- Test: `DouYu/app/src/test/java/cn/edu/app/douyu/core/RouteTest.kt`

步骤：

1. 为通用页面容器、卡片、Chip、状态页、输入框建立或收敛单一组件，不在每页重复写同类样式。

2. 保持当前 `DoyuBottomNavBar` 容器样式、显隐规则、选中态和导航行为，不因旧设计稿底栏容器差异改动底部导航。

3. 对齐底栏 icon 语义和文字：五项文案使用 `社区 / 商城 / AI / 消息 / 我的`；icon 语义保持社区、商城、AI、消息、我的五类，禁止 emoji 或含义不清的图标。

4. 给新增 route 或 helper 写单测。若只改 Compose 视觉且无法 JVM 断言，至少保留 route/helper 级别测试。

5. 运行：

```powershell
cd DouYu
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

退出标准：

- 五个主 Tab 顶栏和页面容器一致。
- 底部导航未回退。
- 没有 emoji 作为正式 icon。

## Task 4：五个主 Tab 视觉对齐

**Files:**

- Modify: `CommunityScreens.kt`
- Modify: `CommerceScreens.kt`
- Modify: `AiScreens.kt`
- Modify: `MessageScreens.kt`
- Modify: `ProfileScreens.kt`

页面验收：

- 社区追 `community-home-a.html`：双列 masonry、卡片比例、作者/互动数据、频道和 fallback。
- 商城追 `commerce-home-a.html`：搜索、分类、运营位、商品卡、价格和库存状态。
- AI 追 `ai-home-a.html`：创作入口、任务卡、历史入口、开发态提示。
- 消息追 `messages-a.html`：通知/私信分区、空态和列表项。
- 我的追 `profile-a.html`：头像、昵称、简介、统计、资产区、编辑资料入口。
- 底部导航容器样式保持当前 Android 样式；icon 语义和文字追设计图。

每页改完后运行：

```powershell
cd DouYu
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

退出标准：

- 五个主 Tab 真机截图除底部导航容器样式外与设计稿首屏结构一致；底栏 icon 与文字也需要追设计图。
- 页面没有空点击和假成功 Toast。

## Task 5：核心流程页视觉对齐

**Files:**

- Modify: `CommunityScreens.kt`
- Modify: `SearchScreen.kt`
- Modify: `MessageScreens.kt`
- Modify: `ProfileScreens.kt`
- Modify: `CommerceScreens.kt`
- Modify: `AiScreens.kt`

页面验收：

- 作品详情追 `post-detail-comment-toolbar-a.html`。
- 发帖追 `post-compose-a.html`。
- Search 追 `search-a.html`，且保留“无全局搜索后端”提示。
- 会话详情追 `message-conversation-a.html`。
- 通知详情追 `notification-detail-a.html`，且保留“列表内详情，无独立后端接口”提示。
- Profile Edit 追 `profile-edit-a.html`，城市/地区仍 UI-only。
- Settings 追 Settings 系列页面，合规材料只标待补。
- 支付状态只展示 STUB 联调支付单。

退出标准：

- 关键流程页都有加载、空态、错误、未登录、禁用或 UI-only 状态。
- 不新增后端 API。

## Task 6：真机视觉 QA 与文档关闭

**Files:**

- Modify: `doc/development/current-status.md`
- Modify: `doc/development/10-testing-acceptance.md`
- Modify: `doc/development/03-android-client.md` if route/page facts changed
- Modify: `doc/development/13-ui-screen-blueprints.md` if design rules changed

步骤：

1. 运行最终验证：

```powershell
git diff --check
git diff --name-only -- doyu-server
cd DouYu
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

2. 真机 smoke：

```powershell
D:\AndroidChace\platform-tools\adb.exe devices -l
D:\AndroidChace\platform-tools\adb.exe -s <adb-serial> install -r DouYu\app\build\outputs\apk\debug\app-debug.apk
D:\AndroidChace\platform-tools\adb.exe -s <adb-serial> shell am start -n cn.edu.app.douyu/.MainActivity
```

3. 把截图路径和结果写回 `10-testing-acceptance.md`。如果真机断连，写“未覆盖”，继续完成构建和文档检查。

4. 关闭临时启动的 Spring Boot 服务；`.qa-output/` 不提交。

完成定义：

- Android 主要页面除底部导航容器样式外，与 Open Design A 页面稿结构和视觉高度一致。
- Open Design 和 SVG 的底部导航容器样式已经反向改成当前 Android 真机底栏，icon 语义与文字仍保持设计图口径。
- 所有 UI-only、开发态、Stub、未完成合规和未接 API 能力都有清楚提示。
- 文档、页面稿、流程图和 Android 当前事实一致。
- 构建、单测和真机 smoke 结果写回 `current-status.md` 与 `10-testing-acceptance.md`。
