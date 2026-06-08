# 2026-06-07 AI 创作详情内容 UI 与功能还原计划

## 目标

仅开发 AI 创作工作树范围内的页面和功能：

- `doc/development/open-design/ai-home-a.html` 对应 `AiFragment` / `fragment_ai_home.xml`。
- AI 图纸流程、任务详情、图纸详情由 `AiFlowActivity` / `activity_ai_flow.xml` 承载。

不开发社区、商城、消息、我的、设置等页面；这些页面由其他工作树处理。

## 实施方案

- 在现有 Android Java/XML 架构内还原 Open Design 结构，不引入 Compose/Kotlin。
- 不新增后端接口、不改数据库；Android 只接入后端已存在的上传、任务、取消、图纸详情、收藏、额度接口。
- 不使用本地假任务、假图纸、假成功 Toast。未登录、空数据、失败、未闭环能力必须显示明确边界。
- 本轮完成后只做本地非真机验证；不执行 ADB、真机仪器测试、截图拉取或 evidence 更新。

## 开发任务

1. AI 首页
   - 将 `fragment_ai_home.xml` 改为顶部 chips、开发态 Hero、相册上传、拍照、当前任务、历史列表、登录/空/错误边界结构。
   - 将 `AiFragment` 改为 AI 专属渲染逻辑，加载真实 `patternJobs()`，优先展示 PENDING/PROCESSING 任务，历史区展示其他任务。
   - 相册上传打开系统图片选择器并进入 `AiFlowActivity`；拍照进入 `CameraActivity`。

2. AI 图纸流程/详情
   - 将 `activity_ai_flow.xml` 改为上传/输入图片区、参数区、任务状态区、结果图纸区、操作区、失败/登录/开发态说明区。
   - `AiFlowActivity` 支持图片 URI、`jobId`、`patternId` 三种入口。
   - 真实渲染 PENDING、PROCESSING、SUCCEEDED、FAILED、CANCELED；使用真实进度和失败原因。
   - 收藏图纸、取消任务接真实接口；PDF、材料加购、分享到社区保持禁用并标注开发态。

3. AI 客户端接口
   - 扩展 `DoyuApi` / `DoyuRepository`，接入：
     - `POST /api/v1/uploads/presign`
     - 预签名 PUT
     - `POST /api/v1/uploads/confirm`
     - `POST /api/v1/patterns/jobs`
     - `POST /api/v1/patterns/jobs/{jobId}/cancel`
     - `GET /api/v1/patterns/jobs`
     - `GET /api/v1/patterns/jobs/{jobId}`
     - `GET /api/v1/patterns/{patternId}`
     - `POST /api/v1/patterns/{patternId}/favorite`
     - `GET /api/v1/patterns/quota`
   - 新增 DTO：`PresignUploadRequest`、`PresignUploadResponse`、`ConfirmUploadRequest`、`FileAsset`、`CreatePatternJobRequest`、`FavoriteResult`、`AiQuota`。
   - 预签名 PUT 使用无登录拦截器 OkHttp client。

4. 局部导航
   - `MainActivity` quick menu 的“AI 创作”切回 AI Tab，而不是打开空流程页。
   - `CameraActivity` 拍照后提供“使用照片生成图纸”入口，把本地图片 URI 交给 `AiFlowActivity`。

## 测试与验收

- 新增/扩展单元测试：
  - `DoyuApiDetailContractTest` 覆盖 AI/upload/favorite/quota Retrofit endpoint。
  - `AiUiFormatterTest` 覆盖状态文案、进度、可取消、可收藏、失败原因。
  - `AiOpenDesignContentTest` 覆盖 AI 首页和流程页关键文案/控件 ID。
  - 保持 `SourceMojibakeSpotTest` 覆盖新增中文文件。
- 本地验证命令：
  - `rg --files DouYu/app/src | rg "\.kt$"`
  - `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil.compose|paging.compose" DouYu/app`
  - `cd DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
  - `cd DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
  - `cd DouYu; .\gradlew.bat :app:lintDebug --console=plain`
  - 可运行 `cd DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain` 做编译检查。
- 明确不执行：
  - 不运行 `adb install`。
  - 不运行 `VisualSmokeInstrumentedTest` 或 `RealBackendSmokeInstrumentedTest`。
  - 不更新真机截图 evidence，不声明新的真机 1:1 通过结论。

## 假设

- “详情内容”指 AI 图纸流程、任务详情、图纸详情，即 `AiFlowActivity`。
- `ai-home-a.html` 是本轮 AI 首页设计权威；`future-capability-ui-a.html` 只作为未闭环能力的 UI-only 边界参考。
- 上传、任务、取消、收藏、额度接口以后端现有契约为准。
