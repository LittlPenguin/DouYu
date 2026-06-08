# 2026-06-08 作品详情键盘 Composer 验收记录

## 目标

验证 `PostDetailActivity` 的评论输入行为是否符合
`doc/development/open-design/post-detail-comment-toolbar-a.html`：

- 默认显示底部静态评论栏。
- 点击评论入口后，静态栏隐藏，真实输入框作为底部 overlay 显示。
- 真实输入框贴在系统键盘上方，包含 `图 / @ / # / 发送`。
- 发送真实评论后，键盘收起，静态栏恢复，评论列表刷新。

## 修复摘要

- `activity_post_detail.xml` 已重建为独立底部 overlay 结构：
  - `post_comment_bar`：默认静态评论栏。
  - `post_comment_overlay_container`：键盘上方真实输入框容器。
  - `post_comment_editor` 不在 `post_comment_section` 内。
- `PostDetailActivity` 使用 `adjustNothing`，由 `SystemBarInsets` 统一处理 system bars 和 IME，避免 pre-35 设备系统 resize 与手动 IME padding 叠加。
- `SystemBarInsets` 新增 `applyToContentWithBottomContainers(...)`，同一个 root listener 同时处理 system bars 和 IME，并保留原始 padding。
- `PostDetailActivity` 触发评论时隐藏静态栏、显示真实输入 overlay；成功发送后恢复静态栏。
- `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly` 已覆盖真实键盘输入状态和真实评论发送。

## 命令证据

Android 单元测试：

```text
cd D:\Studio\SpellBean\DouYu
.\gradlew.bat :app:testDebugUnitTest --console=plain
BUILD SUCCESSFUL
```

Android debug / androidTest 构建：

```text
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
BUILD SUCCESSFUL
```

Android lint：

```text
.\gradlew.bat :app:lintDebug --console=plain
BUILD SUCCESSFUL
```

后端健康：

```text
curl.exe -fsS http://127.0.0.1:8081/actuator/health
{"groups":["liveness","readiness"],"status":"UP"}
```

真实 feed：

```text
curl.exe -fsS "http://127.0.0.1:8081/api/v1/posts/feed?page=1&size=20"
code=OK, total=10, first postId=post_5c87f1270a2b4eb58bb30e0a10bebda5
```

真机专项 smoke：

```text
adb -s 10.64.241.158:41817 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
OK (1 test)
```

## 真机信息

- Serial：`10.64.241.158:41817`
- Model：`ELI-AN00`
- Android：`16`
- Physical size：`1200x2664`
- Physical density：`520`

## 截图证据

目录：

`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-keyboard-composer/`

文件：

- `real-backend-smoke/post_detail_real_home.png`
- `real-backend-smoke/post_detail_keyboard_composer.png`
- `real-backend-smoke/post_detail_comments_after_submit.png`
- `post-detail-keyboard-composer-contact-sheet.png`

视觉结论：

- `post_detail_real_home.png`：默认显示底部静态评论栏；静态栏不显示 `图 / @ / # / 发送`。
- `post_detail_keyboard_composer.png`：真实输入框显示在键盘上方，静态栏隐藏，`图 / @ / # / 发送` 可见。
- `post_detail_comments_after_submit.png`：发送后键盘收起，静态评论栏恢复，评论成功状态可见。

## 差异状态

| 编号 | 页面 | 问题 | 状态 |
|---|---|---|---|
| PDK-001 | 作品详情 | 真实输入框曾放在评论区内容流内 | Closed |
| PDK-002 | 作品详情 | 只依赖 `adjustResize`，未显式处理 IME inset | Closed：改为 `adjustNothing` + `WindowInsetsCompat.Type.ime()` |
| PDK-003 | 作品详情 | 真机 smoke 未断言静态栏/真实输入框互斥 | Closed |
| PDK-004 | 文档 | 旧文档仍写“评论区内展开” | Closed |

## 未覆盖项

- 图片评论上传、`@` 用户选择、`#` 话题选择和贴纸选择仍为开发边界。
- 本轮只验证作品详情专项 smoke；其它 18 页 parity 不在本轮改动范围。
