# 06. 数据模型

## 建模原则

- 对外业务 ID 使用字符串。
- 所有核心表包含创建时间和更新时间。
- 所有用户生成内容保留审核状态。
- 订单、支付、退款分表建模。
- AI 输入、输出和审核记录可追踪。
- 删除优先软删除，涉及合规注销时执行脱敏和归档策略。

## 用户域

### User

| 字段 | 说明 |
|---|---|
| id | 对外用户 ID |
| phone | 手机号，需加密或脱敏展示 |
| nickname | 昵称 |
| avatarFileId | 头像文件 |
| bio | 简介 |
| ageGroup | 年龄段；当前登录接口仍要求传入，Android 默认传 `AGE_18_PLUS`；长期目标是由实名或后端规则判定 |
| isMinor | 是否未成年人，由后端维护 |
| realNameStatus | 实名状态 |
| accountStatus | 账号状态 |

账号状态：

- `ACTIVE`
- `LIMITED`
- `TRADE_LIMITED`
- `BANNED`
- `CANCELING`
- `CANCELED`

实名状态：

- `UNVERIFIED`
- `REVIEWING`
- `VERIFIED`
- `REJECTED`

## 社区域

### Post

| 字段 | 说明 |
|---|---|
| id | 帖子 ID |
| authorId | 作者 |
| title | 标题 |
| content | 正文 |
| mediaFileIds | 图片或视频 |
| coverImageUrl | 第四轮本地 seed / 演示封面 URL，可为空；完整媒体体系仍以 FileAsset 为目标 |
| topicIds | 话题 |
| linkedPatternId | 关联图纸 |
| status | 内容状态 |
| likeCount | 点赞数 |
| favoriteCount | 收藏数 |
| commentCount | 评论数 |
| likedByMe / favoritedByMe / followedAuthorByMe | 帖子详情响应中的当前用户互动视角字段；未登录时为 `false`，不是持久化在帖子表的字段 |

内容状态：

- `REVIEWING`（前端不再展示审核中 UI，发布后直接展示内容）
- `VISIBLE`
- `SELF_VISIBLE`
- `REJECTED`
- `DELETED`

### Comment

| 字段 | 说明 |
|---|---|
| id | 评论 ID |
| postId | 帖子 |
| authorId | 作者 |
| parentId | 父评论 |
| content | 内容 |
| mediaFileIds | 第六轮评论图片文件 ID 列表，最多 9 个，可为空 |
| mediaAssets | 接口响应中的评论图片渲染对象，包含 `fileId`、`publicUrl`、`mimeType`、`width`、`height`、`auditStatus` |
| mentions | 响应中的 @ 用户摘要列表，来自 `comment_mentions` |
| topics | 响应中的评论话题摘要列表，来自 `comment_topics` |
| stickers | 响应中的评论贴纸列表，来自 `comment_stickers` |
| status | 审核状态 |

评论内容校验：文字、图片、贴纸三者至少一种存在；`mentionUserIds` / `topicIds` 不能单独构成有效评论。评论图片当前只支持 `POST_IMAGE` 类型的图片 FileAsset；视频评论、用户自定义贴纸、付费表情包、图片私信和生产级图片审核不在第六轮关闭。

### Topic / Sticker

| 表 | 字段 | 说明 |
|---|---|---|
| `topics` | `id`、`name`、`description`、`post_count`、`created_at`、`updated_at` | MVP seed 话题；本轮支持列表、关键字搜索和话题作品查询，不做运营后台 |
| `comment_mentions` | `comment_id`、`user_id`、`created_at` | 评论 @ 用户关系；同一评论同一用户唯一 |
| `comment_topics` | `comment_id`、`topic_id`、`created_at` | 评论绑定话题关系；同一评论同一话题唯一 |
| `sticker_packs` | `id`、`name`、`sort_order`、`created_at`、`updated_at` | 内置贴纸包 |
| `stickers` | `id`、`pack_id`、`name`、`emoji_text`、`image_url`、`sort_order`、`created_at`、`updated_at` | 内置贴纸项；可用文本或静态资源 URL 表达 |
| `comment_stickers` | `comment_id`、`sticker_id`、`created_at` | 评论贴纸关系；同一评论同一贴纸唯一 |

