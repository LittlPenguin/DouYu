# 作品详情 Composer 功能验证记录（plan-22，2026-06-08）

对应执行计划：`doc/development/22-post-detail-composer-features-plan.md`。
本轮在 Android Java/XML 客户端补齐作品详情页 图片评论上传、@ 用户、# 话题、关注作者、多图大图查看，后端契约未改动。

## 已实现（对照设计稿 post-detail-comment-toolbar-a.html）

- 图片评论上传：`post_comment_tool_image` 选图→真实 presign/PUT/confirm（`usage=POST_IMAGE`）；编辑器内缩略图草稿条；上传中/失败(点击重试)/成功(点击移除)三态；9 张上限拦截；上传未完成禁发；不伪造成功。
- @ 用户：`post_comment_tool_mention` 打开 `PickerSheet(USER)`，调用真实 `GET /api/v1/users/search`；选中以 Chip 回填，可点击移除；提交写入 `mentionUserIds`。
- # 话题：`post_comment_tool_topic` 打开 `PickerSheet(TOPIC)`，调用真实 `GET /api/v1/topics`（按关键词本地过滤，不造推荐运营接口）；Chip 回填、可移除；提交写入 `topicIds`。
- 发送条件：文字或图片至少其一存在才可发；成功后清空草稿(图/@/#)、收键盘、恢复静态栏、刷新评论；新评论回显图片缩略图与 @昵称 / #话题。
- 关注作者：`post_author_follow` 接真实 `POST/DELETE /api/v1/users/{userId}/follow`，切换关注/已关注样式；进行中禁用按钮；失败保持原状态并提示；未登录走登录边界。
- 多图大图：新增 `PhotoViewerActivity`（全屏 carousel：大图/页码/左右箭头/缩略图 strip/选中边框）。作品大图与评论图缩略图点击进入查看器。

## 已运行验证（本机，带输出）

- `find DouYu/app/src -name "*.kt"` → 无输出（无 Kotlin 源）。
- `grep -rnE "compose|Composable|kotlinx|MockData|coil\.compose" DouYu/app/src/main/java` → 无输出。
- 新增/改动 Java 文件 `[\x{FFFD}]` 替换字符扫描 → 无乱码。
- `./gradlew.bat :app:compileDebugJavaWithJavac --console=plain` → `BUILD SUCCESSFUL`。
- `./gradlew.bat :app:testDebugUnitTest --console=plain` → `BUILD SUCCESSFUL`（含更新后的 `OpenDesignLayoutMappingTest`）。
- `./gradlew.bat :app:testDebugUnitTest --tests OpenDesignLayoutMappingTest --tests SourceMojibakeSpotTest` → `BUILD SUCCESSFUL`。
- `./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` → `BUILD SUCCESSFUL`（debug 与 androidTest APK 均产出）。
- `./gradlew.bat :app:lintDebug --console=plain` → `BUILD SUCCESSFUL`。
- `cd doyu-server && mvn test` → `Tests run: 69, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`。

## 未覆盖（按 AGENTS.md 记为未覆盖，不写成已通过）

- 真机专项 smoke：`adb devices -l` 本轮无连接设备，未运行
  `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly`，未产出本轮新截图。
  风险：图片上传、@/# 选择面板、关注切换、大图查看的真机交互与键盘上方布局未经设备验证，仅由构建/单元/lint 与代码路径覆盖。建议接入设备后运行该 smoke 并补截图。
- 贴纸选择、话题运营推荐接口、评论二级回复、设为封面：本轮非目标，保持未接。

## 关键改动文件

- 网络层：`network/DoyuApi.java`、`data/DoyuRepository.java`、`model/FollowResult.java`(新)、`core/IntentExtras.java`
- 页面：`feature/community/PostDetailActivity.java`、`feature/community/PhotoViewerActivity.java`(新)、`feature/community/PickerSheet.java`(新)
- 布局：`res/layout/activity_post_detail.xml`、`res/layout/activity_photo_viewer.xml`(新)、`res/layout/dialog_picker.xml`(新)、`res/layout/item_picker_row.xml`(新)
- 清单：`AndroidManifest.xml`（注册 PhotoViewerActivity）
- 测试：`test/.../core/OpenDesignLayoutMappingTest.java`

## Review 处理（After Review，2026-06-08）

针对审核 review 的逐条处理，全部成立并已修复：

- **B1（Blocking）真机新增交互无 smoke 覆盖**：扩展 instrumentation。
  - 新增 `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer`，串起
    @ 选择 → # 选择 → 图片上传（上传中禁发、成功后可发）→ 提交 → 后端 GET 校验
    新评论回显 `mediaAssets/mentions/topics` → 关注切换并恢复 → 进入 `PhotoViewerActivity`。
  - `PostDetailActivity` 新增 `@VisibleForTesting` 测试钩子（驱动与 UI 相同的生产代码路径，
    不绕过真实 upload/comment/follow 网络调用）。
  - 新增 debug-only `FileProvider`（`app/src/debug/`）提供可控评论图 URI，仅 debug 生效，不进入 release。
  - 说明：本机当前无连接真机（reviewer 的无线设备已退出），该 instrumentation 已随 androidTest APK
    构建通过（`assembleDebugAndroidTest` 绿）；**真机执行与截图仍记为未覆盖**，等接入设备后运行。
- **I1（Important）自关注**：
  - 后端 `UserController.follow()` 增加 self-follow 拦截（`INVALID_ARGUMENT`「不能关注自己」），
    新增 `DouyuBackendContractTests#userCannotFollowThemselves`。
  - Android 进入页面拉取 `me()` 缓存 `currentUserId`，本人作品关注按钮显示「本人」并禁用，
    `toggleFollow` 对本人直接拦截。
- **I2（Important）PickerSheet 请求堆积**：改为主线程 `Handler` 280ms debounce，输入即取消上一个
  pending runnable，executor 任务进入前用 token 丢弃过期查询，`dismiss` 清理 pending。
- **I3（Important）PhotoViewerActivity 系统栏 inset**：根视图加 id，
  `WindowCompat.setDecorFitsSystemWindows(false)` + `systemBars()` inset padding，顶/底栏避开系统栏。
- **M1（Minor）上传中误移除**：上传中点击改为提示「上传中，完成或失败后才能移除」，仅 DONE 可移除、
  FAILED 可重试。

### Review 处理后已运行验证（本机）

- `./gradlew.bat :app:compileDebugJavaWithJavac :app:compileDebugAndroidTestJavaWithJavac :app:compileDebugUnitTestJavaWithJavac --rerun-tasks` → `BUILD SUCCESSFUL`（三套源集全量重编）。
- `./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug` → 全部 `BUILD SUCCESSFUL`。
- `./gradlew.bat :app:testDebugUnitTest --tests OpenDesignLayoutMappingTest --tests SourceMojibakeSpotTest` → `BUILD SUCCESSFUL`。
- 红线扫描（无 .kt、无 compose/mock、改动文件无替换字符乱码）→ 均无命中。
- `cd doyu-server && mvn test` → `Tests run: 70, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`（含新增 self-follow 测试）。
- `adb -s 10.64.241.158:37035 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner` → `OK (1 test)`，`Time: 39.54`。
- FileProvider 合并校验：debug 合并清单 `android:authorities="cn.edu.app.douyu.fileprovider"`。

### Review 处理后真机复验（Re-review，2026-06-08）

- 复审补丁：`RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer` 不再使用旧 seed 路径或旧公共桶硬编码图片作为大图查看证据，改为读取刚提交评论的 `mediaAssets[].publicUrl`；`OpenDesignLayoutMappingTest` 增加守卫。
- 复审补丁：评论图片上传复用 `uploadPresignedBytes(presign.uploadUrl, bytes, mimeType, presign.headers)`，确保 OSS presign 返回的签名头被 PUT 请求携带；`OpenDesignLayoutMappingTest#commentImageUploadUsesPresignedHeaders` 增加守卫。
- 复审补丁：专项 smoke 固定用作者账号 `13900002777` 创建真实作品，再用主账号 `13900001999` 操作，避免 feed 第一条恰好是本人作品导致关注按钮显示「本人」并使关注翻转断言随机失败。
- 真机：`10.64.241.158:37035`，型号 `ELI-AN00`，Android `16`，分辨率 `1200x2664`，density `520`。
- 命令：`adb -s 10.64.241.158:37035 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner`。
- 结果：`OK (1 test)`，耗时 `39.54s`；阶段输出完整到 `realBackendStage=capture-editor-flow-done`。
- 截图目录：`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-composer-features/real-backend-smoke/`。
- Contact sheet：`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-composer-features/post-detail-composer-features-contact-sheet.png`。
- 截图文件：`post_detail_editor_chips.png`、`post_detail_editor_image_uploaded.png`、`post_detail_editor_after_submit.png`、`post_detail_follow_toggled.png`、`post_detail_photo_viewer.png`。
- 视觉复核：编辑器位于键盘上方，@/# chip、图片草稿、发送按钮、提交后评论刷新、关注切换、大图查看均可见；未发现遮挡、乱码、raw HTTP 或假成功状态。

### Review 处理改动文件（增量）

- `doyu-server/.../user/UserController.java`：self-follow 拦截
- `doyu-server/.../DouyuBackendContractTests.java`：self-follow 测试
- `feature/community/PostDetailActivity.java`：currentUserId/本人判断、上传中防误移除、测试钩子、选择回调重构
- `feature/community/PickerSheet.java`：Handler debounce + token 丢弃
- `feature/community/PhotoViewerActivity.java`、`res/layout/activity_photo_viewer.xml`：系统栏 inset
- `app/src/debug/AndroidManifest.xml`、`app/src/debug/res/xml/file_paths.xml`：debug-only FileProvider
- `app/src/androidTest/.../RealBackendSmokeInstrumentedTest.java`：composer 专项
