# 17. UI 重构设计规范

> 文档版本：V1.0
> 创建日期：2026-05-17
> 技术栈：Jetpack Compose + Material3 + MotionLayout Compose

## 1. 设计哲学

### 1.1 核心理念

**Apple 的克制 × Google 的活力 × 拼豆的温度**

- Apple 设计贡献：大留白、清晰层级、精致排版、微动效、克制用色
- Google 设计贡献：Material You 动态色彩、弹性动效、包容性设计、组件化思维
- 豆屿品牌贡献：拼豆颗粒感、手作温暖、年轻活力、社区亲近

### 1.2 视觉关键词

| 维度 | 关键词 |
|---|---|
| 整体气质 | 轻盈、精致、有呼吸感、年轻但不幼稚 |
| 色彩感觉 | 柔和暖调、低饱和度、明暗自然过渡 |
| 空间关系 | 大量留白、卡片化信息、层次分明 |
| 动效节奏 | 弹性自然、有意义的过渡、不炫技 |
| 交互反馈 | 即时、柔和、可预期 |
| 品牌识别 | 拼豆颗粒作为点缀元素，不喧宾夺主 |

### 1.3 禁止方向

- 禁止重色渐变和霓虹色
- 禁止堆满装饰元素和贴纸
- 禁止 emoji 作为功能图标
- 禁止为了好看牺牲可读性和可点击性
- 禁止儿童教育 App 风格
- 禁止商城/支付页面使用过度卡通表达

## 2. 设计令牌系统

### 2.1 颜色系统

#### 品牌色（明暗共享基因）

| 令牌 | 色值 | 用途 |
|---|---|---|
| `DoyuPetal` | `#F48FA4` | 主品牌色、主要强调、CTA |
| `DoyuCoral` | `#FF8F75` | 次要强调、热门/推荐标记 |
| `DoyuMint` | `#A8DCC6` | 成功状态、签到/奖励 |
| `DoyuSky` | `#9EC9F5` | 信息状态、AI 相关 |

#### 浅色主题色板

| 令牌 | 色值 | 用途 |
|---|---|---|
| `primary` | `#E8778E` | 主色（比 Petal 略深，可读性更好） |
| `onPrimary` | `#FFFFFF` | 主色上的文字 |
| `primaryContainer` | `#FFE0E6` | 主色容器（按钮背景、选中态） |
| `onPrimaryContainer` | `#3B0714` | 主色容器上的文字 |
| `secondary` | `#7DB89A` | 次要色（Mint 深化） |
| `onSecondary` | `#FFFFFF` | |
| `secondaryContainer` | `#D4F0E0` | 次要色容器 |
| `onSecondaryContainer` | `#0A2016` | |
| `tertiary` | `#7AADE0` | 第三色（Sky 深化） |
| `onTertiary` | `#FFFFFF` | |
| `tertiaryContainer` | `#D6E8FF` | 第三色容器 |
| `onTertiaryContainer` | `#0A1A2E` | |
| `background` | `#FFFBF7` | 页面背景（暖白） |
| `onBackground` | `#201A1C` | 页面主文字 |
| `surface` | `#FFFFFF` | 卡片/组件表面 |
| `onSurface` | `#201A1C` | 卡片主文字 |
| `surfaceVariant` | `#FFF0F3` | 次要表面（标签背景、分割线区域） |
| `onSurfaceVariant` | `#524347` | 次要文字 |
| `outline` | `#E0CDD0` | 边框、分割线 |
| `outlineVariant` | `#F5DDE1` | 轻量边框 |
| `error` | `#D95F6A` | 错误 |
| `success` | `#5AAE7A` | 成功 |
| `warning` | `#E9934C` | 警告 |

#### 暗色主题色板

