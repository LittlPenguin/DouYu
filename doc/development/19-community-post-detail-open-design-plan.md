# 社区作品详情 Open Design 1:1 还原开发计划

## 目标

只开发“社区 / 作品详情”页面，对照 Open Design `post-detail-comment-toolbar-a.html` 和 `doc/development` 文档，把 Android Java/XML 的 `PostDetailActivity` 从当前“字段汇总文本页”升级为真实作品详情页：图片优先、内容信息、互动区、评论列表、底部悬浮评论输入栏。

## 权威来源

- Open Design：`doc/development/open-design/post-detail-comment-toolbar-a.html`
- Open Design 项目：`豆屿`
- 文档依据：
  - `doc/development/11-ui-style-guide.md`
  - `doc/development/12-feature-and-flow-map.md`
  - `doc/development/13-ui-screen-blueprints.md`
  - `doc/development/05-api-contract.md`
  - `doc/development/06-data-model.md`

## 范围

只改作品详情及其直接依赖：

- `DouYu/app/src/main/res/layout/activity_post_detail.xml`
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java`
- 社区详情相关 model/API/repository
- 必要 drawable/layout/test

禁止范围：

- 不开发社区首页、搜索页、发帖页、其它主 Tab 或后端新接口。
- 不实现全局搜索、真实 @ 用户搜索面板、真实 # 话题面板、真实图片选择/上传 UI。
- 不进行真机、instrumentation、screenshot、visual smoke 校验。

## API 使用清单

Android 端补齐 Retrofit/API，不改后端路径：

- `GET /api/v1/posts/{postId}/comments`
- `POST /api/v1/posts/{postId}/comments`
- `POST /api/v1/posts/{postId}/like`
- `DELETE /api/v1/posts/{postId}/like`
- `POST /api/v1/posts/{postId}/favorite`
- `DELETE /api/v1/posts/{postId}/favorite`

## UI 要求

主内容顺序固定为：

1. 图片 gallery / carousel 区
2. 作者与作品内容区
3. 点赞 / 评论 / 收藏互动区
4. 评论列表区
5. 底部悬浮评论栏

图片区必须有大图、页码、左右切换按钮、滑动提示、缩略图 strip 和选中态。评论区必须渲染真实评论、空态、失败态和重试。底部悬浮栏只保留 `图`、`@`、`#` 三个工具入口；当前阶段工具入口显示开发态提示，不做假功能。

## 实现步骤

1. 写 API 契约失败测试，覆盖评论、点赞、收藏路径。
2. 补 `DoyuApi`、`DoyuRepository` 和相关 model，使 API 契约测试通过。
3. 写纯 Java helper/formatter 失败测试，覆盖图片列表、互动文案、话题、空评论文案。
4. 新增 `PostDetailFormatter`，只放无 Android 依赖的展示计算逻辑。
5. 重写 `activity_post_detail.xml`，按 Open Design 结构拆出稳定 id。
6. 修改 `PostDetailActivity.java`：
   - 加载帖子详情后渲染 gallery、内容、互动状态。
   - 加载评论列表。
   - 实现图片切换、缩略图选中、评论区重试。
   - 实现点赞/收藏成功后更新、失败不改状态。
   - 实现文字评论提交和刷新。
   - 工具栏 `图/@/#` 显示开发态提示。
7. 跑 `:app:testDebugUnitTest` 和 `:app:assembleDebug`。
8. 在本文档记录本地验证结果，并明确未跑真机校验。

## 验收命令

在 `C:\Users\Oya\.codex\worktrees\25ff\SpellBean\DouYu` 执行：

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

不执行：

- `connectedAndroidTest`
- 真机截图 / visual smoke
- Playwright 或设备浏览器校验

## 执行记录

- `:app:testDebugUnitTest`：已通过。执行时仅在命令进程内设置 `ANDROID_HOME=D:\AndroidChace` 和 `ANDROID_SDK_ROOT=D:\AndroidChace`。
- `:app:assembleDebug`：已通过。执行时仅在命令进程内设置 `ANDROID_HOME=D:\AndroidChace` 和 `ANDROID_SDK_ROOT=D:\AndroidChace`。
- 真机校验：按用户要求不执行，避免占用共享设备。
