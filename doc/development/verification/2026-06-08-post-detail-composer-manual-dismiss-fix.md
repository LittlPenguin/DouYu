# 作品详情 Composer 手动收起补丁验收记录（2026-06-08）

对应人工验收问题：真实输入框展开后，点击页面空白区域不会收起；用户手动下拉隐藏输入法后，底部静态评论栏也不会恢复。

## 修复内容

- `SystemBarInsets.applyToContentWithBottomContainers(...)` 新增 IME 可见性回调，仍保持单一 root `WindowInsets` listener，避免覆盖系统栏 inset。
- `PostDetailActivity` 在真实输入框显示期间记录 IME 曾经可见；当 IME 从可见变为隐藏时，收起真实输入框并恢复底部静态评论栏。
- `post_detail_scroll` 增加空白点击收起路径，复用 `collapseCommentInputIfVisible()`，不改变发送成功、发送失败、返回键、图片上传、@/#、关注和大图查看原有行为。
- `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer` 新增空白点击专项步骤和截图 `post_detail_editor_after_blank_tap.png`。

## 已运行验证

- `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest --console=plain`：`BUILD SUCCESSFUL`。
- `.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain`：`BUILD SUCCESSFUL`。
- `adb -s 10.64.241.158:39577 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\debug\app-debug.apk`：`Success`。
- `adb -s 10.64.241.158:39577 install -r D:\Studio\SpellBean\DouYu\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk`：`Success`。
- `adb -s 10.64.241.158:39577 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner`：`OK (1 test)`，`Time: 46.599s`。
- `.\gradlew.bat :app:testDebugUnitTest --console=plain`：`BUILD SUCCESSFUL`。
- `.\gradlew.bat :app:lintDebug --console=plain`：`BUILD SUCCESSFUL`。
- `git diff --check`：无输出。
- `rg --files DouYu/app/src | rg "\.kt$"`：无输出。
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app`：无输出。
- `rg -n "浣滄|浣滎|浣滂|浣滘|浣滃|璇勮|鍙戦|鐐硅|鏀惰|姝ｅ湪|HTTP 401|�" DouYu/app/src/main DouYu/app/src/test DouYu/app/src/androidTest`：无输出。

- `cd doyu-server && mvn test`：`Tests run: 70, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`。

## 真机与截图证据

- 真机 serial：`10.64.241.158:39577`，`adb devices -l` 显示 `product:ELI-AN00 model:ELI_AN00 device:HNELI`。
- 截图目录：`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-composer-features/manual-dismiss-fix/real-backend-smoke/`。
- 新增关键截图：`post_detail_editor_after_blank_tap.png`，可见真实输入框已收起，底部静态评论栏恢复。
- Contact sheet：`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-composer-features/manual-dismiss-fix/post-detail-composer-manual-dismiss-contact-sheet.png`。

## 状态

Closed。
