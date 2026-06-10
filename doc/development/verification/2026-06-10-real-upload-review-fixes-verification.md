# 真实上传 Review 复改验证记录

日期：2026-06-10

## 范围

- I1：`confirm` 校验 `usage` 与 `fileKey` 前缀 `assets/{usage}/` 一致。
- I2：重复 confirm 同一对象时避免 `storage_key` 唯一键 500；同 owner 和相同元数据返回已有资产，元数据不一致返回 `INVALID_ARGUMENT`。
- I3：当前配置和当前开发文档不再把运行时 `stub` OSS provider 写成可选项。
- M1：上传 confirm OpenAPI 400 描述补充非法 key、上传未完成和大小不匹配边界。

## TDD 记录

| 阶段 | 命令 | 结果 | 关键输出 |
|---|---|---|---|
| 红灯 | `cd D:\Studio\SpellBean\doyu-server; mvn "-Dtest=UploadRealObjectContractTests,UploadConfirmIdempotencyTests" test` | 失败符合预期 | `Tests run: 8, Failures: 3, Errors: 0`；usage mismatch 仍返回 200，重复 confirm 仍返回 500 |
| 绿灯 | `cd D:\Studio\SpellBean\doyu-server; mvn "-Dtest=UploadRealObjectContractTests,UploadConfirmIdempotencyTests" test` | 通过 | `Tests run: 8, Failures: 0, Errors: 0` |

## 已运行验证

| 命令 | 结果 | 关键输出 |
|---|---|---|
| `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=Upload* test` | 通过 | `Tests run: 8, Failures: 0, Errors: 0` |
| `cd D:\Studio\SpellBean\doyu-server; mvn test` | 通过 | `Tests run: 56, Failures: 0, Errors: 0` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain` | 通过 | exit code 0 |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain` | 通过 | `BUILD SUCCESSFUL` |
| 当前 OSS provider 误导说明静态搜索 | 通过 | 对 `.env.example`、README、当前开发文档和架构 SVG 执行 `rg`；无输出，exit code 1 表示未命中误导性当前配置说明 |
| `cd D:\Studio\SpellBean; rg --files DouYu/app/src \| rg '\.kt$'` | 通过 | 无输出，exit code 1 表示未发现 `.kt` 文件 |
| `cd D:\Studio\SpellBean; rg -n 'Compose\|kotlinx\|Navigation Compose\|tab_ai\|quick_post\|PaymentBoundaryActivity\|AiFragment\|PatternJob' DouYu/app/src/main DouYu/app/src/test -S` | 通过 | 无输出，exit code 1 表示未命中 |
| `cd D:\Studio\SpellBean; git diff --check` | 通过 | exit code 0；仅 `.env.example`、两张 SVG 和既有 `DouyuBackendContractTests.java` 行尾转换 warning，无 whitespace error |

## 覆盖结论

- `UploadRealObjectContractTests.confirmRejectsUsageThatDoesNotMatchFileKeyPrefix` 覆盖 `POST_IMAGE` presign + PUT 后用 `AVATAR` confirm 必须失败。
- `UploadConfirmIdempotencyTests.duplicateConfirmWithSameMetadataReturnsExistingAsset` 覆盖对象存储可重复确认时的幂等返回。
- `UploadConfirmIdempotencyTests.duplicateConfirmWithDifferentMetadataIsRejected` 覆盖同一 `storageKey` 参数不一致时返回 `INVALID_ARGUMENT`，避免数据库唯一键异常外泄。
- 当前配置说明和架构文档里的 OSS provider 列表已统一为 `local / aliyun`。

## 复审记录

- 审核 Agent 只读复审结论：本轮 I1/I2/I3/M1 均已解决，未发现新的 Blocking 或 Important。
- 审核 Agent 残余风险提醒：当前实现覆盖顺序重复 confirm；若两个“首次 confirm”完全并发，仍可能在 `findByStorageKey()` 都未命中后竞争唯一键。本轮 review 关注的是响应丢失后的重复 confirm，不把并发首次确认为本轮阻塞项。

