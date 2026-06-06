package cn.edu.app.douyu.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.edu.app.douyu.ui.theme.*

fun disabledClick() = Unit

private val DoyuPageGap = 16.dp
private val DoyuCardBorder = BorderStroke(1.dp, LightOutlineVariant.copy(alpha = 0.72f))

// ── DoyuTopBar ──

@Composable
fun DoyuTopBar(
    title: String,
    modifier: Modifier = Modifier,
    canGoBack: Boolean = false,
    onBack: () -> Unit = {},
    action: (@Composable RowScope.() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = modifier.statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canGoBack) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Spacer(Modifier.width(4.dp))
                } else {
                    DoyuBrandMark(size = 30.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (action != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = action
                    )
                }
            }
            HorizontalDivider(color = LightOutlineVariant.copy(alpha = 0.72f))
        }
    }
}

@Composable
fun DoyuMainTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAi: () -> Unit,
    onCreatePost: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = modifier.statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "新增",
                            tint = LightPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("设置") },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("AI 创作") },
                            leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenAi()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("上传帖子") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onCreatePost()
                            }
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "搜索",
                        tint = LightPrimary
                    )
                }
            }
            HorizontalDivider(color = LightOutlineVariant.copy(alpha = 0.72f))
        }
    }
}

// ── DoyuCard ──

@Composable
fun DoyuCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = DoyuCardBorder
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

// ── DoyuPrimaryButton ──

@Composable
fun DoyuPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.97f else 1f,
        animationSpec = SpringFast,
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 48.dp)
            .scale(scale),
        shape = MaterialTheme.shapes.medium,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = LightPrimary,
            contentColor = LightOnPrimary
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── DoyuOutlinedButton ──

@Composable
fun DoyuOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.97f else 1f,
        animationSpec = SpringFast,
        label = "buttonScale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 46.dp)
            .scale(scale),
        shape = MaterialTheme.shapes.medium,
        interactionSource = interactionSource,
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled).copy(width = 1.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── DoyuSearchField ──

@Composable
fun DoyuSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LightOutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(LightPrimary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "清除")
                }
            }
        }
    }
}

// ── DoyuSegmentedControl ──

@Composable
fun DoyuSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = LightSurfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEachIndexed { index, label ->
                val selected = selectedIndex == index
                Surface(
                    onClick = { onSelected(index) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selected) 1.dp else 0.dp
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 9.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── DisabledFeatureNotice ──

@Composable
fun DisabledFeatureNotice(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = LightSurfaceVariant.copy(alpha = 0.58f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, LightOutlineVariant.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = LightPrimary
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ── TagChip ──

@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    color: Color = if (selected) LightPrimaryContainer else LightSurfaceVariant,
    contentColor: Color = if (selected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurface
) {
    val animateColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(200),
        label = "tagColor"
    )

    Surface(
        modifier = modifier,
        color = animateColor,
        shape = MaterialTheme.shapes.extraSmall,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            maxLines = 1
        )
    }
}

// ── BeadDot ──

@Composable
fun BeadDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    selected: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = SpringFast,
        label = "beadScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
    )
}

// ── BeadCluster ──

@Composable
fun BeadCluster(size: Dp = 42.dp) {
    Box(modifier = Modifier.size(size)) {
        BeadDot(DoyuPetal, Modifier.align(Alignment.TopStart), size / 2)
        BeadDot(DoyuMint, Modifier.align(Alignment.TopEnd), size / 2)
        BeadDot(DoyuSky, Modifier.align(Alignment.BottomStart), size / 2)
        BeadDot(DoyuCoral, Modifier.align(Alignment.BottomEnd), size / 2)
    }
}

// ── ColorSwatchStrip ──

@Composable
fun ColorSwatchStrip(colors: List<Long>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.forEach {
            BeadDot(Color(it), size = 18.dp)
        }
    }
}

// ── PageStateView ──

