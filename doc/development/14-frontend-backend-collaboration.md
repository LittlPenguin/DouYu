# 14. 前后端协作与项目管理规则

## 目标

本分册用于管理豆屿 Doyu 第一阶段前后端分离开发。它面向项目负责人、Android 开发、后端开发、测试和后续 AI Agent。

## 工作边界

- 前端指 Android 客户端，默认工程路径为 `D:\Studio\SpellBean\DouYu`。
- 后端指 Spring Boot API 服务，建议工程路径为 `D:\Studio\SpellBean\doyu-server`。
- 第一阶段优先完成可联调闭环，不追求一次性完成全部运营后台和复杂风控。
- 前端不得绕过后端鉴权、审核、支付状态确认。
- 后端不得把 AI Key、OSS Secret、支付密钥下发给前端。

## 推荐推进顺序

1. 后端建立 Spring Boot 工程骨架、统一响应、错误码、traceId、鉴权占位。
2. 后端提供 OpenAPI 或等价接口契约。
3. 前端建立 Compose 导航、主题、页面状态模型、API Client 骨架。
4. 前端按 Mock 或接口契约完成 5 个主 Tab 和核心页面。
5. 后端补齐登录、社区 Feed、上传、AI 任务、商品、订单等可联调接口。
6. 前后端进行登录、社区、AI 拼图、商城、订单支付占位链路联调。
7. 测试按 `10-testing-acceptance.md` 执行验收。

## 接口冻结规则

Android 与后端联调前必须冻结当前接口版本。冻结内容包括：

- 路径。
- 请求字段。
- 响应字段。
- 错误码。
- 状态枚举。
- 分页结构。
- 鉴权规则。
- 幂等规则。

冻结后如果必须变更，提出方需要同步修改：

- `05-api-contract.md`
- 相关客户端 API model
- 相关后端 DTO 或响应对象
- 相关测试样例

## 文档同步规则

- 修改接口字段、路径、状态枚举时，同步 `05-api-contract.md`。
- 修改核心表、数据对象、状态机时，同步 `06-data-model.md`。
- 修改支付、订单、退款、库存逻辑时，同步 `08-commerce-payment.md`。
- 修改 AI 拼豆流程时，同步 `07-ai-pattern-generation.md`。
- 修改权限、隐私、未成年人、内容审核、账号注销时，同步 `09-security-compliance.md`。
- 修改 Android 页面结构、权限、上传、支付、缓存策略时，同步 `03-android-client.md`。
- 修改后端模块、鉴权、队列、后台、审核策略时，同步 `04-backend-services.md`。

## 联调主链路

第一阶段至少完成以下联调链路：

- 手机号验证码登录到获取当前用户。
- 社区 Feed 到帖子详情。
- 发帖提交到审核中状态展示。
- 图片预签名上传到上传确认。
- AI 任务创建到任务状态轮询。
- AI 任务成功到图纸结果展示。
- 商品列表到商品详情。
- 加入购物车到订单确认。
- 创建订单到创建支付单。
- 支付结果页到后端订单状态查询。
- 消息通知列表展示。
- 我的页面到生成记录和订单入口。

## Mock 策略

- 后端接口未完成前，前端可使用本地 Mock 数据，但数据结构必须贴合 `05-api-contract.md`。
- Mock 数据不得改变真实接口字段名。
- Mock 的错误状态必须覆盖 `UNAUTHORIZED`、`FORBIDDEN`、`AUDIT_REJECTED`、`AI_TASK_FAILED`、`INVENTORY_NOT_ENOUGH`。
- 支付、AI、上传可以先用占位 Provider，但接口形状必须保持真实。

## 风险控制

- AI、支付、OSS、短信均通过后端抽象，客户端只拿业务所需参数。
- 支付成功不得由客户端本地 SDK 返回直接判定。
- 玩家交易第一版不做平台余额，不沉淀资金池。
- 玩家卖家、提现、定制服务发布者必须满足 18+ 实名要求。
- 所有用户上传内容默认按不可信输入处理。
- AI 输入和输出都需要审核记录。

## 验收节奏

### 前端

在 `D:\Studio\SpellBean\DouYu` 执行：

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

### 后端

在后端工程目录执行项目测试命令。若使用 Gradle：

```powershell
.\gradlew test
```

### 联调

联调完成后必须提供：

- 前后端接口版本说明。
- 本地启动方式。
- 测试账号或测试登录方式。
- 已联通链路清单。
- 未接入第三方能力的占位说明。
- 已知风险和下一阶段处理项。

## 第一阶段完成标准

- Android Debug 包可构建。
- 后端服务可本地启动。
- `/api/v1` 接口规范统一。
- 前端 5 个主 Tab 可操作。
- 登录、社区、AI 拼图、商城、订单支付占位、我的页面形成可演示闭环。
- 没有硬编码密钥。
- 文档、接口、数据模型、页面状态保持一致。

## 当前协同整改结论

检查日期：2026-05-13。

当前 Android 与后端均已具备第一阶段骨架，但还不能视为满足前后端协作要求。主要问题是：后端接口已经推进到可测骨架，前端仍以 Mock 页面和本地模型为主，双方字段名、分页形态、上传到 AI 任务的衔接口径尚未完全一致。

### 前端必须先整改

- 按 `12-frontend-android-task-brief.md` 的“当前联调补充整改项”修正 API model 和 API Client。
- 将 Mock 数据结构调整为真实接口结构，不能继续使用只服务 UI 展示的字段名。
- 上传与 AI 任务链路必须改为：`presign -> 直传 -> confirm -> patterns/jobs(inputFileId) -> jobs/{jobId}`。
- 订单、支付、退款等重要提交必须生成并携带 `Idempotency-Key`。
- 支付结果页必须查询后端订单或支付单状态，不能以本地支付 SDK 返回作为最终成功。

### 后端必须同步补齐

- 按 `13-backend-service-task-brief.md` 的“当前联调补充整改项”输出 OpenAPI/Swagger 或等价接口契约。
- 明确所有 Stub Provider：短信、OSS、AI、支付。
- 对前端主链路所需字段保持稳定返回；占位值可以接受，字段缺失不接受。
- 支付回调在未验签前只能标注为 Stub，不得作为正式支付实现。

### 联调冻结点

下一次联调前冻结以下接口口径：

- `/uploads/presign` 返回 `fileKey`，用于对象存储直传。
- `/uploads/confirm` 返回 `fileId`，用于后续业务引用。
- `/patterns/jobs` 请求字段使用 `inputFileId`。
- 列表接口统一返回分页结构，不返回裸数组。
- 对外业务 ID 字段统一使用带业务语义的字符串字段，例如 `userId`、`postId`、`jobId`、`patternId`、`productId`、`skuId`、`orderId`、`paymentId`。

### 协同验收方式

前后端完成整改后，按以下顺序验收：

1. 后端运行测试并提供 OpenAPI/Swagger 地址。
2. 前端基于后端契约完成 API model 和 Mock 结构修正。
3. 联通登录、Feed、上传确认、AI 任务创建与查询、商品列表、购物车、创建订单、创建支付单、支付状态查询。
4. 前端运行 `.\gradlew.bat :app:assembleDebug` 和 `.\gradlew.bat :app:testDebugUnitTest`。
5. 后端运行 `mvn test` 或后端 README 中声明的等价命令。
6. 项目负责人确认“已接真实接口清单”和“仍为 Stub/Mock 清单”。
