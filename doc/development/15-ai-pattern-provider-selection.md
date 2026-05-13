# 15. AI 拼豆图 Provider 选型与接入方案

## 文档目标

本文档用于确定豆屿 Doyu“识别图片生成拼豆图”能力的外部 AI 渠道、Agent 使用边界、Provider 抽象、提示词规则、成本控制和验收标准。

第一版默认决策：

- 不训练专属大模型。
- 不让大模型直接输出最终拼豆格子图。
- 大模型和 Agent 只负责图片理解、主体识别、裁剪建议、风格建议、清背景、低细节化和参数推荐。
- 最终拼豆图纸由后端自研 `BeadPatternEngine` 生成，保证格子矩阵、色卡匹配、豆量统计、材料清单和商城 SKU 可解释、可复现、可验收。
- AI Provider 只由后端访问，Android 客户端不得直连模型供应商、Agent 平台或对象存储密钥。

## 推荐总体路线

```text
用户图片
→ 上传与输入审核
→ 视觉理解模型识别主体、背景、构图和风险
→ 可选图像预处理：清背景、低色块化、可爱化、二次元化
→ 自研拼豆算法：裁剪、缩放、量化、真实色卡匹配、降噪、网格生成
→ 输出：预览图、网格图、色号图、豆量清单、材料清单、PDF
→ 输出审核
→ 用户保存、发帖或加入购物车
```

核心原则：

- 大模型提升“图片变得适合做拼豆”的成功率。
- 自研算法负责“拼豆图真的能做出来”。
- Agent 平台适合做原型、流程编排和提示词调试，不作为第一版生产主链路的唯一依赖。
- 每次生成必须记录 Provider、模型、提示词版本、参数版本、色卡版本和算法版本，便于复现和追责。

## Provider 优先级

| 优先级 | 渠道 | 适用能力 | 定位 | 结论 |
|---|---|---|---|---|
| P0 | 阿里云百炼 Qwen-VL + 通义万相 | 视觉理解、图像编辑、图像生成 | 主 Provider | 国内云生态成熟，适合作为第一版默认接入 |
| P1 | 火山方舟 豆包视觉理解 + Seedream/即梦 | 视觉理解、图像生成、图像编辑 | 备用 Provider 和 A/B 测试 | 适合做效果对比和容灾 |
| P2 | 百度千帆视觉理解 + 图像生成 | 视觉理解、图像生成 | 备用 Provider | 可作为价格、稳定性或审核策略备选 |
| P2 | 腾讯云 TokenHub 视觉模型 + 混元图像生成 | 视觉理解、图像生成 | 备用 Provider | 适合在腾讯云生态或渠道成本更优时接入 |
| P3 | Dify Workflow | Agent 编排、提示词实验、运营调参 | 原型和内部工具 | 不作为生产最终图纸生成引擎 |
| P3 | Coze/扣子 | Agent 编排、流程实验、运营工具 | 原型和内部工具 | 可用于验证用户流程和后台辅助 |
| 参考 | Beadifier、MakeBead、PixelBead | 拼豆图 UX 和算法参考 | 竞品与参考工具 | 不作为生产依赖 |

## 官方资料入口

以下资料用于后续技术评估和接入核验，正式开发前以后端工程师实际账号、地域、模型版本和计费页为准。

