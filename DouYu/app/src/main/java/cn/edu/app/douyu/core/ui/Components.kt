package cn.edu.app.douyu.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.edu.app.douyu.ui.theme.DoyuCoral
import cn.edu.app.douyu.ui.theme.DoyuMint
import cn.edu.app.douyu.ui.theme.DoyuOutline
import cn.edu.app.douyu.ui.theme.DoyuPetal
import cn.edu.app.douyu.ui.theme.DoyuSky
import cn.edu.app.douyu.ui.theme.DoyuSurfaceSoft
import cn.edu.app.douyu.ui.theme.DoyuTextMuted

@Composable
fun DoyuTopBar(
    title: String,
    modifier: Modifier = Modifier,
    canGoBack: Boolean = false,
    onBack: () -> Unit = {},
    action: (@Composable RowScope.() -> Unit)? = null
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            } else {
                BeadCluster(size = 34.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (action != null) Row(content = action)
        }
    }
}

@Composable
fun DoyuCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun DoyuPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DoyuOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(16.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun TagChip(text: String, modifier: Modifier = Modifier, color: Color = DoyuSurfaceSoft) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(100.dp),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            maxLines = 1
        )
    }
}

@Composable
fun BeadDot(color: Color, modifier: Modifier = Modifier, size: Dp = 14.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
    )
}

@Composable
fun BeadCluster(size: Dp = 42.dp) {
    Box(modifier = Modifier.size(size)) {
        BeadDot(DoyuPetal, Modifier.align(Alignment.TopStart), size / 2)
        BeadDot(DoyuMint, Modifier.align(Alignment.TopEnd), size / 2)
        BeadDot(DoyuSky, Modifier.align(Alignment.BottomStart), size / 2)
        BeadDot(DoyuCoral, Modifier.align(Alignment.BottomEnd), size / 2)
    }
}

@Composable
fun ColorSwatchStrip(colors: List<Long>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.forEach {
            BeadDot(Color(it), size = 18.dp)
        }
    }
}

@Composable
fun PageStateView(
    state: UiState<*>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    val title: String
    val message: String
    val showRetry: Boolean
    when (state) {
        UiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("正在整理拼豆灵感")
                }
            }
            return
        }
        UiState.Empty -> {
            title = "这里还没有内容"
            message = "先去逛逛社区，或生成一张自己的拼豆图纸。"
            showRetry = false
        }
        is UiState.Error -> {
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
        UiState.Reviewing -> {
            title = "内容审核中"
            message = "发布后需要通过审核才会公开展示。"
            showRetry = false
        }
        UiState.WeakNetwork -> {
            title = "网络有点慢"
            message = "关键任务会保留状态，可以稍后重试。"
            showRetry = true
        }
        is UiState.Success<*> -> return
    }
    EmptyContent(title, message, modifier, showRetry, onRetry)
}

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
                .clip(RoundedCornerShape(28.dp))
                .background(DoyuSurfaceSoft)
                .padding(16.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, color = DoyuTextMuted, style = MaterialTheme.typography.bodyMedium)
        if (showRetry) {
            Spacer(Modifier.height(16.dp))
            DoyuPrimaryButton("重试", onRetry)
        }
    }
}

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

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun DoyuPage(
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
        }
    }
}
