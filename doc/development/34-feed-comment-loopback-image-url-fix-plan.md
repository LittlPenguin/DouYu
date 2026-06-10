# 社区与评论图片 Loopback URL 回显修复计划

## 背景

真机上传成功后，社区列表封面和作品详情评论图片会显示灰色占位。当前运行后端的 `GET /api/v1/posts/feed` 已复现最新上传帖子返回：

- `coverImageUrl`: `http://localhost:8081/uploads/assets/post_image/.../post.png`

电脑本机可以访问该 URL，但 Android 真机里的 `localhost` 指向手机自身，Glide 加载失败后落入灰色占位图。此前上传链路只改写了 presigned PUT URL 和上传确认响应中的 `FileAsset.publicUrl`，未覆盖后续从 feed、post detail 和 comments 重新读取的图片 URL。

## 范围

- Android `DoyuRepository` 在读取社区帖子、详情、我的帖子/喜欢/收藏帖子和评论列表/创建评论响应后，统一把 loopback 图片 URL 改写为当前 `BuildConfig.API_BASE_URL` 的 host/scheme/port。
- 保持 Aliyun、CDN 或其他非 loopback URL 原样，避免破坏真实 OSS 公开地址或签名地址。
- 不在 UI 层硬编码图片地址，不回填静态图片，不修改 Open Design 结构。

## 实施步骤

1. 先补 Android 仓库层单测：feed/detail/comments 响应中的 `localhost` 图片 URL 必须按 API base host 改写；非 loopback OSS URL 必须保持原样。
2. 实现仓库层响应归一化，覆盖 `Post.coverImageUrl`、`Post.imageUrls` 和 `Comment.mediaAssets.publicUrl`。
3. 运行目标单测，并尽量运行 Android 单测/build；如未覆盖真机截图，记录为未覆盖。

## 验收

- 社区列表绑定 `Post.coverImageUrl` 时不再收到真机不可访问的 loopback URL。
- 作品详情评论图片绑定 `CommentMediaAsset.publicUrl` 时不再收到真机不可访问的 loopback URL。
- 非 loopback OSS 图片 URL 不被改写。
