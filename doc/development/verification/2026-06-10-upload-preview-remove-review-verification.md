# 上传图片回显与发帖去审核验证记录

日期：2026-06-10

## 变更摘要

- Android 上传页上传成功后保存 `FileAsset.publicUrl`，缩略图优先加载真实远端/后端 URL，避免只依赖本地 `Uri` 导致灰色 placeholder。
- Repository 会把 local provider 返回的 loopback `publicUrl` 从 `localhost/127.0.0.1` 重写为 `BuildConfig.API_BASE_URL` 的主机，避免真机 Glide 加载手机自身的 localhost。
- `DoyuRepository` 新增 `uploadPostImageAsset()`，保留旧 `uploadPostImage()` 返回 `fileId` 的兼容行为。
- 后端上传确认新资产状态改为 `PASS`；新发帖和编辑帖直接写入 `VISIBLE`。
- 移除后台帖子/评论审核端点；保留商城商品 `auditStatus` 字段和商品上架过滤历史逻辑。
- 上传页、消息通知、Open Design 和接口文档去掉发帖/上传审核中语义。

## 红灯记录

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest" --console=plain`
  - 失败原因：`DoyuRepository.uploadPostImageAsset(...)` 尚不存在。
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --console=plain`
  - 第二轮红灯：`uploadPostImageAssetRewritesLoopbackPublicUrlToApiHost` 失败，confirm 返回的 `http://localhost:8081/uploads/...` 没有被重写为 LAN API host。
- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=DouyuBackendContractTests#uploadPresignAndConfirmCreateImmediatelyUsableFileAsset+DouyuBackendContractTests#communityPostLikeFavoriteAndCommentFlow+DouyuBackendContractTests#unifiedResponseContractCoversAuthCommunityAndTraceId test`
  - 失败原因：上传 confirm 仍返回 `NEED_MANUAL_REVIEW`，预期为 `PASS`。

## 通过验证

| 命令 | 结果 |
| --- | --- |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest" --console=plain` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --console=plain` | 第二轮 publicUrl 重写测试 `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=DouyuBackendContractTests#uploadPresignAndConfirmCreateImmediatelyUsableFileAsset+DouyuBackendContractTests#communityPostLikeFavoriteAndCommentFlow+DouyuBackendContractTests#unifiedResponseContractCoversAuthCommunityAndTraceId+DouyuBackendContractTests#publishedCommunityPostAppearsInFeedWithTopicNamesAndCoverDimensions test` | `BUILD SUCCESS`；Surefire 方法过滤本次实际执行 1 个用例，完整覆盖见 `mvn test` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\doyu-server; mvn test` | `Tests run: 56, Failures: 0, Errors: 0, Skipped: 0` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain --rerun-tasks` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain --rerun-tasks` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean; rg --files DouYu/app/src \| rg "\.kt$"` | 无输出 |
| `cd D:\Studio\SpellBean; rg -n "Compose\|kotlinx\|Navigation Compose\|tab_ai\|quick_post\|PaymentBoundaryActivity\|AiFragment\|PatternJob" DouYu/app/src/main DouYu/app/src/test -S` | 无输出 |
| `cd D:\Studio\SpellBean; git diff --check` | exit code 0；仅行尾转换 warning，无 whitespace error |

## 真实后端 smoke

后端已重启并保持运行：

- `http://127.0.0.1:8081/actuator/health` 返回 `UP`
- `http://10.64.241.153:8081/actuator/health` 返回 `UP`
- Spring Boot 监听进程：`29124`，Maven 父进程：`4988`

当前 dev 服务实际 presign 返回 local provider URL：`localhost/uploads/temp/...`，并且 confirm public URL 也是 `localhost/uploads/...`。Android repository 已覆盖 upload URL 与 public URL 两类 loopback 重写。使用 LAN URL 执行本地上传 smoke：

```json
{"uploadHost":"localhost","putStatus":200,"confirmCode":"OK","fileIdPresent":true,"auditStatus":"PASS","publicUrlPresent":true,"publicUrlHost":"localhost","postStatus":"VISIBLE","feedContainsPost":true}
```

## 真机校验尝试

- `cd D:\Studio\SpellBean\DouYu; adb devices -l`
  - 结果：设备列表为空，当前 ADB 没有可用真机或模拟器。
- `cd D:\Studio\SpellBean\DouYu; adb start-server; adb connect 10.64.241.158:43281; adb devices -l`
  - 结果：无线调试地址连接失败，错误为目标计算机拒绝连接；设备列表仍为空。
- `cd D:\Studio\SpellBean; Invoke-RestMethod -Uri 'http://10.64.241.153:8081/actuator/health' -TimeoutSec 5`
  - 结果：`{"groups":["liveness","readiness"],"status":"UP"}`，后端 LAN 地址可达。
- `cd D:\Studio\SpellBean; Get-NetTCPConnection -LocalPort 8081 -State Listen`
  - 结果：8081 仍由进程 `29124` 监听。

## 未覆盖

- 真机安装、真实后端 instrumentation smoke 和上传页截图：`adb devices` 无连接设备；上一轮无线调试地址 `10.64.241.158:43281` 当前拒绝连接，未能执行 `installDebug` 或 `connectedDebugAndroidTest`。
- 真实 Aliyun OSS 端到端：当前 dev 服务运行时实际返回 local provider presign URL，本次未证明 Aliyun provider 的真实 PUT/HEAD/public URL 链路。