| 令牌 | 色值 | 用途 |
|---|---|---|
| `primary` | `#FFB1C1` | 主色（提亮，暗底可读） |
| `onPrimary` | `#5E1126` | |
| `primaryContainer` | `#8B2E42` | 主色容器 |
| `onPrimaryContainer` | `#FFE0E6` | |
| `secondary` | `#A0D4B8` | |
| `onSecondary` | `#0F3526` | |
| `secondaryContainer` | `#2A5A42` | |
| `onSecondaryContainer` | `#D4F0E0` | |
| `tertiary` | `#A8CAEF` | |
| `onTertiary` | `#0F2F4A` | |
| `tertiaryContainer` | `#2A4A68` | |
| `onTertiaryContainer` | `#D6E8FF` | |
| `background` | `#1A1215` | 暖深色背景 |
| `onBackground` | `#F0E0E3` | |
| `surface` | `#241C1F` | 暗色表面 |
| `onSurface` | `#F0E0E3` | |
| `surfaceVariant` | `#322829` | |
| `onSurfaceVariant` | `#D7C1C5` | |
| `outline` | `#9F8C90` | |
| `outlineVariant` | `#524347` | |
| `error` | `#FFB3B3` | |
| `success` | `#8FD4A6` | |
| `warning` | `#FFCF99` | |

### 2.2 形状系统

五级圆角，统一在 `Shapes.kt` 中定义：

| 令牌 | 圆角 | 用途 |
|---|---|---|
| `ShapeXs` | `8.dp` | 小按钮、输入框、小标签 |
| `ShapeSm` | `12.dp` | 中型按钮、列表项内嵌容器 |
| `ShapeMd` | `16.dp` | 标准按钮、对话框内元素 |
| `ShapeLg` | `20.dp` | 卡片、底部弹窗 |
| `ShapeXl` | `28.dp` | 全屏卡片、英雄区块 |

### 2.3 字体系统

引入 **Noto Sans SC**（思源黑体）作为中文字体，搭配系统英文字体。

| 角色 | 字重 | 字号 | 行高 | 字距 | 用途 |
|---|---|---|---|---|---|
| `displayLarge` | Bold | 36sp | 44sp | -0.25sp | 数字大屏（倒计时、统计） |
| `displayMedium` | Bold | 30sp | 38sp | 0sp | 页面大标题 |
| `headlineLarge` | SemiBold | 28sp | 36sp | 0sp | 英雄区域标题 |
| `headlineMedium` | SemiBold | 24sp | 32sp | 0sp | 区块标题 |
| `headlineSmall` | SemiBold | 20sp | 28sp | 0sp | 章节标题 |
| `titleLarge` | SemiBold | 18sp | 26sp | 0sp | 卡片标题 |
| `titleMedium` | Medium | 16sp | 24sp | 0.15sp | 子标题 |
| `bodyLarge` | Normal | 16sp | 24sp | 0.5sp | 正文大 |
| `bodyMedium` | Normal | 14sp | 20sp | 0.25sp | 正文中 |
| `bodySmall` | Normal | 12sp | 16sp | 0.4sp | 辅助说明 |
| `labelLarge` | Medium | 14sp | 20sp | 0.1sp | 大标签、大按钮文字 |
| `labelMedium` | Medium | 12sp | 16sp | 0.5sp | 标签、芯片 |
| `labelSmall` | Medium | 11sp | 16sp | 0.5sp | 角标、极小说明 |

### 2.4 间距系统

基于 4dp 网格：

| 令牌 | 值 | 用途 |
|---|---|---|
| `SpaceXs` | `4.dp` | 紧凑间距（图标与文字） |
| `SpaceSm` | `8.dp` | 元素内部间距 |
| `SpaceMd` | `12.dp` | 列表项间距 |
| `SpaceLg` | `16.dp` | 卡片内边距、区块间距 |
| `SpaceXl` | `20.dp` | 大区块间距 |
| `SpaceXxl` | `24.dp` | 页面分区间距 |
| `Space3xl` | `32.dp` | 英雄区域间距 |
| `Space4xl` | `48.dp` | 页面上下内边距 |

### 2.5 阴影与层级系统

| 层级 | 阴影 | 用途 |
|---|---|---|
| Level 0 | 无阴影 | 页面背景、全屏元素 |
| Level 1 | `elevation = 1.dp`，柔和扩散 | 卡片默认态 |
| Level 2 | `elevation = 3.dp` | 卡片悬浮态、底部导航 |
| Level 3 | `elevation = 6.dp` | 浮动按钮、弹出菜单 |
| Level 4 | `elevation = 12.dp` | 对话框、底部弹窗 |

