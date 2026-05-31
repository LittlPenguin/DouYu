package cn.edu.app.douyu.feature.community

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.CommentMediaAsset
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.CreateCommentRequest
import cn.edu.app.douyu.core.model.CreatePostRequest
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.UploadConfirmRequest
import cn.edu.app.douyu.core.model.UploadPresignRequest
import cn.edu.app.douyu.core.model.UploadUsage
import cn.edu.app.douyu.core.network.upload
import cn.edu.app.douyu.core.network.requireSuccess
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.BeadCluster
import cn.edu.app.douyu.core.ui.ColorSwatchStrip
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
private const val MaxCommentImages = 9

private data class PendingCommentImage(
    val uri: Uri,
    val fileId: String? = null,
    val progress: Float = 0f,
    val failed: Boolean = false
)

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
    var feedRetryCount by remember { mutableIntStateOf(0) }
    var refreshDrag by remember { mutableFloatStateOf(0f) }
    val feedState = safeCallToState(feedRetryCount) { repo.feed() }.value
    val feedRefreshing = feedState is UiState.Loading

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
                    .pointerInput(feedRefreshing) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (refreshDrag > 90f && !feedRefreshing) feedRetryCount++
                                refreshDrag = 0f
                            },
                            onDragCancel = { refreshDrag = 0f },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 0) refreshDrag += dragAmount
                            }
                        )
                    }
            ) {
            if (feedRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = LightPrimary)
            }
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
    var commentExpanded by remember { mutableStateOf(false) }
    var selectedCommentImages by remember { mutableStateOf<List<PendingCommentImage>>(emptyList()) }
    var liked by remember(postId) { mutableStateOf<Boolean?>(null) }
    var favorited by remember(postId) { mutableStateOf<Boolean?>(null) }
    var followedAuthor by remember(postId) { mutableStateOf<Boolean?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MaxCommentImages)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val existing = selectedCommentImages.map { it.uri }.toSet()
            selectedCommentImages = (selectedCommentImages + uris.filterNot { it in existing }.map { PendingCommentImage(it) })
                .take(MaxCommentImages)
            commentExpanded = true
            commentNotice = null
        }
    }

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
        },
        bottomBar = {
            if (postState is UiState.Success) {
                CommentComposer(
                    value = commentText,
                    onValueChange = {
                        commentText = it
                        commentNotice = null
                    },
                    posting = actionInFlight == "comment",
                    notice = commentNotice,
                    expanded = commentExpanded,
                    onToggleExpanded = { commentExpanded = !commentExpanded },
                    images = selectedCommentImages,
                    onPickImages = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onRemoveImage = { uri ->
                        selectedCommentImages = selectedCommentImages.filterNot { it.uri == uri }
                    },
                    onClearImages = { selectedCommentImages = emptyList() },
                    onSubmit = {
                        if (!DoyuAppContainer.isLoggedIn) {
                            showLoginDialog = true
                            return@CommentComposer
                        }
                        val text = commentText.trim()
                        if (text.isBlank() && selectedCommentImages.isEmpty()) return@CommentComposer
                        actionInFlight = "comment"
                        actionError = null
                        commentNotice = null
                        scope.launch {
                            runCatching {
                                val uploadedImages = uploadCommentImages(
                                    images = selectedCommentImages,
                                    context = context,
                                    onProgress = { uri, progress ->
                                        selectedCommentImages = selectedCommentImages.map {
                                            if (it.uri == uri) it.copy(progress = progress, failed = false) else it
                                        }
                                    },
                                    onFailure = { uri ->
                                        selectedCommentImages = selectedCommentImages.map {
                                            if (it.uri == uri) it.copy(failed = true) else it
                                        }
                                    }
                                )
                                selectedCommentImages = uploadedImages
                                val mediaFileIds = uploadedImages.mapNotNull { it.fileId }
                                withContext(Dispatchers.IO) {
                                    repo.createComment(
                                        postId,
                                        CreateCommentRequest(content = text, mediaFileIds = mediaFileIds)
                                    )
                                }
                            }.onSuccess { comment ->
                                commentText = ""
                                selectedCommentImages = emptyList()
                                commentExpanded = false
                                commentNotice = if (comment.status == ContentStatus.REVIEWING) {
                                    "评论已提交，等待审核。"
                                } else {
                                    "评论已发布。"
                                }
                                commentsRetryCount++
                            }.onFailure {
                                commentExpanded = true
                                commentNotice = ErrorMessages.fromException(it as Exception)
                            }
                            actionInFlight = null
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding(),
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = postState) {
                is UiState.Success -> {
                    val post = state.data
                    val isLiked = liked == true
                    val isFavorited = favorited == true

                    item { PostHero(post) }
                    item {
                        PostBody(
                            post = post,
                            followedAuthor = followedAuthor == true,
                            followInFlight = actionInFlight == "follow",
                            onToggleFollow = {
                                if (!DoyuAppContainer.isLoggedIn) {
                                    showLoginDialog = true
                                    return@PostBody
                                }
                                val followed = followedAuthor == true
                                actionInFlight = "follow"
                                actionError = null
                                scope.launch {
                                    runCatching {
                                        withContext(Dispatchers.IO) {
                                            if (followed) repo.unfollowUser(post.authorId) else repo.followUser(post.authorId)
                                        }
                                    }.onSuccess { result ->
                                        followedAuthor = result.followedByMe
                                    }.onFailure { actionError = ErrorMessages.fromException(it as Exception) }
                                    actionInFlight = null
                                }
                            }
                        )
                    }

                    if (actionError != null) {
                        item { InlineError(actionError.orEmpty()) }
                    }

                    item {
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
                                                liked = result.liked ?: !isLiked
                                                postRetryCount++
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
                                                favorited = result.favorited ?: !isFavorited
                                                postRetryCount++
                                            }.onFailure { actionError = ErrorMessages.fromException(it as Exception) }
                                            actionInFlight = null
                                            }
                                    }
                                )
                            }
                        }
                    }

                    val linkedPatternId = post.linkedPatternId
                    if (linkedPatternId != null) {
                        item {
                            DoyuPrimaryButton(
                                text = "查看关联图纸",
                                onClick = { navController?.navigate(AppRoute.patternResult(linkedPatternId)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item { SectionHeader("评论", subtitle = "新评论会按后端审核状态展示") }

                    item {
                        CommentsSection(
                            postId = postId,
                            commentsRetryCount = commentsRetryCount,
                            onRetry = { commentsRetryCount++ }
                        )
                    }
                }
                else -> item { PageStateView(postState, onRetry = { postRetryCount++ }) }
            }
        }
    }
}

