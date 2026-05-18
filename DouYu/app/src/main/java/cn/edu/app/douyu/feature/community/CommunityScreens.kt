package cn.edu.app.douyu.feature.community

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.ui.theme.*

private val repo = DoyuAppContainer.communityRepository

@Preview
@Composable
private fun CommunityFeedScreenPreview() { CommunityFeedScreenContent(navController = null) }

@Composable
fun CommunityFeedScreen(navController: NavHostController) { CommunityFeedScreenContent(navController) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityFeedScreenContent(navController: NavHostController?) {
    var selectedTag by remember { mutableIntStateOf(0) }
    val tags = listOf("推荐", "热门", "教程", "关注", "配件")
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(cn.edu.app.douyu.core.navigation.AppRoute.LOGIN)
            },
            message = "登录后才能使用完整功能"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BeadCluster(28.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "豆屿",
                            style = MaterialTheme.typography.headlineMedium,
                            color = LightPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索", tint = LightPrimary)
                    }
                    IconButton(onClick = {
                        if (DoyuAppContainer.isLoggedIn) {
                            navController?.navigate(AppRoute.POST_CREATE)
                        } else {
                            showLoginDialog = true
                        }
                    }) {
                        Icon(Icons.Filled.AddCircle, contentDescription = "发帖", tint = LightPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (DoyuAppContainer.isLoggedIn) {
                        navController?.navigate(AppRoute.POST_CREATE)
                    } else {
                        showLoginDialog = true
                    }
                },
                containerColor = LightPrimary,
                contentColor = LightOnPrimary,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Filled.Add, contentDescription = "发帖")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search bar (collapsible)
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索帖子...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Tag chips - horizontal scrolling
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags.size) { index ->
                    val selected = index == selectedTag
                    Surface(
                        onClick = { selectedTag = index },
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) LightPrimaryContainer else LightSurfaceVariant
                    ) {
                        Text(
                            tags[index],
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (selected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Feed content
            var feedRetryCount by remember { mutableIntStateOf(0) }
            val feedState = safeCallToState(feedRetryCount) { repo.feed() }.value
            when (val state = feedState) {
                is UiState.Success -> {
                    val filteredPosts = state.data.items.filter { post ->
                        val matchesTag = selectedTag == 0 || // "推荐" = all
                                post.topicNames.any { it.contains(tags[selectedTag]) } ||
                                tags[selectedTag] in post.content
                        val matchesSearch = searchQuery.isBlank() ||
                                post.title.contains(searchQuery, ignoreCase = true) ||
                                post.content.contains(searchQuery, ignoreCase = true)
                        matchesTag && matchesSearch
                    }
                    if (filteredPosts.isEmpty()) {
                        EmptyContent(
                            "没有找到帖子",
                            "换个关键词试试？",
                            showRetry = false
                        )
                    } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        itemsIndexed(filteredPosts) { index, post ->
                            StaggeredItemAnimator(index = index) {
                                PostCard(
                                    post = post,
                                    onClick = { navController?.navigate(AppRoute.postDetail(post.postId)) }
                                )
                            }
                        }
                    }
                    } // end else (filteredPosts not empty)
                }
                is UiState.Empty -> {
                    EmptyContent(
                        "还没有帖子",
                        "去发一条吧，成为豆屿第一位分享者！",
                        showRetry = false
                    )
                }
                else -> PageStateView(feedState, onRetry = { feedRetryCount++ })
            }
        }
    }
}

@Composable
private fun PostCard(post: Post, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = SpringFast,
        label = "cardScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        interactionSource = interactionSource
    ) {
        Column {
            // Image placeholder with aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f + (post.likeCount % 3) * 0.15f)
                    .background(LightSurfaceVariant)
            ) {
            }

            // Content area
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    post.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(LightSurfaceVariant)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            post.author.nickname,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = LightPrimary.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            post.likeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PostDetailScreenPreview() { PostDetailScreenContent(navController = null, postId = "post_001") }

@Composable
fun PostDetailScreen(navController: NavHostController, postId: String) { PostDetailScreenContent(navController, postId) }

@Composable
private fun PostDetailScreenContent(navController: NavHostController?, postId: String) {
    var postRetryCount by remember { mutableIntStateOf(0) }
    val postState = safeCallToState(postId, postRetryCount) { repo.post(postId) }.value
    Scaffold(
        topBar = {
            DoyuTopBar("作品详情", canGoBack = true, onBack = { navController?.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            when (val state = postState) {
                is UiState.Success -> {
                    // Post image area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(LightSurfaceVariant)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Post content card
                    DoyuCard {
                        Text(state.data.title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.data.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        ColorSwatchStrip(state.data.mediaColors)
                    }

                    // Interaction bar
                    DoyuCard {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconText(Icons.Filled.FavoriteBorder, state.data.likeCount.toString())
                            IconText(Icons.Filled.ChatBubbleOutline, state.data.commentCount.toString())
                            IconText(Icons.Filled.BookmarkBorder, state.data.favoriteCount.toString())
                        }
                    }

                    // Comments section
                    SectionHeader("评论")
                    val commentsState = safeCallToState(postId) { repo.comments(postId) }.value
                    when (val cs = commentsState) {
                        is UiState.Success -> cs.data.items.forEach { comment ->
                            DoyuCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(LightSurfaceVariant)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(comment.author.nickname, style = MaterialTheme.typography.titleSmall)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(comment.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is UiState.Empty -> PageStateView(UiState.Empty)
                        else -> PageStateView(commentsState)
                    }

                    // Action buttons
                    DoyuPrimaryButton(
                        "收藏图纸",
                        onClick = { navController?.navigate(AppRoute.patternResult(state.data.linkedPatternId ?: "pattern_001")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DoyuOutlinedButton(
                        "举报内容",
                        onClick = {},
                        icon = Icons.Filled.Report,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> PageStateView(postState, onRetry = { postRetryCount++ })
            }
        }
    }
}

@Preview
@Composable
private fun PostCreateScreenPreview() { PostCreateScreenContent(navController = null) }

@Composable
fun PostCreateScreen(navController: NavHostController) { PostCreateScreenContent(navController) }

@Composable
private fun PostCreateScreenContent(navController: NavHostController?) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                navController?.navigate(cn.edu.app.douyu.core.navigation.AppRoute.LOGIN)
            },
            message = "登录后才能发布作品"
        )
    }

    // Check login status on entry
    LaunchedEffect(Unit) {
        if (!DoyuAppContainer.isLoggedIn) {
            showLoginDialog = true
        }
    }

    Scaffold(
        topBar = {
            DoyuTopBar("发布作品", canGoBack = true, onBack = { navController?.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("正文、教程或踩坑经验") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DoyuCard {
                SectionHeader("图片与图纸")
                DoyuOutlinedButton(
                    "添加图片并确认 fileId",
                    onClick = { navController?.navigate(AppRoute.IMAGE_SELECT) },
                    icon = Icons.Filled.AddPhotoAlternate,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "发布接口使用 mediaFileIds；图片先走 /uploads/presign、直传、/uploads/confirm。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            DoyuPrimaryButton(
                "提交发布",
                onClick = { submitted = true },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (submitted) {
                LaunchedEffect(Unit) { navController?.popBackStack() }
            }
        }
    }
}

@Composable
private fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}
