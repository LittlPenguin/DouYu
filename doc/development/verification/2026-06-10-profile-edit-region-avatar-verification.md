# 编辑资料地区与头像回显验证记录

日期：2026-06-10

## 变更摘要

- 编辑资料页删除年龄段和兴趣标签。
- 城市/地区改为真实手动资料字段，可从常用城市选择或自行填写，并通过 `PATCH /api/v1/users/me` 保存到后端。
- 后端新增 `users.region` 字段和 Flyway 迁移 `V13__profile_region.sql`。
- `PATCH /api/v1/users/me` 保存 `nickname`、`bio`、`avatarFileId`、`region`；头像文件必须属于当前用户且用途为 `AVATAR`。
- `GET /api/v1/users/me` 和登录用户视图返回 `avatarUrl` 时解析为 `FileAsset.publicUrl`，不再把头像 `fileId` 当作 URL 返回。
- 社区作者和评论提及用户头像也改为解析 `FileAsset.publicUrl`，保持头像显示口径一致。
- Open Design `profile-edit-a.html` 与开发文档同步为头像、昵称、简介、手动城市/地区四个编辑字段。

## 红灯记录

- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=DouyuBackendContractTests#profileUpdatePersistsManualRegionAndReturnsResolvedAvatarUrl test`
  - 失败原因：`PATCH /users/me` 不保存/返回 `region`；`avatarUrl` 仍为头像文件 id。
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.uploadAndProfileContractsStayAvailable`
  - 失败原因：编辑资料页和 Android 模型尚未包含 `region`，布局仍保留年龄段、兴趣标签和地区 UI-only 文案。

## 通过验证

| 命令 | 结果 |
| --- | --- |
| `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=DouyuBackendContractTests#profileUpdatePersistsManualRegionAndReturnsResolvedAvatarUrl test` | `BUILD SUCCESS`，`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat --no-daemon :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.uploadAndProfileContractsStayAvailable` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat --no-daemon --console=plain --rerun-tasks :app:testDebugUnitTest` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat --no-daemon --console=plain :app:assembleDebug` | `BUILD SUCCESSFUL` |
| `cd D:\Studio\SpellBean\doyu-server; mvn -DskipTests package` | `BUILD SUCCESS` |
| `cd D:\Studio\SpellBean; Get-ChildItem -Path DouYu\app\src -Recurse -Filter *.kt` | `NO_KT_FILES` |
| `cd D:\Studio\SpellBean; rg -n "Compose\|Navigation Compose\|androidx\.compose\|kotlinx\.coroutines\|kotlinx\.serialization\|DataStore" DouYu\app\src -S` | `NO_COMPOSE_OR_KOTLIN_RUNTIME_REFS` |
| `cd D:\Studio\SpellBean; git diff --check` | exit code 0；仅行尾转换 warning，无 whitespace error |

## 未通过或未覆盖

- `cd D:\Studio\SpellBean\doyu-server; mvn test`
  - 结果：未通过。
  - 当前失败为订单/购买相关用例，不属于本次资料编辑/头像回显改动：
    - `CommercePurchaseContractTests.cartCheckoutRemovesCartItemsAndCancelReleasesLockedStock`
    - `CommercePurchaseContractTests.immediateOrderCreatesCreatedOrderWithAddressSnapshotAndLocksStock`
    - `CommercePurchaseContractTests.orderRejectsMissingManualAddressAndPlayerProductImmediatePurchase`
    - `DouyuBackendContractTests.orderCreationIsIdempotentAndGuardsInventoryAndCancelState`
  - 失败现象主要是订单地址参数校验返回 `400` 或库存冲突返回 `409`，由当前工作树内订单/购买改动影响；本次资料专项后端合同已单独通过。
- 真机安装、真实后端手动编辑资料、头像上传后“我的”页截图：未运行。当前验证覆盖到 Android 单元/构建和后端 MockMvc 合同，不包含设备截图证据。

## 源码检查

- `activity_profile_edit.xml` 不再包含 `@+id/age_value`、`@+id/interest_tags`、`年龄段`、`兴趣标签`、`UI-only，不保存到后端`。
- 编辑资料页保留 `@+id/region_input` 和 `@+id/region_choices`。
- `UpdateProfileRequest` 和 `UserProfile` 均包含 `region` 字段。
- 后端响应仍隐藏 `avatarFileId`，展示端使用 `avatarUrl`。
