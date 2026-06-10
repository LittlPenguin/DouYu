# 上传页、消息页、我的页 UI 复修计划

日期：2026-06-10

## 目标

- 以 `doc/development/open-design/` 为 UI 权威源，完成上传帖子页面的视觉和功能状态复修。
- 修复 `messages-a.html`、`profile-a.html` 的乱码，使消息页和我的页设计稿重新成为可读中文源。
- 修复 Android 上传页、消息页、我的页中的乱码和英文文案，统一为中文运行口径。
- 统一左上角设置图标和底部导航图标的 24dp vector 风格，保持上传为底部导航 action。
- 保留真实发帖链路：登录门禁、选图、CameraX 拍照、OSS 上传、失败重试/删除、9 图上限、话题选择、预览、发布、审核中。

## 非目标

- 不改后端 API，不新增后端能力。
- 不修改 `.env.example` 的 OSS 配置，不弱化 OSS 上传链路。
- 不删除或改写地址管理、订单地址流程。
- 不恢复 AI、地图、支付、future capability 或完整合规材料页面。
- 不做真机、截图、真实 OSS PUT 或视觉冒烟校验；这些交给审查阶段。

## 修改范围

- Open Design：`post-compose-a.html`、`messages-a.html`、`profile-a.html`。
- Android UI 和文案：`PostCreateActivity.java`、`activity_post_create.xml`、`MessagesFragment.java`、`MessageHomeAdapter.java`、`fragment_messages_home.xml`、`ProfileFragment.java`、`ProfileAssetAdapter.java`、`fragment_profile_home.xml`、`item_profile_asset.xml`、`UiCopy.java`、`XmlPageActivity.java`、`include_page_toolbar.xml`。
- 图标：`ic_action_settings.xml`、`ic_tab_upload.xml`、`ic_tab_messages.xml`、`ic_tab_profile.xml`，必要时同步其它底部导航 icon。
- 测试：`OpenDesignLayoutMappingTest.java`、`SourceMojibakeSpotTest.java`。

## 实现步骤

1. 恢复 Open Design 消息页和我的页为正常中文，并保持底部导航顺序为“社区 / 商城 / 上传 / 消息 / 我的”。
2. 修复 Android 公共文案、页面标题、错误态、空态、登录态和按钮文案。
3. 调整上传页布局：保持顶部标题和发布按钮，补齐底部导航展示，上传项高亮，状态区域与设计稿一致。
4. 修复消息页和我的页运行文案、列表 section、统计区、资产 Tab 和资产卡文案。
5. 统一设置图标与底部导航图标的 vector 风格。
6. 更新静态合同测试，覆盖乱码、中文文案、上传链路、底部导航和已删除能力不回流。

## 验证命令

- `rg -n "�|涓|娑|鎴|閫|璞|鍟|绀|Not signed in|Sign in to view|Likes source|Liked posts|Favorite posts|Retry|Load failed|Service is temporarily unavailable|Cannot connect" DouYu/app/src/main doc/development/open-design -g "!**/build/**"`
- `rg -n "ai-home-a.html|future-capability-ui-a.html|settings-about-compliance-a.html|顶部新增菜单进入上传帖子|上传不作为底部主 Tab|payment|地图" doc/development/open-design doc/development/README.md doc/development/13-ui-screen-blueprints.md -S`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain`
- `rg --files DouYu/app/src | rg "\.kt$"`
- `rg -n "Compose|kotlinx|Navigation Compose|activity_ai_flow|fragment_ai_home|PaymentBoundaryActivity|AiFragment|PatternJob|favoritePatterns" DouYu/app/src/main DouYu/app/src/test -S`
- `rg -n "DOUYU_OSS|douyu\.oss|uploads/presign|uploads/confirm|Aliyun|ALIYUN|OSS" .env.example doyu-server/src/main DouYu/app/src/main doc/development/27-remove-ai-map-payment-plan.md AGENTS.md -g "!**/build/**" -g "!**/target/**"`
- `cd D:\Studio\SpellBean; git diff --check`

## 验收记录

- Android 开发阶段只声明单元测试、构建和静态检查结果。
- 真机点击底部“上传”、真实 OSS 上传、截图和像素级视觉验收不在本轮执行 Agent 验证范围内。
