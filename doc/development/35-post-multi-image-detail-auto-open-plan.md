# 多图作品详情与发布后自动跳转修复计划

## 背景

当前上传帖子支持选择并上传多张图片，提交时也会把多个 `mediaFileIds` 传给后端。但帖子响应只包含 `coverImageUrl`，没有包含所有媒体图片 URL。Android 作品详情页已经优先使用 `Post.imageUrls` 渲染轮播，缺少该字段时只能回退到封面，因此多图作品打开后显示为单图。

发帖成功后，上传页当前只进入成功状态并展示 `post_success_actions`，不会自动打开刚创建的作品详情。

## 修复范围

- 后端 `postView` 为帖子响应新增 `imageUrls` 数组，按 `mediaFileIds` 顺序输出所有可用 `FileAsset.publicUrl`。
- 保持现有 `coverImageUrl` / `coverWidth` / `coverHeight` 语义不变，封面仍来自第一张媒体或已保存封面。
- Android 发布成功后用返回的 `postId` 打开 `PostDetailActivity`，传入 `IntentExtras.POST_ID`，并结束上传页，使返回键回到社区或原主栈。
- 后端异常返回空 `postId` 时保留当前成功状态和成功操作区作为 fallback。

## 验收

- 多图帖子创建响应、详情响应、feed 响应都包含按上传顺序排列的 `imageUrls`。
- 打开多图帖子详情时轮播使用 `imageUrls`，不再只显示封面。
- 发帖成功后自动进入对应作品详情。
- 不引入 Kotlin、Compose、mock 图片或静态图片 fallback。
