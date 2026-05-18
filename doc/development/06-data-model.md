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
| ageGroup | 年龄段 |
| isMinor | 是否未成年人 |
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
| topicIds | 话题 |
| linkedPatternId | 关联图纸 |
| status | 内容状态 |
| likeCount | 点赞数 |
| favoriteCount | 收藏数 |
| commentCount | 评论数 |

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
| status | 审核状态 |

### Follow / Like / Favorite

关系表必须包含：

- 用户 ID。
- 目标类型。
- 目标 ID。
- 创建时间。

目标类型包括帖子、图纸、商品、用户。

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

