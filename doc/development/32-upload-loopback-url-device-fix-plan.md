# 上传页真机 Loopback URL 修复计划

日期：2026-06-10

## 问题

真机上传图片失败。后端健康检查正常，电脑端直接跑 `register -> presign -> PUT -> confirm` 成功，但真机 instrumentation 中 `DoyuRepository.uploadPostImage()` 失败：

```text
java.net.ConnectException: Failed to connect to localhost/127.0.0.1:8081
```

这说明当前运行后端返回的 presigned `uploadUrl` 是本地 provider 的 `http://localhost:8081/uploads/temp/...`。电脑可以访问该地址，但 Android 真机中的 `localhost` 指手机自身，无法连到开发机后端。

## 目标

- Android 上传 transport 在 debug/local provider 场景下，把 presigned upload URL 中的 loopback host (`localhost` / `127.0.0.1` / `::1`) 重写为当前 API Base URL 的 host 和 scheme。
- Aliyun 或其他非 loopback presigned URL 必须原样使用，避免破坏真实 OSS 签名。
- 不改变后端接口，不新增 mock fileId，不绕过 PUT。

## 非目标

- 不恢复 stub provider。
- 不改变发帖 API。
- 不声明真实 Aliyun 端到端通过，除非使用真实 Aliyun URL 验证。

## 实施步骤

1. 先补 Android repository 单测：当 presign 返回 `http://localhost:8081/uploads/temp/...` 且 API Base URL 是 `http://10.64.241.153:8081/` 时，实际 PUT 应发往 `10.64.241.153`。
2. 确认单测红灯。
3. 修改 `DoyuRepository` 增加可选 API Base URL，并在 `DoyuApiClient.createRepository()` 传入 `BuildConfig.API_BASE_URL`。
4. 仅当 upload URL host 是 loopback 且 API Base URL host 不是 loopback 时进行 host/scheme/port 重写。
5. 跑 Android repository 单测、真机 instrumentation 上传用例、Android unit test/build 和静态 gate。

## 验证命令

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat "-Pandroid.testInstrumentationRunnerArguments.class=cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#realBackendPostImageUploadReturnsFileId" :app:connectedDebugAndroidTest --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
- `cd D:\Studio\SpellBean; rg --files DouYu/app/src | rg "\.kt$"`
- `cd D:\Studio\SpellBean; rg -n "Compose|kotlinx|Navigation Compose|tab_ai|quick_post|PaymentBoundaryActivity|AiFragment|PatternJob" DouYu/app/src/main DouYu/app/src/test -S`
- `cd D:\Studio\SpellBean; git diff --check`
