package cn.edu.app.douyu.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.DashboardData
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.ui.BeadPattern
import cn.edu.app.douyu.core.ui.DisabledFeatureNotice
import cn.edu.app.douyu.core.ui.DoyuAnimatedCounter
import cn.edu.app.douyu.core.ui.DoyuCard
import cn.edu.app.douyu.core.ui.DoyuOutlinedButton
import cn.edu.app.douyu.core.ui.DoyuPage
import cn.edu.app.douyu.core.ui.DoyuPrimaryButton
import cn.edu.app.douyu.core.ui.DoyuTopBar
import cn.edu.app.douyu.core.ui.LoginRequiredDialog
import cn.edu.app.douyu.core.ui.PageStateView
import cn.edu.app.douyu.core.ui.SectionHeader
import cn.edu.app.douyu.core.ui.UiState
import cn.edu.app.douyu.core.ui.disabledClick
import cn.edu.app.douyu.ui.theme.DoyuCoral
import cn.edu.app.douyu.ui.theme.DoyuPetal
import cn.edu.app.douyu.ui.theme.LightOnPrimary
import cn.edu.app.douyu.ui.theme.LightOnPrimaryContainer
import cn.edu.app.douyu.ui.theme.LightPrimary
import cn.edu.app.douyu.ui.theme.LightPrimaryContainer
import cn.edu.app.douyu.ui.theme.LightSecondary
import cn.edu.app.douyu.ui.theme.LightSecondaryContainer
import cn.edu.app.douyu.ui.theme.LightSurface
import cn.edu.app.douyu.ui.theme.LightSurfaceVariant
import cn.edu.app.douyu.ui.theme.LightTertiary
import cn.edu.app.douyu.ui.theme.LightTertiaryContainer
import kotlinx.coroutines.launch

private val repo = DoyuAppContainer.profileRepository

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreenContent(navController = null)
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    ProfileScreenContent(navController)
}

@Composable
private fun ProfileScreenContent(navController: NavHostController?) {
    var dashboardRetryCount by remember { mutableIntStateOf(0) }
    val dashboardState = safeCallToState(dashboardRetryCount) { repo.dashboard() }.value
    var showLoginDialog by remember { mutableStateOf(false) }

    fun openLogin() {
        showLoginDialog = true
    }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(AppRoute.LOGIN)
            },
            message = "登录后可以查看个人资料、创作资产、收藏和订单状态。"
        )
    }

    Scaffold(
        topBar = {
            DoyuTopBar("我的") {
                IconButton(onClick = { navController?.navigate(AppRoute.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "设置", tint = LightPrimary)
                }
            }
        }
    ) { padding ->
        DoyuPage(padding) {
            when (val state = dashboardState) {
                is UiState.Success -> ProfileDashboardContent(
                    dashboard = state.data,
                    navController = navController
                )

                UiState.RequireLogin -> GuestProfileContent(
                    onLogin = ::openLogin,
                    onSettings = { navController?.navigate(AppRoute.SETTINGS) }
                )

                else -> PageStateView(dashboardState, onRetry = { dashboardRetryCount++ })
            }
        }
    }
}

@Composable
private fun ProfileDashboardContent(
    dashboard: DashboardData,
    navController: NavHostController?
) {
    ProfileHeroCard(dashboard)
    ProfileStatsRow(
        firstLabel = "图纸",
        firstValue = dashboard.patternCount,
        secondLabel = "订单",
        secondValue = dashboard.orderCount,
        thirdLabel = "豆子",
        thirdValue = dashboard.reward.points
    )
    WorkshopCard(navController)
    RewardSummaryCard(dashboard)
    ProfileActionGroup(
        title = "创作资产",
        actions = listOf(
            ProfileActionSpec(
                title = "我的拼豆图纸",
                subtitle = "查看已保存的图纸资产",
                icon = Icons.Filled.GridView,
                iconBgColor = LightPrimaryContainer,
                iconColor = LightPrimary,
                status = "${dashboard.patternCount} 张",
                onClick = { navController?.navigate(AppRoute.MY_PATTERNS) }
            ),
            ProfileActionSpec(
                title = "生成记录",
                subtitle = "查看 AI 图纸任务历史，真实视觉 Provider 后期接入",
                icon = Icons.Filled.AutoAwesome,
                iconBgColor = LightTertiaryContainer,
                iconColor = LightTertiary,
                status = "历史",
                onClick = { navController?.navigate(AppRoute.PATTERN_HISTORY) }
            ),
            ProfileActionSpec(
                title = "徽章与等级",
                subtitle = "只读展示奖励摘要，不开放签到领取",
                icon = Icons.Filled.WorkspacePremium,
                iconBgColor = LightSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                status = dashboard.reward.levelCode.ifBlank { "待同步" },
                enabled = false
            )
        )
    )
    ProfileActionGroup(
        title = "交易资产",
        actions = listOf(
            ProfileActionSpec(
                title = "收藏图纸",
                subtitle = "管理收藏的拼豆图纸",
                icon = Icons.Filled.Favorite,
                iconBgColor = LightSecondaryContainer,
                iconColor = LightSecondary,
                status = "可查看",
                onClick = { navController?.navigate(AppRoute.FAVORITES) }
            ),
            ProfileActionSpec(
                title = "订单记录",
                subtitle = "只展示服务端订单和联调支付状态",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconBgColor = LightTertiaryContainer,
                iconColor = LightTertiary,
                status = "${dashboard.orderCount} 单",
                onClick = { navController?.navigate(AppRoute.MY_ORDERS) }
            ),
            ProfileActionSpec(
                title = "卖家与定制服务",
                subtitle = "玩家交易、实名、提现和纠纷处理本轮不开放",
                icon = Icons.Filled.Storefront,
                iconBgColor = LightSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                status = "待开放",
                enabled = false
            )
        )
    )
    ProfileActionGroup(
        title = "设置 / 安全",
        actions = listOf(
            ProfileActionSpec(
                title = "设置与权限",
                subtitle = "查看权限策略和本阶段合规边界",
                icon = Icons.Filled.Settings,
                iconBgColor = LightPrimaryContainer,
                iconColor = LightPrimary,
                status = "进入",
                onClick = { navController?.navigate(AppRoute.SETTINGS) }
            ),
            ProfileActionSpec(
                title = "登录保持",
                subtitle = "TokenStore 已接入 DataStore，应用启动时会尝试恢复登录态",
                icon = Icons.Filled.Lock,
                iconBgColor = LightSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                status = "已接入",
                enabled = false
            )
        )
    )
}

