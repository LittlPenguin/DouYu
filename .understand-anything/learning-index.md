# SpellBean 学习索引

这份索引只解决一个问题：你现在应该先学哪里。

## 先看哪份

1. `.understand-anything/simple-start-guide.md`
   - 最适合第一次看项目。
   - 只讲主线，不讲全图。

2. `.understand-anything/onboarding-guide.md`
   - 适合已经知道主线后查完整结构。
   - 内容更全，但第一次看会比较乱。

3. Dashboard
   - 适合搜索和追依赖。
   - 不适合当教材从头读。

## 最小学习顺序

### 第 1 关：知道项目边界

读：

- `AGENTS.md`
- `doc/development/current-status.md`

读完应该知道：

- Android 为什么不能写 Kotlin / Compose。
- 页面为什么不能用 mock 数据填满。
- Open Design 为什么是 UI 权威。
- AI、支付、地图、真实 SMS 为什么暂时不在范围内。

### 第 2 关：看懂 Android 到后端

读：

- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/CommunityFragment.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`

读完应该知道：

- Fragment 是页面逻辑。
- Repository 是数据入口。
- DoyuApi 是接口清单。
- 社区首页列表最终请求 `/api/v1/posts/feed`。

### 第 3 关：看懂后端接口

读：

- `doyu-server/src/main/java/cn/edu/app/douyu/server/DouyuServerApplication.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`

读完应该知道：

- Spring Boot 从哪里启动。
- 哪些接口需要登录。
- `/api/v1/posts/feed` 在后端由哪个方法处理。

### 第 4 关：看懂上传链路

读：

- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/UploadController.java`
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/oss/OssProvider.java`

读完应该知道：

- 为什么上传不是直接把图片传给发帖接口。
- `presign -> PUT -> confirm` 每一步做什么。
- `fileId` 为什么比图片 URL 更重要。

### 第 5 关：看懂 UI 验收

读：

- `doc/development/open-design/index.html`
- `doc/development/03-android-client.md`
- `doc/development/10-testing-acceptance.md`
- `DouYu/app/src/test/java/cn/edu/app/douyu/core/OpenDesignLayoutMappingTest.java`

读完应该知道：

- Android XML 页面要对齐哪些设计文件。
- 空态、错误态、登录态为什么必须存在。
- 改 UI 后应该怎么验证。

## Dashboard 怎么用才不乱

不要点全图。只用搜索。

推荐搜索词：

- `CommunityFragment`
- `DoyuRepository`
- `DoyuApi`
- `CommunityController`
- `UploadController`
- `SecurityConfig`

每次只看一个节点，然后只追一层连接。

## 可以直接问的问题

如果你卡住，可以按这种方式问：

- “用很简单的话解释 `CommunityFragment` 到 `/api/v1/posts/feed` 的链路。”
- “解释 `DoyuRepository.uploadPostImageAsset` 每一步在干什么。”
- “我想改社区列表，应该先看哪些文件？”
- “我想加一个后端接口，Android 和后端分别要改哪里？”
- “这个项目哪些东西绝对不能新增？”

## 一句话地图

```text
文档定规则和 UI
Android Java/XML 负责页面
DoyuRepository 负责拿数据
DoyuApi 负责接口路径
Spring Boot Controller 负责接请求
数据库和 OSS 保存真实数据
测试负责防止 Kotlin/Compose/mock/seed 等东西回来
```
