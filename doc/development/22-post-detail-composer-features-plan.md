# 作品详情 Composer 功能完善执行计划（plan-22）

> 角色：执行Agent / First Pass。本文件是本轮任务的权威范围、决策、验收与验证记录来源。
> UI 权威：`doc/development/open-design/post-detail-comment-toolbar-a.html`。
> 前置文档：plan-19/20/21 已完成作品详情结构、键盘 overlay、点赞/收藏/评论提交。本轮在其上补齐 composer 真实功能。

## 目标

把作品详情页当前仅为「开发边界 Toast」或「无点击」的能力接成真实功能，按设计稿 1:1 还原：

1. **图片评论上传**：真实 presign → PUT → confirm（`usage=POST_IMAGE`），最多 9 张，缩略图堆叠预览，单张失败保留缩略图并可重试/删除，不伪造成功。
2. **@ 用户选择**：打开用户搜索面板，调用真实 `GET /api/v1/users/search`，选中用户以 Chip 回填评论栏，提交时写入 `mentionUserIds`。
3. **# 话题选择**：打开话题选择面板，调用真实 `GET /api/v1/topics`，选中话题以 Chip 回填，提交时写入 `topicIds`。
4. **关注作者按钮**（`post_author_follow`）：接真实 `POST/DELETE /api/v1/users/{userId}/follow`，切换「关注/已关注」，失败不伪造。
5. **多图大图查看**：作品图集合与评论图缩略图点击后进入全屏 gallery / carousel（大图、页码、左右箭头、缩略图 strip、当前选中边框）。

## 非目标（本轮不做）

- 贴纸（sticker）选择：设计稿标注 UI 结构，后端 `stickerIds` 支持，但本轮 composer 工具条无贴纸入口，保持不接。
- 话题「热门/推荐」运营接口：设计稿明确标注 `UI-only / 不声明真实运营接口`，仅用 `GET /topics` 列表，不造推荐接口。
- 评论「回复」二级结构（`parentId`）：当前为一级评论，保持现状。
- 「设为封面 UI-only」：设计稿标注 UI-only，大图查看里不接后端。
- 生产短信 / 真实支付 / 生产 AI Provider / 完整合规，按全局文档列为未来能力。

## 后端现状（已确认，无需改后端）

| 能力      | 后端接口                                                      | 契约关键点                                                                                                                                                                                      | 证据                                                      |
| ------- | --------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------- |
| 图片上传    | `POST /api/v1/uploads/presign` → PUT → `/uploads/confirm` | `usage` 允许集含 `POST_IMAGE`；presign 要求 `mimeType` 以 `image/` 开头                                                                                                                              | `UploadController.java:31,54`                           |
| 评论提交    | `POST /api/v1/posts/{postId}/comments`                    | body=`CommentRequest{content,parentId,mediaFileIds,mentionUserIds,topicIds,stickerIds}`；校验：content+media+sticker 不能全空、media≤9、不重复、media 必须本人上传且 `usage=POST_IMAGE`、mention 用户须存在、topic 须存在 | `CommunityController.java:279,535-569,680`              |
| 评论图回显   | 同上响应                                                      | 返回 `mediaAssets[].publicUrl`、`mentions[]`、`topics[]`                                                                                                                                       | `CommunityController.java:481-533`                      |
| 用户搜索（@） | `GET /api/v1/users/search?keyword&page&size`              | 返回 `PageResult`，item 为 userView：`userId,nickname,avatarUrl,...`                                                                                                                            | `UserController.java:90-102`、`AuthService.java:108-126` |
| 话题列表（#） | `GET /api/v1/topics?page&size`                            | 返回 `PageResult<Topic{topicId,name,description,postCount}>`                                                                                                                                 | `CommunityController` topics + `Topic.java`             |
| 关注      | `POST/DELETE /api/v1/users/{userId}/follow`               | 返回 `{followed,followedByMe,followsMe,mutualFollow}`                                                                                                                                        | `UserController.java:191-213`                           |

> 结论：本轮**只改 Android 客户端**，后端契约已满足。

## Android 现状与差距

- `PostDetailActivity.java:133-135`：图/@/# 仅 `showToolBoundary` Toast。
- `PostDetailActivity.java:476`：`new CommentRequest(content, null, List.of(), List.of(), List.of(), List.of())` —— media/mention/topic 全空。
- `post_author_follow`（layout:208-219）：无 `setOnClickListener`，display-only。
- 评论图缩略图、作品大图：无全屏查看入口。
- 网络层缺口：`DoyuApi` / `DoyuRepository` **无** `users/search`、`follow/unfollow`、`uploadPostImage(POST_IMAGE)`。
- 复用资产：`ProfileEditActivity.java:158-267` + `DoyuRepository.uploadAvatar(...)` 已是完整 presign→PUT→confirm 范式，仅 `usage` 不同，可抽取复用。

## 实施步骤

### A. 网络层（model + api + repository）