@Composable
private fun CommentsSection(postId: String, commentsRetryCount: Int, onRetry: () -> Unit) {
    val commentsState = safeCallToState(postId, commentsRetryCount) { repo.comments(postId) }.value
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (val comments = commentsState) {
            is UiState.Success -> comments.data.items.forEach { comment -> CommentListItem(comment) }
            UiState.Empty -> EmptyContent(
                title = "还没有评论",
                message = "登录后可以留下拼豆建议或材料清单。",
                showRetry = false
            )
            else -> PageStateView(commentsState, onRetry = onRetry)
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

            MediaPublishBoundary()

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
private fun PostBody(
    post: Post,
    followedAuthor: Boolean,
    followInFlight: Boolean,
    onToggleFollow: () -> Unit
) {
    DoyuCard {
        Text(post.title.ifBlank { "未命名作品" }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarDot()
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.author.nickname.ifBlank { "豆屿用户" }, style = MaterialTheme.typography.titleSmall)
            }
            DoyuOutlinedButton(
                text = if (followedAuthor) "已关注" else "关注",
                onClick = onToggleFollow,
                enabled = !followInFlight
            )
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
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    images: List<PendingCommentImage>,
    onPickImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onClearImages: () -> Unit,
    onSubmit: () -> Unit
) {
    val canSubmit = !posting && (value.isNotBlank() || images.isNotEmpty())
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 420f
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            AnimatedVisibility(visible = expanded && images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(images, key = { it.uri.toString() }) { image ->
                        PendingCommentImageChip(
                            image = image,
                            posting = posting,
                            onRemove = { onRemoveImage(image.uri) }
                        )
                    }
                    if (images.size < MaxCommentImages) {
                        item {
                            AddCommentImageChip(enabled = !posting, onClick = onPickImages)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (expanded && value.isBlank() && images.isEmpty()) {
                            onToggleExpanded()
                        } else {
                            onPickImages()
                        }
                    },
                    enabled = !posting,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Filled.PhotoLibrary else Icons.Filled.Edit,
                        contentDescription = if (expanded) "添加评论图片" else "展开评论工具条",
                        tint = LightPrimary
                    )
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .widthIn(min = if (expanded) 0.dp else 140.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = LightSurfaceVariant.copy(alpha = if (expanded) 0.66f else 0.46f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightPrimaryContainer.copy(alpha = 0.48f))
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            onValueChange(it)
                            if (!expanded) onToggleExpanded()
                        },
                        placeholder = {
                            Text(
                                if (expanded) "写下你的评论，也可以只发图片" else "写评论",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        maxLines = 1,
                        singleLine = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (expanded && (value.isNotBlank() || images.isNotEmpty()) && !posting) {
                    IconButton(
                        onClick = {
                            onValueChange("")
                            onClearImages()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "清空评论", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(
                    onClick = onSubmit,
                    enabled = canSubmit,
                    modifier = Modifier.size(48.dp)
                ) {
                    if (posting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "提交评论",
                            tint = if (canSubmit) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            if (notice != null) {
                Text(
                    notice,
                    color = if (notice.contains("失败") || notice.contains("无法")) MaterialTheme.colorScheme.error else LightPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 52.dp, top = 6.dp, end = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PendingCommentImageChip(
    image: PendingCommentImage,
    posting: Boolean,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LightSurfaceVariant)
            .border(1.dp, LightPrimaryContainer.copy(alpha = 0.52f), RoundedCornerShape(16.dp))
    ) {
        SubcomposeAsyncImage(
            model = image.uri,
            contentDescription = "评论图片预览",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = LightPrimary)
                }
            }
        )
        if (posting && image.fileId == null && !image.failed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { image.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        if (image.failed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.86f)),
                contentAlignment = Alignment.Center
            ) {
                Text("失败", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall)
            }
        }
        IconButton(
            onClick = onRemove,
            enabled = !posting,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f), CircleShape)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "移除图片", modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun AddCommentImageChip(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = LightPrimaryContainer.copy(alpha = 0.44f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightPrimary.copy(alpha = 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Add, contentDescription = "继续添加图片", tint = LightPrimary)
        }
    }
}

@Composable
private fun CommentListItem(comment: cn.edu.app.douyu.core.model.Comment) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AvatarDot()
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        comment.author.nickname.ifBlank { "豆屿用户" },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        statusLabel(comment.status),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(comment.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (comment.mediaAssets.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    CommentMediaStrip(comment.mediaAssets)
                }
            }
        }
    }
}

@Composable
private fun CommentMediaStrip(mediaAssets: List<CommentMediaAsset>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mediaAssets, key = { it.fileId }) { asset ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LightSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, LightPrimaryContainer.copy(alpha = 0.38f)),
                modifier = Modifier.size(86.dp)
            ) {
                if (asset.publicUrl.isNullOrBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Image, contentDescription = null, tint = LightPrimary)
                    }
                } else {
                    SubcomposeAsyncImage(
                        model = asset.publicUrl,
                        contentDescription = "评论图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = LightPrimary)
                            }
                        }
                    )
                }
            }
        }
    }
}

