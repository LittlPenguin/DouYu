package cn.edu.app.douyu.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.Badge
import cn.edu.app.douyu.core.model.CheckinStatus
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.data.safeCallOrNull
import cn.edu.app.douyu.ui.theme.*

private val repo = DoyuAppContainer.profileRepository

@Preview
@Composable
private fun ProfileScreenPreview() { ProfileScreenContent(navController = null) }

@Composable
fun ProfileScreen(navController: NavHostController) { ProfileScreenContent(navController) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(navController: NavHostController?) {
    var dashboardRetryCount by remember { mutableIntStateOf(0) }
    val dashboardState = safeCallToState(dashboardRetryCount) { repo.dashboard() }.value
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(cn.edu.app.douyu.core.navigation.AppRoute.LOGIN)
            },
            message = "登录后可以查看个人中心"
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("我的", style = MaterialTheme.typography.headlineMedium, color = LightPrimary)
                },
                actions = {
                    IconButton(onClick = { navController?.navigate(AppRoute.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置", tint = LightPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            when (val state = dashboardState) {
                is UiState.Success -> {
                    val dashboard = state.data

                    // User header card
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box {
                            // Decorative blob
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .offset(x = 200.dp, y = (-20).dp)
                                    .clip(CircleShape)
                                    .background(LightPrimaryContainer.copy(alpha = 0.2f))
                            )

                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(LightPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = LightPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))

                                // Name
                                Text(
                                    dashboard.user.nickname,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Level badge
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = LightPrimaryContainer
                                ) {
                                    Text(
                                        dashboard.reward.levelCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LightOnPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    dashboard.user.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Stats panel
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("作品", dashboard.user.followerCount, Modifier.weight(1f))
                        StatCard("获赞", dashboard.reward.points, Modifier.weight(1f))
                        StatCard("收藏", dashboard.patternCount, Modifier.weight(1f))
                    }

                    // Function grid
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("我的工坊", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FunctionGridItem("草稿", Icons.Filled.Palette, LightPrimaryContainer, LightPrimary) {}
                                FunctionGridItem("订单", Icons.Filled.ShoppingBag, LightTertiaryContainer, LightTertiary) { navController?.navigate(AppRoute.MY_ORDERS) }
                                FunctionGridItem("收藏", Icons.Filled.Favorite, LightSecondaryContainer, LightSecondary) { navController?.navigate(AppRoute.FAVORITES) }
                                FunctionGridItem("历史", Icons.Filled.History, LightSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant) { navController?.navigate(AppRoute.PATTERN_HISTORY) }
                            }
                        }
                    }

                    // Check-in card
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier.background(
                                Brush.horizontalGradient(
                                    colors = listOf(LightPrimaryContainer.copy(alpha = 0.3f), LightSurface)
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = DoyuCoral, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("创意打卡", style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "连续创作或探索设计即可打卡收集豆子！",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))

                                // Week calendar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val days = listOf("一", "二", "三", "四", "五", "六", "日")
                                    val signedDays = setOf(0, 1, 2) // Mon, Tue, Wed signed
                                    days.forEachIndexed { index, day ->
                                        val signed = index in signedDays
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(if (signed) DoyuPetal else LightSurfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (signed) {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = LightOnPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                day,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (signed) DoyuPetal else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action list
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileAction("我的拼豆", Icons.Filled.GridView, LightPrimaryContainer, LightPrimary) { navController?.navigate(AppRoute.MY_PATTERNS) }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            ProfileAction("生成记录", Icons.Filled.AutoAwesome, LightTertiaryContainer, LightTertiary) { navController?.navigate(AppRoute.PATTERN_HISTORY) }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            ProfileAction("我的订单", Icons.AutoMirrored.Filled.ReceiptLong, LightSecondaryContainer, LightSecondary) { navController?.navigate(AppRoute.MY_ORDERS) }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            ProfileAction("签到与等级", Icons.Filled.WorkspacePremium, LightSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant) { }
                        }
                    }
                }
                UiState.RequireLogin -> {
                    // Guest placeholder — show layout with default values
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LightSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "未登录",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "登录后解锁完整功能",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stats panel — zeroed
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("作品", 0, Modifier.weight(1f))
                        StatCard("获赞", 0, Modifier.weight(1f))
                        StatCard("收藏", 0, Modifier.weight(1f))
                    }

                    // Function grid (disabled)
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("我的工坊", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FunctionGridItem("草稿", Icons.Filled.Palette, LightSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant) { showLoginDialog = true }
                                FunctionGridItem("订单", Icons.Filled.ShoppingBag, LightSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant) { showLoginDialog = true }
                                FunctionGridItem("收藏", Icons.Filled.Favorite, LightSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant) { showLoginDialog = true }
                                FunctionGridItem("历史", Icons.Filled.History, LightSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant) { showLoginDialog = true }
                            }
                        }
                    }
                }
                else -> PageStateView(dashboardState, onRetry = { dashboardRetryCount++ })
            }

            if (!DoyuAppContainer.isLoggedIn) {
                DoyuPrimaryButton(
                    "登录",
                    onClick = { showLoginDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = LightSurfaceVariant,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DoyuAnimatedCounter(
                targetValue = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FunctionGridItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileAction(
    text: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── MyPatternsScreen ──

@Preview
@Composable
private fun MyPatternsScreenPreview() { MyPatternsScreenContent(navController = null) }

@Composable
fun MyPatternsScreen(navController: NavHostController) { MyPatternsScreenContent(navController) }

@Composable
private fun MyPatternsScreenContent(navController: NavHostController?) {
    val patternsState = safeCallToState { repo.patterns() }.value
    Scaffold(topBar = { DoyuTopBar("我的拼豆", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = patternsState) {
                is UiState.Success -> state.data.forEach {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .background(LightSurfaceVariant, MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) { BeadPattern(Modifier.size(42.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(it.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${it.widthCells} x ${it.heightCells} · ${it.totalBeads} 颗",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                else -> PageStateView(patternsState)
            }
        }
    }
}

// ── FavoritesScreen ──

@Preview
@Composable
private fun FavoritesScreenPreview() { FavoritesScreenContent(navController = null) }

@Composable
fun FavoritesScreen(navController: NavHostController) { FavoritesScreenContent(navController) }

@Composable
private fun FavoritesScreenContent(navController: NavHostController?) {
    val favoritesState = safeCallToState { repo.favorites() }.value
    Scaffold(topBar = { DoyuTopBar("收藏图纸", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = favoritesState) {
                is UiState.Success -> state.data.items.forEach {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .background(LightSurfaceVariant, MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) { BeadPattern(Modifier.size(42.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(it.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${it.widthCells} x ${it.heightCells} · ${it.totalBeads} 颗",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                else -> PageStateView(favoritesState)
            }
        }
    }
}

// ── SettingsScreen ──

@Preview
@Composable
private fun SettingsScreenPreview() { SettingsScreenContent(navController = null) }

@Composable
fun SettingsScreen(navController: NavHostController) { SettingsScreenContent(navController) }

@Composable
private fun SettingsScreenContent(navController: NavHostController?) {
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        runCatching { DoyuAppContainer.authSessionManager.logout() }
                        navController?.navigate(AppRoute.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(topBar = { DoyuTopBar("设置", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                SectionHeader("合规入口")
                listOf("隐私政策", "用户协议", "权限说明", "第三方 SDK 清单", "账号注销", "客服与反馈").forEach {
                    Surface(onClick = {}, color = Color.Transparent) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            DoyuCard {
                Text("权限策略", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "相机仅在拍照时申请；相册优先使用 Photo Picker；不默认申请定位、蓝牙、Wi-Fi 或广泛存储权限。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            DoyuOutlinedButton(
                "退出登录",
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