## 未覆盖

- 未使用真实 Aliyun OSS 凭证运行 `presign -> PUT -> HEAD/confirm -> 发帖查询` 端到端链路。
- 复改当时未启动后端服务做人工 Swagger 或真机联调；2026-06-10 已补充真机联调，见下方补充记录。
- 复改当时未新增真机登录态上传页截图证据；2026-06-10 已补充真机截图，见下方补充记录。

## 2026-06-10 真机补充验证

目标设备：`10.64.241.158:43281`，机型显示为 `ELI-AN00 - 16`。

### 启动与安装

| 检查项 | 结果 | 证据 |
|---|---|---|
| ADB 连接 | 通过 | `adb connect 10.64.241.158:43281` 返回 `already connected`；`adb devices` 显示 `10.64.241.158:43281 device` |
| 后端健康 | 通过 | `http://10.64.241.153:8081/actuator/health` 返回 `200 {"groups":["liveness","readiness"],"status":"UP"}` |
| 设备到后端网络 | 通过 | `adb shell ping -c 1 10.64.241.153` 返回 `1 received, 0% packet loss` |
| Debug 安装 | 通过 | `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:installDebug --console=plain` 返回 `BUILD SUCCESSFUL`，并显示 `Installed on 1 device` |
| App 启动 | 通过 | `adb shell am start -n cn.edu.app.douyu/.MainActivity` 后 `mCurrentFocus` 为 `cn.edu.app.douyu/cn.edu.app.douyu.MainActivity` |

### 真机页面烟测

| 页面 | 结果 | 截图 / UI dump |
|---|---|---|
| 社区首页 | 通过 | `output/device-smoke-20260610/home.png`、`output/device-smoke-20260610/home.xml`；真实后端数据加载，底部 `社区 / 商城 / 上传 / 消息 / 我的` 可见 |
| 未登录上传入口 | 通过 | `output/device-smoke-20260610/upload.png`、`output/device-smoke-20260610/upload.xml`；点击上传后出现 `需要登录` 弹窗 |
| 登录后上传页 | 通过 | `output/device-smoke-20260610/post-create.png`、`output/device-smoke-20260610/post-create.xml`；页面显示 `上传帖子`、右侧 `发布`、`选择图片`、`拍照`、话题列表，底部 `上传` 选中 |
| 消息页 | 部分通过 | `output/device-smoke-20260610/messages.png`、`output/device-smoke-20260610/messages.xml`；页面标题 `消息`，底部 `消息` 选中，当前临时账号无通知或会话，显示 `暂无通知或消息。`；未出现旧 `私信 / 通知` Tab、`互关`、`1/3`、`禁发` |
| 我的页 | 通过 | `output/device-smoke-20260610/profile.png`、`output/device-smoke-20260610/profile.xml`；临时真实账号登录态显示 `Device Smoke`、`豆屿用户 · 已登录`、`编辑资料`、`获赞 / 作品 / 关注 / 粉丝`、`点赞作品 / 收藏作品`，底部 `我的` 选中 |

### 运行状态

- 后端由 `doyu-server/start-dev.bat` 启动，按本次验收要求保持运行；8081 监听进程为 `10236`，Docker `douyu-postgres`、`douyu-redis` 均为 `healthy`。
- App 进程日志检查未命中 `FATAL EXCEPTION`、`AndroidRuntime`、`NullPointerException`、`IllegalStateException`、`Unable to start activity`、网络连接异常等关键错误。
- 为继续验收登录后上传页，先通过真实后端注册临时账号，再把真实登录返回的 token 写入 debug App 的 `doyu_session` 偏好文件；未使用 mock session 或本地假数据。

### 仍未覆盖

- 未选择真实图片执行 `presign -> PUT -> HEAD/confirm -> createPost -> feed/profile 查询` 端到端上传闭环。
- 消息页由于临时账号没有真实通知/会话数据，本次只覆盖空态和旧 Tab/旧徽标不出现，未覆盖“通知 section 在上、消息 section 在下”的非空列表视觉截图。