@Composable
private fun GuestProfileContent(
    onLogin: () -> Unit,
    onSettings: () -> Unit
) {
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                    modifier = Modifier.size(34.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "未登录",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "登录后查看个人资料、图纸、收藏和订单状态。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DoyuPrimaryButton(
                    text = "去登录",
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.AccountCircle
                )
            }
        }
    }
    ProfileStatsRow("图纸", 0, "订单", 0, "豆子", 0)
    DisabledFeatureNotice(
        title = "登录态可在重启后恢复",
        message = "登录成功后令牌会保存到本机 DataStore；退出登录或服务端判定过期时会清理本机令牌。"
    )
    ProfileActionGroup(
        title = "个人资料",
        actions = listOf(
            ProfileActionSpec(
                title = "登录查看资产",
                subtitle = "个人图纸、收藏、订单和等级需要登录后加载",
                icon = Icons.Filled.Person,
                iconBgColor = LightPrimaryContainer,
                iconColor = LightPrimary,
                status = "去登录",
                onClick = onLogin
            ),
            ProfileActionSpec(
                title = "设置与权限",
                subtitle = "未登录也可以查看本阶段权限和合规边界",
                icon = Icons.Filled.Settings,
                iconBgColor = LightSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                status = "进入",
                onClick = onSettings
            )
        )
    )
}

