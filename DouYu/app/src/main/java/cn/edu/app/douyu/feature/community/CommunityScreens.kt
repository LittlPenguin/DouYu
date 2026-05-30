package cn.edu.app.douyu.feature.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.CreateCommentRequest
import cn.edu.app.douyu.core.model.CreatePostRequest
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.BeadCluster
import cn.edu.app.douyu.core.ui.ColorSwatchStrip
import cn.edu.app.douyu.core.ui.DisabledFeatureNotice
import cn.edu.app.douyu.core.ui.DoyuCard
import cn.edu.app.douyu.core.ui.DoyuOutlinedButton
import cn.edu.app.douyu.core.ui.DoyuPage
import cn.edu.app.douyu.core.ui.DoyuPrimaryButton
import cn.edu.app.douyu.core.ui.DoyuSearchField
import cn.edu.app.douyu.core.ui.DoyuTopBar
import cn.edu.app.douyu.core.ui.EmptyContent
import cn.edu.app.douyu.core.ui.ErrorMessages
import cn.edu.app.douyu.core.ui.LoginRequiredDialog
import cn.edu.app.douyu.core.ui.PageStateView
import cn.edu.app.douyu.core.ui.SectionHeader
import cn.edu.app.douyu.core.ui.StaggeredItemAnimator
import cn.edu.app.douyu.core.ui.UiState
import cn.edu.app.douyu.ui.theme.LightOnPrimary
import cn.edu.app.douyu.ui.theme.LightOnPrimaryContainer
import cn.edu.app.douyu.ui.theme.LightPrimary
import cn.edu.app.douyu.ui.theme.LightPrimaryContainer
import cn.edu.app.douyu.ui.theme.LightSurfaceVariant
import cn.edu.app.douyu.ui.theme.SpringFast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val repo = DoyuAppContainer.communityRepository

@Preview
@Composable
private fun CommunityFeedScreenPreview() {
    CommunityFeedScreenContent(navController = null)
}

@Composable
fun CommunityFeedScreen(navController: NavHostController) {
    CommunityFeedScreenContent(navController)
}

