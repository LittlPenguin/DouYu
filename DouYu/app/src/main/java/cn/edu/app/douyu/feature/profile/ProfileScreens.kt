package cn.edu.app.douyu.feature.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.DashboardData
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.UploadConfirmRequest
import cn.edu.app.douyu.core.model.UploadPresignRequest
import cn.edu.app.douyu.core.model.UploadUsage
import cn.edu.app.douyu.core.model.UpdateProfileRequest
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.core.network.requireSuccess
import cn.edu.app.douyu.core.network.upload
import cn.edu.app.douyu.core.ui.BeadPattern
import cn.edu.app.douyu.core.ui.DisabledFeatureNotice
import cn.edu.app.douyu.core.ui.DoyuAnimatedCounter
import cn.edu.app.douyu.core.ui.DoyuCard
import cn.edu.app.douyu.core.ui.DoyuMainTopBar
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val repo = DoyuAppContainer.profileRepository
private val communityRepo = DoyuAppContainer.communityRepository

fun profileAssetTabLabels(): List<String> = listOf("我的图纸", "点赞作品", "收藏作品")

fun profileAssetPreviewBadges(): List<String> = listOf("图纸", "帖子", "收藏")

fun profileEditPrimaryFields(): List<String> =
    listOf("头像", "昵称", "个人简介", "年龄段", "城市 / 地区", "兴趣标签")

fun profileEditAvatarStateLabels(): List<String> =
    listOf("更换头像", "头像上传中", "头像上传失败", "保留旧头像")

fun settingsHomeSectionLabels(): List<String> =
    listOf("账号与安全", "隐私与权限", "通知设置", "帮助、关于与合规")