暗色模式下使用表面色调（surface tint）替代投影表达层级。

## 3. 动效设计系统

### 3.1 动效原则

| 原则 | 说明 |
|---|---|
| 有意义 | 每个动画必须引导用户理解界面变化，不为炫技 |
| 弹性自然 | 使用 Spring 动画模拟物理世界，避免线性机械感 |
| 快速响应 | 交互反馈 < 100ms，过渡动画 200-400ms |
| 统一节奏 | 同类元素使用相同动效参数，保持一致性 |
| 可中断 | 动画支持被打断和反向执行 |

### 3.2 动效参数

```kotlin
// 弹性动画（推荐用于大部分交互）
val SpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,  // 0.5
    stiffness = Spring.StiffnessMedium               // 1500f
)

// 快速弹性（按钮反馈、小元素）
val SpringFast = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,     // 0.75
    stiffness = Spring.StiffnessHigh                  // 10000f
)

// 平滑过渡（颜色、透明度变化）
val SmoothTween = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

// 标准时长
const val DURATION_FAST = 150      // 按钮反馈、标签切换
const val DURATION_NORMAL = 250    // 一般过渡
const val DURATION_SLOW = 400      // 页面级过渡、展开收起
```

### 3.3 页面切换动画

```kotlin
// Tab 切换：MaterialSharedAxis（水平）
enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(300)) { it / 4 } }
exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { -it / 4 } }

// 详情页进入：MaterialSharedAxis（垂直上推）
enterTransition = { fadeIn(tween(300)) + slideInVertically(tween(350)) { it / 3 } }
exitTransition = { fadeOut(tween(250)) + slideOutVertically(tween(300)) { it / 3 } }

// 弹窗/底部面板：从底部滑入
enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(200)) }
exitTransition = { slideOutVertically(tween(300)) { it } + fadeOut(tween(150)) }
```

### 3.4 元素动画

| 动画类型 | 实现 | 参数 |
|---|---|---|
| 列表项交错入场 | `AnimatedVisibility` + `delay(index * 50)` | 淡入 + 从下偏移 20dp |
| 卡片点击缩放 | `animateFloatAsState` | 1.0 → 0.97，SpringFast |
| 数字跳动 | `animateIntAsState` / `animateFloatAsState` | SpringSpec |
| 芯片选中 | `animateColorAsState` + `animateFloatAsState(宽度变化)` | SmoothTween |
| 空状态插画 | `InfiniteTransition` 缓慢上下浮动 | 振幅 6dp，周期 3s |
| 加载骨架屏 | `Brush.linearGradient` 闪烁 | 无限循环，1.2s |
| 进度条脉冲 | `InfiniteTransition.animateFloat` | 0.6 → 1.0 alpha，1s 循环 |
| FAB 展开菜单 | `AnimatedVisibility` + 交错 delay | SpringSpec，每项 delay 50ms |
| 底部导航切换 | 指示器 `animateDpAsState` 滑动 + 图标缩放 | SpringFast |

### 3.5 滚动联动效果

| 效果 | 场景 | 实现 |
|---|---|---|
| 顶部栏折叠 | 社区 Feed、商城 | `CollapsingToolbarLayout` + MotionLayout |
| 图片视差滚动 | 帖子详情、商品详情 | `Modifier.graphicsLayer { translationY = scrollOffset * 0.5f }` |
| 渐隐标题 | 滚动到顶部显示标题 | `Modifier.graphicsLayer { alpha = ... }` |
| FAB 隐藏 | 列表上滑时 FAB 下移退出 | `FabVisibility` 基于 `LazyListState.isScrollingUp()` |
| 底栏隐藏 | 列表上滑时底栏下移 | `NavigationBar` animate exit based on scroll direction |

## 4. 组件库重设计

### 4.1 组件清单

#### 保留并升级的组件