1. `DoyuApi` 新增：
   - `@GET("/api/v1/users/search") Call<ApiResponse<PageResponse<UserProfile>>> searchUsers(@Query("keyword") String kw, @Query("page") int p, @Query("size") int s);`
   - `@POST("/api/v1/users/{userId}/follow") Call<ApiResponse<FollowResult>> followUser(@Path("userId") String id);`
   - `@DELETE("/api/v1/users/{userId}/follow") Call<ApiResponse<FollowResult>> unfollowUser(@Path("userId") String id);`
2. 新增 model `FollowResult{ Boolean followed; Boolean followedByMe; Boolean followsMe; Boolean mutualFollow; }`。
3. `DoyuRepository` 新增：
   - `searchUsers(String keyword)`、`followUser(id)`、`unfollowUser(id)`。
   - `uploadPostImage(byte[] bytes,String mime,String fileName,Integer w,Integer h)`：复用 `uploadAvatar` 流程但 `usage="POST_IMAGE"`，返回 `fileId`。把 `uploadAvatar` 与新方法的公共逻辑抽到 `private String uploadImage(usage,...)`，避免重复。

### B. 图片评论上传（PostDetailActivity）

4. 新增 `registerForActivityResult(GetContent)` 选图 launcher；点击 `post_comment_tool_image` 时校验登录态与 `pendingMedia.size()<9` 后 `launch("image/*")`。
5. 维护 `List<PendingMedia>`（fileId / uri / 上传状态 uploading|done|failed）；读取字节走 `ProfileEditActivity.readAvatar` 同款逻辑（抽成可复用 `MediaBytes` 读取，限制 20MB、记录 width/height）。
6. 在编辑器内新增缩略图行容器（XML：`post_comment_media_strip`），渲染缩略图堆叠 + 单张状态：上传中 / 失败(重试·删除) / 成功。
7. `updateSendEnabled()`：有文字 **或** 至少一张 done 图片即可发送；存在 uploading 或 failed 图片时禁发。
8. 9 张上限：达到上限禁用图片按钮并提示「已达 9 张上限」。

### C. @ 用户选择 + # 话题选择

9. 新增 `BottomSheetDialog`（Material，已在依赖中）两个选择面板，布局新增 `dialog_user_picker.xml` / `dialog_topic_picker.xml`：
   - 搜索框 + 列表 + 空结果 + 加载失败重试（对齐设计稿 selector-panel 状态）。
   - 用户面板调 `searchUsers(keyword)`，话题面板调 `repository.topics()`。
10. 选中回填：维护 `LinkedHashMap<String,String> selectedMentions`（userId→nickname）、`selectedTopics`（topicId→name）；在编辑器新增已选 Chip 容器（XML：`post_comment_chip_row`），Chip 可点击移除。
11. 提交时构造 `CommentRequest(content, null, doneFileIds, mentionIds, topicIds, List.of())`。
12. 发送成功后清空 pendingMedia / selectedMentions / selectedTopics 并刷新列表。

### D. 关注作者按钮

13. `renderPost` 后绑定 `post_author_follow` 点击：根据 `Post.followedAuthorByMe` 调 follow/unfollow，禁用按钮直到返回，成功后更新 `followedAuthorByMe` 与按钮文案/样式（复用 `PostDetailFormatter.authorFollowLabel`），失败 `showPageStatus` 提示不伪造。未登录走登录边界。

### E. 多图大图查看

14. 新增 `PhotoViewerActivity`（Java/XML，`activity_photo_viewer.xml`）：接收 `imageUrls` 列表 + 起始 index，复用作品详情同款大图 + 页码 + 左右箭头 + 缩略图 strip + 选中边框，Glide 加载。
15. 作品大图 `post_gallery_image` 与评论图缩略图点击后 `startActivity` 进入查看器（评论图用该评论 `mediaAssets[].publicUrl`）。

### F. 文档与测试

16. 实施中若范围/验收变化，先更新本文件再改代码。
17. 测试：
    - `OpenDesignLayoutMappingTest`：补充新容器（media strip / chip row）与 picker 布局的 DOM 父子断言。
    - `PostDetailFormatterTest`：若新增 mention/topic chip 文案 formatter，补单测。
    - `SourceMojibakeSpotTest`：确保新增中文无乱码。
    - 真机 `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly`：扩展覆盖「选图上传→@→#→发送→列表回显图片/提及/话题」与关注切换；若设备不可用，按 AGENTS.md 记为未覆盖，不写成已通过。

## likely files

- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/model/FollowResult.java`（新）
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostDetailFormatter.java`（如需 chip 文案）
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PhotoViewerActivity.java`（新）
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/UserPickerSheet.java` / `TopicPickerSheet.java`（新，或内嵌 Activity）
- `DouYu/app/src/main/res/layout/activity_post_detail.xml`（编辑器加 media strip + chip row）
- `DouYu/app/src/main/res/layout/activity_photo_viewer.xml`、`dialog_user_picker.xml`、`dialog_topic_picker.xml`（新）
- `DouYu/app/src/main/res/layout/item_user_pick.xml`、`item_topic_pick.xml`、`item_comment_media_thumb.xml`（新，按需）
- `DouYu/app/src/main/AndroidManifest.xml`（注册 `PhotoViewerActivity`）
- 测试：`OpenDesignLayoutMappingTest.java`、`PostDetailFormatterTest.java`、`RealBackendSmokeInstrumentedTest.java`
- 文档：本文件、`13-ui-screen-blueprints.md`、`current-status.md`、本轮 verification 记录