@Composable
fun PageStateView(
    state: UiState<*>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = state !is UiState.Success,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val title: String
        val message: String
        val showRetry: Boolean
        when (state) {
            UiState.Loading -> {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LightPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("正在整理拼豆灵感", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                return@AnimatedVisibility
            }
            UiState.Empty -> {
                title = "这里还没有内容"
                message = "先去逛逛社区，或生成一张自己的拼豆图纸。"
                showRetry = false
            }
            is UiState.Error -> {
                val traceMarker = "\ntraceId: "
                if (state.message.contains(traceMarker)) {
                    val parts = state.message.split(traceMarker, limit = 2)
                    title = "加载失败"
                    message = parts[0]
                    showRetry = true
                    EmptyContent(title, message, modifier, showRetry, onRetry, traceId = parts[1])
                    return@AnimatedVisibility
                }
                title = "加载失败"
                message = state.message
                showRetry = true
            }
            UiState.RequireLogin -> {
                title = "需要登录"
                message = "登录后可以收藏图纸、保存生成记录和查看订单。"
                showRetry = false
            }
            UiState.Forbidden -> {
                title = "暂时无法访问"
                message = "当前账号没有这个操作权限。"
                showRetry = false
            }
            UiState.WeakNetwork -> {
                title = "网络有点慢"
                message = "关键任务会保留状态，可以稍后重试。"
                showRetry = true
            }
            is UiState.Success<*> -> return@AnimatedVisibility
        }
        EmptyContent(title, message, modifier, showRetry, onRetry)
    }
}

// ── EmptyContent ──

@Composable
fun EmptyContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    showRetry: Boolean = false,
    onRetry: () -> Unit = {},
    traceId: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DoyuBrandMark(
            modifier = Modifier
                .size(104.dp)
                .clip(MaterialTheme.shapes.extraLarge),
            showContainer = true,
            beadSize = 8.dp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "豆屿",
            style = MaterialTheme.typography.labelLarge,
            color = LightPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (traceId != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "traceId: $traceId",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
        if (showRetry) {
            Spacer(Modifier.height(16.dp))
            DoyuPrimaryButton("重试", onRetry)
        }
    }
}

// ── BeadPattern ──

@Composable
fun BeadPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val colors = listOf(DoyuPetal, DoyuMint, DoyuSky, DoyuCoral)
        val radius = size.minDimension / 10f
        var index = 0
        for (row in 0..3) {
            for (col in 0..3) {
                drawCircle(
                    color = colors[index % colors.size],
                    radius = radius,
                    center = Offset(
                        x = radius * 1.5f + col * radius * 2.25f,
                        y = radius * 1.5f + row * radius * 2.25f
                    )
                )
                index++
            }
        }
    }
}

// ── DoyuBrandMark ──

