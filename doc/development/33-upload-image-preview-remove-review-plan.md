# 上传图片回显与发帖去审核计划

## 目标

- 修复上传帖子页图片上传成功后仍显示灰色缩略图的问题。
- 上传确认后保留后端返回的真实 `publicUrl`，上传页缩略图优先使用远端 URL 回显，只有远端 URL 为空时才回退到本地 `Uri`。
- 删除发帖链路中的审核态：新发帖和编辑帖直接进入公开可见状态，不再返回或展示 `REVIEWING` / 审核中。
- 上传确认后的图片资产直接标记为可用状态，避免继续展示或传播“待人工审核”语义。

## 非目标

- 不新增 Kotlin、Compose、新导航框架或客户端 mock 数据。
- 不改动 `POST /api/v1/posts` 请求结构。
- 不删除数据库已有 `status` / `audit_status` 字段，避免本轮引入破坏性迁移；运行时不再把新上传/新发帖写入审核态。
- 不重写商品上架审核和历史管理后台结构；本轮只处理上传页、帖子发布和图片资产确认链路。

## 根因假设

Android `DoyuRepository.uploadPostImage()` 当前执行了真实 `presign -> PUT -> confirm`，但只返回 `fileId`。`confirm` 响应中的 `FileAsset.publicUrl` 被丢弃，`PostCreateActivity` 的缩略图一直用本地 `Uri` 加载：

```java
Glide.with(image).load(media.uri)...
```

当系统内容 URI 对 Glide 不稳定、权限失效、或者加载失败时，上传成功后的 UI 没有可切换的真实远端图片 URL，只能停留在灰色 placeholder。

同时后端 `createPost()` / `updatePost()` 仍写入 `REVIEWING`，客户端发布成功文案和按钮也继续显示“审核中”，与当前“删除审核功能”的要求冲突。

## 实施步骤

1. 先补 Android repository 单测：`uploadPostImageAsset()` 必须返回 `FileAsset.fileId` 和 `publicUrl`；原 `uploadPostImage()` 继续保留兼容，只返回 `fileId`。
2. 先补/调整 Android 上传页静态测试：上传页不再包含 `REVIEWING`，发布成功按钮和提示不再出现审核态文案。
3. 先补/调整后端契约测试：上传 confirm 后图片资产为可用状态；创建帖子直接返回 `VISIBLE` 并进入公开 Feed。
4. 实现 Android repository 返回 `FileAsset` 的真实上传方法，保留旧 `String uploadPostImage(...)` 兼容评论图片上传。
5. 实现 `PostCreateActivity` 在上传成功后保存 `publicUrl`，缩略图优先加载远端 URL；失败和上传中仍保留原本重试/删除逻辑。
6. 实现后端去审核：`CommunityController.createPost()` / `updatePost()` 写入 `VISIBLE`；`UploadController.confirm()` 新建图片资产写入 `PASS`。
7. 更新上传页文案、按钮状态和开发文档中的审核描述。
8. 运行 Android 单测、后端目标测试、构建和静态检查；如果真机可用，再跑上传页 smoke。

## 验证命令

- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=DouyuBackendContractTests,Upload* test`
- `cd D:\Studio\SpellBean\doyu-server; mvn test`
- `cd D:\Studio\SpellBean; rg --files DouYu/app/src | rg "\.kt$"`
- `cd D:\Studio\SpellBean; rg -n "Compose|kotlinx|Navigation Compose|tab_ai|quick_post|PaymentBoundaryActivity|AiFragment|PatternJob" DouYu/app/src/main DouYu/app/src/test -S`
- `cd D:\Studio\SpellBean; git diff --check`

