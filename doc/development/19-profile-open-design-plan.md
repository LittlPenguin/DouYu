# 19 · “我的”页 Open Design 还原开发计划

> 工作树：`profile-open-design`（路径 `…-wt-profile`）。本计划仅覆盖“我的”页（Profile）与“编辑资料”页（Profile Edit）。
> 顶栏 `+ / 我的 / 搜索 / 快捷菜单`、底部导航、设置入口均属 `MainActivity` 共享脚手架，已实现且符合设计，本工作树不重写。
> 消息 / 社区 / 商城 / AI / 设置 等页面由其它工作树负责，本工作树不触碰其专属代码与布局。

## 0. 设计权威与范围

- UI 权威：`doc/development/open-design/profile-a.html`、`profile-edit-a.html`。
- 强约束（`AGENTS.md` 18–25）：禁用客户端 mock；空结果渲染空态/登录态/错误态；不得把未完成能力宣称为已完成；城市/地区、兴趣标签为 UI-only；不得假成功。
- 范围内文件（仅本工作树修改）：
  - `feature/profile/ProfileFragment.java` + `res/layout/fragment_profile_home.xml`
  - `feature/profile/ProfileEditActivity.java` + `res/layout/activity_profile_edit.xml`
  - `model/UserProfile.java`、`model/UpdateProfileRequest.java`（追加字段）
  - `network/DoyuApi.java`、`data/DoyuRepository.java`（追加接口，向后兼容）
  - 新增 `model/*`（上传请求/响应）、新增若干 `res/drawable`、`core/UiCopy.java` 文案
  - 后端 `server/user/UserController.java` 的 `me()`（仅追加真实统计，最小改动）
- 范围外：底部导航、顶栏、quick-menu、Settings*、消息/社区/商城/AI 页面与其后端控制器。

## 1. 后端真实数据现状（已核实）

| 数据 | 来源 | 现状 |
| --- | --- | --- |
| 昵称/头像/简介 | `GET /api/v1/users/me` | 真实；`avatarUrl` 实为 fileId（非 URL）|
| 关注数 / 粉丝数 | `GET /api/v1/users/me` | 真实（`followingCount` / `followerCount`）|
| 获赞数 / 作品数 | `GET /api/v1/users/me` | **后端当前不返回**（需本计划在 `me()` 内补真实统计）|
| 年龄段 | `GET /api/v1/users/me` → `ageGroup` | 真实（`AGE_16_17` / `AGE_18_PLUS`）|
| 点赞作品 | `GET /api/v1/users/me/liked-posts` | 真实分页 Post 列表 |
| 收藏作品 | `GET /api/v1/users/me/favorite-posts` | 真实分页 Post 列表（Android 旧 `favoritePatterns()` 指向 **不存在** 的 `/me/favorite-patterns`，无调用方，本计划改用 favorite-posts）|
| 我的图纸 | `GET /api/v1/patterns/jobs` | 真实，用户自己的 AI 拼豆任务（含 `patternAsset`）|
| 头像上传 | `POST /uploads/presign` → `PUT uploadUrl` → `POST /uploads/confirm` → `PATCH /me {avatarFileId}` | 本地 OSS stub 支持真实 PUT（`/uploads/temp/**`）|

**三个资产 Tab 的真实接口映射**：我的图纸 → `patternJobs()`；点赞作品 → `likedPosts()`；收藏作品 → `favoritePosts()`（新增）。

## 2. “我的”页（profile-a.html）目标结构

自上而下（顶栏由 MainActivity 提供，不在 Fragment 内）：

1. **个人资料头部 hero**：头像 56dp（圆形，Glide 加载，缺失走占位）；昵称（H3，加粗）；副标题“拼豆爱好者 · 已登录 / 未登录”；右侧“编辑资料”胶囊按钮 → `ProfileEditActivity`。
2. **统计区 stat-grid**：四格等宽，**顺序固定 获赞 / 作品 / 关注 / 粉丝**；数字加粗 + 灰色标签；轻容器，不卡片套卡片。
3. **资产 Tab 段控**：`我的图纸 / 点赞作品 / 收藏作品` 三段；选中态高亮（petal_deep 文字 + 下划线/底色），未选中为 muted。
4. **资产内容区**：单 RecyclerView，2 列瀑布流（复用 `SummaryAdapter` masonry + `SummaryItem`）。按当前 Tab 加载对应接口。
5. **状态**：loading / 内容 / 空态 / 登录态 / 错误态（带重试），逐 Tab 独立；未登录展示登录引导，不显示假头像/假数据。

资产卡片映射 `SummaryItem`：
- 我的图纸（PatternJob）：title=`patternAsset.title`/`inputName`；subtitle=`paletteName · widthCells×heightCells · totalBeads色`/状态；image=`patternAsset.previewImageUrl`/`sourceImageUrl`。
- 点赞作品（Post）：title=`title`；subtitle=`author.nickname · 赞 likeCount`；image=`coverImageUrl`。
- 收藏作品（Post）：同点赞作品。

