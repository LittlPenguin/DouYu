# 上传页真机 Loopback URL 修复验证记录

日期：2026-06-10

## 根因

真机上传图片失败并不是后端对象确认失败。当前运行后端返回了本地 provider 的 presigned URL：

```text
http://localhost:8081/uploads/temp/...
```

电脑端直接调用 `register -> presign -> PUT -> confirm` 可以成功，因为电脑的 `localhost` 是后端所在机器；Android 真机里的 `localhost` 指手机自身，因此 OkHttp PUT 失败：

```text
java.net.ConnectException: Failed to connect to localhost/127.0.0.1:8081
```

## 修复

- `DoyuApiClient.createRepository()` 将 `BuildConfig.API_BASE_URL` 传给 `DoyuRepository`。
- `DoyuRepository.uploadPresignedBytes()` 在 presigned URL 是 loopback host 且 API Base URL 不是 loopback host 时，把 upload URL 的 scheme/host/port 重写为 API Base URL。
- 非 loopback 的真实 OSS presigned URL 原样使用，避免破坏 Aliyun 签名。
- `PostCreateActivity` 增加与现有详情页一致的 `@VisibleForTesting` 上传测试入口，只用于 instrumentation 驱动真实上传路径。

## 已运行验证

| 命令 | 结果 | 关键输出 |
|---|---|---|
| `cd D:\Studio\SpellBean; Invoke-RestMethod register/presign + Invoke-WebRequest PUT + confirm` | 通过 | 电脑端当前后端链路返回 `registerCode=OK`、`putStatus=200`、`confirmCode=OK`、`fileIdPresent=true` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat "-Pandroid.testInstrumentationRunnerArguments.class=cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#realBackendPostImageUploadReturnsFileId" :app:connectedDebugAndroidTest --console=plain` | 红灯 | 修复前失败：`Failed to connect to localhost/127.0.0.1:8081` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --console=plain` | 红灯 | 修复前编译失败，缺少 `DoyuRepository(DoyuApi, OkHttpClient, String)` 构造器 |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat "-Pandroid.testInstrumentationRunnerArguments.class=cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#realBackendPostImageUploadReturnsFileId" :app:connectedDebugAndroidTest --console=plain` | 通过 | `BUILD SUCCESSFUL`，设备端直接 repository 上传完成并返回真实 `fileId` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat "-Pandroid.testInstrumentationRunnerArguments.class=cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailCommentInput" :app:connectedDebugAndroidTest --console=plain` | 通过 | `BUILD SUCCESSFUL`，评论图片 UI 上传路径完成 |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat "-Pandroid.testInstrumentationRunnerArguments.class=cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#realBackendPostCreateImageUploadCompletes" :app:connectedDebugAndroidTest --console=plain` | 通过 | `BUILD SUCCESSFUL`，上传页 `PostCreateActivity` 图片上传路径完成 |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:installDebug --console=plain` | 通过 | `Installed on 1 device`；真机 `lastUpdateTime=2026-06-10 15:06:53` |
| `cd D:\Studio\SpellBean; rg --files DouYu/app/src \| rg '\.kt$'` | 通过 | 无输出，exit code 1 表示没有 `.kt` 文件 |
| `cd D:\Studio\SpellBean; rg -n 'Compose\|kotlinx\|Navigation Compose\|tab_ai\|quick_post\|PaymentBoundaryActivity\|AiFragment\|PatternJob' DouYu/app/src/main DouYu/app/src/test -S` | 通过 | 无输出，exit code 1 表示未命中 |
| `cd D:\Studio\SpellBean; git diff --check` | 通过 | exit code 0；仅行尾转换 warning，无 whitespace error |

## 未覆盖

- 未通过系统相册选择一张真实用户图片做人工点选；本轮使用 instrumentation 生成的 PNG 和测试 seam 走真实上传链路。
- 未使用真实 Aliyun presigned URL 复跑手机端 PUT；本轮修复的是本地 provider 返回 loopback upload URL 的真机不可达问题，非 loopback OSS URL 有单测保证不重写。