| 组件 | 改动要点 |
|---|---|
| `DoyuTopBar` | 透明背景 + 模糊效果（Haze），滚动时渐变为实色；支持大标题模式 |
| `DoyuCard` | 圆角改为 `ShapeLg`（20dp），增加 Level 1 阴影，pressed 态缩放 0.97 |
| `DoyuPrimaryButton` | 圆角改为 `ShapeMd`（16dp），增加 pressed 弹性缩放 + Ripple 水波纹 |
| `DoyuOutlinedButton` | 同 PrimaryButton，边框色使用 outline |
| `TagChip` | 圆角 `ShapeXs`（8dp），选中态颜色动画 `animateColorAsState` |
| `BeadDot` | 增加 `animateFloatAsState` 选中缩放效果 |
| `BeadCluster` | 增加入场动画：4 个点交错缩放入场 |
| `PageStateView` | 增加状态切换 `AnimatedVisibility` + `Crossfade` |
| `SectionHeader` | 标题字重 SemiBold，支持 subtitle 插槽 |
| `DoyuPage` | 支持 stickyHeader 和 parallaxHeader 模式 |

#### 新增组件

| 组件 | 说明 |
|---|---|
| `DoyuHeroCard` | 英雄大卡片：圆角 28dp、图片占满、底部渐变遮罩、标题叠加 |
| `DoyuGlassCard` | 毛玻璃卡片：`HazeEffect` + 半透明背景，用于浮动元素 |
| `DoyuShimmerBox` | 骨架屏占位：渐变闪烁动画，替代 CircularProgressIndicator |
| `DoyuAnimatedCounter` | 数字跳动组件：`animateIntAsState` + Spring 动画 |
| `DoyuStaggeredList` | 交错入场列表包装器：`AnimatedVisibility` + index-based delay |
| `DoyuCollapsingHeader` | 可折叠头部：MotionLayout 驱动，支持图片缩放 + 标题渐显 |
| `DoyuBottomSheet` | 底部弹窗：ModalBottomSheet 封装，统一圆角和把手样式 |
| `DoyuSnackbar` | 统一样式 Snackbar：圆角、品牌色、支持操作按钮 |
| `DoyuPulsingDot` | 脉冲圆点：用于 AI 生成中的动态提示 |
| `DoyuGradientOverlay` | 渐变遮罩：用于图片底部文字叠加 |

### 4.2 底部导航栏重设计

```
┌──────────────────────────────────────────────────┐
│  ┌──┐   ┌──┐   ┌──┐   ┌──┐   ┌──┐              │
│  │🏠│   │🛒│   │✨│   │💬│   │👤│              │
│  └──┘   └──┘   └──┘   └──┘   └──┘              │
│  社区    商城   AI拼图  消息    我的               │
│          └─ indicator 滑动动画 ─┘                │
└──────────────────────────────────────────────────┘
```

改动要点：
- 选中指示器：底部 3dp 圆角条，`animateDpAsState` 跟随滑动
- 选中图标：缩放 1.15x + 颜色渐变动画
- 未选中图标：`onSurfaceVariant` 色
- AI Tab 特殊处理：使用 `AutoAwesome` 图标，选中时有微光效果
- 未读角标：`DoyuPulsingDot` 红色脉冲圆点
- 背景：`DoyuGlassCard` 毛玻璃效果（Haze）

## 5. 逐页面重构指南

### 5.1 社区 Feed

**改动要点**：
- 双列瀑布流布局（`LazyVerticalStaggeredGrid`，2 列）
- 帖子卡片：圆角 16dp、图片 aspectRatio 原始比例、底部文字区域
- 标签栏改为水平滚动 `LazyRow`，选中标签弹性动画
- 顶部栏：透明 → 滚动后实色 + 标题渐显
- 列表项交错入场动画（fade + slideUp，delay 50ms/项）
- 空状态：拼豆图案 + "还没有帖子，去发一条吧"

**UI 提示词**：
```
设计一个面向年轻女性的拼豆社区首页。
整体风格：Apple 式大留白 + Material You 柔和色彩。
配色：暖白背景 #FFFBF7，主色柔粉 #E8778E，薄荷绿点缀 #A8DCC6。
布局：双列瀑布流，卡片圆角 16dp，轻阴影。
顶部：透明导航栏，左侧 App Logo（拼豆 2x2 罒格），右侧搜索+发布图标。
标签栏：水平滚动胶囊标签，默认"推荐"选中（粉色填充），其他未选中（浅灰底）。
帖子卡片：图片自适应高度 + 底部文字标题 + 作者头像 + 点赞数。
卡片间距 12dp，整体有呼吸感。
滚动时顶部栏渐变为白色实底。
氛围：温暖、有创作灵感、年轻活力。
```