删除展示（设计要求）：我的订单 / 评论作品 / 关注作品入口；正文内不再展示“设置”入口（设置走顶栏 quick-menu，已存在）。

## 3. “编辑资料”页（profile-edit-a.html）目标结构

顶栏（`include_page_toolbar` + 右侧保存按钮，复用 `XmlPageActivity` 的返回/标题）：左返回、中“编辑资料”、右“保存”（昵称为空时禁用）。

字段与规则：
1. **头像 72dp + “更换头像”**：相册选图（`GetContent`）→ presign → PUT → confirm 得 `fileId`；上传中保存禁用并显示“头像上传中”；失败保留旧头像 + 重试/删除新图；成功后保存时随 `avatarFileId` 一起 PATCH。不假成功。
2. **昵称**：单行 `EditText`；为空 → 显示“昵称不能为空”并禁用保存。
3. **简介**：多行 `EditText`。
4. **年龄段**：只读展示（来自 `ageGroup`，映射“16+/18+”，标注“仅展示，不做实名”）。
5. **城市/地区**：UI-only，明确标注“UI-only，不接地图定位”，不入 PATCH。
6. **兴趣标签**：UI-only chips，明确标注不保存到后端，不入 PATCH。

保存流程（`PATCH /api/v1/users/me`，字段：nickname、bio、可选 avatarFileId）：
- 成功 → 返回“我的”页并刷新（`setResult` + 我的页 `onResume`/`ActivityResultLauncher` 重载）。
- 失败 → 保留草稿，不返回，显示“保存失败，请重试”+ 重试。
- 头像上传失败不阻断昵称/简介保存（仅不带 avatarFileId）。

## 4. 任务分解（按依赖顺序）

- **P0 文档先行**：本计划文档；在 `05-api-contract.md` 标注 `/me` 新增 `likedCount/postCount`、收藏作品改用 favorite-posts（AGENTS：后端/Android 改动前更新文档）。
- **P1 后端真实统计**：`UserController.me()` 内对返回 view 追加 `likedCount`（用户可见作品 `likeCount` 之和）、`postCount`（用户可见作品数）；复用既有 `postRepository` 与可列出判定，不改 `AuthService`。`mvn -q -pl doyu-server compile` / `test` 验证。
- **P2 Android 数据层**：
  - `DoyuApi`：新增 `favoritePosts`、`uploadPresign`、`uploadPut(@Url)`、`uploadConfirm`；保留旧方法。
  - `DoyuRepository`：新增 `favoritePosts()`、`updateMe(nickname,bio,avatarFileId)` 重载、头像上传封装 `uploadAvatar(bytes,mime,name,w,h)`。
  - `model`：`UserProfile` 追加 `ageGroup/level/isMinor`；`UpdateProfileRequest` 追加 `avatarFileId`；新增 `PresignRequest/PresignResponse/ConfirmRequest/FileAsset`。
- **P3 “我的”页**：新 `fragment_profile_home.xml`（hero+stat+tabs+RecyclerView+状态层）；重写 `ProfileFragment`（自包含加载，三 Tab 切换，映射 SummaryItem，登录/空/错误态）。卡片点击跳详情（图纸→AI 详情边界、帖子→PostDetail，按现有 Activity 能力）。
- **P4 “编辑资料”页**：新 `activity_profile_edit.xml`；重写 `ProfileEditActivity`（加载 me 预填、相册选图上传、状态机、保存 PATCH、结果回传）。
- **P5 资源**：新增 drawable（段控选中/未选中、统计容器、头像圆形占位、warning band 等，尽量复用既有 `bg_*`）；`UiCopy` 文案。
- **P6 构建验证**：`./gradlew :app:assembleDebug`（仅编译，不连真机）；后端 `mvn test`。记录真实输出。

## 5. 验收标准（不含真机，真机校验留待统一设备）

- [ ] “我的”页结构、统计顺序（获赞/作品/关注/粉丝）、三 Tab 文案与设计一致。
- [ ] 三 Tab 均走真实接口；空/登录/错误态正确，无本地假数据。
- [ ] “编辑资料”含全部字段；昵称空禁用保存；保存失败保留草稿；头像失败保留旧图；城市/兴趣标签明确 UI-only。
- [ ] 保存成功返回“我的”页并刷新统计/资料。
- [ ] 后端 `me()` 返回真实 `likedCount/postCount`；`mvn` 编译/测试通过（附输出）。
- [ ] Android `assembleDebug` 通过（附输出）。
- [ ] 未做真机校验的项明确记录为“未覆盖”，不写成已通过。

## 6. 风险与边界

- **头像上传链路** 仅编译验证，未做真机端到端校验（设备占用约束）；失败态为安全网，保存仍可仅更新昵称/简介。
- **avatarUrl 为 fileId**：`/me` 不返回头像 URL，头像默认走占位；不在本工作树扩展后端头像 URL 解析以控制改动面。
- **后端改动** 仅限 `me()` 单方法，向后兼容；与其它工作树（社区/商城/AI/消息）控制器无重叠。
- 城市/地区、兴趣标签、实名、地图定位、生产合规：保持 UI-only，文案显式标注，不入接口、不假成功。
