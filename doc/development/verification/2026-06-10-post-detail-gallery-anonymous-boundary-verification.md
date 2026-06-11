# 作品详情图片滑动与匿名互动边界验证

## 修复内容

- Android 作品详情页主图区域支持横向滑动切换上一张/下一张，同时保留左右按钮、缩略图点击和点击进入全屏查看器。
- 缩略图导航条新增滚动容器 id，切图后同步粉色选中边框，并自动滚动到当前缩略图附近。
- 主图从 `centerCrop` 改为 `fitCenter`，画廊 frame 开启 `clipToOutline`，避免图片裁切或越过画廊视觉边界。
- 后端放开 `GET /api/v1/posts/{postId}/comments` 匿名读取；详情 GET 和评论 GET 可匿名访问，点赞、收藏、发评论等写操作仍返回 401。

## TDD 记录

- Red:
  - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postDetailAndAuthContractsRemainRealBackendDriven`
    - 失败原因：`PostDetailActivity` 缺少 gallery 滑动契约、缩略图自动滚动契约，详情 XML 仍为 `centerCrop` 且没有缩略图滚动 id / 裁剪边界。
  - `mvn -Dtest=DouyuBackendContractTests#anonymousUsersCanReadPostDetailAndCommentsButCannotMutateInteractions test`
    - 失败原因：匿名 `GET /api/v1/posts/{postId}/comments` 返回 401。
- Green:
  - 上述两个目标测试在实现后均通过。

## 验证命令

- 目标 Android 契约测试：
  - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postDetailAndAuthContractsRemainRealBackendDriven`
  - 结果：通过。
- 目标后端契约测试：
  - `mvn -Dtest=DouyuBackendContractTests#anonymousUsersCanReadPostDetailAndCommentsButCannotMutateInteractions test`
  - 结果：1 test, 0 failures, 0 errors, 0 skipped。
- 完整验证：
  - `mvn test`
  - 结果：57 tests, 0 failures, 0 errors, 0 skipped。
  - `.\gradlew.bat :app:testDebugUnitTest`
  - 结果：通过。
  - `.\gradlew.bat :app:assembleDebug`
  - 结果：通过。
  - `Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt`
  - 结果：无输出。
  - `rg -n "Compose|compose|kotlinx|DataStore|coroutines|serialization|Jetpack Compose|androidx\.compose" DouYu\app\src`
  - 结果：无匹配。
  - `git diff --check -- <本次相关文件>`
  - 结果：退出码 0；提示 `DouyuBackendContractTests.java` 下次被 Git 触碰时会做 CRLF/LF 规范化。
  - `adb devices`
  - 结果：`List of devices attached` 下无设备。

## 未覆盖

- 未执行真机/模拟器多图详情滑动和匿名互动 smoke，因为当前没有连接的 Android 设备。