@Composable
private fun ProfileHeroCard(dashboard: DashboardData) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        LightPrimaryContainer.copy(alpha = 0.38f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(LightPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = LightPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    LevelPill(dashboard.reward.levelCode.ifBlank { "LV" })
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        dashboard.user.nickname,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        dashboard.user.bio.ifBlank { "还没有填写个人简介" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SoftTag("创作者", LightPrimaryContainer, LightOnPrimaryContainer)
                        SoftTag(
                            if (dashboard.badges.any { it.achieved }) "徽章已点亮" else "徽章待点亮",
                            LightSecondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelPill(text: String) {
    Surface(
        shape = CircleShape,
        color = DoyuPetal,
        contentColor = LightOnPrimary,
        shadowElevation = 1.dp
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SoftTag(
    text: String,
    color: Color,
    contentColor: Color
) {
    Surface(
        shape = CircleShape,
        color = color,
        contentColor = contentColor
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProfileStatsRow(
    firstLabel: String,
    firstValue: Int,
    secondLabel: String,
    secondValue: Int,
    thirdLabel: String,
    thirdValue: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(firstLabel, firstValue, Modifier.weight(1f))
        StatCard(secondLabel, secondValue, Modifier.weight(1f))
        StatCard(thirdLabel, thirdValue, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DoyuAnimatedCounter(
                targetValue = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkshopCard(navController: NavHostController?) {
    DoyuCard(contentPadding = PaddingValues(20.dp)) {
        SectionHeader(
            title = "我的工坊",
            subtitle = "常用资产入口"
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FunctionGridItem(
                label = "图纸",
                icon = Icons.Filled.Palette,
                bgColor = LightPrimaryContainer,
                iconColor = LightPrimary,
                onClick = { navController?.navigate(AppRoute.MY_PATTERNS) }
            )
            FunctionGridItem(
                label = "订单",
                icon = Icons.Filled.ShoppingBag,
                bgColor = LightTertiaryContainer,
                iconColor = LightTertiary,
                onClick = { navController?.navigate(AppRoute.MY_ORDERS) }
            )
            FunctionGridItem(
                label = "收藏",
                icon = Icons.Filled.Favorite,
                bgColor = LightSecondaryContainer,
                iconColor = LightSecondary,
                onClick = { navController?.navigate(AppRoute.FAVORITES) }
            )
            FunctionGridItem(
                label = "历史",
                icon = Icons.Filled.History,
                bgColor = LightSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { navController?.navigate(AppRoute.PATTERN_HISTORY) }
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
    enabled: Boolean = true,
    onClick: () -> Unit = ::disabledClick
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun RewardSummaryCard(dashboard: DashboardData) {
    val achievedBadges = dashboard.badges.count { it.achieved }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        LightPrimaryContainer.copy(alpha = 0.28f),
                        LightSurface
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = DoyuCoral,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "等级与奖励",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    SoftTag(
                        dashboard.reward.levelCode.ifBlank { "等级待同步" },
                        LightPrimaryContainer,
                        LightOnPrimaryContainer
                    )
                }
                Text(
                    "这里只展示后端奖励摘要；签到领取、实名奖励和交易激励不在本轮开放。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RewardMetric("豆子", dashboard.reward.points, Modifier.weight(1f))
                    RewardMetric("经验", dashboard.reward.experience, Modifier.weight(1f))
                    RewardMetric("徽章", achievedBadges, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RewardMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = LightSurface.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class ProfileActionSpec(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color,
    val status: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit = ::disabledClick
)

@Composable
private fun ProfileActionGroup(
    title: String,
    actions: List<ProfileActionSpec>
) {
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        actions.forEachIndexed { index, action ->
            ProfileAction(action)
            if (index < actions.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun ProfileAction(action: ProfileActionSpec) {
    Surface(
        onClick = action.onClick,
        enabled = action.enabled,
        color = Color.Transparent
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(action.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    action.icon,
                    contentDescription = null,
                    tint = action.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    action.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (action.enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    action.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                action.status,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (action.enabled) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── MyPatternsScreen ──

@Preview
@Composable
private fun MyPatternsScreenPreview() {
    MyPatternsScreenContent(navController = null)
}

@Composable
fun MyPatternsScreen(navController: NavHostController) {
    MyPatternsScreenContent(navController)
}

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
private fun FavoritesScreenPreview() {
    FavoritesScreenContent(navController = null)
}

@Composable
fun FavoritesScreen(navController: NavHostController) {
    FavoritesScreenContent(navController)
}

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
private fun SettingsScreenPreview() {
    SettingsScreenContent(navController = null)
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    SettingsScreenContent(navController)
}

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
                        navController?.navigate(BottomTab.PROFILE.route) {
                            popUpTo(BottomTab.PROFILE.route) { inclusive = true }
                            launchSingleTop = true
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
            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "账号与安全",
                    subtitle = "只保留当前阶段真实可用的账号操作"
                )
                Spacer(Modifier.height(10.dp))
                DisabledFeatureNotice(
                    title = "登录保持已接入",
                    message = "登录、刷新、退出登录和 401 过期清理共用同一个 TokenStore；Preview 和无 Context 测试场景仍回退内存实现。"
                )
                Spacer(Modifier.height(12.dp))
                SettingsStatusRow(
                    title = "短信登录",
                    description = "开发环境验证码固定为 123456",
                    status = "可用",
                    icon = Icons.Filled.Lock
                )
                SettingsStatusRow(
                    title = "账号注销",
                    description = "生产闭环、人工审核和冷静期流程待补齐",
                    status = "待补齐",
                    icon = Icons.Filled.Security
                )
            }
            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "隐私与合规入口",
                    subtitle = "只展示清单，不表达为生产材料已完成"
                )
                Spacer(Modifier.height(8.dp))
                listOf(
                    "隐私政策" to "文本、版本记录和弹窗确认待补齐",
                    "用户协议" to "正式协议和版本变更流程待补齐",
                    "权限说明" to "按实际 SDK 和权限清单后续补齐",
                    "第三方 SDK 清单" to "真实 SDK 接入后再维护清单",
                    "客服与反馈" to "生产客服渠道待补齐"
                ).forEachIndexed { index, item ->
                    SettingsStatusRow(
                        title = item.first,
                        description = item.second,
                        status = "待补齐",
                        icon = Icons.Filled.Info
                    )
                    if (index < 4) {
                        HorizontalDivider()
                    }
                }
            }
            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "权限策略",
                    subtitle = "当前 Android 权限申请原则"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "相机仅在拍照时申请；相册优先使用 Photo Picker；不默认申请定位、蓝牙、Wi-Fi 或广泛存储权限。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (DoyuAppContainer.isLoggedIn) {
                DoyuOutlinedButton(
                    "退出登录",
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Filled.Logout
                )
            } else {
                DoyuPrimaryButton(
                    "去登录",
                    onClick = { navController?.navigate(AppRoute.LOGIN) },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Filled.AccountCircle
                )
            }
        }
    }
}

@Composable
private fun SettingsStatusRow(
    title: String,
    description: String,
    status: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LightSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            status,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
