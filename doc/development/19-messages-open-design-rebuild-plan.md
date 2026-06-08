# 19 · 消息页 Open Design 1:1 重建计划

> Worktree: `codex/messages-open-design` ·
> 范围：仅「消息」体系三屏（消息首页 / 私信对话详情 / 通知详情）。
> UI 权威：`doc/development/open-design/` 下 HTML。数据权威：`doyu-server` 真实接口。
> 约束遵循 `AGENTS.md`：Java + XML，无 Kotlin/Compose，无本地假数据，空/错/未登录/禁用/UI-only 均渲染真实边界。

## 1. 目标与边界

### 负责（本工作树）
| Open Design | Android 目标文件 |
| --- | --- |
| `messages-a.html` | `MessagesFragment.java` · `fragment_messages_home.xml` |
| `message-conversation-a.html` | `ConversationActivity.java` · `activity_conversation.xml` |
| `notification-detail-a.html` | `NotificationDetailActivity.java` · `activity_notification_detail.xml` |

### 不负责（其他工作树 / 共享框架，禁止改动）
- `MainActivity` / `activity_main.xml`：顶部栏（消息标题、搜索、快捷菜单）与底部导航是全局框架，已正确，不动。
- community / commerce / ai / profile 任何文件。

### 需要的共享文件改动（仅追加，低冲突）
- `network/DoyuApi.java`：新增「发送私信」「标记通知已读」两个端点（后端已实现，客户端缺接入）。
- `data/DoyuRepository.java`：对应封装方法。
- `core/IntentExtras.java`：新增 `PEER_NAME`、`TYPE`、`CREATED_AT` 键。
- `model/SendMessageRequest.java`：新增请求体。
- 后端 `message/MessageController.java`：`notificationView` 补 `createdAt`（实体已有该字段），让通知时间可真实渲染。仅追加字段，不改语义。

## 2. 数据契约（来自 `MessageController`，真实接口）
- `GET /messages/conversations` → 列表项：`conversationId, peerName, peer, lastMessage, mutualFollow, remainingNonMutualMessages, canSend, unreadCount`。
- `GET /messages/conversations/{id}` → `{conversation, messages[]}`；`messages` 项：`messageId, senderName, content, mine, createdAt`。
- `POST /messages/conversations/{id}` body `{content}` → 新消息视图；未互关且已发 ≥3 条时返回业务错误「互相关注后可继续聊天」。
- `GET /messages/notifications` → 列表项：`notificationId, type, title, content, unread`（本计划补 `createdAt`）。
- `POST /messages/notifications/read` → 全部标记已读 `{read:true}`。

## 3. 现状差距
当前实现为纯文本占位（`StringBuilder` 灌进 `TextView`），与设计差距大：
- 首页把会话+通知合并成一个无差别 `SummaryItem` 列表，无私信/通知切换、无状态徽标、无头像/图标。
- 对话详情仅文本转储，输入框写死禁用，无真实发送、无气泡、无互关/剩余/禁发/失败态。
- 通知详情仅回显 intent 透传文本，无类型徽标、无「标为已读」、无不可回复边界结构。

## 4. 逐屏实现规格

### 4.1 消息首页 `MessagesFragment` + `fragment_messages_home.xml`
改为自定义 Fragment（不再继承 `BaseListFragment`），结构自上而下：
1. **分段 Tab**：`私信 / 通知`，可切换；选中态用 `bg_chip_selected`/`bg_chip_plain` 风格（沿用社区 chip 配色：选中 `doyu_petal_deep` 描白字）。
2. **hero-strip 卡片**（`bg_state_card`）：品牌徽标「新消息进入私信」(`bg_brand_pill`) + 标题「未读对话」+ 说明「对话输入中、未互关剩余条数、禁发态只在私信里展示」。
3. **列表区**（`FrameLayout`，复用 `summary_list/loading/empty_text/error_box/error_text/retry_button` id）：按当前 Tab 切换数据源与适配器。
   - **私信 Tab** → `ConversationAdapter` + `item_conversation.xml`：头像（渐变 `bg_thumb_*` 按位轮换）、昵称、副标题、状态徽标。
     - 互关：副标题 `互相关注[ · lastMessage]`，徽标「互关」(`bg_pill_ok`)。
     - 未互关可发：副标题 `未互关剩余 N 条[ · lastMessage]`，徽标 `N/3`(`bg_pill_warn`)。
     - 未互关禁发：副标题 `超过 3 条，互相关注后可继续聊天`，徽标「禁发」(`bg_pill_stop`)。
     - 点击 → `ConversationActivity`（传 `CONVERSATION_ID` + `PEER_NAME`）。
   - **通知 Tab** → `NotificationAdapter` + `item_notification.xml`：圆角图标（按 type：含 AI/PATTERN→`ic_action_ai`；含 AT/MENTION→`ic_notify_at`；否则 `ic_notify_bell`）、标题、内容、时间（`createdAt` 有则显示，无则省略，不编造）。
     - 点击 → `NotificationDetailActivity`（传 `NOTIFICATION_ID, TITLE, BODY, TYPE, CREATED_AT`）。
4. **状态**：每个 Tab 独立渲染 loading / content / empty(`UiCopy.MESSAGES_EMPTY`) / error(可重试) / login。

### 4.2 私信对话详情 `ConversationActivity` + `activity_conversation.xml`
1. **Toolbar**：`include_page_toolbar`，标题=对方昵称（先用 `PEER_NAME` 占位，详情加载后用真实 peerName 覆盖）。
2. **hero-strip**：按会话状态：
   - 互关 → 徽标「互相关注」(ok) + 「可以继续聊天」+ 说明。
   - 未互关可发 → 徽标「未互关剩余 N 条」(warn) + 「你还可以发送 N 条消息」。
   - 禁发 → 徽标「已达上限」(stop) + 「互相关注后可继续聊天」。