- [阿里云百炼视觉理解模型](https://help.aliyun.com/zh/model-studio/vision/)：用于图片内容理解、主体识别、场景描述和结构化分析。
- [阿里云百炼通义万相图像生成与编辑](https://help.aliyun.com/zh/model-studio/wan-image-generation-and-editing-api-reference)：用于清背景、风格化、低细节化等图像预处理能力评估。
- [火山方舟图片生成 API](https://www.volcengine.com/docs/82379/1666945)：用于 Seedream/即梦相关图像生成或编辑能力评估。
- [火山方舟视觉理解方案](https://www.volcengine.com/docs/82379/1588784)：用于豆包视觉理解能力评估。
- [百度千帆 API V2](https://cloud.baidu.com/doc/qianfan/s/qmh4sv5vi)：用于统一理解千帆模型调用规范。
- [百度千帆视觉理解模型](https://cloud.baidu.com/doc/qianfan-docs/s/fm8r1ndsm)：用于图片理解能力评估。
- [百度千帆图像生成模型](https://cloud.baidu.com/doc/qianfan-docs/s/bm8wv3h6f)：用于图像生成和图像编辑能力评估。
- [腾讯云 TokenHub 视觉模型](https://cloud.tencent.com/document/product/1823/130085)：用于视觉理解模型备选评估。
- [腾讯云混元生图 API](https://cloud.tencent.com/document/product/1668/120721)：用于图像生成能力备选评估。
- [Dify Workflow Run API](https://docs.dify.ai/api-reference/workflows/run-workflow)：用于内部 Agent 流程原型。
- [Dify LLM 节点](https://docs.dify.ai/zh-hans/guides/workflow/node/llm)：用于提示词链路和多节点编排参考。
- [Coze Java SDK](https://github.com/coze-dev/coze-java)：用于后端接入 Coze/扣子生态时评估 SDK。
- [Coze Studio API Reference](https://github.com/coze-dev/coze-studio/wiki/6.-API-Reference)：用于 Coze Studio API 能力评估。
- [Beadifier GitHub](https://github.com/maxcleme/beadifier)：用于研究开源拼豆图生成思路。
- [MakeBead](https://makebead.com/)：用于参考在线拼豆图工具的交互和输出。
- [PixelBead](https://www.pixelbead.art/)：用于参考 AI 拼豆图工具的参数和结果表达。

## 能力边界

### 大模型负责的部分

大模型可以做：

- 判断图片主体是什么。
- 判断主体是否清晰、是否被遮挡、是否适合转拼豆图。
- 推荐裁剪区域。
- 推荐拼豆难度、尺寸、颜色数量和风格。
- 识别复杂背景并建议清背景。
- 将复杂照片处理成更适合像素化的平面色块图。
- 生成用户可读的失败原因或优化建议。

大模型不得做：

- 直接决定最终每个格子的色号。
- 直接输出商城材料清单。
- 直接输出最终豆量统计。
- 直接判断支付、订单、库存或用户权益。
- 直接接收客户端密钥或用户身份凭证。
- 绕过输入审核和输出审核。

### 自研算法负责的部分

`BeadPatternEngine` 必须负责：

- 按目标格数裁剪和缩放图片。
- 控制颜色数量。
- 色彩量化。
- 映射真实拼豆色卡。
- 使用 Lab 色彩空间和 CIEDE2000 色差计算最近可用色。
- 处理孤立像素和噪点。
- 生成格子矩阵。
- 生成预览图、纯网格图、色号图和 PDF。
- 统计每种颜色豆量和总豆量。
- 根据色号匹配商城 SKU。
- 输出可复现的算法版本和参数。

## 后端模块设计

建议后端按以下服务拆分职责：

| 服务 | 职责 |
|---|---|
| `PatternJobService` | 创建任务、校验权益、状态流转、失败处理、任务查询 |
| `AiImageAnalysisService` | 调用视觉理解 Provider，输出结构化图片分析 JSON |
| `AiImagePreparationService` | 调用图像编辑 Provider，生成清背景或低细节中间图 |
| `AiProviderRouter` | 按场景、成本、可用性、灰度策略选择 Provider |
| `PromptTemplateService` | 管理提示词模板、版本、变量和灰度 |
| `BeadPatternEngine` | 自研拼豆算法主引擎 |
| `PaletteMatchingService` | 色卡管理、色差匹配、缺货替代色 |
| `PatternAssetService` | 输出文件生成、对象存储写入、资产记录 |
| `MaterialRecommendationService` | 根据色号和豆量匹配商城 SKU |
| `ModerationBridgeService` | 输入、输出和文本说明审核 |
| `AiCostMeterService` | 调用成本、额度扣减、失败返还和报表 |

## Provider 抽象接口

后端应以能力抽象 Provider，不以某一家供应商的字段直接污染业务层。

### 视觉理解能力

输入：

```json
{
  "imageFileKey": "oss://bucket/user/ai-input/xxx.jpg",
  "scene": "PATTERN_ANALYSIS",
  "promptVersion": "vision_analysis_v1",
  "userOptions": {
    "style": "CUTE",
    "difficulty": "NORMAL",
    "beadSize": "MM_5"
  }
}
```

输出：

```json
{
  "subject": "一只白色猫咪",
  "subjectType": "PET",
  "subjectClarity": "GOOD",
  "backgroundComplexity": "HIGH",
  "recommendedCrop": {
    "x": 0.18,
    "y": 0.12,
    "width": 0.64,
    "height": 0.72
  },
  "recommendedStyle": "CUTE",
  "recommendedDifficulty": "NORMAL",
  "recommendedGridWidth": 64,
  "recommendedColorLimit": 24,
  "beadSuitabilityScore": 82,
  "riskFlags": [],
  "advice": "建议去除背景并保留猫咪头部轮廓"
}
```

字段约束：

- `recommendedCrop` 坐标使用 0 到 1 的相对比例。
- `beadSuitabilityScore` 使用 0 到 100。
- `riskFlags` 只允许返回后端定义的枚举，例如 `COPYRIGHT_RISK`、`FACE_PRIVACY_RISK`、`LOW_QUALITY`、`UNSAFE_CONTENT`。
- Provider 返回非 JSON 或缺字段时，后端必须执行结构化修复或降级，不得直接暴露原始输出给客户端。

### 图像预处理能力

输入：

```json
{
  "sourceFileKey": "oss://bucket/user/ai-input/xxx.jpg",
  "scene": "BEAD_PATTERN_PREPARE",
  "promptVersion": "image_prepare_v1",
  "operations": ["REMOVE_BACKGROUND", "LOW_DETAIL", "EDGE_ENHANCE"],
  "targetStyle": "CUTE",
  "negativeRules": [
    "不要添加新物体",
    "不要生成文字",
    "不要改变主体身份",
    "不要改变主体姿态"
  ]
}
```

输出：

```json
{
  "preparedFileKey": "oss://bucket/pattern/intermediate/xxx.png",
  "providerTaskId": "provider_task_xxx",
  "safetyFlags": [],
  "changedSubject": false,
  "message": "已生成适合拼豆算法处理的低细节图片"
}
```

## Agent 使用策略

### 适合使用 Agent 的场景

- 内部验证不同提示词版本。
- 快速串联“视觉理解 → 预处理 → 参数建议”的实验流程。
- 为运营后台生成用户可读的失败原因。
- 对用户上传图做多轮分析，输出“怎么裁剪更好”的建议。
- 灰度比较不同 Provider 的效果。

### 不适合使用 Agent 的场景

- 第一版生产主链路完全依赖 Agent 平台。
- 让 Agent 直接生成最终格子矩阵。
- 让 Agent 持有支付密钥、OSS Secret 或用户 token。
- 让 Agent 修改订单、库存、退款和权益数据。
- 让 Agent 在没有审核记录的情况下处理用户上传内容。

### Agent 原型方案

Dify 或 Coze 可以作为内部原型工具，建议流程：

```text
输入图片 fileKey 和用户参数
→ 调用视觉模型节点
→ 输出结构化 JSON
→ 调用图像编辑节点生成中间图
→ 返回中间图 fileKey、推荐参数和解释
→ 后端继续调用 BeadPatternEngine
```

生产接入要求：

- Agent API 只由后端调用。
- Agent 返回必须经过 JSON Schema 校验。
- Agent 平台的应用 ID、API Key 和工作流 ID 只能存储在服务端配置或密钥管理系统。
- Agent 失败必须降级到直接 Provider 调用或纯算法模式。
- 所有 Agent 调用必须记录 `provider`、`workflowId`、`promptVersion`、`inputAssetId`、`outputAssetId`、`traceId`。

## 提示词模板管理

提示词不得散落在业务代码中，必须使用版本化模板。

建议模板：

| 模板 ID | 用途 |
|---|---|
| `vision_analysis_v1` | 图片理解、主体识别、裁剪建议、参数推荐 |
| `image_prepare_v1` | 清背景、低细节化、边缘增强 |
| `failure_reason_v1` | 生成用户可读失败原因 |
| `admin_review_summary_v1` | 后台审核辅助摘要 |

### 视觉理解提示词方向

```text
你是豆屿 Doyu 的拼豆图纸分析助手。
请分析输入图片是否适合制作拼豆图。
只输出 JSON，不输出 Markdown，不输出解释文本。
需要判断主体、主体类型、主体清晰度、背景复杂度、推荐裁剪区域、推荐难度、推荐格数、推荐颜色数量、风险标记和用户建议。
裁剪区域使用 0 到 1 的相对坐标。
如果图片存在低清晰度、主体过小、复杂背景、版权角色、明显人脸隐私或不适宜内容，请写入 riskFlags。
不要生成最终拼豆格子，不要编造色号，不要编造材料清单。
```

### 图像预处理提示词方向

```text
保留原图主体身份、轮廓和主要颜色。
去除复杂背景或将背景简化为纯色。
将画面处理为适合拼豆制作的平面色块风格。
减少渐变、减少碎细节、强化主体边缘。
不要添加新物体，不要生成文字，不要改变主体姿态。
输出干净、清晰、低细节、边界明确的图像。
```

### 失败原因提示词方向

```text
把技术失败原因转换成用户能理解的简短中文提示。
语气友好但不夸张。
不要暴露 Provider、内部错误码、密钥、模型名、堆栈或审核策略细节。
必须给出下一步建议，例如重新上传更清晰的图片、裁剪主体或降低难度。
```

## 拼豆算法流程

`BeadPatternEngine` 处理步骤：

1. 读取原图或 AI 预处理图。
2. 按用户裁剪或视觉模型推荐裁剪区域截取主体。
3. 统一输出尺寸，例如 32x32、48x48、64x64、80x80、120x120。
4. 修正方向、透明背景、色彩空间和尺寸。
5. 根据难度限制最大颜色数。
6. 执行色彩量化。
7. 映射到真实拼豆色卡。
8. 使用 Lab 色彩空间和 CIEDE2000 色差匹配最近可用色。
9. 替换缺货色或标记不可购买色。
10. 平滑孤立像素和过小色块。
11. 生成格子矩阵。
12. 生成预览图。
13. 生成纯网格图。
14. 生成带色号图。
15. 统计每种颜色豆量。
16. 匹配商城 SKU 和替代 SKU。
17. 输出 PDF 和材料清单 JSON。
18. 写入 `PatternAsset` 和任务结果。

难度默认值：

| 难度 | 格数建议 | 颜色数建议 | 适用用户 |
|---|---:|---:|---|
| `BEGINNER` | 32 到 48 | 8 到 16 | 新手、头像、小图标 |
| `NORMAL` | 48 到 80 | 16 到 32 | 常规宠物、人物半身、手作分享 |
| `ADVANCED` | 80 到 120 | 32 到 48 | 复杂插画、多人图、大尺寸作品 |

## 任务状态流转

```text
PENDING
→ PROCESSING
→ SUCCEEDED
```

失败分支：

```text
PENDING 或 PROCESSING
→ REJECTED：输入或输出审核不通过
→ FAILED：Provider、算法、存储、队列或系统错误
→ CANCELED：用户取消或后台取消
```

失败记录必须包含：

- `failureCode`
- `userMessage`
- `internalMessage`
- `retryable`
- `refundQuota`
- `provider`
- `providerTaskId`
- `traceId`

## 成本控制

第一版成本策略：

- 免费用户每日限制生成次数。
- 高级尺寸和高级风格消耗更多次数。
- 视觉理解和图像预处理分开计费记录。
- 相同图片哈希、参数、色卡和算法版本命中缓存时不重复调用大模型。
- AI 预处理失败时可降级为纯算法，且不扣或返还对应 AI 处理次数。
- Provider 超时后不立即无限重试，使用最多一次短重试和一次降级。
- 后台按日统计 Provider 调用量、成功率、平均耗时、单次成本和用户转化。

缓存键建议：

```text
sha256(image_content)
+ crop
+ style
+ difficulty
+ grid_size
+ color_limit
+ palette_version
+ prompt_version
+ algorithm_version
```

## 降级策略

| 故障 | 降级方式 | 用户感知 |
|---|---|---|
| 主 Provider 超时 | 切换备用 Provider | 任务时间略变长 |
| 所有 Provider 不可用 | 走纯算法模式 | 提示“已使用基础模式生成” |
| 图像预处理失败 | 使用原图进入算法 | 若结果差，提示重新裁剪或换图 |
| 视觉理解 JSON 无效 | 使用默认参数和用户参数 | 不暴露模型错误 |
| 输入审核不通过 | 任务 `REJECTED` | 展示合规提示 |
| 输出审核不通过 | 任务 `REJECTED` 或人工复核 | 展示内容无法生成 |
| 对象存储失败 | 任务 `FAILED`，允许重试 | 展示网络或服务异常 |

## 安全与合规

必须执行：

- 上传图、AI 中间图、最终图纸和用户发布图都要有审核记录。
- 人脸、儿童、证件、隐私场景要进行风险标记。
- 对明显侵权角色、品牌 Logo、影视动漫角色图纸要预留版权投诉和下架机制。
- Provider API Key、Agent Key、工作流 ID、OSS Secret 不得下发客户端。
- 原图和中间图保留周期必须可配置。
- 用户删除生成记录时，前端展示删除结果，后端按数据保留策略处理资产和审计记录。
- 后台查看用户图片需有权限控制和操作日志。
- 用于后续训练或评估的数据必须经过用户授权和脱敏。

## 训练策略

### V1：不训练

第一版使用：

- 固定规则。
- 版本化提示词。
- Provider 抽象。
- 自研拼豆算法。
- 人工样例验收集。

原因：

- 拼豆图纸的核心不是开放式创作，而是强约束的网格、色卡、豆量和材料清单。
- 初期数据量不足，训练成本和合规成本高。
- 直接训练无法保证每次输出都可购买、可复现、可解释。

### V2：PromptOps 和 RAG

当样例足够后，引入：

- 好图纸案例库。
- 失败案例库。
- 风格参数推荐规则库。
- 色卡和材料规则 RAG。
- 不同 Provider 的 A/B 评估。

V2 的目标是提升“参数推荐”和“预处理成功率”，仍不替代最终算法。

### V3：微调或专用模型

只有在满足以下条件后再考虑：

- 已积累足够用户授权样例。
- 有原图、用户参数、AI 中间图、最终图纸、用户编辑、保存、下单、评分等闭环数据。
- 已建立版权、隐私、未成年人和删除机制。
- 已证明 Provider 调用成本或效果成为明显瓶颈。

即使进入 V3，最终格子矩阵仍应由可解释算法校验或生成。

## 接口影响

现有 `/api/v1/patterns/jobs` 接口保持不变，但任务详情建议包含以下字段：

```json
{
  "jobId": "pat_job_001",
  "status": "SUCCEEDED",
  "style": "CUTE",
  "difficulty": "NORMAL",
  "beadSize": "MM_5",
  "gridWidth": 64,
  "gridHeight": 64,
  "colorLimit": 24,
  "provider": "ALIYUN_BAILIAN",
  "providerMode": "VISION_AND_PREPARE",
  "promptVersion": "vision_analysis_v1",
  "algorithmVersion": "bead_engine_v1",
  "paletteVersion": "doyu_palette_v1",
  "analysis": {
    "subject": "一只白色猫咪",
    "beadSuitabilityScore": 82,
    "advice": "建议保留猫咪头部轮廓"
  },
  "assets": [
    {
      "type": "PREVIEW",
      "fileKey": "pattern/preview/xxx.png"
    }
  ],
  "materials": [
    {
      "colorCode": "A01",
      "colorName": "奶油白",
      "quantity": 320,
      "skuId": "sku_001"
    }
  ]
}
```

如果后续采用这些字段，必须同步更新：

- `doc/development/05-api-contract.md`
- `doc/development/06-data-model.md`
- `doc/development/07-ai-pattern-generation.md`

## 后台运营能力

后台应预留：

- Provider 开关。
- Provider 优先级配置。
- 提示词版本查看。
- AI 任务列表。
- 失败任务查询。
- 单任务 traceId 检索。
- 成本统计。
- 生成质量人工评分。
- 样例图纸标记。
- 审核状态处理。
- 用户投诉和版权下架入口。

第一版不要求运营后台完全自动调参，但必须保证任务可查、失败可定位、费用可统计。

## 验收标准

功能验收：

- 上传一张清晰宠物图，可以生成预览图、网格图、色号图、豆量清单和材料清单。
- 上传一张复杂背景图，系统能给出清背景或裁剪建议。
- 上传一张低清晰度图，系统能给出用户可读失败原因或降低质量提示。
- 用户选择 `BEGINNER` 时，颜色和格数明显少于 `ADVANCED`。
- 材料清单中的色号能对应真实色卡和商城 SKU。

技术验收：

- Android 客户端不直接访问 AI Provider、Agent 平台或 OSS Secret。
- 后端记录 Provider、模型或工作流标识、提示词版本、算法版本、色卡版本和 traceId。
- Provider 超时、失败、返回无效 JSON 时任务不会卡死。
- 相同输入和参数可以命中缓存或复现结果。
- 支持主 Provider 到备用 Provider 的降级。

质量验收：

- 主体轮廓可辨认。
- 颜色不过碎。
- 色号图可读。
- 豆量统计准确。
- 新手模式可实际完成。
- 高级模式保留更多细节。
- 输出图纸不能包含大模型生成的无关文字、水印或新增物体。

合规验收：

- 输入图片和输出资产有审核记录。
- 审核拒绝任务状态为 `REJECTED`。
- 后台可以按 traceId 查询处理过程。
- 用户删除生成记录时有明确处理路径。
- 版权投诉和下架流程有后台入口。

## 推荐实施顺序

1. 完成纯算法 `BeadPatternEngine` 原型，支持固定色卡、固定尺寸和基础输出。
2. 接入阿里云百炼视觉理解，输出结构化分析 JSON。
3. 接入阿里云通义万相或等价图像编辑能力，生成清背景低细节中间图。
4. 将视觉分析结果接入参数推荐，但允许用户覆盖参数。
5. 加入 Provider Router 和失败降级。
6. 建立 50 到 100 张人工验收样例集。
7. 加入成本统计、缓存和后台任务追踪。
8. 引入火山方舟或百度千帆作为备用 Provider。
9. 用 Dify 或 Coze 做内部 PromptOps 和工作流实验。
10. 根据真实用户保存率、发帖率、下单率和评分决定是否进入 V2 优化。

