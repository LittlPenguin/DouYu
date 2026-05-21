package cn.edu.app.douyu.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.edu.app.douyu.feature.ai.*
import cn.edu.app.douyu.feature.auth.LoginScreen
import cn.edu.app.douyu.feature.commerce.*
import cn.edu.app.douyu.feature.community.*
import cn.edu.app.douyu.feature.message.*
import cn.edu.app.douyu.feature.profile.*
import cn.edu.app.douyu.ui.theme.*

private const val TRANSITION_DURATION = 150

private data class TabUi(val tab: BottomTab, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabItems = listOf(
    TabUi(BottomTab.COMMUNITY, Icons.Filled.Groups, Icons.Filled.Groups),
    TabUi(BottomTab.COMMERCE, Icons.Filled.Storefront, Icons.Filled.Storefront),
    TabUi(BottomTab.AI, Icons.Filled.AutoAwesome, Icons.Filled.AutoAwesome),
    TabUi(BottomTab.MESSAGE, Icons.Filled.ChatBubble, Icons.Filled.ChatBubble),
    TabUi(BottomTab.PROFILE, Icons.Filled.Person, Icons.Filled.Person)
)

@Composable
fun DoyuApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = destination?.route in BottomTab.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DoyuBottomNavBar(destination, tabItems) { tab ->
                    navController.navigateBottomTab(tab.route)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.COMMUNITY.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(TRANSITION_DURATION)) + slideInHorizontally(tween(TRANSITION_DURATION)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(TRANSITION_DURATION)) + slideOutHorizontally(tween(TRANSITION_DURATION)) { -it / 4 }
            },
            popEnterTransition = {
                fadeIn(tween(TRANSITION_DURATION)) + slideInHorizontally(tween(TRANSITION_DURATION)) { -it / 4 }
            },
            popExitTransition = {
                fadeOut(tween(TRANSITION_DURATION)) + slideOutHorizontally(tween(TRANSITION_DURATION)) { it / 4 }
            }
        ) {
            composable(BottomTab.COMMUNITY.route) { CommunityFeedScreen(navController) }
            composable(BottomTab.AI.route) { AiHomeScreen(navController) }
            composable(BottomTab.COMMERCE.route) { CommerceHomeScreen(navController) }
            composable(BottomTab.MESSAGE.route) { MessageListScreen(navController) }
            composable(BottomTab.PROFILE.route) { ProfileScreen(navController) }
            composable(AppRoute.LOGIN) { LoginScreen(navController) }
            composable(AppRoute.POST_CREATE) { PostCreateScreen(navController) }
            composable(
                AppRoute.POST_DETAIL,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) {
                PostDetailScreen(navController, it.arguments?.getString("postId").orEmpty())
            }
            composable(AppRoute.IMAGE_SELECT) { ImageSelectScreen(navController) }
            composable(AppRoute.CAMERA_CAPTURE) { CameraCaptureScreen(navController) }
            composable(
                AppRoute.AI_PARAMS,
                arguments = listOf(navArgument("uploadedFileId") { type = NavType.StringType })
            ) {
                AiParamsScreen(navController, it.arguments?.getString("uploadedFileId").orEmpty())
            }
            composable(
                AppRoute.AI_PROGRESS,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) {
                AiProgressScreen(navController, it.arguments?.getString("jobId").orEmpty())
            }
            composable(
                AppRoute.PATTERN_RESULT,
                arguments = listOf(navArgument("patternId") { type = NavType.StringType })
            ) {
                PatternResultScreen(navController, it.arguments?.getString("patternId").orEmpty())
            }
            composable(AppRoute.PATTERN_HISTORY) { PatternHistoryScreen(navController) }
            composable(AppRoute.PRODUCT_LIST) { ProductListScreen(navController) }
            composable(
                AppRoute.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) {
                ProductDetailScreen(navController, it.arguments?.getString("productId").orEmpty())
            }
            composable(AppRoute.CART) { CartScreen(navController) }
            composable(AppRoute.ORDER_CONFIRM) { OrderConfirmScreen(navController) }
            composable(
                AppRoute.PAYMENT_RESULT,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) {
                PaymentResultScreen(navController, it.arguments?.getString("orderId").orEmpty())
            }
            composable(
                AppRoute.CONVERSATION,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) {
                ConversationScreen(navController, it.arguments?.getString("conversationId").orEmpty())
            }
            composable(AppRoute.MY_PATTERNS) { MyPatternsScreen(navController) }
            composable(AppRoute.FAVORITES) { FavoritesScreen(navController) }
            composable(AppRoute.MY_ORDERS) { MyOrdersScreen(navController) }
            composable(AppRoute.SETTINGS) { SettingsScreen(navController) }
        }
    }
}

@Composable
private fun DoyuBottomNavBar(
    destination: NavDestination?,
    tabItems: List<TabUi>,
    onTabClick: (BottomTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, LightOutlineVariant.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabItems.forEach { item ->
                val selected = destination?.route == item.tab.route
                DoyuTabItem(
                    tab = item,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabClick(item.tab) }
                )
            }
        }
    }
}

@Composable
private fun DoyuTabItem(
    tab: TabUi,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.98f,
        animationSpec = SpringFast,
        label = "tabScale"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = SpringFast,
        label = "iconScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) LightPrimaryContainer.copy(alpha = 0.64f) else Color.Transparent,
        modifier = modifier
            .heightIn(min = 54.dp)
            .scale(scale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.icon,
                contentDescription = tab.tab.label,
                tint = if (selected) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                tab.tab.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(24.dp)
                        .height(3.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(LightPrimary)
                )
            }
        }
    }
}

private fun NavHostController.navigateBottomTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
