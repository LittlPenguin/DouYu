# 真实上传与底部导航验证记录

日期：2026-06-10

## 范围

- 后端上传确认：`POST /api/v1/uploads/confirm` 必须确认对象已经真实上传且大小匹配后才创建 `FileAsset`。
- Android 上传链路：上传帖子图片必须走 presign -> OkHttp PUT -> confirm，PUT 失败时不得 confirm。
- 底部导航：主页和上传页共用五项导航结构，主页上传项不显示当前内容 Tab 指示条，上传页高亮上传项。
- 架构约束：Android 继续使用 Java + XML，不引入 Kotlin/Compose，不新增客户端 mock fileId 或运行时假数据。

## 已运行验证

| 命令 | 结果 | 关键输出 |
|---|---|---|
| `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=UploadRealObjectContractTests test` | 通过 | `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --tests "cn.edu.app.douyu.network.DoyuApiDetailContractTest" --console=plain` | 先红后绿 | 红测阶段因重复 `uploadPresign`/`uploadConfirm` 方法失败；实现后 `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest" --console=plain` | 先红后绿 | 红测阶段缺少 `include_bottom_nav.xml` 且上传页仍有 `post_nav_*`；实现后 `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.network.DoyuRepositoryUploadTest" --tests "cn.edu.app.douyu.network.DoyuApiDetailContractTest" --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest" --tests "cn.edu.app.douyu.core.RemovedFeatureContractTest" --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=Upload* test` | 通过 | 审核后复跑：`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` |
| `cd D:\Studio\SpellBean\doyu-server; mvn test` | 通过 | 审核后复跑：`Tests run: 53, Failures: 0, Errors: 0, Skipped: 0` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean; rg --files DouYu/app/src \| rg "\.kt$"` | 通过 | 无输出，exit code 1 表示未找到 `.kt` 文件 |
| `cd D:\Studio\SpellBean; rg -n "Compose\|kotlinx\|Navigation Compose\|tab_ai\|quick_post\|PaymentBoundaryActivity\|AiFragment\|PatternJob" DouYu/app/src/main DouYu/app/src/test -S` | 通过 | 无输出，exit code 1 表示未命中 |
| `cd D:\Studio\SpellBean; git diff --check` | 通过 | exit code 0；仅提示 `DouyuBackendContractTests.java` 下次 Git touch 时会做 CRLF/LF 转换，无 whitespace error |

## 覆盖结论

- 本地 OSS 路径覆盖：已覆盖未 PUT 直接 confirm、空文件或 size 不匹配 confirm、非空且 size 匹配 confirm 成功并可访问本地文件。
- Aliyun OSS 路径覆盖：已覆盖 provider 的 public URL 兼容测试；真实 HEAD 对象存在与 size 匹配逻辑已编译通过，但未连接真实 Aliyun OSS 做端到端 PUT/HEAD/confirm 烟测。
- Android repository 覆盖：已覆盖 presign 后发出 PUT、PUT body 等于原图字节、presign headers 透传、PUT 非 2xx 不 confirm、PUT 成功后 confirm 并返回真实 `fileId`。
- 底部导航覆盖：已覆盖共享 `include_bottom_nav.xml`、五项顺序、主页上传 indicator 默认隐藏、上传页使用共享 ID 并在 `PostCreateActivity` 中高亮上传。
- 构建覆盖：Android debug 包和 androidTest 包均已编译通过；后端全量测试通过。

## 审核后复改

- 审核 B1：`StubOssProvider` 仍可作为运行时 provider 创建假资产。
  - 处理：删除主代码 `StubOssProvider`，移除 `OssProviderConfig` 的 stub bean；`application-test.yml` 和 `application-qa-empty.yml` 默认改为 local provider；测试中的 confirm 流程补真实本地 PUT。
  - 验证：`OssProviderConfigTest.stubProviderCannotBeSelectedAsRuntimeProvider`、`OssProviderConfigTest.qaEmptyProfileDoesNotDefaultToStubProvider`、后端全量 `mvn test` 通过。
- 审核 B2：本地上传/confirm 路径缺少边界校验。
  - 处理：`UploadController` 对客户端 `fileName` 做安全化并生成 `assets/{usage}/{fileId}/{safeName}` key；confirm 前校验 `fileKey`；`LocalOssUploadController` 与 `LocalOssProvider` 均使用 `safeResolve()`，拒绝 `..`、反斜杠、越界路径。
  - 验证：`UploadRealObjectContractTests.presignSanitizesClientFileNameBeforeBuildingStorageKey`、`UploadRealObjectContractTests.localProviderRejectsUnsafeFileKeyBeforeMovingFiles`、`mvn -Dtest=Upload* test`、`mvn test` 通过。
- 复审结论：审核 Agent 复审确认 B1/B2 已解决，无新的 Blocking 或 Important 问题。

## 未覆盖

- 未做真机或模拟器 UI 操作烟测：没有新鲜设备截图或手动路径证据。
- 未做真实 Aliyun OSS 端到端上传烟测：没有使用真实 `.env` 凭证执行 presign -> PUT -> HEAD/confirm -> 发帖查询链路。
- 未启动后端服务做人工联调；本轮只运行自动化测试和构建命令。
