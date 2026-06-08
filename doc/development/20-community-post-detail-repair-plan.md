# 作品详情修复阶段记录

> 本文档原为 2026-06-08 作品详情修复执行计划。修复和真机验证已经完成，开放 checklist 已按任务要求收敛为已验证事实与未覆盖项；详细命令、截图和差异关闭记录见 `verification/2026-06-08-post-detail-repair-verification.md`。

## 已验证目标

对照 `doc/development/open-design/post-detail-comment-toolbar-a.html` 和 `doc/development` 文档，本轮完成 Android Java/XML `PostDetailActivity` 的作品详情补齐：

- 图片优先 gallery / carousel 保持首屏权重，使用真实 `Post.imageUrls`，无多图时回退到 `coverImageUrl`。
- 顶部标题固定为 `作品详情`，作者、标题、正文、话题、封面和互动计数来自真实 `/api/v1/posts/{postId}`。
- 作者 meta 从后端 raw `VISIBLE · ISO 时间` 改为可读 `作品 · yyyy-MM-dd`。
- 评论列表来自真实 `/api/v1/posts/{postId}/comments`，支持加载、空态、错误重试和未登录边界。
- 评论行通过 `PostDetailFormatter` 渲染作者、正文、`@`、`#`、贴纸文字和图片评论摘要。
- 底部静态评论入口承载 `点赞 / 评论 / 收藏` 操作，移除了评论区上方独立互动卡。
- 点击底部评论入口或评论按钮后，真实文本输入区在评论区内展开，避免被软键盘遮挡。
- 提交评论调用真实 `POST /api/v1/posts/{postId}/comments`，成功后清空输入、收起键盘并刷新评论列表。
- 点赞/收藏调用真实 like/favorite API；接口失败时不伪造本地成功状态。
- `图 / @ / #` 当前保持开发边界提示，不写入请求、不伪造选择结果。

## 修改范围

- `DouYu/app/src/main/AndroidManifest.xml`
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostDetailFormatter.java`
- `DouYu/app/src/main/res/layout/activity_post_detail.xml`
- `DouYu/app/src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java`
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/OpenDesignLayoutMappingTest.java`
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/SourceMojibakeSpotTest.java`
- `DouYu/app/src/test/java/cn/edu/app/douyu/feature/community/PostDetailFormatterTest.java`
- `doc/development/13-ui-screen-blueprints.md`
- `doc/development/current-status.md`
- `doc/development/verification/2026-06-08-post-detail-repair-verification.md`

## 已验证命令

本轮已执行并记录：

- `git status --short --branch`
- `rg --files DouYu/app/src | rg "\.kt$"`
- `rg -n "compose|Composable|Navigation Compose|kotlinx|MockData|coil\.compose|paging\.compose" DouYu/app`
- `rg -n "浣滎|璇︽儏|璇勮|鐐硅|鏀惰|鍙戦|鍥剧墖|HTTP 401" DouYu/app/src/main DouYu/app/src/test DouYu/app/src/androidTest`
- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`
- `cd DouYu && .\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain`
- `cd DouYu && .\gradlew.bat :app:lintDebug --console=plain`
- `cd doyu-server && mvn test`
- `adb -s 10.64.241.158:41817 shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner`

## 真机证据

截图目录：

`doc/development/verification/android-java-xml-screenshots/2026-06-08-post-detail-repair/real-backend-smoke/`

关键截图：

- `post_detail_real_home.png`
- `post_detail_comment_input.png`
- `post_detail_comments_after_submit.png`
- `real-backend-smoke-contact-sheet.png`

真机专项 smoke 使用真实后端 feed postId，先写入一条真实评论，再通过 UI 输入并提交一条真实评论。最终截图显示：

- 首页有真实 OSS 拼豆封面和可读作者 meta。
- 输入态中评论输入框、`图 / @ / #` 工具和发送按钮位于键盘上方，无遮挡。
- 提交后键盘收起，评论数刷新，底部静态评论栏恢复。

## 未覆盖项

- 图片评论上传、`@` 用户选择、`#` 话题选择和贴纸选择仍是开发边界。
- 真机专项 smoke 覆盖真实评论提交；点赞/收藏 API 行为由 Activity 代码路径和单元/构建 gate 覆盖，本轮没有新增逐按钮真机截图。
- 完整生产短信、真实支付、生产 AI Provider、地图/地址和完整合规材料仍按全局文档列为未来能力，不在本轮写成已完成。
