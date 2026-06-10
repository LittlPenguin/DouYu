# 作品详情键盘 Composer 修复计划

## 目标

本轮由 Codex 直接完成文档、Android Java/XML 代码、测试、真机校验和验收记录。

Open Design `open-design/post-detail-comment-toolbar-a.html` 是作品详情页 UI 权威：

- 默认只显示底部静态评论栏。
- 静态栏包含 `写下你的评论`、点赞、评论、收藏。
- 静态栏不包含 `图 / @ / # / 发送`。
- 点击静态输入框或评论按钮后，静态栏隐藏。
- 真实输入框作为底部 overlay 显示，并贴在系统键盘上方。
- 真实输入框包含文本输入、`图 / @ / #` 工具和真实 `发送` 按钮。
- 发送调用真实 `POST /api/v1/posts/{postId}/comments`，不得伪造成功。

## 当前偏差与修复方向

上一轮中断前，旧布局把 `post_comment_editor` 放在 `post_comment_section`
评论区内容流内，这会让真实输入框成为页面内展开区域，而不是键盘上方的独立
真实输入区域。本轮修复已将真实输入框移到新的 `post_comment_overlay_container`。

`targetSdk=36` 下不能只依赖 `adjustResize`。本轮将作品详情改为
`adjustNothing`，通过 `WindowInsetsCompat.Type.ime()` 消费键盘 inset，并扩展共享
`SystemBarInsets`，避免 `PostDetailActivity` 直接覆盖 root inset listener 或在
API 26-34 上出现系统 resize 与手动 IME padding 叠加。

## 实施内容

- 重建 `activity_post_detail.xml`：
  - 保留图片 carousel、内容区、评论区、底部静态评论栏。
  - `post_comment_section` 只包含评论列表相关状态。
  - `post_comment_editor` 不在评论区内容流内。
  - 新增 `post_comment_overlay_container` 承载真实输入框。
- 扩展 `SystemBarInsets`：
  - 保留 `applyToContent(Activity)` 原行为。
  - 新增 `applyToContentWithBottomContainers(Activity, View...)`。
  - system bars 和 IME 由同一个 root listener 处理。
  - bottom inset 使用 `Math.max(ime.bottom, bars.bottom)`，只加一次。
  - 保存原始 padding，避免基类 system bar listener 与详情页 IME listener 切换时重复叠加。
- 更新 `PostDetailActivity`：
  - 点击静态入口时隐藏 `post_comment_bar`，显示真实输入 overlay。
  - 发送成功后隐藏真实输入 overlay，恢复静态栏并刷新评论。
  - 发送失败保留草稿。
  - 返回键优先收起真实输入框。
- 更新测试：
  - `OpenDesignLayoutMappingTest` 使用 DOM 判断 XML 父子关系。
  - `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly`
    覆盖默认态、键盘真实输入态和发送后恢复态。

## 验收命令

```bash
cd /d/Studio/SpellBean
git status --short --branch
git diff --check
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app
# scan for common mojibake fragments and Unicode replacement characters in DouYu/app/src

cd /d/Studio/SpellBean/DouYu
./gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest --console=plain
./gradlew.bat :app:testDebugUnitTest --console=plain
./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
./gradlew.bat :app:lintDebug --console=plain
```

真机专项：

```bash
adb devices -l
cd /d/Studio/SpellBean/DouYu
adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <serial> install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s <serial> shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

## 验收标准

- 默认状态只显示底部静态评论栏。
- 输入状态静态栏隐藏，真实输入 overlay 显示。
- 真实输入框贴键盘上方，未被遮挡。
- 空输入禁发，有文本可发送。
- 成功后评论列表刷新、键盘收起、静态栏恢复。
- 失败后草稿保留，不显示 raw `HTTP 401`，不伪造成功。
- 返回键优先收起真实输入框。