private suspend fun uploadCommentImages(
    images: List<PendingCommentImage>,
    context: android.content.Context,
    onProgress: (Uri, Float) -> Unit,
    onFailure: (Uri) -> Unit
): List<PendingCommentImage> {
    if (images.isEmpty()) return emptyList()
    val uploadApi = DoyuAppContainer.apiClient.uploadApi
    val transport = DoyuAppContainer.uploadTransport
    return withContext(Dispatchers.IO) {
        images.map { image ->
            if (image.fileId != null) return@map image
            try {
                val bytes = context.contentResolver.openInputStream(image.uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("无法读取评论图片")
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                val mimeType = context.contentResolver.getType(image.uri) ?: "image/jpeg"
                val fileName = "comment_${System.currentTimeMillis()}_${image.uri.lastPathSegment ?: "image"}.jpg"
                val presign = requireSuccess(
                    uploadApi.presign(
                        UploadPresignRequest(
                            usage = UploadUsage.POST_IMAGE,
                            fileName = fileName,
                            mimeType = mimeType,
                            sizeBytes = bytes.size.toLong()
                        )
                    )
                )
                transport.upload(presign, bytes) { progress ->
                    onProgress(image.uri, progress * 0.9f)
                }
                val file = requireSuccess(
                    uploadApi.confirm(
                        UploadConfirmRequest(
                            fileKey = presign.fileKey,
                            usage = UploadUsage.POST_IMAGE,
                            mimeType = mimeType,
                            sizeBytes = bytes.size.toLong(),
                            width = options.outWidth.takeIf { it > 0 },
                            height = options.outHeight.takeIf { it > 0 }
                        )
                    )
                )
                onProgress(image.uri, 1f)
                image.copy(fileId = file.fileId, progress = 1f, failed = false)
            } catch (e: Exception) {
                onFailure(image.uri)
                throw e
            }
        }
    }
}

@Composable
private fun MediaPublishBoundary() {
    DoyuCard {
        SectionHeader("媒体")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MediaTypeChip(Icons.Filled.Image, "单图", enabled = true, modifier = Modifier.weight(1f))
            MediaTypeChip(Icons.Filled.Collections, "多图", enabled = false, modifier = Modifier.weight(1f))
            MediaTypeChip(Icons.Filled.Videocam, "视频", enabled = false, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "当前发布先提交文字作品；多图和视频入口已按样式预留，待媒体契约闭环后再开放。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MediaTypeChip(icon: ImageVector, text: String, enabled: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 44.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) LightPrimaryContainer else LightSurfaceVariant,
        contentColor = if (enabled) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
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
