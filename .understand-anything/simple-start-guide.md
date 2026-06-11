# SpellBean 极简上手讲解

这份文档不是完整图谱，而是给第一次接手项目的人看的最小路线。先按这里读，读完再用 dashboard 搜索细节。

## 0. 先别看图谱，先记住这个模型

这个项目可以先当成三层来看：

```text
Android App
  负责页面、按钮、列表、空态、登录态

Spring Boot 后端
  负责接口、鉴权、业务规则、数据库、上传确认

文档和 Open Design
  负责告诉你哪些功能允许存在、页面应该长什么样、怎么验收
```

刚开始不要试图同时理解所有类。你只需要问一个问题：

```text
这个页面的数据从哪里来？
```

答案通常都是：

```text
Fragment / Activity
  -> DoyuRepository
  -> DoyuApi
  -> 后端 Controller
  -> 数据库或对象存储
```

## 1. 这个项目是什么

SpellBean / Doyu 是一个 Android 拼豆社区和材料商城应用。

你可以把它理解成五个主要区域：

- 社区：看作品、发作品、看详情、评论、点赞、收藏。
- 商城：看材料商品、商品详情、购物车、下单。
- 上传：上传图片，走后端签名和 OSS/local storage。
- 消息：通知、会话、私信。
- 我的：个人资料、关注粉丝、设置、账号状态。

当前项目不是 Kotlin/Compose 项目。Android 端必须继续用 Java + Activity/Fragment + XML。

## 2. 最重要的规则

先记住这几条，后面看代码会清楚很多：

- Android 不能新增 Kotlin 或 Jetpack Compose。
- 页面不能用本地 mock/demo 数据填充。
- 运行期数据必须来自后端 API。
- 后端空列表是正常结果，Android 要显示空态。
- UI 结构以 `doc/development/open-design/` 里的 HTML 为准。
- 当前范围不包括 AI、支付、地图定位、真实 SMS、完整合规页面。
- 上传图片必须走后端 `presign -> PUT -> confirm`，不能假装上传成功。

## 3. 最先读的 6 个文件

不要一开始看全图。先只看这 6 个文件。

1. `AGENTS.md`
   - 这是最高优先级规则。
   - 主要看 Android 禁用项、真实数据规则、Open Design 权威和验收要求。

2. `doc/development/00-project-handoff.md`
   - 这是项目交接说明。
   - 主要看项目定位、当前保留范围、删除范围。

3. `doc/development/current-status.md`
   - 这是当前状态快照。
   - 主要看现在什么是事实，什么还没覆盖。

4. `doc/development/03-android-client.md`
   - 这是 Android 端说明。
   - 主要看主 Tab、页面映射、导航规则、数据规则。

5. `doc/development/04-backend-services.md`
   - 这是后端服务说明。
   - 主要看后端保留哪些模块，删除哪些能力。

6. `doc/development/05-api-contract.md`
   - 这是 API 契约。
   - 主要看 Android 应该调哪些 `/api/v1` 接口。

## 4. Android 端怎么看

Android 端的主线是：

```text
XML 页面
  -> Activity / Fragment
  -> DoyuRepository
  -> DoyuApi
  -> 后端 /api/v1
```

这几个词可以这样理解：

- XML 页面：只管“长什么样”，比如按钮、列表、输入框放在哪里。
- Activity / Fragment：管“用户看到哪个页面、点按钮后发生什么”。
- Repository：管“页面要数据时，去哪里拿、怎么处理返回结果”。
- DoyuApi：Retrofit 接口清单，管“Android 具体请求哪个后端 URL”。

先看这三个文件：

- `DouYu/app/src/main/java/cn/edu/app/douyu/MainActivity.java`
  - 这是 Android 主壳。
  - 管五个底部 Tab：社区、商城、上传、消息、我的。
  - 负责 Fragment 切换和从别的页面回到指定 Tab。

- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`
  - 这是 Retrofit 接口表。
  - Android 调后端的接口基本都在这里。
  - 看到一个页面要请求什么数据，通常先来这里找 endpoint。

- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
  - 这是 Android 数据仓库。
  - 页面不应该直接拼接口逻辑，而是通过这里封装 API 调用。
  - 上传、图片 URL 归一化、分页、订单请求组装都在这里。

## 5. 后端怎么看

后端主线是：

```text
Controller
  -> entity/repository
  -> database / storage
```

这几个词可以这样理解：

- Controller：后端接口入口，Android 请求先进这里。
- entity：数据库里的表在 Java 里的表示。
- repository：读写数据库的工具。
- SecurityConfig：决定哪些接口可以匿名访问，哪些必须登录。

先看这几个文件：

- `doyu-server/src/main/java/cn/edu/app/douyu/server/DouyuServerApplication.java`
  - Spring Boot 启动入口。

- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java`
  - 鉴权规则。
  - 哪些接口公开，哪些接口必须登录，看这里。

- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`
  - 社区接口：feed、发帖、详情、评论、点赞、收藏、话题。

- `doyu-server/src/main/java/cn/edu/app/douyu/server/commerce/CommerceController.java`
  - 商城接口：商品、分类、购物车。

- `doyu-server/src/main/java/cn/edu/app/douyu/server/order/OrderController.java`
  - 订单接口：创建订单、查询订单、取消订单。

- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/UploadController.java`
  - 上传接口：presign 和 confirm。

