# 19 · 消息页通知优先与首页状态移除计划

> 范围：仅「消息」体系三屏中的消息首页调整；私信对话详情和通知详情保留既有真实接口能力。
> UI 权威：`doc/development/open-design/messages-a.html`。数据权威：`doyu-server` 真实接口。
> 约束：Java + XML；无 Kotlin/Compose；无本地假数据；空、错、登录、禁用/UI-only 边界必须真实渲染。

## 1. 本轮目标

- 消息首页从「私信 / 通知 Tab」改为一个纵向页面。
- 首页内容顺序固定为：通知在上，消息在下。
- 首页消息行删除互关、剩余额度、禁发等状态展示和计算。
- 私信对话详情继续使用 `mutualFollow`、`remainingNonMutualMessages`、`canSend` 控制输入、发送、禁发、重试和去关注提示。

## 2. 不做

- 不新增后端接口。
- 不修改 `MessageController`、消息数据模型或私信发送限制语义。
- 不新增群聊、好友申请审批、通知对象跳转闭环或本地 mock 数据。
- 不删除 `ConversationActivity` 中的互关/3 条限制，因为该限制属于私信详情发送功能。

## 3. 文件范围

| 类型 | 文件 |
| --- | --- |
| Open Design | `doc/development/open-design/messages-a.html` |
| Android 首页 | `DouYu/app/src/main/java/cn/edu/app/douyu/feature/message/MessagesFragment.java` |
| Android 适配器 | `DouYu/app/src/main/java/cn/edu/app/douyu/feature/message/MessageHomeAdapter.java`，`ConversationAdapter.java` |
| Android 布局 | `fragment_messages_home.xml`，`item_conversation.xml`，`item_message_section_header.xml`，`item_message_section_empty.xml` |
| 测试 | `OpenDesignLayoutMappingTest` 或消息页专用 JVM 测试 |
| 验收记录 | `doc/development/verification/2026-06-08-messages-home-notification-first-verification.md` |

## 4. 首页实现规格

### Open Design

- 删除首页首屏中的旧 Tab、旧 hero 卡片和旧说明文案。
- 手机稿中先展示「通知」section：通知图标、标题、内容、真实时间；点击进入 `notification-detail-a.html`。
- 再展示「消息」section：头像、昵称、最后一条消息摘要；点击进入 `message-conversation-a.html`。
- 消息行右侧不展示 `互关`、`1/3`、`禁发` 等 pill；消息行副标题不展示剩余额度或禁发提示。
- 首页说明文字只描述通知和消息入口，不承载私信输入限制说明。

### Android

- `MessagesFragment` 删除 Tab 状态和单 Tab 加载逻辑，一次加载通知和会话。
- 加载策略：
  - 两类接口都成功才进入 content。
  - 401 显示统一登录态。
  - 任一非登录错误显示整体错误态和重试。
  - 两类都为空显示整体空态。
  - 只有一类为空时，保留该 section 并显示简短 section 空态行。
- `MessageHomeAdapter` 用多 view type 渲染 section header、通知行、消息行和 section 空态。
- 通知点击继续传 `NOTIFICATION_ID`、`TITLE`、`BODY`、`TYPE`、`CREATED_AT`。
- 消息点击继续传 `CONVERSATION_ID` 和 `PEER_NAME`。
- `ConversationAdapter` 不再读取或渲染 `mutualFollow`、`remainingNonMutualMessages`、`canSend`。

## 5. 数据契约

- `GET /messages/notifications`：通知列表，字段包括 `notificationId`、`type`、`title`、`content`、`createdAt`。
- `GET /messages/conversations`：会话列表，首页只使用 `conversationId`、`peerName`/`peer.nickname`、`lastMessage`。
- `GET /messages/conversations/{id}` 和 `POST /messages/conversations/{id}`：仍由私信详情使用，用于真实聊天和发送限制。
- `POST /messages/notifications/read`：仍由通知详情使用。

## 6. 验收标准

- `messages-a.html` 不再包含首页旧文案、Tab、消息行互关/额度/禁发 pill。
- Android 消息首页首屏结构为通知 section 在上、消息 section 在下。
- 首页消息行无 `conversation_status`，也不再按互关/额度/禁发设置样式。
- 私信详情仍能根据后端返回字段控制输入栏和发送限制。
- 所有列表只渲染后端数据或真实空/错/登录状态。

## 7. 验证命令

- `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain`
- `cd DouYu && .\gradlew.bat :app:compileDebugJavaWithJavac --console=plain`
- `git diff --check`
- 若有 Android 设备/模拟器，再运行视觉冒烟并记录截图；若无设备，验收记录必须写明未覆盖。
