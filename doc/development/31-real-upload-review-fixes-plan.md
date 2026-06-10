# 真实上传 Review 复改计划

日期：2026-06-10

## 目标

- 修复 `POST /api/v1/uploads/confirm` 中 `usage` 与 `fileKey` 未绑定的问题。
- 避免对象存储路径下重复 confirm 因 `storage_key` 唯一键冲突返回 500。
- 清理当前配置和当前文档中对运行时 `stub` OSS provider 的误导性说明。
- 更新上传 confirm 的 OpenAPI 400 描述，使其覆盖当前真实错误边界。

## 非目标

- 不改变 `POST /api/v1/posts` 请求或响应结构。
- 不新增 Android、Compose、Kotlin、支付、AI、地图或 mock 数据。
- 不恢复运行时 `StubOssProvider`，不允许 `stub` 作为运行时 provider。
- 不声明真实 Aliyun OSS 端到端验证通过，除非本轮实际使用凭证运行链路。

## Review Triage

| ID | 判断 | 证据 | 本轮处理 |
|---|---|---|---|
| I1 | 成立，修复 | `UploadController.confirm()` 只校验 `usage` 和 `fileKey` 各自合法，未校验 `fileKey` 以 `assets/{usage}/` 开头 | 补后端失败用例；新增 `validateFileKeyMatchesUsage(fileKey, usage)` |
| I2 | 成立，修复 | `file_assets.storage_key` 唯一，但 controller 每次 confirm 都新建 `FileAssetEntity`；Aliyun 对象确认可重复通过 | 补 provider 无关的重复 confirm 用例；同 owner/usage/mime/size/width/height 返回已有资产，元数据不一致返回 `INVALID_ARGUMENT` |
| I3 | 成立，修复 | 当前配置样例和文档仍把 `stub` 写成可选 provider，但主代码已删除 `StubOssProvider` | 当前配置/说明统一改为 `local / aliyun`，历史 verification 中的旧记录不作为当前配置说明 |
| M1 | 成立，修复 | `confirm` 的 OpenAPI 400 只描述“用途不支持” | 补充非法 fileKey、文件未上传、大小不匹配等描述 |

## 实施步骤

1. 先补后端测试：
   - `POST_IMAGE` presign + PUT 后用 `AVATAR` confirm 必须返回 400。
   - provider 无关的重复 confirm 必须返回同一个 `fileId`，不能暴露唯一键 500。
2. 跑目标测试确认红灯。
3. 实现后端修复：
   - `confirm` 校验 `fileKey` 与 `usage` 前缀一致。
   - `FileAssetRepository` 增加 `findByStorageKey`。
   - `UploadController` 保存前检查已有资产；同参数幂等返回，不同参数拒绝。
   - 更新 confirm OpenAPI 400 描述。
4. 清理当前配置和文档中的运行时 `stub` provider 说明。
5. 跑目标和回归验证，更新 verification 记录。

## 验证命令

- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=UploadRealObjectContractTests,UploadConfirmIdempotencyTests test`
- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=Upload* test`
- `cd D:\Studio\SpellBean\doyu-server; mvn test`
- `cd D:\Studio\SpellBean; rg -n "local / stub / aliyun|DOUYU_OSS_PROVIDER=stub|stub：|stub:" .env.example doyu-server/README.md doc/development/01-tech-stack.md doc/development/02-architecture.md doc/development/04-backend-services.md -S`
- `cd D:\Studio\SpellBean; rg --files DouYu/app/src | rg "\.kt$"`
- `cd D:\Studio\SpellBean; rg -n "Compose|kotlinx|Navigation Compose|tab_ai|quick_post|PaymentBoundaryActivity|AiFragment|PatternJob" DouYu/app/src/main DouYu/app/src/test -S`
- `cd D:\Studio\SpellBean; git diff --check`
