package cn.edu.app.douyu.feature.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.Product
import cn.edu.app.douyu.core.model.Topic
import cn.edu.app.douyu.core.model.UserProfile
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.core.ui.DisabledFeatureNotice
import cn.edu.app.douyu.core.ui.DoyuCard
import cn.edu.app.douyu.core.ui.DoyuPage
import cn.edu.app.douyu.core.ui.DoyuSearchField
import cn.edu.app.douyu.core.ui.DoyuSegmentedControl
import cn.edu.app.douyu.core.ui.DoyuTopBar
import cn.edu.app.douyu.core.ui.EmptyContent
import cn.edu.app.douyu.core.ui.PageStateView
import cn.edu.app.douyu.core.ui.SectionHeader
import cn.edu.app.douyu.core.ui.UiState
import cn.edu.app.douyu.ui.theme.LightPrimary
import cn.edu.app.douyu.ui.theme.LightPrimaryContainer
import cn.edu.app.douyu.ui.theme.LightSurfaceVariant

private val searchCommunityRepo = DoyuAppContainer.communityRepository
private val searchCommerceRepo = DoyuAppContainer.commerceRepository

private enum class SearchScope(val label: String) {
    All("全部"),
    Posts("作品"),
    Patterns("图纸"),
    Products("商品"),
    Users("用户"),
    Topics("话题")
}

fun searchDisabledResultTrailingLabel(): String = "UI-only"

fun searchPatternScopeBoundaryCopy(): List<String> =
    listOf("图纸搜索待接入", "当前没有全局图纸搜索后端；可先查看 AI 历史或我的图纸。")

@Composable
fun SearchScreen(navController: NavHostController) {
    SearchScreenContent(navController)
}

@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreenContent(navController = null)
}

