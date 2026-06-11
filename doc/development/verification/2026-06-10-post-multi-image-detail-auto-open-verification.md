# 多图作品详情与发布后自动跳转验证

## 修复内容

- 后端帖子响应新增 `imageUrls`，按 `mediaFileIds` 顺序输出所有可用上传图片 `publicUrl`。
- `coverImageUrl` 和封面尺寸继续来自第一张媒体，兼容社区瀑布流封面。
- Android 发帖成功后，如果返回 `postId`，立即打开 `PostDetailActivity` 并传入 `IntentExtras.POST_ID`，随后结束上传页。
- 如果后端异常返回空 `postId`，上传页保留原成功 fallback 状态和成功操作区。

## TDD 记录

- Red:
  - `mvn -Dtest=DouyuBackendContractTests#publishedCommunityPostAppearsInFeedWithTopicNamesAndCoverDimensions test`
    - 失败原因：创建响应没有 `imageUrls`。
  - `.\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.postDetailAndAuthContractsRemainRealBackendDriven`
    - 失败原因：`PostCreateActivity` 没有 `openCreatedPost(post)` / `PostDetailActivity` / `IntentExtras.POST_ID` 发布成功跳转契约。
- Green:
  - 两个目标测试在实现后均通过。

## 验证命令

1. 后端全量测试：
   - `mvn test`
   - 结果：56 tests, 0 failures, 0 errors, 0 skipped。
2. Android JVM 单测：
   - `.\gradlew.bat :app:testDebugUnitTest`
   - 结果：通过。
3. Android debug 构建：
   - `.\gradlew.bat :app:assembleDebug`
   - 结果：通过。
4. Android Java/XML 架构硬规则：
   - `Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt`
   - 结果：无输出。
   - `rg -n "Compose|compose|kotlinx|DataStore|coroutines|serialization|Jetpack Compose|androidx\.compose" DouYu\app\src`
   - 结果：无匹配。
5. 真机设备检查：
   - `adb devices`
   - 结果：`List of devices attached` 下无设备。

## 未覆盖

- 未执行真机多图发布 smoke，因为当前没有连接的 Android 设备。