话题、贴纸和 @ 用户只关闭评论互动 MVP：不建模话题热榜、用户自定义贴纸、付费表情包、贴纸上传、复杂审核或运营配置台。

### Follow / Like / Favorite

关系表必须包含：

- 用户 ID。
- 目标类型。
- 目标 ID。
- 创建时间。

目标类型包括帖子、图纸、商品、用户。

第六轮好友语义：

- `Follow(targetType=USER)` 是当前“好友”能力的数据基础。
- 当前不新增好友申请状态机；`mutualFollow = A 关注 B 且 B 关注 A`。
- 关注关系需要唯一约束：同一用户对同一目标只能存在一条有效关注记录。
- 取消关注后互关状态实时失效；历史私信不删除。

## 文件域

### FileAsset

| 字段 | 说明 |
|---|---|
| id | 文件 ID |
| ownerId | 上传用户 |
| usage | 文件用途 |
| storageKey | 对象存储 Key |
| mimeType | 文件类型 |
| sizeBytes | 文件大小 |
| width | 图片宽度 |
| height | 图片高度 |
| auditStatus | 审核状态 |
| publicUrl | 公开访问地址，按需生成 |

文件用途：

- `AVATAR`
- `POST_IMAGE`
- `POST_VIDEO`
- `AI_INPUT`
- `PATTERN_OUTPUT`
- `PRODUCT_IMAGE`
- `TRADE_IMAGE`

## AI 图纸域

### PatternJob

| 字段 | 说明 |
|---|---|
| id | 任务 ID |
| userId | 用户 |
| inputFileId | 输入图片 |
| beadSize | 豆子规格 |
| targetSize | 目标尺寸 |
| difficulty | 难度 |
| paletteId | 色卡 |
| style | 风格 |
| status | 任务状态 |
| failureReason | 失败原因 |
| patternId | 成功后的图纸 ID |
| progress | 任务进度，0.0-1.0 |
| analysisResultJson | 视觉分析结果 JSON |

任务状态：

- `PENDING`
- `PROCESSING`
- `SUCCEEDED`
- `FAILED`
- `REJECTED`（前端展示为"失败"，与 `FAILED` 统一）
- `CANCELED`

### PatternAsset

| 字段 | 说明 |
|---|---|
| id | 图纸 ID |
| jobId | 来源任务 |
| ownerId | 用户 |
| previewFileId | 预览图 |
| gridFileId | 网格图 |
| colorMapFileId | 色号图 |
| pdfFileId | PDF 图纸 |
| beadSize | 豆子规格 |
| widthCells | 宽度格数 |
| heightCells | 高度格数 |
| totalBeads | 总豆量 |
| materialsJson | 材料清单 JSON |
| status | 可见状态 |

### PaletteColor

| 字段 | 说明 |
|---|---|
| id | 颜色 ID |
| paletteId | 色卡 |
| colorCode | 色号 |
| displayName | 颜色名 |
| hex | 屏幕色值 |
| productSkuId | 可关联 SKU |

## 商品域

### Product

| 字段 | 说明 |
|---|---|
| id | 商品 ID |
| type | 商品类型 |
| sellerId | 卖家，自营可为空或平台账号 |
| title | 标题 |
| description | 描述 |
| imageUrl | 第四轮商品展示图 URL，可为空；购物车摘要同步返回该字段 |
| categoryId | 分类 |
| status | 商品状态 |
| auditStatus | 审核状态 |

商品类型：

- `SELF_OPERATED`
- `PLAYER_SECOND_HAND`
- `PLAYER_CUSTOM_SERVICE`

商品状态：

