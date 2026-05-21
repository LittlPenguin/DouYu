package cn.edu.app.douyu.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.edu.app.douyu.ui.theme.*

fun disabledClick() = Unit

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
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            } else {
                BeadCluster(size = 28.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (action != null) Row(content = action)
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
        shadowElevation = 1.dp
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
        Text(text, fontWeight = FontWeight.SemiBold)
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
        Text(text)
    }
}

// ── DoyuSearchField ──

@Composable
fun DoyuSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = MaterialTheme.shapes.large,
        color = LightSurfaceVariant.copy(alpha = 0.58f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 6.dp),
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
                IconButton(onClick = { onValueChange("") }) {
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        color = LightSurfaceVariant.copy(alpha = 0.65f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = LightPrimary,
                modifier = Modifier.size(20.dp)
            )
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
    val infiniteTransition = rememberInfiniteTransition(label = "beadCluster")
    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { appeared = true }

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
                    EmptyContent(title, message, modifier, showRetry, onRetry)
                    Text(
                        "traceId: ${parts[1]}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
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
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BeadPattern(
            modifier = Modifier
                .size(108.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(LightSurfaceVariant)
                .padding(16.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
    content: @Composable ColumnScope.() -> Unit
) {
    val horizontalPadding = adaptiveHorizontalPadding()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = horizontalPadding,
            bottom = contentPadding.calculateBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
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