### 5.2 帖子详情

**改动要点**：
- 顶部图片全屏展开，向下滚动时图片缩小 + 顶部栏渐显
- 使用 `DoyuCollapsingHeader` 实现视差滚动
- 互动栏（点赞/评论/收藏）固定底部，毛玻璃背景
- 评论列表交错入场
- 发帖按钮：浮动 FAB，上滑隐藏

**UI 提示词**：
```
设计拼豆帖子详情页。
顶部：大图占满屏幕宽度，向下滚动时图片视差缩小至 40% 高度，白色导航栏渐显。
正文区域：16dp 内边距，标题 20sp SemiBold，正文 16sp，行高 1.5。
色号条：横向色块展示（拼豆色号），圆角 8dp 卡片。
互动数据行：点赞/评论/收藏图标 + 数字，数字用 animateIntAsState 跳动。
底部固定栏：毛玻璃背景（白色 80% 透明度），三个操作按钮均匀分布。
评论区：头像 + 昵称 + 评论文字，每条间距 12dp，入场时交错淡入。
整体氛围：沉浸式阅读体验，内容为主，交互自然。
```

### 5.3 AI 拼图首页

**改动要点**：
- 英雄卡片占满首屏：`DoyuHeroCard`，渐变背景 + 大标题 + CTA 按钮
- 当前任务卡片：进度条脉冲动画 + 状态图标
- 历史图纸网格：`LazyVerticalGrid`，2 列，卡片交错入场
- 底部推荐标签：`LazyRow` 横向滚动

**UI 提示词**：
```
设计 AI 拼豆图纸生成首页。
顶部英雄区：渐变背景（从粉色到薄荷绿，135 度角），大标题"照片变拼豆图纸"30sp Bold 白色，
副标题"上传照片，AI 帮你生成可拼图纸"14sp 白色 70% 透明度，
CTA 按钮"开始创作"白色圆角按钮，带 AutoAwesome 图标。
英雄区圆角 28dp，底部有轻微阴影。
当前任务卡片：如果正在生成，显示脉冲圆点 + 进度条 + "AI 正在努力中..."。
历史记录：2 列网格，每个小卡片显示预览缩略图 + 尺寸 + 状态标签。
"创作灵感"区域：横向滚动标签"头像"、"宠物"、"二次元"、"节日"、"情侣"。
整体氛围：创作感、科技感、温暖。
```

### 5.4 AI 参数选择

**改动要点**：
- 分步式卡片布局（垂直滚动，每步一个卡片）
- 参数选择使用自定义圆形选择器（非 FilterChip）
- 每个参数区域选中时卡片边框变色动画
- 底部固定 CTA 按钮 + 预估时间

**UI 提示词**：
```
设计 AI 拼豆图纸参数选择页。
布局：垂直滚动，分 4 个参数卡片，每个卡片圆角 20dp。
卡片 1"豆子规格"：两个大选项卡（2.6mm / 5mm），选中态粉色填充 + 白色文字，
未选中态浅灰底 + 深色文字，切换时有弹性缩放动画。
卡片 2"成品尺寸"：四个选项横向排列为 2x2 网格，每个选项显示图标+文字+尺寸标注。
卡片 3"难度"：三个难度用递进式选择器（新手/普通/进阶），选中项放大 1.05x。
卡片 4"风格"：横向滚动预览卡片，每个风格显示名称+示例效果图。
底部固定区域：粉色 CTA"开始生成"按钮（48dp 高度），下方显示"预计需要 30 秒"。
整体氛围：选择有趣、操作流畅、每步都有视觉反馈。
```

### 5.5 AI 生成进度

**改动要点**：
- 全屏居中设计，大动画 + 进度信息
- 使用 Lottie 动画展示拼豆生成过程（彩色豆子汇聚）
- 进度条改为环形，内显百分比数字（`DoyuAnimatedCounter`）
- 底部状态文案渐变切换

