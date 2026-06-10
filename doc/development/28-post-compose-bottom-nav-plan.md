# 上传帖子底部导航与真实发帖闭环计划

日期：2026-06-09

## 目标

- 修复当前 Open Design 关键页面乱码，并恢复“上传帖子”设计稿的可读结构。
- 将“上传”放入 Android 和 Open Design 底部导航，顺序固定为：社区、商城、上传、消息、我的。
- “上传”是底部导航中的 action item：点击后走登录门禁并打开全屏发帖页，不新增第五个内容 Fragment。
- 按 `post-compose-a.html` 和文档描述实现 Android `PostCreateActivity`：标题、正文、选图、CameraX 拍照、最多 9 图、OSS 上传、失败重试/删除、话题选择、预览、发布和审核中状态。
- 接通 Android `POST /api/v1/posts` 契约，使用后端已有审核流，发布成功后展示 `REVIEWING` / 审核中。

## 非目标

- 不删除、不弱化 OSS；`.env.example` 中 `DOUYU_OSS_*` / `DOUYU_ALIYUN_OSS_*` 配置必须保持可用。
- 不删除地址管理、收货地址字段或订单地址流程。
- 不恢复 AI、地图、支付、future capability 或完整合规材料页面与 Provider。
- 不新增后端接口，不改变后端 `POST /api/v1/posts` 的请求/响应结构。
- 不使用客户端 mock/demo 数据、假图片、假 fileId 或本地 bundled placeholder 满足运行态。

## 文件范围

- Open Design：
  - `doc/development/open-design/community-home-a.html`
  - `doc/development/open-design/commerce-home-a.html`
  - `doc/development/open-design/messages-a.html`
  - `doc/development/open-design/profile-a.html`
  - `doc/development/open-design/post-compose-a.html`
  - `doc/development/open-design/index.html`
  - `doc/development/open-design/doyu-design-directions.html`
  - `doc/development/open-design/design-decision.md`
- Android：
  - `DouYu/app/src/main/res/layout/activity_main.xml`
  - `DouYu/app/src/main/res/drawable/ic_tab_upload.xml`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/MainActivity.java`
  - `DouYu/app/src/main/res/layout/activity_post_create.xml`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostCreateActivity.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostCaptureActivity.java`
  - `DouYu/app/src/main/res/layout/activity_post_capture.xml`
  - `DouYu/app/src/main/AndroidManifest.xml`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/core/IntentExtras.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/model/PostRequest.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`
  - `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
- 文档/测试：
  - `doc/development/05-api-contract.md`
  - `doc/development/12-feature-and-flow-map.md`
  - `doc/development/13-ui-screen-blueprints.md`
  - `DouYu/app/src/test/java/cn/edu/app/douyu/core/OpenDesignLayoutMappingTest.java`
  - `DouYu/app/src/test/java/cn/edu/app/douyu/core/SourceMojibakeSpotTest.java`
  - `DouYu/app/src/androidTest/java/cn/edu/app/douyu/VisualSmokeInstrumentedTest.java`

## 实现步骤

1. 从 git 历史恢复关键 Open Design 页面的可读文本结构，再按当前产品边界移除 AI、地图、支付、future capability 和完整合规材料入口。
2. 将所有主页面底部导航统一为 `社区 / 商城 / 上传 / 消息 / 我的`，`上传` 链接到 `post-compose-a.html`。
3. 更新 Android 首页底部导航，新增 `tab_upload` 并绑定登录门禁；保留社区、商城、消息、我的四个内容 Fragment。
4. 移除顶部 quick menu 中的上传入口；顶部左侧按钮直接进入设置，避免双入口误导。
5. 重写 `PostCreateActivity` 和 XML，使其具备真实输入、状态、预览、话题加载、OSS 图片上传和发布闭环。
6. 新增发帖 CameraX 拍照页，拍照后回填发帖页并走同一 OSS 上传链路。
7. 新增 Android `PostRequest`、`DoyuApi.createPost()` 和 `DoyuRepository.createPost()`。
8. 更新 API 文档、功能流文档、UI 蓝图和相关测试。

## 验证命令

- 静态搜索已删除的 settings compliance 身份，期望当前运行代码无命中。
- 静态搜索 Open Design 乱码、已删除页面链接和旧发帖入口口径，期望当前文档无误导命中。
- 静态搜索 OSS 相关配置和上传接口，期望 `.env.example`、后端 upload/oss provider、Android 上传链路仍保留。
- Android：`DouYu` 下运行 `.\gradlew.bat :app:testDebugUnitTest --console=plain`。
- Android：`DouYu` 下运行 `.\gradlew.bat :app:assembleDebug --console=plain`。
- Android：`DouYu` 下运行 `.\gradlew.bat :app:assembleDebugAndroidTest --console=plain`。
- 后端：`doyu-server` 下运行 `mvn test`。
- 仓库根目录运行 `git diff --check`。

## 验收记录

- Open Design 乱码和旧入口口径必须用静态搜索确认。
- Android 单测和构建必须有新鲜输出；如无设备，不声明视觉冒烟或真机截图通过。
- 后端回归只验证已有接口不回归，不新增后端能力。
