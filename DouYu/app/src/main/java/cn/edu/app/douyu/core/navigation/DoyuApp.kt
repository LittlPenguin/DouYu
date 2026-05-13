package cn.edu.app.douyu.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

private data class TabUi(val tab: BottomTab, val icon: ImageVector)

private val tabItems = listOf(
    TabUi(BottomTab.COMMUNITY, Icons.Filled.Groups),
    TabUi(BottomTab.AI, Icons.Filled.AutoAwesome),
    TabUi(BottomTab.COMMERCE, Icons.Filled.Storefront),
    TabUi(BottomTab.MESSAGE, Icons.Filled.ChatBubble),
    TabUi(BottomTab.PROFILE, Icons.Filled.Person)
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
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabItems.forEach { item ->
                        val selected = destination?.route == item.tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateBottomTab(item.tab.route) },
                            icon = { Icon(item.icon, contentDescription = item.tab.label) },
                            label = { Text(item.tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.COMMUNITY.route,
            modifier = Modifier.padding(padding)
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
            composable(AppRoute.AI_PARAMS) { AiParamsScreen(navController) }
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
            composable(AppRoute.SETTINGS) { SettingsScreen(navController) }
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
