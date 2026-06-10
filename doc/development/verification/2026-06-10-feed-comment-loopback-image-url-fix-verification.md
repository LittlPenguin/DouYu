# 社区与评论图片 Loopback URL 修复验证

## 现象与根因

当前运行后端的 `GET /api/v1/posts/feed?page=1&size=5` 返回了最新上传帖子的 `coverImageUrl=http://localhost:8081/uploads/...`。该 URL 在电脑本机可访问，但 Android 真机中的 `localhost` 指手机自身，Glide 加载失败后显示灰色占位。

## 修复内容

- 在 Android `DoyuRepository` 读响应边界统一归一化社区帖子和评论图片 URL：
  - `Post.coverImageUrl`
  - `Post.imageUrls`
  - `Comment.mediaAssets.publicUrl`
- 仅当图片 URL host 是 `localhost`、`127.0.0.1`、`::1` 等 loopback，且当前 API base host 不是 loopback 时改写为 API base 的 scheme/host/port。
- Aliyun / CDN / 其他非 loopback URL 保持原样。

## 验证命令

1. Red 阶段：
   - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.network.DoyuRepositoryImageUrlNormalizationTest`
   - 结果：3 个新增测试按预期失败，证明当前 repository 会把 loopback 图片 URL 原样交给 UI。
2. Green 阶段：
   - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.network.DoyuRepositoryImageUrlNormalizationTest`
   - 结果：通过。
3. 上传链路回归：
   - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.network.DoyuRepositoryImageUrlNormalizationTest --tests cn.edu.app.douyu.network.DoyuRepositoryUploadTest`
   - 结果：通过。
4. Android JVM 单测：
   - `.\gradlew.bat :app:testDebugUnitTest`
   - 结果：通过。
5. Android debug 构建：
   - `.\gradlew.bat :app:assembleDebug`
   - 结果：通过。
6. Android 架构硬规则：
   - `Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt`
   - 结果：无输出，`DouYu/app/src` 无 `.kt` 文件。
   - `rg -n "Compose|compose|kotlinx|DataStore|coroutines|serialization|Jetpack Compose|androidx\.compose" DouYu\app\src`
   - 结果：无匹配。
7. 后端测试：
   - `mvn test`
   - 结果：56 tests, 0 failures, 0 errors, 0 skipped。

## 未覆盖

- 本轮未重新采集真机截图；Open Design 结构未改动，视觉 parity 依赖已有 JVM 布局映射测试和后续真机 smoke。