## 6. 一个功能怎么从头看到尾

以“发帖上传图片”为例：

```text
PostCreateFragment
  -> DoyuRepository.uploadPostImage(...)
  -> DoyuApi.uploadPresign(...)
  -> 直接 PUT uploadUrl
  -> DoyuApi.uploadConfirm(...)
  -> DoyuRepository.createPost(...)
  -> DoyuApi.createPost(...)
  -> CommunityController
```

这条链路的意思是：

1. 用户在 Android 发帖页选择图片。
2. Android 不能直接拿 OSS 密钥上传，所以先问后端要一个临时上传地址。
3. 后端返回 `uploadUrl` 和 `fileKey`。
4. Android 把图片 bytes 用 HTTP PUT 传到这个 `uploadUrl`。
5. Android 再通知后端“我传完了”，也就是 confirm。
6. 后端确认文件存在后，生成 `fileId`。
7. Android 创建帖子时只提交 `fileId`，不把图片本身塞进帖子接口。

看任何功能都按这个方法：

1. 找 Android 页面。
2. 找它调用的 Repository 方法。
3. 找 DoyuApi 里的接口。
4. 找后端 Controller。
5. 找对应测试。

## 7. UI 怎么判断对不对

不要凭感觉改 UI。

UI 权威在：

- `doc/development/open-design/index.html`
- `doc/development/open-design/*.html`
- `doc/development/13-ui-screen-blueprints.md`

Android XML 要对齐这些 HTML 的：

- 页面结构
- 信息层级
- 文案
- 空态
- 错误态
- 登录边界
- 禁用态
- 点击去向

## 8. 测试怎么看

Android 测试重点：

- `DouYu/app/src/test/java/cn/edu/app/douyu/core/OpenDesignLayoutMappingTest.java`
  - 检查 Open Design 页面是否有对应 XML。

- `DouYu/app/src/test/java/cn/edu/app/douyu/core/RemovedFeatureContractTest.java`
  - 检查 AI、支付、地图等移除能力没有被恢复。

- `DouYu/app/src/test/java/cn/edu/app/douyu/network/*`
  - 检查 Retrofit 接口和 Repository 行为。

后端测试重点：

- `doyu-server/src/test/java/cn/edu/app/douyu/server/DouyuBackendContractTests.java`
  - 大的后端契约测试集合。

- `doyu-server/src/test/java/cn/edu/app/douyu/server/DefaultRuntimeNoSeedContractTests.java`
  - 检查运行期不能恢复 seed/demo 数据。

- `doyu-server/src/test/java/cn/edu/app/douyu/server/CommercePurchaseContractTests.java`
  - 检查购买、购物车、订单、库存边界。

## 9. Dashboard 应该怎么用

不要把 dashboard 当第一本教材。它更像地图和搜索工具。

推荐用法：

1. 先读这份极简指南。
2. 打开 dashboard。
3. 搜索一个具体词，比如 `DoyuRepository`、`UploadController`、`CommunityController`。
4. 点节点看它连接到哪些文件。
5. 只追一条链路，不要一次看全图。

适合问 dashboard 的问题：

- 这个文件依赖谁？
- 谁依赖这个文件？
- 这个功能涉及 Android 哪些文件？
- 后端哪个 Controller 对应这个接口？
- 当前改动可能影响哪些模块？

## 10. 推荐学习顺序

第一天只做这件事：

1. 读 `AGENTS.md`。
2. 读 `00-project-handoff.md`。
3. 读 `current-status.md`。
4. 读 `MainActivity.java`。
5. 读 `DoyuApi.java`。
6. 读 `DoyuRepository.java`。

第二天再看后端：

1. `DouyuServerApplication.java`
2. `SecurityConfig.java`
3. `CommunityController.java`
4. `CommerceController.java`
5. `OrderController.java`
6. `UploadController.java`

第三天按业务流看：

1. 上传
2. 社区
3. 商城和订单
4. 消息
5. Profile 和 Settings

## 11. 你现在只需要记住的一句话

这个项目的核心不是“画很多页面”，而是：

```text
Java/XML Android 页面
  通过 Retrofit Repository
  调 Spring Boot /api/v1 后端
  使用真实数据库和 OSS-backed 图片
  按 Open Design 呈现真实状态
```

先围绕这条主线看代码，其他细节以后再展开。

## 12. 如果你完全看不懂，先按这个 20 分钟路线

第 1 步，只看规则，不看代码：

- `AGENTS.md`
- `doc/development/current-status.md`

你只要回答出：这个项目不能用什么、数据必须从哪里来。

第 2 步，只看 Android 数据怎么来：

- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community/CommunityFragment.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/data/DoyuRepository.java`
- `DouYu/app/src/main/java/cn/edu/app/douyu/network/DoyuApi.java`

你只要找出：社区列表调用了哪个 Repository 方法，Repository 又调用了哪个 API。

第 3 步，只看后端谁接住这个 API：

- `doyu-server/src/main/java/cn/edu/app/douyu/server/community/CommunityController.java`

你只要找出：`/api/v1/posts/feed` 对应哪个方法。

这 20 分钟读完，你就已经掌握项目最重要的一条线：社区首页列表从 Android 到后端是怎么走的。
