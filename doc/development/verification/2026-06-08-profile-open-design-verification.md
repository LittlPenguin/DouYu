# 2026-06-08 · “我的”页 Open Design 还原验证

工作树分支：`profile-open-design`。范围：`profile-a.html` / `profile-edit-a.html` 的 Java/XML 还原，外加 `/api/v1/users/me` 真实统计补全。

## 改动清单

后端：
- `server/user/UserController.me()`：在真实资料视图上补 `likedCount`（用户可见作品 `likeCount` 求和）、`postCount`（用户可见作品数）。不改 `AuthService`，向后兼容。
- `doc/development/05-api-contract.md`：记录 `/me` 新增统计字段与三 Tab 的真实接口映射。

Android：
- `feature/profile/ProfileFragment.java` + `res/layout/fragment_profile_home.xml`：重写为自包含页面——hero（头像/昵称/副标题/编辑资料）、统计区 `获赞 / 作品 / 关注 / 粉丝`、资产 Tab `我的图纸 / 点赞作品 / 收藏作品`、2 列瀑布流，逐 Tab 真实接口与 加载/空/登录/错误 状态。
- `feature/profile/ProfileEditActivity.java` + `res/layout/activity_profile_edit.xml`：重写为可用编辑页——头像更换（真实 presign→PUT→confirm 上传）、昵称（空禁用保存）、简介、年龄段（展示）、城市/兴趣标签（UI-only）、保存走真实 `PATCH /me`，含上传中/上传失败重试/保存失败保留草稿/保存成功回刷状态机。
- `feature/profile/AssetCard.java`、`ProfileAssetAdapter.java`、`res/layout/item_profile_asset.xml`：资产卡片（缩略图 + 图纸/帖子/收藏 标签 + 标题 + meta）。
- 数据层：`DoyuApi` 新增 `favoritePosts` 与上传三接口；`DoyuRepository` 新增 `favoritePosts()`、`updateMe(…, avatarFileId)`、`uploadAvatar(...)`；模型新增 `UploadPresignRequest/Response`、`UploadConfirmRequest`、`FileAsset`，`UserProfile` 补 `ageGroup/level/isMinor`，`UpdateProfileRequest` 补 `avatarFileId`。
- 资源：`bg_avatar_circle / bg_stat_cell / bg_asset_tab / bg_asset_tab_on / bg_asset_card`。

## 已执行的验证（含真实输出）

- Android 编译：`./gradlew :app:assembleDebug -x lint` → `BUILD SUCCESSFUL in 23s`，产物 `DouYu/app/build/outputs/apk/debug/app-debug.apk`（约 10MB）。
- 后端测试：`mvn -q test` → 退出码 0；surefire 共 9 个测试类、63 项，`Failures: 0, Errors: 0`（含 `DouyuBackendContractTests` 25 项、`QaEmptyBackendContractTests` 5 项契约测试）。`/me` 统计补全未破坏既有契约断言。

## 真实数据边界（不得宣称为已完成）

- `获赞 / 作品`：现由后端真实聚合返回；无作品时为 `0`（合法空态）。
- `avatarUrl` 仍为 fileId：`/me` 不返回头像公网 URL，本页头像默认走渐变占位；未在本工作树扩展后端头像 URL 解析。
- 头像上传链路（presign→PUT→confirm→PATCH avatarFileId）针对本地 OSS stub 实现，**仅编译通过，未做真机端到端校验**；上传失败时保留旧头像并提供重试，保存仍可仅更新昵称/简介。
- 城市/地区、兴趣标签：UI-only，页面显式标注，不入 `PATCH /me`，不假成功。

## 未覆盖项（按要求不做真机校验，记录为未覆盖）

- 真机/真实后端端到端校验：**未覆盖**。设备唯一且多工作树并行，本轮不占用设备；待统一设备时再做 Open Design parity 真机比对并回填 `2026-06-07-open-design-parity-matrix.md` 的 `profile-a` / `profile-edit-a` 行。
- 因此 parity 状态本轮记为：Java/XML 已按设计重写并通过构建，真机 parity **尚未复核**，不标记为 Covered。