@Composable
private fun CommunityFeedScreenContent(navController: NavHostController?) {
    var selectedTag by remember { mutableIntStateOf(0) }
    val tags = listOf("推荐", "教程", "作品", "新手", "材料")
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    CommunityLoginDialog(
        visible = showLoginDialog,
        navController = navController,
        onDismiss = { showLoginDialog = false },
        message = "登录后才能发布、点赞、收藏和评论。"
    )

    Scaffold(
        topBar = {
            DoyuTopBar(
                title = "豆屿 Doyu",
                action = {
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
                        Icon(Icons.Filled.Add, contentDescription = "发布", tint = LightPrimary)
                    }
                }
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
                Icon(Icons.Filled.Add, contentDescription = "发布")
            }
        }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
            AnimatedVisibility(visible = showSearch) {
                DoyuSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "搜索帖子、教程或材料",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            LazyRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tags) { index, tag ->
                    val selected = index == selectedTag
                    Surface(
                        onClick = { selectedTag = index },
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) LightPrimaryContainer else LightSurfaceVariant
                    ) {
                        Text(
                            tag,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (selected) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            var feedRetryCount by remember { mutableIntStateOf(0) }
            val feedState = safeCallToState(feedRetryCount) { repo.feed() }.value
            when (val state = feedState) {
                is UiState.Success -> {
                    val filteredPosts = state.data.items.filter { post ->
                        val matchesTag = selectedTag == 0 ||
                            post.topicNames.any { it.contains(tags[selectedTag]) } ||
                            post.content.contains(tags[selectedTag])
                        val matchesSearch = searchQuery.isBlank() ||
                            post.title.contains(searchQuery, ignoreCase = true) ||
                            post.content.contains(searchQuery, ignoreCase = true)
                        matchesTag && matchesSearch
                    }

                    if (filteredPosts.isEmpty()) {
                        EmptyContent(
                            title = "没有找到帖子",
                            message = "换个关键词，或者发布一条新的拼豆记录。",
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
                    }
                }
                UiState.Empty -> EmptyContent(
                    title = "还没有帖子",
                    message = "发一条拼豆记录，第一条内容也会先进入审核。",
                    showRetry = false
                )
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
            PostCoverImage(
                post = post,
                beadSize = 42.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.78f + (post.likeCount % 3) * 0.12f)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    post.title.ifBlank { "未命名作品" },
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AvatarDot()
                        Spacer(Modifier.width(6.dp))
                        Text(
                            post.author.nickname.ifBlank { "豆屿用户" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconText(Icons.Filled.Favorite, post.likeCount.toString())
                }
            }
        }
    }
}

@Preview
@Composable
private fun PostDetailScreenPreview() {
    PostDetailScreenContent(navController = null, postId = "post_001")
}

@Composable
fun PostDetailScreen(navController: NavHostController, postId: String) {
    PostDetailScreenContent(navController, postId)
}

@Composable
private fun PostDetailScreenContent(navController: NavHostController?, postId: String) {
    val scope = rememberCoroutineScope()
    var postRetryCount by remember { mutableIntStateOf(0) }
    var commentsRetryCount by remember { mutableIntStateOf(0) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var actionInFlight by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }
    var commentNotice by remember { mutableStateOf<String?>(null) }
    var liked by remember(postId) { mutableStateOf<Boolean?>(null) }
    var favorited by remember(postId) { mutableStateOf<Boolean?>(null) }

    CommunityLoginDialog(
        visible = showLoginDialog,
        navController = navController,
        onDismiss = { showLoginDialog = false },
        message = "登录后才能点赞、收藏和发表评论。"
    )

    val postState = safeCallToState(postId, postRetryCount) { repo.post(postId) }.value

    Scaffold(
        topBar = {
            DoyuTopBar("作品详情", canGoBack = true, onBack = { navController?.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            when (val state = postState) {
                is UiState.Success -> {
                    var post by remember(state.data.postId, postRetryCount) { mutableStateOf(state.data) }
                    val isLiked = liked == true
                    val isFavorited = favorited == true

                    PostHero(post)
                    PostBody(post)

                    if (actionError != null) {
                        InlineError(actionError.orEmpty())
                    }

                    DoyuCard {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InteractionButton(
                                icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                text = post.likeCount.toString(),
                                active = isLiked,
                                enabled = actionInFlight == null,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (!DoyuAppContainer.isLoggedIn) {
                                        showLoginDialog = true
                                        return@InteractionButton
                                    }
                                    actionInFlight = "like"
                                    actionError = null
                                    scope.launch {
                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                if (isLiked) repo.unlikePost(postId) else repo.likePost(postId)
                                            }
                                        }.onSuccess { result ->
                                            val next = result.liked ?: !isLiked
                                            liked = next
                                            post = withContext(Dispatchers.IO) { repo.post(postId) }
                                        }.onFailure { actionError = ErrorMessages.fromException(it as Exception) }
                                        actionInFlight = null
                                    }
                                }
                            )
                            InteractionMetric(
                                icon = Icons.Filled.ChatBubbleOutline,
                                text = post.commentCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            InteractionButton(
                                icon = if (isFavorited) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                text = post.favoriteCount.toString(),
                                active = isFavorited,
                                enabled = actionInFlight == null,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (!DoyuAppContainer.isLoggedIn) {
                                        showLoginDialog = true
                                        return@InteractionButton
                                    }
                                    actionInFlight = "favorite"
                                    actionError = null
                                    scope.launch {
                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                if (isFavorited) repo.unfavoritePost(postId) else repo.favoritePost(postId)
                                            }
                                        }.onSuccess { result ->
                                            val next = result.favorited ?: !isFavorited
                                            favorited = next
                                            post = withContext(Dispatchers.IO) { repo.post(postId) }
                                        }.onFailure { actionError = ErrorMessages.fromException(it as Exception) }
                                        actionInFlight = null
                                    }
                                }
                            )
                        }
                    }

                    val linkedPatternId = post.linkedPatternId
                    if (linkedPatternId != null) {
                        DoyuPrimaryButton(
                            text = "查看关联图纸",
                            onClick = { navController?.navigate(AppRoute.patternResult(linkedPatternId)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SectionHeader("评论", subtitle = "新评论会按后端审核状态展示")
                    CommentComposer(
                        value = commentText,
                        onValueChange = {
                            commentText = it
                            commentNotice = null
                        },
                        posting = actionInFlight == "comment",
                        notice = commentNotice,
                        onSubmit = {
                            if (!DoyuAppContainer.isLoggedIn) {
                                showLoginDialog = true
                                return@CommentComposer
                            }
                            val text = commentText.trim()
                            if (text.isBlank()) return@CommentComposer
                            actionInFlight = "comment"
                            actionError = null
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        repo.createComment(postId, CreateCommentRequest(content = text))
                                    }
                                }.onSuccess { comment ->
                                    commentText = ""
                                    commentNotice = if (comment.status == ContentStatus.REVIEWING) {
                                        "评论已提交，等待审核。"
                                    } else {
                                        "评论已发布。"
                                    }
                                    commentsRetryCount++
                                    post = post.copy(commentCount = post.commentCount + 1)
                                }.onFailure { actionError = ErrorMessages.fromException(it as Exception) }
                                actionInFlight = null
                            }
                        }
                    )

                    val commentsState = safeCallToState(postId, commentsRetryCount) { repo.comments(postId) }.value
                    when (val comments = commentsState) {
                        is UiState.Success -> comments.data.items.forEach { comment ->
                            DoyuCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarDot()
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            comment.author.nickname.ifBlank { "豆屿用户" },
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            statusLabel(comment.status),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(comment.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        UiState.Empty -> EmptyContent(
                            title = "还没有评论",
                            message = "登录后可以留下拼豆建议或材料清单。",
                            showRetry = false
                        )
                        else -> PageStateView(commentsState, onRetry = { commentsRetryCount++ })
                    }

                    DisabledFeatureNotice(
                        title = "举报入口暂不开放",
                        message = "第一轮样板先完成登录与社区主链路；举报会在审核接口统一梳理后开放。"
                    )
                }
                else -> PageStateView(postState, onRetry = { postRetryCount++ })
            }
        }
    }
}

@Preview
@Composable
private fun PostCreateScreenPreview() {
    PostCreateScreenContent(navController = null)
}

@Composable
fun PostCreateScreen(navController: NavHostController) {
    PostCreateScreenContent(navController)
}

@Composable
private fun PostCreateScreenContent(navController: NavHostController?) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdPost by remember { mutableStateOf<Post?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }

    CommunityLoginDialog(
        visible = showLoginDialog,
        navController = navController,
        onDismiss = { showLoginDialog = false },
        message = "登录后才能发布作品。"
    )

    LaunchedEffect(Unit) {
        if (!DoyuAppContainer.isLoggedIn) showLoginDialog = true
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
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        errorMessage = null
                    },
                    label = { Text("正文、教程或踩坑经验") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DisabledFeatureNotice(
                title = "图片发布串联后续补齐",
                message = "第一轮样板先提交文字帖子。图片上传已有独立链路，但本页暂不把 fileId 写入发帖请求，避免半成品入口误导。"
            )

            if (errorMessage != null) InlineError(errorMessage.orEmpty())

            createdPost?.let { post ->
                DoyuCard {
                    Text("提交成功", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (post.status == ContentStatus.REVIEWING) {
                            "帖子已进入审核中，审核通过前不会包装成公开内容。"
                        } else {
                            "服务端已返回状态：${statusLabel(post.status)}。"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DoyuOutlinedButton(
                            text = "返回社区",
                            onClick = { navController?.popBackStack() },
                            modifier = Modifier.weight(1f)
                        )
                        DoyuPrimaryButton(
                            text = "查看详情",
                            onClick = { navController?.navigate(AppRoute.postDetail(post.postId)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            DoyuPrimaryButton(
                text = if (isSubmitting) "提交中" else "提交发布",
                onClick = {
                    if (!DoyuAppContainer.isLoggedIn) {
                        showLoginDialog = true
                        return@DoyuPrimaryButton
                    }
                    val cleanTitle = title.trim()
                    val cleanContent = content.trim()
                    if (cleanTitle.isBlank() || cleanContent.isBlank()) {
                        errorMessage = "请填写标题和正文。"
                        return@DoyuPrimaryButton
                    }
                    isSubmitting = true
                    errorMessage = null
                    createdPost = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                repo.createPost(
                                    CreatePostRequest(
                                        title = cleanTitle,
                                        content = cleanContent,
                                        mediaFileIds = emptyList()
                                    )
                                )
                            }
                        }.onSuccess { post ->
                            createdPost = post
                            title = ""
                            content = ""
                        }.onFailure { errorMessage = ErrorMessages.fromException(it as Exception) }
                        isSubmitting = false
                    }
                },
                enabled = !isSubmitting && title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PostHero(post: Post) {
    PostCoverImage(
        post = post,
        beadSize = 72.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(MaterialTheme.shapes.large)
    )
}

@Composable
private fun PostCoverImage(post: Post, beadSize: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val fallbackContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            BeadCluster(beadSize)
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
private fun PostBody(post: Post) {
    DoyuCard {
        Text(post.title.ifBlank { "未命名作品" }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarDot()
            Spacer(Modifier.width(8.dp))
            Column {
                Text(post.author.nickname.ifBlank { "豆屿用户" }, style = MaterialTheme.typography.titleSmall)
                Text(statusLabel(post.status), style = MaterialTheme.typography.labelSmall, color = LightPrimary)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            post.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        if (post.mediaColors.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ColorSwatchStrip(post.mediaColors)
        }
    }
}

@Composable
private fun CommentComposer(
    value: String,
    onValueChange: (String) -> Unit,
    posting: Boolean,
    notice: String?,
    onSubmit: () -> Unit
) {
    DoyuCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("写评论") },
                minLines = 1,
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onSubmit,
                enabled = !posting && value.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                if (posting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "提交评论", tint = LightPrimary)
                }
            }
        }
        if (notice != null) {
            Spacer(Modifier.height(8.dp))
            Text(notice, color = LightPrimary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InteractionMetric(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 44.dp),
        shape = MaterialTheme.shapes.medium,
        color = LightSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InteractionButton(
    icon: ImageVector,
    text: String,
    active: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 44.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (active) LightPrimaryContainer else LightSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (active) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InlineError(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CommunityLoginDialog(
    visible: Boolean,
    navController: NavHostController?,
    onDismiss: () -> Unit,
    message: String
) {
    if (!visible) return
    LoginRequiredDialog(
        onDismiss = onDismiss,
        onLogin = {
            onDismiss()
            navController?.navigate(AppRoute.LOGIN)
        },
        message = message
    )
}

@Composable
private fun AvatarDot() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(LightSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        BeadCluster(18.dp)
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = LightPrimary.copy(alpha = 0.75f))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

private fun statusLabel(status: ContentStatus): String = when (status) {
    ContentStatus.REVIEWING -> "审核中"
    ContentStatus.VISIBLE -> "已公开"
    ContentStatus.SELF_VISIBLE -> "仅自己可见"
    ContentStatus.REJECTED -> "审核未通过"
    ContentStatus.DELETED -> "已删除"
}