@Composable
fun DoyuBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    showContainer: Boolean = false,
    beadSize: Dp = 5.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (showContainer) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val canvasSize = this.size
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(
                            LightSecondaryContainer.copy(alpha = 0.9f),
                            LightPrimaryContainer.copy(alpha = 0.72f),
                            LightTertiaryContainer.copy(alpha = 0.82f)
                        )
                    ),
                    cornerRadius = CornerRadius(canvasSize.minDimension * 0.24f, canvasSize.minDimension * 0.24f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.42f),
                    radius = canvasSize.minDimension * 0.26f,
                    center = Offset(canvasSize.width * 0.72f, canvasSize.height * 0.22f)
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showContainer) size * 0.18f else 0.dp)
        ) {
            val w = this.size.width
            val h = this.size.height
            val island = Path().apply {
                moveTo(w * 0.14f, h * 0.66f)
                cubicTo(w * 0.24f, h * 0.42f, w * 0.42f, h * 0.32f, w * 0.55f, h * 0.34f)
                cubicTo(w * 0.72f, h * 0.36f, w * 0.86f, h * 0.5f, w * 0.92f, h * 0.68f)
                cubicTo(w * 0.75f, h * 0.82f, w * 0.32f, h * 0.82f, w * 0.14f, h * 0.66f)
                close()
            }
            drawPath(island, LightSecondary.copy(alpha = 0.95f))
            drawOval(
                color = Color(0xFFE8C48E),
                topLeft = Offset(w * 0.18f, h * 0.58f),
                size = Size(w * 0.68f, h * 0.24f)
            )
            drawRoundRect(
                color = LightPrimary,
                topLeft = Offset(w * 0.22f, h * 0.62f),
                size = Size(w * 0.16f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.018f, w * 0.018f)
            )
            drawRoundRect(
                color = LightTertiary,
                topLeft = Offset(w * 0.42f, h * 0.56f),
                size = Size(w * 0.16f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.018f, w * 0.018f)
            )
            drawRoundRect(
                color = LightSurface,
                topLeft = Offset(w * 0.62f, h * 0.62f),
                size = Size(w * 0.16f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.018f, w * 0.018f)
            )
            drawCircle(
                color = LightPrimary.copy(alpha = 0.88f),
                radius = w * 0.08f,
                center = Offset(w * 0.37f, h * 0.32f)
            )
            drawCircle(
                color = LightSecondary.copy(alpha = 0.9f),
                radius = w * 0.085f,
                center = Offset(w * 0.51f, h * 0.24f)
            )
            drawCircle(
                color = LightTertiary.copy(alpha = 0.9f),
                radius = w * 0.08f,
                center = Offset(w * 0.66f, h * 0.35f)
            )
        }
        BeadDot(
            color = Color.White.copy(alpha = 0.9f),
            size = beadSize,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(size * 0.12f))
        )
    }
}

// ── LoginRequiredDialog ──

@Composable
fun LoginRequiredDialog(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    message: String = "登录后可以使用完整功能"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要登录") },
        text = {
            Column {
                Text(message)
                Spacer(Modifier.height(8.dp))
                Text("登录后可以保存图纸、收藏作品、查看订单和同步生成记录。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onLogin) {
                Text("去登录")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("返回")
            }
        }
    )
}

// ── SectionHeader ──

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (action != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

// ── DoyuPage ──

@Composable
fun DoyuPage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val horizontalPadding = adaptiveHorizontalPadding()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            top = contentPadding.calculateTopPadding() + DoyuPageGap,
            end = horizontalPadding,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(DoyuPageGap)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DoyuPageGap),
                content = content
            )
        }
    }
}

// ── DoyuMetricItem ──

@Composable
fun DoyuMetricItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = LightPrimary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── DoyuReadOnlyEntry ──

@Composable
fun DoyuReadOnlyEntry(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailingText: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = DoyuCardBorder
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = LightPrimaryContainer.copy(alpha = 0.72f),
                contentColor = LightPrimary
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp).size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingText != null) {
                TagChip(text = trailingText, selected = true)
            }
        }
    }
}

// ── DoyuHeroCard (NEW) ──

@Composable
fun DoyuHeroCard(
    title: String,
    subtitle: String,
    ctaText: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    backgroundContent: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Gradient background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                LightPrimaryContainer.copy(alpha = 0.4f),
                                LightSurface,
                                LightSecondaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
            backgroundContent()

            // Content
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (badge != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = LightSurface.copy(alpha = 0.8f)
                    ) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = LightPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DoyuPrimaryButton(ctaText, onCtaClick)
            }
        }
    }
}

// ── DoyuShimmerBox (NEW) ──

@Composable
fun DoyuShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        LightSurfaceVariant,
                        LightSurface,
                        LightSurfaceVariant
                    ),
                    start = Offset(shimmerTranslate, 0f),
                    end = Offset(shimmerTranslate + 300f, 300f)
                )
            )
    )
}

// ── DoyuAnimatedCounter (NEW) ──

@Composable
fun DoyuAnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "counter"
    )
    Text(
        text = animatedValue.toString(),
        style = style,
        color = color,
        modifier = modifier
    )
}