- `DRAFT`
- `ON_SALE`
- `OFF_SALE`
- `SOLD_OUT`
- `DELETED`

### Sku

| 字段 | 说明 |
|---|---|
| id | SKU ID |
| productId | 商品 |
| specName | 规格名 |
| priceCent | 价格，单位分 |
| stock | 库存 |
| status | 状态 |

## 订单支付域

### Order

| 字段 | 说明 |
|---|---|
| id | 订单 ID |
| buyerId | 买家 |
| sellerType | 自营或玩家 |
| sellerId | 卖家 |
| orderType | 订单类型 |
| status | 订单状态 |
| totalAmountCent | 总金额 |
| payableAmountCent | 应付金额 |
| addressSnapshot | 地址快照 |

订单状态：

- `CREATED`
- `WAITING_PAYMENT`
- `PAID`
- `FULFILLING`
- `SHIPPED`
- `COMPLETED`
- `CANCELED`
- `REFUNDING`
- `REFUNDED`

### Payment

| 字段 | 说明 |
|---|---|
| id | 支付单 ID |
| orderId | 订单 |
| channel | 支付渠道 |
| status | 支付状态 |
| amountCent | 支付金额 |
| channelTradeNo | 渠道交易号 |
| paidAt | 支付时间 |

支付状态：

- `CREATED`
- `PROCESSING`
- `SUCCEEDED`
- `FAILED`
- `CLOSED`

### Refund

| 字段 | 说明 |
|---|---|
| id | 退款单 ID |
| orderId | 订单 |
| paymentId | 支付单 |
| amountCent | 退款金额 |
| reason | 原因 |
| status | 状态 |

## 消息和激励域

### Message

| 字段 | 说明 |
|---|---|
| id | 消息 ID |
| userId | 接收用户 |
| type | 类型 |
| title | 标题 |
| content | 内容 |
| readAt | 已读时间 |

消息类型包含评论 @ 触发的 `MENTION`；本轮只要求写入通知、列表展示和已读标记，不扩展推送或复杂通知状态机。

### Conversation

| 字段 | 说明 |
|---|---|
| id | 会话 ID |
| participantAId | 参与用户 A |
| participantBId | 参与用户 B |
| lastMessageId | 最后一条私信，可为空 |
| unreadCount | 当前用户视角未读数由查询层计算 |
| createdAt | 创建时间 |
| updatedAt | 更新时间 |

### DirectMessage

| 字段 | 说明 |
|---|---|
| id | 私信 ID |
| conversationId | 会话 ID |
| senderId | 发送者 |
| receiverId | 接收者 |
| content | 文本内容 |
| readAt | 已读时间 |
| createdAt | 创建时间 |

未互关私信限制：

- 互关判断基于 `Follow(targetType=USER)` 双向关系。
- 未互关时，同一发送者对同一会话最多发送 3 条 `DirectMessage`。
- 达到限制后不写入新消息，后端返回业务错误；互关后限制解除。
- 本轮不建模图片私信、撤回、删除、黑名单和复杂风控状态。

### RewardLedger

| 字段 | 说明 |
|---|---|
| id | 流水 ID |
| userId | 用户 |
| type | 积分或经验 |
| amount | 数量 |
| reason | 来源 |
| relatedId | 关联对象 |

## 审核与风控域

### Report

| 字段 | 说明 |
|---|---|
| id | 举报 ID |
| reporterId | 举报人 |
| targetType | 目标类型 |
| targetId | 目标 ID |
| reason | 原因 |
| status | 处理状态 |

### ModerationRecord

| 字段 | 说明 |
|---|---|
| id | 审核记录 ID |
| targetType | 目标类型 |
| targetId | 目标 ID |
| result | 审核结果 |
| reason | 原因 |
| operatorType | 机审或人审 |

审核结果：

- `PASS`
- `REJECT`
- `SELF_VISIBLE`
- `NEED_MANUAL_REVIEW`