## 验收标准（对照设计稿）

- 图片：点图标选图→真实上传；缩略图堆叠；9 图上限拦截第 10 张；单张失败保留缩略图 + 重试/删除，不假成功；上传中禁发。
- @：打开真实用户搜索面板；选中回填 Chip；空结果/失败有状态；提交写入 `mentionUserIds`，列表回显 `@昵称`。
- #：打开真实话题面板；选中回填 Chip；提交写入 `topicIds`，列表回显 `#话题`。
- 发送：文字或图片至少其一存在才可发；成功后清空草稿、收键盘、恢复静态栏、刷新评论并展示新评论的图/@/#。
- 关注：点击切换真实 follow/unfollow；失败保持原状态并提示；未登录走登录边界。
- 大图：作品图与评论图点击进入全屏 carousel，含大图/页码/左右箭头/缩略图/选中边框。
- 全局红线：无 Kotlin/Compose；无 mock/假数据/假成功；中文无乱码；空结果走空态/登录态。

## 验证命令

```bash
cd /d/Studio/SpellBean
git status --short --branch
git diff --check
rg --files DouYu/app/src | rg "\.kt$"
rg -n "compose|Composable|kotlinx|MockData|coil\.compose" DouYu/app/src

cd /d/Studio/SpellBean/DouYu
./gradlew.bat :app:testDebugUnitTest --tests cn.edu.app.douyu.core.OpenDesignLayoutMappingTest --console=plain
./gradlew.bat :app:testDebugUnitTest --console=plain
./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
./gradlew.bat :app:lintDebug --console=plain

cd /d/Studio/SpellBean/doyu-server
mvn -q test
```

真机专项（设备可用时）：

```bash
adb devices -l
cd /d/Studio/SpellBean/DouYu
adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <serial> install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s <serial> shell am instrument -w -r -e class cn.edu.app.douyu.RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailOnly cn.edu.app.douyu.test/androidx.test.runner.AndroidJUnitRunner
```

## 风险与回退

- 评论图上传要求 `usage=POST_IMAGE` 且本人所有：必须用当前登录态上传，未登录时图片按钮走登录边界。
- `users/search` 关键词为空会返回全量用户分页：面板默认可加载首页，输入后按关键词过滤；保留空结果状态。
- 大图查看新增 Activity 需在 Manifest 注册，否则运行期崩溃；构建后用真机/单测兜底。
- 若 `RealBackendSmoke` 设备不可用：本轮 Android 构建 + 单元 + lint 必须全绿，真机项明确记为未覆盖并说明风险。

## 执行状态（Re-review 完成，2026-06-08）

实现完成并通过本机与真机验证。真机专项已在 `10.64.241.158:37035`（ELI-AN00 / Android 16）运行 `RealBackendSmokeInstrumentedTest#captureRealBackendPostDetailComposer`，结果 `OK (1 test)`，截图与 contact sheet 已归档到 `verification/android-java-xml-screenshots/2026-06-08-post-detail-composer-features/`。
验证证据：`verification/2026-06-08-post-detail-composer-features-verification.md`。

实施与计划的差异：
- 「话题选择」未新增 `dialog_user_picker.xml` / `dialog_topic_picker.xml` 两套布局，改为单一 `dialog_picker.xml` + `item_picker_row.xml`，由 `PickerSheet`（USER/TOPIC 模式）共用，减少重复。
- 未引入 Flexbox 依赖：已选 @/# Chip 行改用 `HorizontalScrollView + LinearLayout`（`post_comment_chip_scroll` / `post_comment_chip_row`）。
- `post_author_follow` 已关注态使用现有 `bg_chip_plain`（无 `bg_pill_plain`）。

已运行（结果见 verification）：testDebugUnitTest、assembleDebug、assembleDebugAndroidTest、lintDebug 全部 BUILD SUCCESSFUL；后端 `mvn test` 70/0/0；真机 `captureRealBackendPostDetailComposer` OK。

## 人工验收补丁（2026-06-08）

人工验收发现：真实输入框展开后，点击页面空白区域不会收起；用户手动下拉/隐藏输入法时，也不会恢复底部静态评论栏。

修复口径：
- 点击作品详情滚动内容空白区域时，若真实输入框正在显示，则收起键盘、隐藏真实输入框、恢复底部静态评论栏。
- 监听 IME 可见状态；当 IME 从可见变为隐藏且真实输入框仍显示时，同步恢复底部静态评论栏。
- 不改变发送成功、发送失败、返回键、图片上传、@/#、关注和大图查看现有行为。
- 增加单元级结构守卫与真机专项复验，截图证明手动收起后静态栏恢复。
