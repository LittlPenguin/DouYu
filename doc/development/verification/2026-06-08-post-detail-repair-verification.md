# 2026-06-08 作品详情修复验收记录

## 目标

验证 `PostDetailActivity` 是否符合 `doc/development/open-design/post-detail-comment-toolbar-a.html` 与开发文档要求：真实帖子详情、真实评论列表、真实评论提交、底部静态评论栏、键盘上方真实输入 overlay、点赞/收藏 API 边界和 375dp 左右真机宽度 UI parity。

## 环境

- 分支：`main`
- 真机 serial：`10.64.241.158:41817`
- 真机型号：`ELI_AN00`
- 真机分辨率：`1200x2664`
- 真机 density：`520`
- 后端：复用当前 dev 后端，`http://127.0.0.1:8081/actuator/health` 返回 `UP`
- Android API URL：`http://10.64.241.153:8081/`
- Spring Boot PID：`27980`
- Docker：`douyu-postgres` 与 `douyu-redis` 均为 running / healthy
- 数据策略：不清空 dev volume，不恢复 seed/demo，不使用客户端 MockData

## 差异关闭记录

| 编号 | 页面 | 期望 | 实际修复 | 状态 |
|---|---|---|---|---|
| PDR-001 | 作品详情 | 真机 smoke 使用真实后端和真实评论流 | `captureRealBackendPostDetailOnly` 登录 dev 用户、读取真实 feed postId、写入真实评论、通过 UI 提交真实评论并截图 | Closed |
| PDR-002 | 作品详情 | 用户可见文案不显示 raw `HTTP 401` 或乱码 | `LoadState.LOGIN_REQUIRED` 仍映射 `UiCopy.LOGIN_REQUIRED`；源码残留扫描和 `SourceMojibakeSpotTest` 覆盖作品详情相关 Java/XML/测试 fixture | Closed |
| PDR-003 | 作品详情 | 底部评论栏和评论输入态在 375dp 左右宽度无遮挡 | 后续发现“输入区移动到评论区内”的实现与 Open Design 不一致；真实输入框必须是底部 overlay，并在 `21-post-detail-keyboard-composer-repair-plan.md` / `2026-06-08-post-detail-keyboard-composer-verification.md` 中重新验收 | Superseded |
| PDR-004 | 作品详情 | 作者信息应为设计可读文案，不暴露后端状态字段 | `PostDetailFormatter.authorMeta` 将 `VISIBLE · ISO 时间` 改为 `作品 · yyyy-MM-dd`，截图 `post_detail_real_home.png` 已验证 | Closed |

## 命令结果

### 提交前同步

- `git status --short --branch`：执行时当前分支为 `main`
- `git diff --check`：提交前无 whitespace/patch 错误
- `git commit -m "feat: complete commerce real products flow and verification"`：创建提交 `17d2d8c`
- `git push origin HEAD:main`：已推送到当前分支和 `main`

### 单元与构建

- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`：`BUILD SUCCESSFUL`
- `cd DouYu && .\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain`：`BUILD SUCCESSFUL`
- `cd DouYu && .\gradlew.bat :app:lintDebug --console=plain`：`BUILD SUCCESSFUL`
- `cd doyu-server && mvn test`：`Tests run: 69, Failures: 0, Errors: 0, Skipped: 0`
- `git diff --check`：无输出

### 残留扫描

- `rg --files DouYu/app/src | rg "\.kt$"`：无输出
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app`：无输出
- `rg -n "浣滎|璇︽儏|璇勮|鐐硅|鏀惰|鍙戦|鍥剧墖|HTTP 401" DouYu/app/src/main DouYu/app/src/test DouYu/app/src/androidTest`：无输出

### 真机

- `adb devices -l`：`10.64.241.158:41817 device product:ELI-AN00 model:ELI_AN00`
- `adb -s 10.64.241.158:41817 install -r app\build\outputs\apk\debug\app-debug.apk`：`Success`
- `adb -s 10.64.241.158:41817 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk`：`Success`
- `adb -s 10.64.241.158:41817 shell am instrument -w -r -e class cn.edu.app.douyu.VisualSmokeInstrumentedTest cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner`：`OK (1 test)`，`Time: 126.583`
- `adb -s 10.64.241.158:41817 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner`：`OK (1 test)`，`Time: 15.207`
- `adb -s 10.64.241.158:41817 shell monkey -p cn.edu.app.douyu 1`：App 已启动到 `cn.edu.app.douyu.MainActivity`

说明：一次完整 `RealBackendSmokeInstrumentedTest` 在本轮中超时，且未产出新截图；该结果没有写成通过。本轮对作品详情的最终证据来自专项真实后端真机 smoke。

## 截图证据

目录：

`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-repair/`

文件：

- `real-backend-smoke/post_detail_real_home.png`
- `real-backend-smoke/post_detail_comment_input.png`（旧截图名，已由 `post_detail_keyboard_composer.png` 取代）
- `real-backend-smoke/post_detail_comments_after_submit.png`
- `real-backend-smoke-contact-sheet.png`
- `visual-smoke/*.png`
- `visual-smoke-contact-sheet.png`

截图结论：

- `post_detail_real_home.png`：真实 OSS 拼豆封面、真实标题正文、话题 chip、可读 `作品 · 2026-06-07` 作者 meta、底部静态评论栏。
- `post_detail_comment_input.png`：旧验收截图；后续发现输入框位置属于错误实现，改由 `post_detail_keyboard_composer.png` 验证底部 overlay 与键盘贴合。
- `post_detail_comments_after_submit.png`：真实评论提交后评论数刷新，键盘收起，底部静态评论栏恢复。

## 未覆盖项

- 图片评论上传、`@` 用户选择、`#` 话题选择和贴纸选择仍为开发边界。
- 本轮没有新增点赞/收藏逐按钮真机截图；相关 API 调用路径保留在 `PostDetailActivity`，失败态不伪造本地成功。
- 生产短信、真实支付、生产 AI Provider、地图和完整合规材料不属于本轮作品详情修复范围。