**UI 提示词**：
```
设计 AI 拼豆图纸生成进度页。
布局：全屏居中，垂直居中对齐。
中央动画：Lottie 动画——彩色拼豆颗粒从四周飞入汇聚成图案，
动画尺寸 200x200dp，下方进度环形条。
环形进度条：4dp 粗细，粉色轨道 + 薄荷绿进度，中间显示百分比数字（36sp Bold，数字跳动动画）。
进度文案：分析图片中 → 生成图纸中 → 即将完成，文案切换时淡入淡出。
底部：任务 ID 小字 + "取消"文字按钮。
如果失败：红色卡片显示失败原因 + "重试"按钮（粉色填充）+ "取消"按钮（文字）。
整体氛围：等待不焦虑，有视觉愉悦感，进度清晰。
```

### 5.6 商城首页

**改动要点**：
- 顶部搜索栏 + 横向分类标签
- 推荐商品大卡片轮播（`HorizontalPager`）
- 全部商品双列网格（`LazyVerticalGrid`）
- 滚动时搜索栏固定 + 分类标签吸顶

**UI 提示词**：
```
设计拼豆材料商城首页。
顶部：搜索栏（圆角 12dp，浅灰背景，左侧搜索图标，右侧扫码图标）。
分类标签：水平滚动，"全部"、"色号豆"、"材料包"、"工具"、"图纸"，
选中粉色下划线指示器 + 文字变色。
推荐区：大卡片轮播（高度 200dp），每张显示商品主图 + 标题 + 价格 + "立即购买"按钮，
圆角 20dp，卡片间间距 16dp，带页面指示点。
商品列表：双列网格，每张商品卡片：方形图片（圆角 12dp）+ 标题（2行截断）
+ 价格（粉色 Bold）+ 销量（灰色小字）。
卡片间距 12dp，轻阴影 Level 1。
滚动时：搜索栏上滑消失，分类标签吸顶（白色实底）。
整体氛围：清晰、可信、购买路径直接。
```

### 5.7 消息页

**改动要点**：
- 顶部标签切换（通知/私信），选中标签滑动动画
- 通知列表：图标 + 标题 + 摘要 + 时间，未读项左侧 3dp 品牌色竖条
- 私信列表：头像 + 昵称 + 最后消息 + 时间 + 未读数角标
- 未登录态：居中登录引导卡片

**UI 提示词**：
```
设计消息页。
顶部标签栏："通知"和"私信"两个标签，选中标签下方粉色指示条（宽度匹配文字，弹性动画）。
通知列表：每条通知 = 左侧圆形图标（系统通知蓝色/订单绿色/互动粉色）
+ 标题（16sp SemiBold）+ 摘要（14sp 灰色，1行截断）+ 右上时间（12sp 灰色）。
未读通知左侧有 3dp 宽品牌色竖条。
私信列表：圆形头像（40dp）+ 昵称 + 最后消息预览 + 时间，
未读数角标（红色圆形，白色数字，最小 16dp）。
列表项间距 1dp 分割线，点击时背景色变化反馈。
空状态：拼豆图案 + "还没有消息" + "去社区看看"按钮。
整体氛围：信息清晰、未读醒目、操作便捷。
```

### 5.8 个人主页

**改动要点**：
- 顶部用户卡片：大头像 + 昵称 + 等级 + 关注/粉丝
- 数据面板：3 列统计（作品数/获赞/收藏），数字使用 `DoyuAnimatedCounter`
- 功能入口列表：图标 + 标题 + 箭头，每项点击有涟漪反馈
- 签到卡片：日历样式，已签到日显示拼豆圆点

**UI 提示词**：
```
设计个人主页。
顶部用户区：居中圆形头像（72dp）+ 昵称（20sp Bold）+ 等级徽章（粉色胶囊标签）
+ "关注 N | 粉丝 N"（14sp 灰色）。
数据面板：三个数据卡片横排，每个卡片圆角 16dp 浅色背景，
数字 28sp Bold（动画跳动）+ 标签 12sp 灰色。
功能列表：每项 = 左侧圆形图标背景（品牌四色循环）+ 功能名 + 右箭头，
项高 56dp，点击涟漪效果。
签到卡片：圆角 20dp，渐变背景（浅粉到白），显示本周 7 天，
已签到日显示彩色拼豆圆点，今日可签到日脉冲动画。
整体氛围：成长感、资产管理、个人归属。
```