@Composable
private fun SearchScreenContent(navController: NavHostController?) {
    var query by remember { mutableStateOf("") }
    var selectedScope by remember { mutableIntStateOf(0) }
    var retryCount by remember { mutableIntStateOf(0) }
    val scopes = SearchScope.entries
    val feedState = safeCallToState(retryCount) { searchCommunityRepo.feed() }.value
    val productState = safeCallToState(retryCount) { searchCommerceRepo.products() }.value
    val userState = safeCallToState(query, retryCount) {
        if (query.isBlank()) PageResponse(emptyList(), 1, 20, 0, false) else searchCommunityRepo.searchUsers(query)
    }.value
    val topicState = safeCallToState(query, retryCount) {
        if (query.isBlank()) searchCommunityRepo.topics("") else searchCommunityRepo.topics(query)
    }.value

    Scaffold(
        topBar = {
            DoyuTopBar("搜索", canGoBack = true, onBack = { navController?.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            DoyuSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "搜索作品、图纸、商品、用户或话题"
            )
            DoyuSegmentedControl(
                options = scopes.map { it.label },
                selectedIndex = selectedScope,
                onSelected = { selectedScope = it }
            )
            DisabledFeatureNotice(
                title = "全局搜索仍是 UI-only",
                message = "当前只聚合已有局部能力：社区作品、商品本地筛选、用户搜索和话题搜索；没有新增全站搜索后端 API。"
            )
            SearchResults(
                scope = scopes[selectedScope],
                query = query,
                feedState = feedState,
                productState = productState,
                userState = userState,
                topicState = topicState,
                navController = navController,
                onRetry = { retryCount++ }
            )
        }
    }
}

@Composable
private fun SearchResults(
    scope: SearchScope,
    query: String,
    feedState: UiState<PageResponse<Post>>,
    productState: UiState<PageResponse<Product>>,
    userState: UiState<PageResponse<UserProfile>>,
    topicState: UiState<PageResponse<Topic>>,
    navController: NavHostController?,
    onRetry: () -> Unit
) {
    when (scope) {
        SearchScope.All -> {
            SearchPostResults(query, feedState, navController, onRetry, limit = 3)
            SearchProductResults(query, productState, navController, onRetry, limit = 3)
            SearchUserResults(userState, onRetry, limit = 3)
            SearchTopicResults(topicState, onRetry, limit = 5)
        }
        SearchScope.Posts -> SearchPostResults(query, feedState, navController, onRetry)
        SearchScope.Patterns -> SearchPatternBoundary()
        SearchScope.Products -> SearchProductResults(query, productState, navController, onRetry)
        SearchScope.Users -> SearchUserResults(userState, onRetry)
        SearchScope.Topics -> SearchTopicResults(topicState, onRetry)
    }
}

@Composable
private fun SearchPatternBoundary() {
    val copy = searchPatternScopeBoundaryCopy()
    SectionHeader("图纸")
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        Text(
            copy[0],
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            copy[1],
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchPostResults(
    query: String,
    state: UiState<PageResponse<Post>>,
    navController: NavHostController?,
    onRetry: () -> Unit,
    limit: Int = Int.MAX_VALUE
) {
    SectionHeader(if (limit == Int.MAX_VALUE) "作品" else "作品结果")
    when (state) {
        is UiState.Success -> {
            val items = state.data.items
                .filter { post ->
                    query.isBlank() ||
                        post.title.contains(query, ignoreCase = true) ||
                        post.content.contains(query, ignoreCase = true) ||
                        post.topicNames.any { it.contains(query, ignoreCase = true) }
                }
                .take(limit)
            if (items.isEmpty()) {
                EmptyContent("没有找到作品", "换个关键词再试。", showRetry = false)
            } else {
                items.forEach { post ->
                    SearchResultRow(
                        icon = Icons.Filled.Image,
                        title = post.title,
                        subtitle = post.author.nickname,
                        trailing = "${post.likeCount} 赞",
                        onClick = { navController?.navigate(AppRoute.postDetail(post.postId)) }
                    )
                }
            }
        }
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun SearchProductResults(
    query: String,
    state: UiState<PageResponse<Product>>,
    navController: NavHostController?,
    onRetry: () -> Unit,
    limit: Int = Int.MAX_VALUE
) {
    SectionHeader(if (limit == Int.MAX_VALUE) "商品" else "商品结果")
    when (state) {
        is UiState.Success -> {
            val items = state.data.items
                .filter { product ->
                    query.isBlank() ||
                        product.title.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.categoryName.contains(query, ignoreCase = true)
                }
                .take(limit)
            if (items.isEmpty()) {
                EmptyContent("没有找到商品", "商城当前只做已有商品本地筛选。", showRetry = false)
            } else {
                items.forEach { product ->
                    SearchResultRow(
                        icon = Icons.Filled.Storefront,
                        title = product.title,
                        subtitle = product.categoryName.ifBlank { product.type.name },
                        trailing = "¥${"%.2f".format(product.priceCent / 100.0)}",
                        onClick = { navController?.navigate(AppRoute.productDetail(product.productId)) }
                    )
                }
            }
        }
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun SearchUserResults(
    state: UiState<PageResponse<UserProfile>>,
    onRetry: () -> Unit,
    limit: Int = Int.MAX_VALUE
) {
    SectionHeader(if (limit == Int.MAX_VALUE) "用户" else "用户结果")
    when (state) {
        is UiState.Success -> {
            val items = state.data.items.take(limit)
            if (items.isEmpty()) {
                EmptyContent("输入关键词搜索用户", "用户搜索使用现有 /users/search 接口。", showRetry = false)
            } else {
                items.forEach { user ->
                    SearchResultRow(
                        icon = Icons.Filled.Person,
                        title = user.nickname,
                        subtitle = user.bio.ifBlank { "关注 ${user.followingCount} · 粉丝 ${user.followerCount}" },
                        trailing = searchDisabledResultTrailingLabel(),
                        onClick = null
                    )
                }
            }
        }
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun SearchTopicResults(
    state: UiState<PageResponse<Topic>>,
    onRetry: () -> Unit,
    limit: Int = Int.MAX_VALUE
) {
    SectionHeader(if (limit == Int.MAX_VALUE) "话题" else "话题结果")
    when (state) {
        is UiState.Success -> {
            val items = state.data.items.take(limit)
            if (items.isEmpty()) {
                EmptyContent("没有找到话题", "话题搜索使用现有 /topics 接口。", showRetry = false)
            } else {
                items.forEach { topic ->
                    SearchResultRow(
                        icon = Icons.Filled.Tag,
                        title = "#${topic.name}",
                        subtitle = topic.description.ifBlank { "当前话题仍按社区接口展示" },
                        trailing = searchDisabledResultTrailingLabel(),
                        onClick = null
                    )
                }
            }
        }
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun SearchResultRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: String,
    onClick: (() -> Unit)?
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = LightPrimary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, LightSurfaceVariant),
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    } else {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, LightSurfaceVariant),
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}