fun profilePrimaryAssetBoundaryMessage(): String =
    "更多个人资产入口不再作为我的页首屏展示；已有历史路由保留，不代表订单、玩家交易或互动资产闭环已删除。"

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
            DoyuMainTopBar(
                title = "我的",
                onSearch = { navController?.navigate(AppRoute.SEARCH) },
                onOpenSettings = { navController?.navigate(AppRoute.SETTINGS) },
                onOpenAi = { navController?.navigate(BottomTab.AI.route) },
                onCreatePost = { navController?.navigate(AppRoute.POST_CREATE) }
            )
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
    ProfileHeroCard(
        dashboard = dashboard,
        onEditProfile = { navController?.navigate(AppRoute.PROFILE_EDIT) }
    )
    ProfileStatsRow(dashboard)
    ProfilePrimaryAssetTabs(
        dashboard = dashboard,
        navController = navController
    )
    DisabledFeatureNotice(
        title = "更多资产入口已收敛",
        message = profilePrimaryAssetBoundaryMessage()
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
    ProfileStatsRow()
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
private fun ProfileHeroCard(
    dashboard: DashboardData,
    onEditProfile: () -> Unit
) {
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
                    DoyuOutlinedButton(
                        text = "编辑资料",
                        onClick = onEditProfile,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Edit
                    )
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
private fun ProfileStatsRow(dashboard: DashboardData? = null) {
    val likedCount = dashboard?.reward?.points ?: 0
    val worksCount = dashboard?.patternCount ?: 0
    val followingCount = dashboard?.user?.followingCount ?: 0
    val followerCount = dashboard?.user?.followerCount ?: 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard("获赞", likedCount, Modifier.weight(1f))
        StatCard("作品", worksCount, Modifier.weight(1f))
        StatCard("关注", followingCount, Modifier.weight(1f))
        StatCard("粉丝", followerCount, Modifier.weight(1f))
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
private fun ProfilePrimaryAssetTabs(
    dashboard: DashboardData,
    navController: NavHostController?
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val labels = profileAssetTabLabels()
    val previews = profileAssetPreviewSpecs(dashboard)

    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        SectionHeader(
            title = "作品资产",
            subtitle = "我的图纸、点赞作品、收藏作品放在同一个 Tab 区域"
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEachIndexed { index, label ->
                val selected = selectedTab == index
                Surface(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    color = if (selected) LightPrimaryContainer else LightSurfaceVariant,
                    contentColor = if (selected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        ProfileAssetPreviewFlow(
            previews = previews[selectedTab],
            navController = navController
        )
    }
}

private data class ProfileAssetPreviewSpec(
    val badge: String,
    val title: String,
    val meta: String,
    val route: String,
    val color: Color,
    val icon: ImageVector,
    val height: Dp
)

private fun profileAssetPreviewSpecs(dashboard: DashboardData): List<List<ProfileAssetPreviewSpec>> {
    val badges = profileAssetPreviewBadges()
    return listOf(
        listOf(
            ProfileAssetPreviewSpec(
                badge = badges[0],
                title = "薄荷小岛挂件",
                meta = "${dashboard.patternCount.coerceAtLeast(1)} 张图纸 · 48 色",
                route = AppRoute.MY_PATTERNS,
                color = LightPrimaryContainer,
                icon = Icons.Filled.GridView,
                height = 154.dp
            ),
            ProfileAssetPreviewSpec(
                badge = badges[0],
                title = "海盐钥匙扣",
                meta = "28x28 · 初学",
                route = AppRoute.MY_PATTERNS,
                color = LightTertiaryContainer,
                icon = Icons.Filled.Palette,
                height = 112.dp
            ),
            ProfileAssetPreviewSpec(
                badge = badges[0],
                title = "草莓小熊图纸",
                meta = "52 色 · 进阶",
                route = AppRoute.MY_PATTERNS,
                color = LightSecondaryContainer,
                icon = Icons.Filled.AutoAwesome,
                height = 122.dp
            )
        ),
        listOf(
            ProfileAssetPreviewSpec(
                badge = badges[1],
                title = "点赞的海盐配色",
                meta = "社区作品 · 88",
                route = AppRoute.LIKED_POSTS,
                color = LightTertiaryContainer,
                icon = Icons.Filled.Favorite,
                height = 132.dp
            ),
            ProfileAssetPreviewSpec(
                badge = badges[1],
                title = "像素小岛教程",
                meta = "阿澄 · 新手友好",
                route = AppRoute.LIKED_POSTS,
                color = LightPrimaryContainer,
                icon = Icons.Filled.Favorite,
                height = 112.dp
            ),
            ProfileAssetPreviewSpec(
                badge = badges[1],
                title = "暖白底板搭配",
                meta = "工具心得 · 42",
                route = AppRoute.LIKED_POSTS,
                color = LightSecondaryContainer,
                icon = Icons.Filled.Favorite,
                height = 144.dp
            )
        ),
        listOf(
            ProfileAssetPreviewSpec(
                badge = badges[2],
                title = "草莓小熊教程",
                meta = "收藏作品 · 126",
                route = AppRoute.FAVORITE_POSTS,
                color = LightSecondaryContainer,
                icon = Icons.Filled.Bookmark,
                height = 148.dp
            ),
            ProfileAssetPreviewSpec(
                badge = badges[2],
                title = "薄荷花边图纸",
                meta = "图纸收藏 · 36 色",
                route = AppRoute.FAVORITE_POSTS,
                color = LightPrimaryContainer,
                icon = Icons.Filled.Bookmark,
                height = 116.dp
            ),
            ProfileAssetPreviewSpec(
                badge = badges[2],
                title = "玩家配色灵感",
                meta = "帖子收藏 · 19",
                route = AppRoute.FAVORITE_POSTS,
                color = LightTertiaryContainer,
                icon = Icons.Filled.Bookmark,
                height = 126.dp
            )
        )
    )
}

@Composable
private fun ProfileAssetPreviewFlow(
    previews: List<ProfileAssetPreviewSpec>,
    navController: NavHostController?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileAssetPreviewCard(
            spec = previews[0],
            modifier = Modifier
                .weight(1f)
                .height(previews[0].height),
            navController = navController
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            previews.drop(1).forEach { preview ->
                ProfileAssetPreviewCard(
                    spec = preview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(preview.height),
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun ProfileAssetPreviewCard(
    spec: ProfileAssetPreviewSpec,
    modifier: Modifier,
    navController: NavHostController?
) {
    Surface(
        onClick = { navController?.navigate(spec.route) },
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = spec.color.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    spec.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        spec.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    spec.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    spec.meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
                .heightIn(min = 72.dp)
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

// ── Interaction post asset screens ──

@Composable
fun LikedPostsScreen(navController: NavHostController) {
    ProfilePostAssetScreen(
        title = "点赞作品",
        emptyMessage = "还没有点赞过作品。去社区遇到喜欢的拼豆作品时点个赞，它会沉淀在这里。",
        navController = navController,
        loader = { communityRepo.likedPosts() }
    )
}

@Composable
fun CommentedPostsScreen(navController: NavHostController) {
    ProfilePostAssetScreen(
        title = "评论作品",
        emptyMessage = "还没有评论过作品。参与过讨论的作品会显示在这里，方便回看互动。",
        navController = navController,
        loader = { communityRepo.commentedPosts() }
    )
}

@Composable
fun FavoritePostsScreen(navController: NavHostController) {
    ProfilePostAssetScreen(
        title = "收藏作品",
        emptyMessage = "还没有收藏作品。收藏社区作品后，可以从这里继续查看详情。",
        navController = navController,
        loader = { communityRepo.favoritePosts() }
    )
}

@Composable
fun FollowedPostsScreen(navController: NavHostController) {
    ProfilePostAssetScreen(
        title = "关注作品",
        emptyMessage = "关注作者发布的可见作品会显示在这里；这里不是关注关系列表。",
        navController = navController,
        loader = { communityRepo.followedPosts() }
    )
}

@Composable
private fun ProfilePostAssetScreen(
    title: String,
    emptyMessage: String,
    navController: NavHostController?,
    loader: () -> PageResponse<Post>
) {
    var retryCount by remember(title) { mutableIntStateOf(0) }
    val postsState = safeCallToState(retryCount, title) { loader() }.value

    Scaffold(
        topBar = {
            DoyuTopBar(
                title = title,
                canGoBack = true,
                onBack = { navController?.popBackStack() }
            )
        }
    ) { padding ->
        DoyuPage(padding) {
            when (val state = postsState) {
                is UiState.Success -> {
                    if (state.data.items.isEmpty()) {
                        ProfilePostAssetEmpty(message = emptyMessage)
                    } else {
                        state.data.items.forEach { post ->
                            ProfilePostCard(
                                post = post,
                                onClick = { navController?.navigate(AppRoute.postDetail(post.postId)) }
                            )
                        }
                    }
                }

                else -> PageStateView(postsState, onRetry = { retryCount++ })
            }
        }
    }
}

@Composable
private fun ProfilePostAssetEmpty(message: String) {
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                "这里还没有内容",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfilePostCard(
    post: Post,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfilePostCover(
                post = post,
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    post.title.ifBlank { "未命名作品" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    post.content.ifBlank { "暂无作品说明" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(LightPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            post.author.nickname.firstOrNull()?.toString().orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = LightPrimary,
                            maxLines = 1
                        )
                    }
                    Text(
                        post.author.nickname.ifBlank { "豆屿用户" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePostMetric(Icons.Filled.Favorite, post.likeCount.toString())
                    ProfilePostMetric(Icons.Filled.ChatBubble, post.commentCount.toString())
                    ProfilePostMetric(Icons.Filled.Bookmark, post.favoriteCount.toString())
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ProfilePostCover(
    post: Post,
    modifier: Modifier = Modifier
) {
    val fallbackContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            BeadPattern(Modifier.size(44.dp))
        }
    }

    Box(
        modifier = modifier.background(LightSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val coverUrl = post.coverImageUrl
        if (coverUrl.isNullOrBlank()) {
            fallbackContent()
        } else {
            SubcomposeAsyncImage(
                model = coverUrl,
                contentDescription = post.title.ifBlank { "作品图片" },
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { fallbackContent() },
                error = { fallbackContent() }
            )
        }
    }
}

@Composable
private fun ProfilePostMetric(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    title = "账号与应用设置",
                    subtitle = "只展示可解释入口，未完成能力明确标注"
                )
                Spacer(Modifier.height(10.dp))
                settingsHomeEntries().forEachIndexed { index, entry ->
                    SettingsStatusRow(
                        title = entry.title,
                        description = entry.description,
                        status = entry.status,
                        icon = entry.icon,
                        onClick = { navController?.navigate(AppRoute.settingsSection(entry.section)) }
                    )
                    if (index < settingsHomeEntries().lastIndex) {
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
            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "危险操作",
                    subtitle = "退出登录和账号注销必须二次确认，注销前说明数据影响"
                )
                Spacer(Modifier.height(10.dp))
                SettingsStatusRow(
                    title = "账号注销",
                    description = "生产冷静期、人工审核和数据影响说明待补齐。",
                    status = "待补",
                    icon = Icons.Filled.Security,
                    onClick = { navController?.navigate(AppRoute.settingsSection("account-security")) }
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

private data class SettingsHomeEntry(
    val title: String,
    val description: String,
    val status: String,
    val icon: ImageVector,
    val section: String
)

private fun settingsHomeEntries(): List<SettingsHomeEntry> {
    val labels = settingsHomeSectionLabels()
    return listOf(
        SettingsHomeEntry(labels[0], "手机号、登录设备、退出登录、注销账号", "可用", Icons.Filled.Lock, "account-security"),
        SettingsHomeEntry(labels[1], "相机、相册、通知、位置权限", "待补", Icons.Filled.Settings, "privacy-permissions"),
        SettingsHomeEntry(labels[2], "私信、互动、系统通知", "UI-only", Icons.Filled.Notifications, "notifications"),
        SettingsHomeEntry(labels[3], "协议、隐私政策、SDK 清单、备案待补", "待补", Icons.Filled.Info, "about-compliance")
    )
}

@Composable
private fun SettingsStatusRow(
    title: String,
    description: String,
    status: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(vertical = 12.dp)
    Row(
        modifier = rowModifier,
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
        if (onClick != null) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SettingsSectionScreen(navController: NavHostController, section: String) {
    val config = settingsSectionConfig(section)
    Scaffold(
        topBar = {
            DoyuTopBar(config.title, canGoBack = true, onBack = { navController.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = config.title,
                    subtitle = config.subtitle
                )
                Spacer(Modifier.height(12.dp))
                config.items.forEachIndexed { index, item ->
                    SettingsStatusRow(
                        title = item.title,
                        description = item.description,
                        status = item.status,
                        icon = item.icon
                    )
                    if (index < config.items.lastIndex) HorizontalDivider()
                }
            }
            DisabledFeatureNotice(
                title = "开发态边界",
                message = config.boundary
            )
        }
    }
}

private data class SettingsSectionConfig(
    val title: String,
    val subtitle: String,
    val boundary: String,
    val items: List<SettingsSectionItem>
)

private data class SettingsSectionItem(
    val title: String,
    val description: String,
    val status: String,
    val icon: ImageVector
)

private fun settingsSectionConfig(section: String): SettingsSectionConfig = when (section) {
    "account-security" -> SettingsSectionConfig(
        title = "账号与安全",
        subtitle = "登录、退出、注销申请和账号保护状态",
        boundary = "本页不新增实名、风控或账号审批接口；账号注销仍需生产流程、冷静期和人工审核方案。",
        items = listOf(
            SettingsSectionItem("短信登录", "开发环境验证码固定为 123456；登录请求仍传 ageGroup=AGE_18_PLUS。", "可用", Icons.Filled.Lock),
            SettingsSectionItem("登录态持久化", "TokenStore 使用 DataStore，401 或刷新失败会清理本地会话。", "可用", Icons.Filled.Security),
            SettingsSectionItem("账号注销", "后端已有申请骨架，生产闭环与人工审核仍待补齐。", "待补", Icons.Filled.AccountCircle)
        )
    )
    "privacy-permissions" -> SettingsSectionConfig(
        title = "隐私与权限",
        subtitle = "相机、相册、通知、位置权限的开发态说明",
        boundary = "权限说明必须以后续真实权限清单为准；本页不请求地图定位，不新增生产合规材料。",
        items = listOf(
            SettingsSectionItem("相机权限", "仅拍照时申请，用于 AI 输入或未来头像上传。", "按需", Icons.Filled.PhotoCamera),
            SettingsSectionItem("相册权限", "优先使用 Photo Picker，不默认申请广泛存储权限。", "按需", Icons.Filled.PhotoLibrary),
            SettingsSectionItem("通知权限", "通知偏好后端接口未完成，当前仅展示 UI 状态。", "UI-only", Icons.Filled.Notifications),
            SettingsSectionItem("位置权限", "城市/地区只能手动填写，不接地图或定位 API。", "不接入", Icons.Filled.Map)
        )
    )
    "privacy-compliance", "about-compliance" -> SettingsSectionConfig(
        title = "帮助、关于与合规",
        subtitle = "生产前必须补齐的公开材料清单",
        boundary = "隐私政策、用户协议、备案、SDK 清单、版权投诉和客服渠道目前只能标为待补，不能写成已经完成。",
        items = listOf(
            SettingsSectionItem("隐私政策", "正式文本、版本记录和首次弹窗确认待补齐。", "待补", Icons.Filled.Info),
            SettingsSectionItem("用户协议", "正式协议、版本变更和撤回机制待补齐。", "待补", Icons.Filled.Description),
            SettingsSectionItem("SDK 清单", "真实 SDK 接入后再按生产版本维护。", "待补", Icons.Filled.Security),
            SettingsSectionItem("版权投诉", "投诉入口、工单流和人工处置 SLA 待补齐。", "待补", Icons.Filled.WorkspacePremium),
            SettingsSectionItem("客服与反馈", "生产客服渠道、工单和反馈 SLA 待补齐。", "待补", Icons.Filled.Info)
        )
    )
    "notifications" -> SettingsSectionConfig(
        title = "通知设置",
        subtitle = "当前只做本地 UI 状态展示",
        boundary = "当前没有通知偏好后端接口，不保存服务端开关；后续需要先补 API 契约。",
        items = listOf(
            SettingsSectionItem("互动通知", "点赞、收藏、评论、关注提醒。", "UI-only", Icons.Filled.Favorite),
            SettingsSectionItem("订单通知", "订单和联调支付状态提醒。", "UI-only", Icons.Filled.ShoppingBag),
            SettingsSectionItem("系统通知", "审核、风控、合规和系统公告。", "UI-only", Icons.Filled.Info)
        )
    )
    else -> SettingsSectionConfig(
        title = "设置分区",
        subtitle = "当前分区还没有独立实现",
        boundary = "这是开发态占位页，不代表已有生产能力或新增接口。",
        items = listOf(
            SettingsSectionItem("当前状态", "该设置分区只作为后续 UI 承载入口。", "开发态", Icons.Filled.Settings)
        )
    )
}

@Preview
@Composable
private fun ProfileEditScreenPreview() {
    ProfileEditScreenContent(navController = null)
}

@Composable
fun ProfileEditScreen(navController: NavHostController) {
    ProfileEditScreenContent(navController)
}

@Composable
private fun ProfileEditAvatarCard(
    currentAvatarUrl: String?,
    selectedAvatarUri: Uri?,
    uploading: Boolean,
    failed: Boolean,
    onPickAvatar: () -> Unit,
    onCameraAvatar: () -> Unit,
    onClearAvatar: () -> Unit
) {
    val labels = profileEditAvatarStateLabels()
    DoyuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(LightPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val avatarModel = selectedAvatarUri ?: currentAvatarUrl
                if (avatarModel == null || avatarModel.toString().isBlank()) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = LightPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = avatarModel,
                        contentDescription = "头像预览",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = LightPrimary)
                            }
                        }
                    )
                }
                if (uploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("头像", style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        uploading -> labels[1]
                        failed -> "${labels[2]}，${labels[3]}。"
                        selectedAvatarUri != null -> "已选择新头像，保存时上传。"
                        else -> "${labels[0]}；失败时${labels[3]}。"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DoyuOutlinedButton(
                text = "相册",
                onClick = onPickAvatar,
                enabled = !uploading,
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PhotoLibrary
            )
            DoyuOutlinedButton(
                text = "拍照",
                onClick = onCameraAvatar,
                enabled = !uploading,
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PhotoCamera
            )
        }
        if (selectedAvatarUri != null || failed) {
            Spacer(Modifier.height(8.dp))
            DoyuOutlinedButton(
                text = "删除新图",
                onClick = onClearAvatar,
                enabled = !uploading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileEditScreenContent(navController: NavHostController?) {
    var retryCount by remember { mutableIntStateOf(0) }
    val dashboardState = safeCallToState(retryCount) { repo.dashboard() }.value
    val context = androidx.compose.ui.platform.LocalContext.current
    var nickname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarFileId by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarUploadFailed by remember { mutableStateOf(false) }
    var city by remember { mutableStateOf("手动选择城市 / 地区") }
    var interests by remember { mutableStateOf("拼豆, 教程, 材料") }
    var initialized by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (dashboardState is UiState.Success && !initialized) {
        nickname = dashboardState.data.user.nickname
        bio = dashboardState.data.user.bio
        initialized = true
    }
    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri
            avatarFileId = ""
            avatarUploadFailed = false
            message = null
        }
    }

    Scaffold(
        topBar = {
            DoyuTopBar("编辑资料", canGoBack = true, onBack = { navController?.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            when (dashboardState) {
                is UiState.Success -> {
                    ProfileEditAvatarCard(
                        currentAvatarUrl = dashboardState.data.user.avatarUrl,
                        selectedAvatarUri = selectedAvatarUri,
                        uploading = saving && selectedAvatarUri != null,
                        failed = avatarUploadFailed,
                        onPickAvatar = {
                            avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onCameraAvatar = {
                            message = "头像拍照入口为开发态；当前先使用相册选择，不新增 CameraX 头像链路。"
                        },
                        onClearAvatar = {
                            selectedAvatarUri = null
                            avatarFileId = ""
                            avatarUploadFailed = false
                            message = "已删除新头像，旧头像会保留。"
                        }
                    )

                    DoyuCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = {
                                nickname = it
                                message = null
                            },
                            label = { Text("昵称") },
                            singleLine = true,
                            isError = nickname.isBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = bio,
                            onValueChange = {
                                bio = it
                                message = null
                            },
                            label = { Text("个人简介") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    DoyuCard(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader(
                            title = "资料扩展",
                            subtitle = "以下字段只做 UI 目标展示，当前不接地图 API 或新增后端字段"
                        )
                        Spacer(Modifier.height(10.dp))
                        SettingsStatusRow(
                            title = "年龄段",
                            description = "16+ 用户展示，不做实名或年龄认证扩展。",
                            status = "16+",
                            icon = Icons.Filled.AccountCircle
                        )
                        HorizontalDivider()
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("城市 / 地区（UI-only）") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = interests,
                            onValueChange = { interests = it },
                            label = { Text("兴趣标签（UI-only）") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        DisabledFeatureNotice(
                            title = "不接定位能力",
                            message = "城市/地区只允许手动填写或后续字段设计；本轮不接地图、定位或隐私合规新接口。"
                        )
                    }

                    message?.let {
                        Text(
                            it,
                            color = if (it.contains("成功")) LightPrimary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    DoyuPrimaryButton(
                        text = if (saving) "保存中" else "保存",
                        onClick = {
                            val cleanName = nickname.trim()
                            if (cleanName.isBlank()) {
                                message = "昵称不能为空。"
                                return@DoyuPrimaryButton
                            }
                            saving = true
                            message = null
                            avatarUploadFailed = false
                            scope.launch {
                                runCatching {
                                    val confirmedAvatarFileId = selectedAvatarUri?.let { uri ->
                                        uploadProfileAvatar(uri, context)
                                    } ?: avatarFileId.trim().ifBlank { null }
                                    repo.updateProfile(
                                        UpdateProfileRequest(
                                            nickname = cleanName,
                                            avatarFileId = confirmedAvatarFileId,
                                            bio = bio.trim().ifBlank { null }
                                        )
                                    )
                                }.onSuccess {
                                    message = "保存成功"
                                    selectedAvatarUri = null
                                    avatarFileId = ""
                                    retryCount++
                                    navController?.popBackStack()
                                }.onFailure {
                                    if (selectedAvatarUri != null) avatarUploadFailed = true
                                    message = it.message ?: "保存失败，请稍后重试。"
                                }
                                saving = false
                            }
                        },
                        enabled = !saving && nickname.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Edit
                    )
                    DoyuOutlinedButton(
                        text = "取消",
                        onClick = { navController?.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> PageStateView(dashboardState, onRetry = { retryCount++ })
            }
        }
    }
}

private suspend fun uploadProfileAvatar(uri: Uri, context: android.content.Context): String =
    withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("无法读取头像图片")
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val fileName = "avatar_${System.currentTimeMillis()}_${uri.lastPathSegment ?: "image"}.jpg"
        val presign = requireSuccess(
            DoyuAppContainer.apiClient.uploadApi.presign(
                UploadPresignRequest(
                    usage = UploadUsage.AVATAR,
                    fileName = fileName,
                    mimeType = mimeType,
                    sizeBytes = bytes.size.toLong()
                )
            )
        )
        DoyuAppContainer.uploadTransport.upload(presign, bytes)
        val file = requireSuccess(
            DoyuAppContainer.apiClient.uploadApi.confirm(
                UploadConfirmRequest(
                    fileKey = presign.fileKey,
                    usage = UploadUsage.AVATAR,
                    mimeType = mimeType,
                    sizeBytes = bytes.size.toLong(),
                    width = options.outWidth.takeIf { it > 0 },
                    height = options.outHeight.takeIf { it > 0 }
                )
            )
        )
        file.fileId
    }
