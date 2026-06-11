# Code Directory And Table Guide

这份文档只做快速上手索引：说明前端代码目录、后端代码目录，以及当前数据库每张表的用途。

## Frontend Android Directory Map

- `DouYu/app/src/main/java/cn/edu/app/douyu`: 应用入口、全局 `Application`、主 Activity/Fragment Tab 容器。
- `DouYu/app/src/main/java/cn/edu/app/douyu/auth`: 登录、注册、需要登录时的拦截跳转、本地会话保存。
- `DouYu/app/src/main/java/cn/edu/app/douyu/core`: 通用常量和工具，包括 Intent extra 名称、金额格式化、系统栏适配、页面文案。
- `DouYu/app/src/main/java/cn/edu/app/douyu/data`: Repository 数据层，把页面动作转换成真实后端 API 调用；运行时页面不能从这里返回 mock 数据。
- `DouYu/app/src/main/java/cn/edu/app/douyu/network`: Retrofit 接口定义、OkHttp/Retrofit 客户端创建、统一响应模型和 API 异常。
- `DouYu/app/src/main/java/cn/edu/app/douyu/model`: Android 端 Java POJO DTO，对应后端 `/api/v1` 请求和响应 JSON。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/community`: 社区首页、发帖、帖子详情、评论、搜索、拍照/图片预览和相关列表适配器。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/commerce`: 商城首页、商品详情、购物车、下单表单状态和商品列表适配器。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/message`: 通知列表、通知详情、通知类型和通知适配器；当前不是已移除的私信聊天功能。
- `DouYu/app/src/main/java/cn/edu/app/douyu/feature/profile`: 个人主页、资料编辑、个人帖子、关注/粉丝列表、设置页和相关适配器。
- `DouYu/app/src/main/java/cn/edu/app/douyu/ui`: 可复用 XML 页面基类、列表状态工具、摘要列表 UI 组件。
- `DouYu/app/src/main/res/layout`: 从 Open Design HTML 映射来的页面和列表项 XML 布局。
- `DouYu/app/src/main/res/drawable`: XML 页面使用的图标、背景和视觉资源。
- `DouYu/app/src/main/AndroidManifest.xml`: Android 组件声明、权限和 Activity 启动配置。

## Backend Directory Map

- `doyu-server/src/main/java/cn/edu/app/douyu/server`: Spring Boot 后端入口和顶层后端包。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/auth`: 注册、登录、密码校验、Token 签发和用户响应视图组装。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/user`: 个人资料、关注/取关、设置、用户个人资源接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload`: 上传预签名、上传确认、本地上传辅助接口和本地 OSS 静态资源映射。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/upload/oss`: OSS Provider 接口，以及本地存储和阿里云 OSS 两种实现。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/community`: 帖子、评论、话题、贴纸、点赞、收藏，以及社区开发导入辅助接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/commerce`: 商品分类、商品列表、商品详情、购物车，以及商城开发导入辅助接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/order`: 创建订单、消费购物车条目、锁定 SKU 库存和幂等请求处理。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/notification`: 保留的通知列表和已读回执接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/search`: 面向当前保留业务的跨域搜索接口。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common`: 统一响应包装、错误码、异常、安全、JWT、配置属性、分页、TraceId、OpenAPI 和 JSON 配置。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/entity`: JPA Entity 和 Spring Data Repository，对应运行时数据库表。
- `doyu-server/src/main/java/cn/edu/app/douyu/server/common/persistence`: 普通 JPA Repository 之外的底层持久化辅助代码。
- `doyu-server/src/main/resources/db/migration`: Flyway 数据库迁移，负责创建、调整和清理表结构。
- `doyu-server/src/main/resources/application.yml`: 后端运行配置，包括安全、数据源、OSS 等设置。

## Runtime Table Dictionary

当前运行时数据库结构由 `doyu-server/src/main/resources/db/migration` 下的 Flyway 迁移定义。

当前没有独立的 `user_settings` 表。用户隐私设置和通知偏好现在都是 `users` 表上的字段。

| 表名                    | 这是什么表                                                        |
| --------------------- | ------------------------------------------------------------ |
| `users`               | 用户账号表，保存登录邮箱、密码哈希、昵称、头像、简介、地区、账号状态和设置偏好。                     |
| `follows`             | 用户关注关系表，一行表示某个用户关注了另一个用户。                                    |
| `file_assets`         | 上传文件资源表，保存文件归属用户、用途、OSS/local 存储 key、公开 URL、文件类型、大小、宽高和审核状态。 |
| `posts`               | 社区帖子表，保存作者、标题、正文、媒体文件 id、话题 id、状态、点赞/收藏/评论计数、置顶状态和封面图信息。     |
| `comments`            | 帖子评论表，保存评论所属帖子、作者、父评论、正文、状态和评论媒体文件 id。                       |
| `likes`               | 通用点赞关系表，通过 `target_type` 和 `target_id` 表示点赞的是帖子、评论等目标。       |
| `favorites`           | 通用收藏关系表，通过 `target_type` 和 `target_id` 表示收藏的是帖子等目标。          |
| `topics`              | 社区话题表，保存话题名称、描述和帖子数量。                                        |
| `comment_mentions`    | 评论提及用户关系表，保存某条评论里 @ 了哪些用户。                                   |
| `comment_topics`      | 评论关联话题表，保存某条评论引用了哪些话题。                                       |
| `sticker_packs`       | 贴纸包表，保存贴纸包名称和展示排序。                                           |
| `stickers`            | 贴纸表，保存贴纸所属包、名称、文本/图片展示和排序。                                   |
| `comment_stickers`    | 评论贴纸关系表，保存某条评论附带了哪些贴纸。                                       |
| `products`            | 商城商品主表，保存商品类型、卖家、标题、描述、分类、图片信息、上架状态和审核状态。                    |
| `skus`                | 商品 SKU 表，保存商品规格、价格、总库存、锁定库存和销售状态。                            |
| `cart_items`          | 购物车条目表，保存用户选中的 SKU 和数量。                                      |
| `orders`              | 订单主表，保存买家、卖家信息、订单类型、状态、金额、地址快照和过期时间。                         |
| `order_items`         | 订单明细表，保存订单里的商品、SKU、数量和下单单价。                                  |
| `idempotency_records` | 幂等请求记录表，保存操作请求和响应，避免重复下单等重复写入。                               |
| `notifications`       | 用户通知表，保存通知类型、标题、内容、已读时间和时间戳。                                 |

## Removed Runtime Tables

清理迁移会删除已经废弃或未完成的运行时功能表。这些表可能出现在旧迁移里，但不属于当前运行产品。

| 已移除领域             | 表                                                      |
| ----------------- | ------------------------------------------------------ |
| Refresh token 登录态 | `auth_tokens`, `refresh_tokens`                        |
| 后台管理和操作日志         | `admin_users`, `admin_operation_logs`                  |
| AI/图纸生成           | `pattern_jobs`, `pattern_assets`, `ai_usage`           |
| 支付和退款             | `payments`, `refunds`                                  |
| 私信聊天              | `conversations`, `messages`                            |
| 积分/签到/成长体系        | `reward_accounts`, `checkin_records`, `reward_ledgers` |
| 举报和审核             | `reports`, `moderation_records`                        |