## 6. 新增依赖

```kotlin
// gradle/libs.versions.toml
[versions]
motionlayout = "1.1.0"
lottie = "6.6.7"
haze = "1.6.4"
navigation-animation = "2.9.0"

[libraries]
androidx-compose-animation-animation = { module = "androidx.compose.animation:animation" }
androidx-navigation-animation = { module = "androidx.navigation:navigation-animation", version.ref = "navigation-animation" }
androidx-constraintlayout-compose = { module = "androidx.constraintlayout:constraintlayout-compose", version = "1.1.1" }
com-airbnb-android-lottie-compose = { module = "com.airbnb.android:lottie-compose", version.ref = "lottie" }
dev-chrisbanes-haze = { module = "dev.chrisbanes:haze", version.ref = "haze" }
dev-chrisbanes-haze-material3 = { module = "dev.chrisbanes:haze-material3", version.ref = "haze" }
```

## 7. 实施阶段

### 阶段 1：设计令牌 + 主题（1-2 天）

- 重写 `Color.kt`：完整明暗色板
- 新建 `Shape.kt`：五级圆角
- 重写 `Type.kt`：引入 Noto Sans SC + 完整字体层级
- 新建 `Motion.kt`：动效参数常量
- 更新 `Theme.kt`：整合 Shapes + Typography + 动效令牌
- 新增 `gradle/libs.versions.toml` 依赖
- 验证：所有现有页面正常显示，明暗切换正常

### 阶段 2：组件库（2-3 天）

- 重设计 `Components.kt` 中所有现有组件
- 新增 `Animations.kt`：通用动画工具函数
- 新增组件：DoyuHeroCard, DoyuGlassCard, DoyuShimmerBox, DoyuAnimatedCounter 等
- 底部导航栏重设计（毛玻璃 + 滑动指示器 + 未读角标）
- 验证：所有组件 @Preview 正常，功能不退化

### 阶段 3：导航 + 动效（1-2 天）

- 更新 `DoyuApp.kt`：页面切换动画分类（Tab 水平/详情垂直/弹窗底部）
- 更新 Navigation 依赖为 `navigation-animation`
- 实现滚动联动工具（CollapsingHeader, Parallax, FAB auto-hide）
- 验证：所有页面跳转动效正确，无卡顿

### 阶段 4：逐页面重构（5-7 天）

按优先级逐页面应用新设计系统：
1. 社区 Feed → 瀑布流 + 交错入场
2. AI 首页 → HeroCard + 动态任务卡片
3. 商城首页 → 轮播 + 双列网格
4. 个人主页 → 数据面板 + 签到卡片
5. 消息页 → 标签切换 + 列表重设计
6. AI 参数 → 分步卡片 + 自定义选择器
7. AI 进度 → Lottie + 环形进度
8. 详情页 → CollapsingHeader + 视差
9. 其余页面 → 应用新令牌（卡片圆角/颜色/间距）

每完成一个页面验证：功能不变 + @Preview + 375dp 无溢出

### 阶段 5：收尾（1-2 天）

- 暗色模式全面测试 + 手动切换开关
- 骨架屏替换 Loading 状态
- Lottie 动画文件集成（空状态、加载态、成功态）
- 性能优化（LazyList 预取、动画跳帧检测）
- 更新 `11-ui-style-guide.md` 和 `03-android-client.md`

## 8. UI 提示词合集

以下提示词可用于 AI 设计工具（如 Midjourney、DALL-E、Figma AI）生成参考图：

### 8.1 全局风格提示词

```
A mobile app UI design for a Chinese perler bead (拼豆) social commerce platform
targeting young women aged 16-30. Design language blends Apple's clarity and
spaciousness with Google Material You's expressiveness. Color palette: warm cream
background (#FFFBF7), soft pink primary (#E8778E), mint green accent (#A8DCC6),
sky blue (#9EC9F5), coral (#FF8F75). Typography: modern sans-serif Chinese font,
generous whitespace, large rounded corners (20-28dp), soft shadows (1-3dp).
Cards float above the background with subtle elevation. Navigation uses a
frosted glass bottom bar with 5 tabs. Overall mood: warm, creative, youthful,
handmade craft feeling, modern mobile app. Dark mode: deep warm brown background
(#1A1215) with muted pink accents. No gradients, no neon colors, no emoji icons.
```

