# 2026-06-08 消息首页通知优先验收记录

## 范围

- Open Design `messages-a.html` 改为通知在上、消息在下。
- Android 消息首页删除 `私信 / 通知` Tab、旧 hero 文案和首页会话状态徽标。
- 首页消息行不再展示或计算互关、剩余额度、禁发状态。
- 私信详情发送限制保留在 `ConversationActivity`。

## 已验证

| 命令 | 结果 | 关键输出 |
| --- | --- | --- |
| `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --tests "cn.edu.app.douyu.core.OpenDesignLayoutMappingTest.messagesHomeShowsNotificationsBeforeMessagesWithoutPrivateQuotaBadges" --console=plain` | 通过 | `BUILD SUCCESSFUL in 4s` |
| `cd DouYu && .\gradlew.bat :app:testDebugUnitTest --console=plain` | 通过 | `BUILD SUCCESSFUL in 4s` |
| `cd DouYu && .\gradlew.bat :app:compileDebugJavaWithJavac --console=plain` | 通过 | `BUILD SUCCESSFUL in 2s` |
| `git diff --check` | 通过 | 无空白错误；仅提示 `messages-a.html` 未来 Git 触碰时会做 CRLF 转换 |
| `D:\AndroidChace\platform-tools\adb.exe devices` | 未发现设备 | 输出只有 `List of devices attached`，没有设备行 |

## 未覆盖

- 未运行视觉冒烟截图；原因是 `adb devices` 未列出可用 Android 设备或模拟器。
- 未运行后端测试；本轮未修改 `doyu-server/` 或后端契约。

## 结论

- JVM 约束测试已覆盖：Open Design 旧首页文案和状态 pill 删除、Android 首页无 Tab、`item_conversation.xml` 无 `conversation_status`、`ConversationAdapter` 不再计算首页互关/额度/禁发状态、`ConversationActivity` 仍保留详情页发送状态字段。
- Android Java/XML 编译通过。
- 截图 parity 仍需在可用设备上补跑并记录。