3. **消息列表**：`RecyclerView` + `ChatAdapter` + `item_chat_message.xml`（两种对齐）：
   - 对方（`mine=false`）：左对齐，头像 + 气泡（`bg_bubble_peer`）。
   - 自己（`mine=true`）：右对齐，气泡（`bg_bubble_mine`，`doyu_petal_soft`）。
4. **浮动输入栏**（`bg_floating_bar`）：`EditText`「输入私信内容」+ 状态徽标 + 发送按钮。
   - 可发（互关 / 剩余>0）：输入与发送可用；徽标「互关可发送」或「未互关剩余 N 条」。
   - 禁发（未互关且剩余=0）：警示条（`bg_warning_band`）「互相关注后可继续聊天」；输入禁用（不弹键盘）；发送隐藏；显示「去关注」幽灵按钮。
5. **发送逻辑**（真实 `POST`）：
   - 成功(200)：把返回的真实消息追加到列表、清空输入、重载会话刷新 remaining/canSend、滚到底。**仅真实成功才追加**。
   - 上限错误：切换禁发态，保留草稿，不弹假成功。
   - 网络/其他错误：保留草稿在输入框，显示警示条「网络异常，消息未发送」+「重试 / 取消」。
6. **错误/缺参**：缺 `conversationId` 或加载失败 → 错误态 + 重试，不吞白屏。

### 4.3 通知详情 `NotificationDetailActivity` + `activity_notification_detail.xml`
1. **Toolbar**：`include_page_toolbar`，标题「通知详情」。
2. **hero-strip**：类型徽标（审核→`审核通知`warn / 互动→`互动通知`ok / 任务→`任务通知`ok / 系统→`系统通知`）+ 标题 + 内容。
3. **事件卡**：圆角图标（按 type）+ 标题 +（`createdAt` 有则时间）。
4. **动作**：「标为已读」按钮 → `POST /notifications/read`（真实，全部已读）；成功后更新为已读态。
5. **不可回复边界条**：「通知详情不是私信会话；如需沟通，进入作者资料或私信会话。」
6. **诚实边界**：无输入框、不展示私信剩余条数；对象跳转（查看作品/图纸）后端暂无关联对象数据，以 UI-only 边界标注，不放假按钮。
7. 缺 `notificationId` → 维持现有边界提示。

## 5. 新增/复用资源
- 新增矢量：`ic_notify_bell.xml`、`ic_notify_at.xml`（沿用现有 24dp viewport、`doyu_text` 填充风格）。
- 新增气泡：`bg_bubble_mine.xml`（`doyu_petal_soft`，圆角）、`bg_bubble_peer.xml`（`doyu_surface` + `doyu_open_line` 描边）。
- 复用：`bg_pill_ok/warn/stop`、`bg_round_icon`、`bg_warning_band`、`bg_floating_bar`、`bg_input_line`、`bg_brand_pill`、`bg_state_card`、`bg_card`、`bg_thumb_petal/mint/sky`、`bg_icon_button`、`ic_action_*`。

## 6. 验收标准（对齐设计要点）
**首页**：① 私信/通知 Tab 切换；② 私信行头像+昵称+状态副标题+徽标(互关/N/3/禁发)；③ 通知行图标+标题+内容(+时间)；④ 私信行进对话、通知行进详情；⑤ 每 Tab loading/content/empty/error/login；⑥ 无本地假数据。
**对话**：① Toolbar 显对方名；② hero 反映互关/剩余/禁发；③ 气泡左右分；④ 输入栏「输入私信内容」+发送；⑤ 互关发送成功追加真实消息；⑥ 剩余 N 显示并递减；⑦ 禁发禁用+去关注+无假成功；⑧ 失败保留草稿+重试/取消。
**通知**：① Toolbar「通知详情」；② 类型徽标+标题+内容；③ 事件卡；④ 无输入框/无私信剩余态；⑤ 标为已读调真实接口；⑥ 不可回复边界；⑦ 缺参边界 + 对象跳转 UI-only 诚实标注。

## 7. 验证方式
- **编译**：`./gradlew :app:compileDebugJavaWithJavac`（或 `assembleDebug`），捕获真实输出。
- **后端**：若改动 `MessageController`，跑相关后端测试。
- **真机/模拟器截图校验：本轮延后（DEFERRED）**——设备只有一台，多工作树并发占用会冲突；按指令本轮不做，记为「未覆盖」，不谎报通过。

### 7.1 验证结果（真实输出）
- Android：`./gradlew :app:compileDebugJavaWithJavac --console=plain` → **BUILD SUCCESSFUL in 1m 7s**（9 tasks executed）。已执行 `generateDebugResources` / `packageDebugResources` / `generateDebugRFile` / `compileDebugJavaWithJavac`，即新增/重写的 XML 布局、drawable、颜色与全部 Java 均通过编译。唯一提示为 `notifyDataSetChanged` 等过时 API 告警，与既有 `SummaryAdapter`/`CommunityPostAdapter` 一致，非本轮引入。
- 后端：`./mvnw -o -q compile`（doyu-server）→ **EXIT 0**，`MessageController.notificationView` 追加 `createdAt` 改动通过。
- 真机/模拟器截图校验：**未覆盖（DEFERRED）**——按指令不占用唯一设备，留待统一真机验收阶段补做。


## 8. 风险与边界
- 共享文件（`DoyuApi`/`DoyuRepository`/`IntentExtras`/后端 `MessageController`）仅做**追加式**改动，降低与其他工作树的合并冲突。
- 严格无本地假数据：所有列表/详情只渲染后端真实返回；空库即空态。
- 不触碰全局框架与其他业务页。