### 8.2 社区 Feed 提示词

```
A social feed screen in a perler bead crafting app. Two-column masonry/staggered
grid layout showing posts with images of handmade bead art. Each card has rounded
corners (16dp), subtle shadow, image on top with natural aspect ratio, title below,
author avatar + name + like count. Top area: translucent app bar with logo and
search icon. Horizontal scrolling tag chips below the app bar: "推荐", "热门",
"教程", "成品", "二手". Tags have pill shape, selected tag filled with soft pink.
Background: warm cream (#FFFBF7). Generous spacing between cards (12dp).
The overall feel is like a cozy, creative Pinterest-style board for craft lovers.
```

### 8.3 AI 生成页提示词

```
An AI image-to-pattern conversion screen for perler beads. Hero section at top
with gradient background (pink to mint green, 135 degrees), large title in white
"照片变拼豆图纸", CTA button "开始创作" in white with sparkle icon. Below:
current task card showing progress ring animation with percentage. History section:
2-column grid of small pattern preview cards. Bottom section: horizontal scrolling
inspiration tags. The page conveys technology meets creativity, warm and inviting,
not cold or clinical. Frosted glass effect on floating elements.
```

### 8.4 商城提示词

```
A clean e-commerce home screen for perler bead materials. Top: rounded search bar
(12dp corners, light gray background). Category tabs below with animated underline
indicator. Featured products in a horizontal pager/carousel (200dp height, 20dp
rounded cards). Product grid below: 2 columns, each card with square product image
(12dp rounded), title (2 lines max), price in pink bold, sales count in gray.
Cards have Level 1 shadow, 12dp gap between them. The feel is trustworthy,
organized, easy to browse and buy. Like a mix of Apple Store and Xiaohongshu
aesthetics.
```

### 8.5 个人主页提示词

```
A user profile screen for a crafting community app. Top: centered circular avatar
(72dp), nickname in 20sp bold, level badge as pink pill tag, follower/following
count in gray. Data dashboard: 3 stat cards in a row (作品数/获赞/收藏), each
with animated counter number (28sp bold) and label. Below: clean function list
with icon circles (cycling through 4 brand colors), function name, and chevron
right. Sign-in card at top with gradient background (light pink to white), showing
a week calendar with colored bead dots for signed-in days. Overall: personal,
growth-oriented, asset management feeling.
```

---

## 附录：文件影响清单

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `gradle/libs.versions.toml` | 修改 | 新增 Haze/Lottie/MotionLayout 依赖 |
| `ui/theme/Color.kt` | 重写 | 完整明暗色板 |
| `ui/theme/Theme.kt` | 重写 | 整合 Shapes + 动效令牌 |
| `ui/theme/Type.kt` | 重写 | Noto Sans SC + 完整字体层级 |
| `ui/theme/Shape.kt` | 新建 | 五级圆角系统 |
| `ui/theme/Motion.kt` | 新建 | 动效参数常量 |
| `core/ui/Components.kt` | 重设计 | 所有组件升级 + 新增组件 |
| `core/ui/Animations.kt` | 新建 | 通用动画工具 |
| `core/navigation/DoyuApp.kt` | 修改 | 页面切换动画分类 + 底栏重设计 |
| `feature/community/CommunityScreens.kt` | 重构 | 瀑布流 + 交错入场 |
| `feature/ai/AiScreens.kt` | 重构 | HeroCard + Lottie + 环形进度 |
| `feature/commerce/CommerceScreens.kt` | 重构 | 轮播 + 双列网格 |
| `feature/message/MessageScreens.kt` | 重构 | 标签切换 + 列表重设计 |
| `feature/profile/ProfileScreens.kt` | 重构 | 数据面板 + 签到卡片 |
| `feature/auth/LoginScreen.kt` | 重构 | 新设计令牌应用 |
| `doc/development/11-ui-style-guide.md` | 更新 | 同步新设计系统 |
| `doc/development/03-android-client.md` | 更新 | 新增 UI 架构说明 |
