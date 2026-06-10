# 真实图片上传与底部导航优化计划

日期：2026-06-10

## 目标

- 修复上传帖子页图片不能真实上传的问题，确保图片必须完成 presign -> PUT -> confirm 后才产生可用于发帖的 fileId。
- 收紧后端上传确认语义，禁止未 PUT、空文件或大小不匹配的对象被确认成 FileAsset。
- 优化底部导航 UI，主页和上传页共用同一套五项导航结构，避免上传 action 在主页被误显示为当前 Tab。
- 保持 Android Java + XML 架构，不引入 Kotlin、Compose、客户端 mock 数据或假 fileId。

## 非目标

- 不改变 `POST /api/v1/posts` 的请求或响应结构。
- 不新增支付、AI、地图、短信或其他已移除运行时能力。
- 不把 OSS 凭证写入 Android、文档或测试输出。
- 不声明真机、真实 Aliyun PUT 或截图验证通过，除非本轮有新鲜命令输出或证据。

## 范围

- 后端：`UploadController`、`OssProvider` 及 Local/Aliyun provider，上传相关后端测试。
- Android：`DoyuRepository`、`DoyuApi` 上传模型声明、上传页和主页底部导航布局、`MainActivity`、`PostCreateActivity`、静态布局测试。
- 文档：本计划和本轮 verification 记录。

## 实现步骤

1. 先加后端上传确认测试：未 PUT 直接 confirm 失败，PUT 空文件 confirm 失败，PUT 非空且大小匹配后 confirm 成功并可访问本地文件。
2. 修改 OSS provider 确认契约，Local provider 检查 temp 文件存在且大小匹配后再移动；Aliyun provider 通过 HeadObject 校验对象存在和 contentLength；UploadController 将缺失或大小不匹配映射为 `INVALID_ARGUMENT`。
3. 先加 Android repository 测试：presign 后必须发 PUT，PUT 携带 presign headers 和原始字节；PUT 失败时不 confirm；PUT 成功后才 confirm 并返回 fileId。
4. 收敛 Android 上传模型和 repository 代码，保留 OkHttp 执行 presigned PUT，不保留未使用的 Retrofit `uploadPut` 或重复 DTO。
5. 先加底部导航测试：主页和上传页共用同一结构，顺序为社区、商城、上传、消息、我的；主页上传 action 不显示当前 Tab indicator；上传页高亮上传。
6. 抽共享底部导航布局并更新 `MainActivity`、`PostCreateActivity` 绑定；主页只高亮四个内容 Fragment，上传作为 action 进入上传页；上传页高亮上传并能跳回各主页面 section。
7. 更新 verification 记录并运行相关验证。

## 验证命令

- `cd D:\Studio\SpellBean\doyu-server; mvn -Dtest=Upload* test`
- `cd D:\Studio\SpellBean\doyu-server; mvn test`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:testDebugUnitTest --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebug --console=plain`
- `cd D:\Studio\SpellBean\DouYu; .\gradlew.bat :app:assembleDebugAndroidTest --console=plain`
- `rg --files DouYu/app/src | rg "\.kt$"`
- `rg -n "Compose|kotlinx|Navigation Compose|tab_ai|quick_post|PaymentBoundaryActivity|AiFragment|PatternJob" DouYu/app/src/main DouYu/app/src/test -S`
- `git diff --check`

## 验收标准

- 后端 confirm 不再为未真实上传的对象创建 FileAsset。
- Android 上传帖子页只提交真实上传成功返回的 fileId。
- 上传中或失败图片继续禁用发布，失败图片可重试或删除。
- 主页和上传页底部导航视觉结构一致；主页上传 action 不被误标成当前 Tab，上传页才高亮上传项。
- 所有相关 Android/backend 测试和构建通过；没有 Kotlin/Compose 回流。

## 实现状态

- 已实现后端真实对象确认、Android presign -> PUT -> confirm 上传链路收敛、共享底部导航和上传页选中态。
- 审核后追加修复：移除主运行时 `stub` OSS provider 选择，QA/test 默认改用 local provider；后端 presign 会清理客户端文件名，本地 PUT/confirm 会拒绝不安全 `fileKey`。
- 验证记录：`doc/development/verification/2026-06-10-real-upload-bottom-nav-verification.md`。
- 未覆盖项：真机/模拟器 UI 烟测和真实 Aliyun OSS 端到端 PUT/HEAD/confirm 烟测。
