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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.Comment
import cn.edu.app.douyu.core.model.CommentMediaAsset
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.CreateCommentRequest
import cn.edu.app.douyu.core.model.CreatePostRequest
import cn.edu.app.douyu.core.model.Sticker
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.Topic
import cn.edu.app.douyu.core.model.UploadConfirmRequest
import cn.edu.app.douyu.core.model.UploadPresignRequest
import cn.edu.app.douyu.core.model.UploadUsage
import cn.edu.app.douyu.core.model.UserProfile
import cn.edu.app.douyu.core.network.upload
import cn.edu.app.douyu.core.network.requireSuccess
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.ui.BeadCluster
import cn.edu.app.douyu.core.ui.ColorSwatchStrip
import cn.edu.app.douyu.core.ui.DoyuCard
import cn.edu.app.douyu.core.ui.DoyuMainTopBar
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
import cn.edu.app.douyu.ui.theme.LightSecondary
import cn.edu.app.douyu.ui.theme.LightSecondaryContainer
import cn.edu.app.douyu.ui.theme.LightSurfaceVariant
import cn.edu.app.douyu.ui.theme.LightSurface
import cn.edu.app.douyu.ui.theme.SpringFast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

internal data class CommentImageUploadState(
    val fileId: String?,
    val progress: Float,
    val failed: Boolean
)

internal data class CommentEmptyStateCopy(
    val title: String,
    val message: String,
    val showRetry: Boolean
)

internal data class SelectedMention(
    val userId: String,
    val nickname: String
)

internal data class SelectedTopic(
    val topicId: String,
    val name: String
)

private enum class CommentPickerMode {
    Mention,
    Topic,
    Sticker
}

private fun insertCommentToken(current: String, token: String): String {
    val cleanToken = token.trim()
    val base = current.trimEnd()
    return if (base.isBlank()) "$cleanToken " else "$base $cleanToken "
}

internal fun hasMeaningfulCommentText(
    value: String,
    mentions: List<SelectedMention>,
    topics: List<SelectedTopic>
): Boolean {
    var clean = value
    mentions.forEach { mention ->
        clean = clean.replace("@${mention.nickname.ifBlank { mention.userId }}", "")
    }
    topics.forEach { topic ->
        clean = clean.replace("#${topic.name.ifBlank { topic.topicId }}", "")
    }
    return clean.trim().isNotBlank()
}

internal fun shouldOfferCommentImageRetry(failed: Boolean, posting: Boolean): Boolean =
    failed && !posting

internal fun commentImageRetryActionLabel(failed: Boolean, posting: Boolean): String? =
    if (shouldOfferCommentImageRetry(failed, posting)) "重试上传" else null

internal fun commentFailedImagesRetryActionLabel(failedImageCount: Int, posting: Boolean): String? =
    if (failedImageCount > 0 && !posting) "重试上传失败图片" else null

internal fun commentImageUploadedState(fileId: String): CommentImageUploadState =
    CommentImageUploadState(fileId = fileId, progress = 1f, failed = false)

internal fun commentImageFailedState(): CommentImageUploadState =
    CommentImageUploadState(fileId = null, progress = 0f, failed = true)

internal fun commentImageLimitLabel(imageCount: Int): String? =
    if (imageCount >= MaxCommentImages) "$MaxCommentImages/$MaxCommentImages 已达上限" else null

internal fun commentCountAfterReviewingSubmit(currentPublicCount: Int): Int =
    currentPublicCount

internal fun publicCommentListItems(comments: List<Comment>): List<Comment> =
    comments.filter { it.status == ContentStatus.VISIBLE }

internal fun publicCommentEmptyStateCopy(
    rawCommentCount: Int,
    fallbackCount: Int
): CommentEmptyStateCopy =
    when {
        rawCommentCount > 0 -> CommentEmptyStateCopy(
            title = "暂无公开评论",
            message = "评论提交后会等待审核，通过后才会公开展示。",
            showRetry = false
        )
        fallbackCount > 0 -> CommentEmptyStateCopy(
            title = "评论暂未加载",
            message = "服务端显示已有评论数，但当前页没有返回评论列表，可稍后重试。",
            showRetry = true
        )
        else -> CommentEmptyStateCopy(
            title = "还没有评论",
            message = "登录后可以留下拼豆建议或材料清单。",
            showRetry = false
        )
    }

private fun shouldCollapseCommentComposerOnFocusLoss(
    expanded: Boolean,
    focused: Boolean,
    everFocused: Boolean,
    posting: Boolean,
    pickerOpen: Boolean,
    toolInteractionInProgress: Boolean
): Boolean =
    expanded && everFocused && !focused && !posting && !pickerOpen && !toolInteractionInProgress

internal fun postCarouselItems(coverImageUrl: String?, mediaFileIds: List<String>): List<String> {
    val items = buildList {
        val cover = coverImageUrl?.trim().orEmpty()
        if (cover.isNotBlank()) add(cover)
        mediaFileIds.map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { add(it) }
    }.distinct()
    return items.ifEmpty { listOf("fallback") }
}

fun communityHomeTopBarTitle(): String = "社区"

fun communityHomeChannelLabels(): List<String> = listOf("推荐", "关注", "教程", "图纸", "新手")

fun communityHomeHeroCopy(): List<String> =
    listOf("今日灵感", "真实作品图优先展示", "图片失败时保持卡片宽度与高度上限，回退拼豆色块占位。")

fun postComposeTopBarTitle(): String = "上传帖子"

fun postComposePrimarySections(): List<String> = listOf("图片", "正文", "话题", "审核前预览")

fun postComposeStateLabels(): List<String> = listOf("上传中", "上传失败", "9/9", "审核中")

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
    val tags = communityHomeChannelLabels()
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
            DoyuMainTopBar(
                title = communityHomeTopBarTitle(),
                onSearch = { navController?.navigate(AppRoute.SEARCH) },
                onOpenSettings = { navController?.navigate(AppRoute.SETTINGS) },
                onOpenAi = { navController?.navigate(BottomTab.AI.route) },
                onCreatePost = {
                    if (DoyuAppContainer.isLoggedIn) {
                        navController?.navigate(AppRoute.POST_CREATE)
                    } else {
                        navController?.navigate(AppRoute.login(AppRoute.POST_CREATE))
                    }
                }
            )
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
                            item(
                                key = "community_home_hero",
                                span = StaggeredGridItemSpan.FullLine
                            ) {
                                CommunityHomeHero()
                            }
                            itemsIndexed(
                                items = filteredPosts,
                                key = { _, post -> post.postId }
                            ) { index, post ->
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
private fun CommunityHomeHero() {
    val copy = communityHomeHeroCopy()
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(LightPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    contentDescription = null,
                    tint = LightPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = LightSecondaryContainer,
                    contentColor = LightSecondary
                ) {
                    Text(
                        copy[0],
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    copy[1],
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    copy[2],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PostCard(post: Post, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val imageHeight = remember(post.postId) {
        (120 + kotlin.math.abs(post.postId.hashCode() % 141)).dp
    }
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
                    .height(imageHeight)
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
    var selectedMentions by remember { mutableStateOf<List<SelectedMention>>(emptyList()) }
    var selectedTopics by remember { mutableStateOf<List<SelectedTopic>>(emptyList()) }
    var selectedStickers by remember { mutableStateOf<List<Sticker>>(emptyList()) }
    var pickerMode by remember { mutableStateOf<CommentPickerMode?>(null) }
    var commentFieldFocused by remember { mutableStateOf(false) }
    var commentFieldEverFocused by remember { mutableStateOf(false) }
    var commentToolInteractionInProgress by remember { mutableStateOf(false) }
    var commentToolInteractionResetJob by remember { mutableStateOf<Job?>(null) }
    var commentImagePickerOpen by remember { mutableStateOf(false) }
    var liked by remember(postId) { mutableStateOf<Boolean?>(null) }
    var favorited by remember(postId) { mutableStateOf<Boolean?>(null) }
    var followedAuthor by remember(postId) { mutableStateOf<Boolean?>(null) }
    var likeCount by remember(postId) { mutableIntStateOf(0) }
    var favoriteCount by remember(postId) { mutableIntStateOf(0) }
    var commentCount by remember(postId) { mutableIntStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current

    fun markCommentToolInteraction() {
        commentToolInteractionInProgress = true
        commentToolInteractionResetJob?.cancel()
        commentToolInteractionResetJob = scope.launch {
            delay(120)
            commentToolInteractionInProgress = false
        }
    }

    fun collapseCommentComposer() {
        commentExpanded = false
        commentFieldFocused = false
        commentFieldEverFocused = false
        focusManager.clearFocus()
    }

    fun expandCommentComposer() {
        markCommentToolInteraction()
        commentExpanded = true
        commentFieldFocused = false
        commentFieldEverFocused = false
    }

    fun runCommentToolAction(block: () -> Unit) {
        markCommentToolInteraction()
        block()
    }

    fun collapseIfCommentFocusLeft() {
        scope.launch {
            delay(80)
            if (shouldCollapseCommentComposerOnFocusLoss(
                    expanded = commentExpanded,
                    focused = commentFieldFocused,
                    everFocused = commentFieldEverFocused,
                    posting = actionInFlight == "comment",
                    pickerOpen = pickerMode != null || commentImagePickerOpen,
                    toolInteractionInProgress = commentToolInteractionInProgress
                )
            ) {
                commentExpanded = false
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MaxCommentImages)
    ) { uris ->
        commentImagePickerOpen = false
        if (uris.isNotEmpty()) {
            val existing = selectedCommentImages.map { it.uri }.toSet()
            val newUris = uris.filterNot { it in existing }
            val availableSlots = (MaxCommentImages - selectedCommentImages.size).coerceAtLeast(0)
            selectedCommentImages = selectedCommentImages + newUris.take(availableSlots).map { PendingCommentImage(it) }
            expandCommentComposer()
            commentNotice = if (newUris.size > availableSlots) {
                "最多添加 $MaxCommentImages 张图片，已保留前 $MaxCommentImages 张。"
            } else {
                null
            }
        }
    }

    CommunityLoginDialog(
        visible = showLoginDialog,
        navController = navController,
        onDismiss = { showLoginDialog = false },
        message = "登录后才能点赞、收藏和发表评论。"
    )

    val postState = safeCallToState(postId, postRetryCount) { repo.post(postId) }.value
    LaunchedEffect(commentExpanded) {
        if (!commentExpanded) {
            commentFieldFocused = false
            commentFieldEverFocused = false
        }
    }
    LaunchedEffect(postState) {
        val post = (postState as? UiState.Success)?.data ?: return@LaunchedEffect
        liked = post.likedByMe
        favorited = post.favoritedByMe
        followedAuthor = post.followedAuthorByMe
        likeCount = post.likeCount
        favoriteCount = post.favoriteCount
        commentCount = post.commentCount
    }

    when (pickerMode) {
        CommentPickerMode.Mention -> CommentMentionPickerDialog(
            onDismiss = { pickerMode = null },
            onSelect = { user ->
                if (selectedMentions.none { it.userId == user.userId }) {
                    selectedMentions = selectedMentions + SelectedMention(user.userId, user.nickname)
                    commentText = insertCommentToken(commentText, "@${user.nickname}")
                }
                expandCommentComposer()
                pickerMode = null
            }
        )
        CommentPickerMode.Topic -> CommentTopicPickerDialog(
            onDismiss = { pickerMode = null },
            onSelect = { topic ->
                if (selectedTopics.none { it.topicId == topic.topicId }) {
                    selectedTopics = selectedTopics + SelectedTopic(topic.topicId, topic.name)
                    commentText = insertCommentToken(commentText, "#${topic.name}")
                }
                expandCommentComposer()
                pickerMode = null
            }
        )
        CommentPickerMode.Sticker -> CommentStickerPickerDialog(
            onDismiss = { pickerMode = null },
            onSelect = { sticker ->
                if (selectedStickers.none { it.stickerId == sticker.stickerId }) {
                    selectedStickers = selectedStickers + sticker
                    val label = sticker.emojiText.ifBlank { sticker.name }
                    commentText = insertCommentToken(commentText, label)
                }
                expandCommentComposer()
                pickerMode = null
            }
        )
        null -> Unit
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                DoyuTopBar("作品详情", canGoBack = true, onBack = { navController?.popBackStack() })
            }
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
                    onToggleExpanded = {
                        if (commentExpanded) {
                            collapseCommentComposer()
                        } else {
                            expandCommentComposer()
                        }
                    },
                    onFieldFocusChanged = { focused ->
                        commentFieldFocused = focused
                        if (focused) {
                            commentFieldEverFocused = true
                            commentExpanded = true
                        } else {
                            collapseIfCommentFocusLeft()
                        }
                    },
                    onInternalInteraction = { markCommentToolInteraction() },
                    images = selectedCommentImages,
                    mentions = selectedMentions,
                    topics = selectedTopics,
                    stickers = selectedStickers,
                    onPickImages = {
                        runCommentToolAction {
                            commentImagePickerOpen = true
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                    onPickMention = { runCommentToolAction { pickerMode = CommentPickerMode.Mention } },
                    onPickTopic = { runCommentToolAction { pickerMode = CommentPickerMode.Topic } },
                    onPickSticker = { runCommentToolAction { pickerMode = CommentPickerMode.Sticker } },
                    onRemoveImage = { uri ->
                        selectedCommentImages = selectedCommentImages.filterNot { it.uri == uri }
                    },
                    onRemoveMention = { userId ->
                        selectedMentions = selectedMentions.filterNot { it.userId == userId }
                    },
                    onRemoveTopic = { topicId ->
                        selectedTopics = selectedTopics.filterNot { it.topicId == topicId }
                    },
                    onRemoveSticker = { stickerId ->
                        selectedStickers = selectedStickers.filterNot { it.stickerId == stickerId }
                    },
                    onClearDraft = {
                        runCommentToolAction {
                            commentText = ""
                            selectedCommentImages = emptyList()
                            selectedMentions = emptyList()
                            selectedTopics = emptyList()
                            selectedStickers = emptyList()
                            commentNotice = null
                            collapseCommentComposer()
                        }
                    },
                    onSubmit = {
                        markCommentToolInteraction()
                        if (!DoyuAppContainer.isLoggedIn) {
                            showLoginDialog = true
                            return@CommentComposer
                        }
                        val text = commentText.trim()
                        val hasMeaningfulText = hasMeaningfulCommentText(text, selectedMentions, selectedTopics)
                        if (!hasMeaningfulText && selectedCommentImages.isEmpty() && selectedStickers.isEmpty()) {
                            commentExpanded = true
                            commentNotice = "请补充评论内容，不能只发送 @ 或 #。"
                            return@CommentComposer
                        }
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
                                    onUploaded = { uri, fileId ->
                                        selectedCommentImages = selectedCommentImages.map {
                                            if (it.uri == uri) {
                                                val state = commentImageUploadedState(fileId)
                                                it.copy(fileId = state.fileId, progress = state.progress, failed = state.failed)
                                            } else {
                                                it
                                            }
                                        }
                                    },
                                    onFailure = { uri ->
                                        selectedCommentImages = selectedCommentImages.map {
                                            if (it.uri == uri) {
                                                val state = commentImageFailedState()
                                                it.copy(fileId = state.fileId, progress = state.progress, failed = state.failed)
                                            } else {
                                                it
                                            }
                                        }
                                    }
                                )
                                selectedCommentImages = uploadedImages
                                val mediaFileIds = uploadedImages.mapNotNull { it.fileId }
                                withContext(Dispatchers.IO) {
                                    repo.createComment(
                                        postId,
                                        CreateCommentRequest(
                                            content = text,
                                            mediaFileIds = mediaFileIds,
                                            mentionUserIds = selectedMentions.map { it.userId },
                                            topicIds = selectedTopics.map { it.topicId },
                                            stickerIds = selectedStickers.map { it.stickerId }
                                        )
                                    )
                                }
                            }.onSuccess {
                                commentText = ""
                                selectedCommentImages = emptyList()
                                selectedMentions = emptyList()
                                selectedTopics = emptyList()
                                selectedStickers = emptyList()
                                collapseCommentComposer()
                                commentNotice = "评论已提交，等待审核"
                                commentCount = commentCountAfterReviewingSubmit(commentCount)
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
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(commentExpanded) {
                    if (commentExpanded) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.any { it.changedToDown() }) {
                                    collapseCommentComposer()
                                }
                            }
                        }
                    }
                },
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
                    val isLiked = liked ?: post.likedByMe
                    val isFavorited = favorited ?: post.favoritedByMe
                    val isFollowedAuthor = followedAuthor ?: post.followedAuthorByMe
                    val displayLikeCount = if (liked == null) post.likeCount else likeCount
                    val displayFavoriteCount = if (favorited == null) post.favoriteCount else favoriteCount
                    val displayCommentCount = if (commentCount == 0 && post.commentCount > 0) post.commentCount else commentCount

                    item { PostHero(post) }
                    item {
                        PostBody(
                            post = post,
                            followedAuthor = isFollowedAuthor,
                            followInFlight = actionInFlight == "follow",
                            onToggleFollow = {
                                if (!DoyuAppContainer.isLoggedIn) {
                                    showLoginDialog = true
                                    return@PostBody
                                }
                                val followed = isFollowedAuthor
                                actionInFlight = "follow"
                                actionError = null
                                scope.launch {
                                    runCatching {
                                        withContext(Dispatchers.IO) {
                                            if (followed) repo.unfollowUser(post.authorId) else repo.followUser(post.authorId)
                                        }
                                    }.onSuccess { result ->
                                        followedAuthor = result.followedByMe
                                        postRetryCount++
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
                                    text = displayLikeCount.toString(),
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
                                                val nextLiked = result.likedByMe ?: result.liked ?: !isLiked
                                                liked = nextLiked
                                                likeCount = result.likeCount ?: displayLikeCount
                                                result.favoriteCount?.let { favoriteCount = it }
                                                result.favoritedByMe?.let { favorited = it }
                                                postRetryCount++
                                            }.onFailure { actionError = ErrorMessages.fromException(it as Exception) }
                                            actionInFlight = null
                                        }
                                    }
                                )
                                InteractionMetric(
                                    icon = Icons.Filled.ChatBubbleOutline,
                                    text = displayCommentCount.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                InteractionButton(
                                    icon = if (isFavorited) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    text = displayFavoriteCount.toString(),
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
                                                val nextFavorited = result.favoritedByMe ?: result.favorited ?: !isFavorited
                                                favorited = nextFavorited
                                                favoriteCount = result.favoriteCount ?: displayFavoriteCount
                                                result.likeCount?.let { likeCount = it }
                                                result.likedByMe?.let { liked = it }
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

                    item { SectionHeader("评论", subtitle = "${displayCommentCount} 条 · 新评论会按后端审核状态展示") }

                    item {
                        CommentsSection(
                            postId = postId,
                            commentsRetryCount = commentsRetryCount,
                            fallbackCount = displayCommentCount,
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
private fun CommentsSection(
    postId: String,
    commentsRetryCount: Int,
    fallbackCount: Int,
    onRetry: () -> Unit
) {
    val commentsState = safeCallToState(postId, commentsRetryCount) { repo.comments(postId) }.value
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (val comments = commentsState) {
            is UiState.Success -> {
                val publicComments = publicCommentListItems(comments.data.items)
                if (publicComments.isEmpty()) {
                    val emptyState = publicCommentEmptyStateCopy(
                        rawCommentCount = comments.data.items.size,
                        fallbackCount = fallbackCount
                    )
                    EmptyContent(
                        title = emptyState.title,
                        message = emptyState.message,
                        showRetry = emptyState.showRetry
                    )
                } else {
                    publicComments.forEach { comment -> CommentListItem(comment) }
                }
            }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<PendingCommentImage>>(emptyList()) }
    var selectedTopics by remember { mutableStateOf<List<SelectedTopic>>(emptyList()) }
    var topicPickerOpen by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdPost by remember { mutableStateOf<Post?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MaxCommentImages)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val existing = selectedImages.map { it.uri }.toSet()
            val newUris = uris.filterNot { it in existing }
            val availableSlots = (MaxCommentImages - selectedImages.size).coerceAtLeast(0)
            selectedImages = selectedImages + newUris.take(availableSlots).map { PendingCommentImage(it) }
            errorMessage = if (newUris.size > availableSlots) {
                "最多添加 $MaxCommentImages 张图片，已保留前 $MaxCommentImages 张。"
            } else {
                null
            }
        }
    }

    CommunityLoginDialog(
        visible = showLoginDialog,
        navController = navController,
        onDismiss = { showLoginDialog = false },
        message = "登录后才能发布作品。"
    )

    LaunchedEffect(Unit) {
        if (!DoyuAppContainer.isLoggedIn) showLoginDialog = true
    }
    if (topicPickerOpen) {
        CommentTopicPickerDialog(
            onDismiss = { topicPickerOpen = false },
            onSelect = { topic ->
                if (selectedTopics.none { it.topicId == topic.topicId }) {
                    selectedTopics = selectedTopics + SelectedTopic(topic.topicId, topic.name)
                }
                topicPickerOpen = false
            }
        )
    }

    fun submitPost() {
        if (!DoyuAppContainer.isLoggedIn) {
            showLoginDialog = true
            return
        }
        val cleanTitle = title.trim()
        val cleanContent = content.trim()
        if (cleanTitle.isBlank() || cleanContent.isBlank()) {
            errorMessage = "请填写标题和正文。"
            return
        }
        if (isSubmitting) return
        isSubmitting = true
        errorMessage = null
        createdPost = null
        scope.launch {
            runCatching {
                val uploadedImages = uploadCommentImages(
                    images = selectedImages,
                    context = context,
                    onProgress = { uri, progress ->
                        selectedImages = selectedImages.map {
                            if (it.uri == uri) it.copy(progress = progress, failed = false) else it
                        }
                    },
                    onUploaded = { uri, fileId ->
                        selectedImages = selectedImages.map {
                            if (it.uri == uri) {
                                val state = commentImageUploadedState(fileId)
                                it.copy(fileId = state.fileId, progress = state.progress, failed = state.failed)
                            } else {
                                it
                            }
                        }
                    },
                    onFailure = { uri ->
                        selectedImages = selectedImages.map {
                            if (it.uri == uri) {
                                val state = commentImageFailedState()
                                it.copy(fileId = state.fileId, progress = state.progress, failed = state.failed)
                            } else {
                                it
                            }
                        }
                    }
                )
                selectedImages = uploadedImages
                withContext(Dispatchers.IO) {
                    repo.createPost(
                        CreatePostRequest(
                            title = cleanTitle,
                            content = cleanContent,
                            mediaFileIds = uploadedImages.mapNotNull { it.fileId },
                            topicIds = selectedTopics.map { it.topicId }
                        )
                    )
                }
            }.onSuccess { post ->
                createdPost = post
                title = ""
                content = ""
                selectedImages = emptyList()
                selectedTopics = emptyList()
            }.onFailure { errorMessage = ErrorMessages.fromException(it as Exception) }
            isSubmitting = false
        }
    }

    Scaffold(
        topBar = {
            DoyuTopBar(
                postComposeTopBarTitle(),
                canGoBack = true,
                onBack = { navController?.popBackStack() },
                action = {
                    TextButton(
                        onClick = { submitPost() },
                        enabled = !isSubmitting && title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text(if (isSubmitting) "提交中" else "发布")
                    }
                }
            )
        }
    ) { padding ->
        DoyuPage(padding) {
            PostComposeMediaSection(
                images = selectedImages,
                submitting = isSubmitting,
                onPickImages = {
                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemoveImage = { uri ->
                    selectedImages = selectedImages.filterNot { it.uri == uri }
                },
                onRetryImage = { submitPost() }
            )

            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("正文", "标题和正文不能为空；图片可选，发布后进入审核流程。")
                Spacer(Modifier.height(12.dp))
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

            PostComposeTopicSection(
                selectedTopics = selectedTopics,
                submitting = isSubmitting,
                onPickTopic = { topicPickerOpen = true },
                onRemoveTopic = { topicId ->
                    selectedTopics = selectedTopics.filterNot { it.topicId == topicId }
                }
            )

            PostComposePreviewSection(
                title = title,
                content = content,
                images = selectedImages,
                topics = selectedTopics
            )

            if (errorMessage != null) InlineError(errorMessage.orEmpty())

            createdPost?.let { post ->
                DoyuCard {
                    Text("提交成功", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (post.status == ContentStatus.REVIEWING) {
                            "审核中：帖子已提交审核，审核通过后展示。"
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
                onClick = { submitPost() },
                enabled = !isSubmitting && title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PostComposeMediaSection(
    images: List<PendingCommentImage>,
    submitting: Boolean,
    onPickImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onRetryImage: () -> Unit
) {
    DoyuCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("图片", "最多 9 张，上传失败保留缩略图并可重试或删除。")
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(images, key = { it.uri.toString() }) { image ->
                PendingCommentImageChip(
                    image = image,
                    posting = submitting,
                    onRemove = { onRemoveImage(image.uri) },
                    onRetry = onRetryImage
                )
            }
            if (images.size < MaxCommentImages) {
                item {
                    AddCommentImageChip(enabled = !submitting, onClick = onPickImages)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        val stateLabels = postComposeStateLabels()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CommentSelectionChip("${images.size}/$MaxCommentImages", enabled = false, onRemove = {})
            if (submitting) CommentSelectionChip(stateLabels[0], enabled = false, onRemove = {})
            if (images.any { it.failed }) CommentSelectionChip(stateLabels[1], enabled = false, onRemove = {})
            if (images.size >= MaxCommentImages) CommentSelectionChip(stateLabels[2], enabled = false, onRemove = {})
        }
    }
}

@Composable
private fun PostComposeTopicSection(
    selectedTopics: List<SelectedTopic>,
    submitting: Boolean,
    onPickTopic: () -> Unit,
    onRemoveTopic: (String) -> Unit
) {
    DoyuCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("话题", "选择话题有助于进入社区频道；未选择也可发布。")
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(selectedTopics, key = { it.topicId }) { topic ->
                CommentSelectionChip(
                    text = "#${topic.name.ifBlank { topic.topicId }}",
                    enabled = !submitting,
                    onRemove = { onRemoveTopic(topic.topicId) }
                )
            }
            item {
                Surface(
                    onClick = onPickTopic,
                    enabled = !submitting,
                    shape = RoundedCornerShape(999.dp),
                    color = LightSecondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Tag, contentDescription = null, tint = LightSecondary, modifier = Modifier.size(18.dp))
                        Text("添加话题", style = MaterialTheme.typography.labelMedium, color = LightSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PostComposePreviewSection(
    title: String,
    content: String,
    images: List<PendingCommentImage>,
    topics: List<SelectedTopic>
) {
    DoyuCard(modifier = Modifier.fillMaxWidth()) {
        val sections = postComposePrimarySections()
        SectionHeader(sections[3], "图片、正文和话题按作品详情结构预览。")
        Spacer(Modifier.height(10.dp))
        Text(
            title.ifBlank { "标题预览" },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            content.ifBlank { "正文预览会显示在这里。" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (topics.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topics, key = { it.topicId }) { topic ->
                    InlineCommentChip("#${topic.name.ifBlank { topic.topicId }}")
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (images.isEmpty()) "无上传图；图片可选，但标题/正文不能为空。" else "已选择 ${images.size} 张图片，发布前会先上传。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PostHero(post: Post) {
    val items = remember(post.postId, post.coverImageUrl, post.mediaFileIds) {
        postCarouselItems(post.coverImageUrl, post.mediaFileIds)
    }
    var selectedIndex by remember(post.postId) { mutableIntStateOf(0) }
    val selectedItem = items.getOrElse(selectedIndex) { items.first() }

    DoyuCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(312.dp)
                .clip(MaterialTheme.shapes.large)
                .background(LightSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            CarouselImageFrame(
                item = selectedItem,
                title = post.title,
                beadSize = 76.dp,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(
                    "${selectedIndex + 1}/${items.size}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (items.size > 1) {
                IconButton(
                    onClick = {
                        selectedIndex = if (selectedIndex == 0) items.lastIndex else selectedIndex - 1
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上一张",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        selectedIndex = if (selectedIndex == items.lastIndex) 0 else selectedIndex + 1
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一张",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            itemsIndexed(items) { index, item ->
                CarouselThumbnail(
                    item = item,
                    title = post.title,
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index }
                )
            }
        }
        Text(
            "图片优先展示；非封面媒体当前只返回 fileId 时使用占位缩略图，不伪装成真实图片 URL。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
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
private fun CarouselImageFrame(
    item: String,
    title: String,
    beadSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    if (item.startsWith("http://") || item.startsWith("https://")) {
        SubcomposeAsyncImage(
            model = item,
            contentDescription = title.ifBlank { "作品图片" },
            contentScale = ContentScale.Crop,
            modifier = modifier,
            loading = {
                CarouselFallbackFrame(beadSize = beadSize, label = "加载中", modifier = Modifier.fillMaxSize())
            },
            error = {
                CarouselFallbackFrame(beadSize = beadSize, label = "图片加载失败", modifier = Modifier.fillMaxSize())
            }
        )
    } else {
        CarouselFallbackFrame(
            beadSize = beadSize,
            label = if (item == "fallback") "暂无图片" else item,
            modifier = modifier
        )
    }
}

@Composable
private fun CarouselFallbackFrame(
    beadSize: androidx.compose.ui.unit.Dp,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BeadCluster(beadSize)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 220.dp)
            )
        }
    }
}

@Composable
private fun CarouselThumbnail(
    item: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) LightSecondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = LightSurface,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.size(width = 64.dp, height = 54.dp)
    ) {
        CarouselImageFrame(
            item = item,
            title = title,
            beadSize = 22.dp,
            modifier = Modifier.fillMaxSize()
        )
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
    onFieldFocusChanged: (Boolean) -> Unit,
    onInternalInteraction: () -> Unit,
    images: List<PendingCommentImage>,
    mentions: List<SelectedMention>,
    topics: List<SelectedTopic>,
    stickers: List<Sticker>,
    onPickImages: () -> Unit,
    onPickMention: () -> Unit,
    onPickTopic: () -> Unit,
    onPickSticker: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onRemoveMention: (String) -> Unit,
    onRemoveTopic: (String) -> Unit,
    onRemoveSticker: (String) -> Unit,
    onClearDraft: () -> Unit,
    onSubmit: () -> Unit
) {
    val hasMeaningfulText = hasMeaningfulCommentText(value, mentions, topics)
    val canSubmit = !posting && (hasMeaningfulText || images.isNotEmpty() || stickers.isNotEmpty())
    val hasSelections = mentions.isNotEmpty() || topics.isNotEmpty() || stickers.isNotEmpty()
    val hasDraft = value.isNotBlank() || images.isNotEmpty() || hasSelections
    val imageLimitLabel = commentImageLimitLabel(images.size)
    val failedImagesRetryLabel = commentFailedImagesRetryActionLabel(images.count { it.failed }, posting)
    val toolTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
    val shellShape = RoundedCornerShape(28.dp)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(expanded, posting) {
        if (expanded && !posting) {
            focusRequester.requestFocus()
        }
    }
    Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.any { it.changedToDown() }) {
                                onInternalInteraction()
                            }
                        }
                    }
                }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 360f
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            AnimatedVisibility(visible = expanded && images.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(images, key = { it.uri.toString() }) { image ->
                            PendingCommentImageChip(
                                image = image,
                                posting = posting,
                                onRemove = { onRemoveImage(image.uri) },
                                onRetry = onSubmit
                            )
                        }
                        if (images.size < MaxCommentImages) {
                            item {
                                AddCommentImageChip(enabled = !posting, onClick = onPickImages)
                            }
                        }
                    }
                    if (imageLimitLabel != null) {
                        Text(
                            imageLimitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = LightSecondary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    if (failedImagesRetryLabel != null) {
                        Surface(
                            onClick = onSubmit,
                            enabled = !posting,
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                failedImagesRetryLabel,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(visible = expanded && hasSelections) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    items(mentions, key = { it.userId }) { mention ->
                        CommentSelectionChip(
                            text = "@${mention.nickname.ifBlank { mention.userId }}",
                            enabled = !posting,
                            onRemove = { onRemoveMention(mention.userId) }
                        )
                    }
                    items(topics, key = { it.topicId }) { topic ->
                        CommentSelectionChip(
                            text = "#${topic.name.ifBlank { topic.topicId }}",
                            enabled = !posting,
                            onRemove = { onRemoveTopic(topic.topicId) }
                        )
                    }
                    items(stickers, key = { it.stickerId }) { sticker ->
                        CommentSelectionChip(
                            text = sticker.emojiText.ifBlank { sticker.name },
                            enabled = !posting,
                            onRemove = { onRemoveSticker(sticker.stickerId) }
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (expanded) Modifier else Modifier.height(56.dp)),
                shape = shellShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 9.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
                )
            ) {
                if (expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CommentExpandButton(
                                expanded = true,
                                enabled = !posting,
                                onClick = onToggleExpanded
                            )
                            Text(
                                "写下你的评论",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            if (hasDraft && !posting) {
                                CommentToolButton(
                                    icon = Icons.Filled.Close,
                                    contentDescription = "清空评论",
                                    enabled = true,
                                    tint = toolTint,
                                    onClick = onClearDraft
                                )
                            }
                        }

                        ExpandedCommentTextField(
                            value = value,
                            onValueChange = onValueChange,
                            posting = posting,
                            focusRequester = focusRequester,
                            onFocusChanged = onFieldFocusChanged
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CommentToolButton(
                                icon = Icons.Filled.Image,
                                contentDescription = "添加评论图片",
                                enabled = !posting,
                                tint = toolTint,
                                onClick = onPickImages
                            )
                            CommentToolButton(
                                icon = Icons.Filled.AlternateEmail,
                                contentDescription = "提及好友",
                                enabled = !posting,
                                tint = toolTint,
                                onClick = onPickMention
                            )
                            CommentToolButton(
                                icon = Icons.Filled.Tag,
                                contentDescription = "添加话题",
                                enabled = !posting,
                                tint = toolTint,
                                onClick = onPickTopic
                            )
                            CommentToolButton(
                                icon = Icons.Filled.Mood,
                                contentDescription = "表情",
                                enabled = !posting,
                                tint = toolTint,
                                onClick = onPickSticker
                            )
                            Spacer(Modifier.weight(1f))
                            CommentSendButton(
                                canSubmit = canSubmit,
                                posting = posting,
                                onSubmit = onSubmit
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CommentExpandButton(
                            expanded = false,
                            enabled = !posting,
                            onClick = onToggleExpanded
                        )
                        CollapsedCommentField(
                            value = value,
                            posting = posting,
                            onExpand = onToggleExpanded,
                            modifier = Modifier.weight(1f)
                        )
                        CommentToolButton(
                            icon = Icons.Filled.Mood,
                            contentDescription = "表情",
                            enabled = !posting,
                            tint = toolTint,
                            onClick = onPickSticker
                        )
                    }
                }
            }
            if (notice != null) {
                Text(
                    notice,
                    color = if (notice.contains("失败") || notice.contains("无法")) MaterialTheme.colorScheme.error else LightPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 56.dp, top = 8.dp, end = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandedCommentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    posting: Boolean,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp, max = 184.dp),
        shape = RoundedCornerShape(22.dp),
        color = LightSurfaceVariant.copy(alpha = 0.34f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            LightSecondaryContainer.copy(alpha = 0.78f)
        )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = false,
            enabled = !posting,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 21.sp
            ),
            cursorBrush = SolidColor(LightSecondary),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp, max = 184.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            "写下你的评论",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun CollapsedCommentField(
    value: String,
    posting: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(22.dp),
        color = LightSurfaceVariant.copy(alpha = 0.22f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
        ),
        onClick = onExpand,
        enabled = !posting
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                value.ifBlank { "写下你的评论" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (value.isBlank()) 0.62f else 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CommentSendButton(
    canSubmit: Boolean,
    posting: Boolean,
    onSubmit: () -> Unit
) {
    val highlighted = canSubmit || posting
    Surface(
        onClick = onSubmit,
        enabled = canSubmit,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = if (highlighted) LightSecondary else LightSecondaryContainer.copy(alpha = 0.54f),
        contentColor = if (highlighted) LightOnPrimary else LightSecondary.copy(alpha = 0.52f),
        shadowElevation = if (highlighted) 7.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (posting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LightOnPrimary
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "提交评论",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CommentSelectionChip(text: String, enabled: Boolean, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = LightSecondaryContainer.copy(alpha = 0.62f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightSecondary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 6.dp, end = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = LightSecondary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.widthIn(max = 148.dp)
            )
            IconButton(
                onClick = onRemove,
                enabled = enabled,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@Composable
private fun CommentExpandButton(
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(if (expanded) 40.dp else 36.dp),
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = LightSecondaryContainer.copy(alpha = 0.86f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightSecondary.copy(alpha = 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            BeadCluster(if (expanded) 22.dp else 20.dp)
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "收起评论输入框" else "展开评论输入框",
                tint = LightSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), CircleShape)
                    .scale(scaleX = 1f, scaleY = if (expanded) -1f else 1f)
            )
        }
    }
}

@Composable
private fun CommentToolButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(34.dp)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.32f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PendingCommentImageChip(
    image: PendingCommentImage,
    posting: Boolean,
    onRemove: () -> Unit,
    onRetry: () -> Unit
) {
    val retryEnabled = shouldOfferCommentImageRetry(failed = image.failed, posting = posting)
    val retryLabel = commentImageRetryActionLabel(failed = image.failed, posting = posting)
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LightSecondaryContainer.copy(alpha = 0.78f), RoundedCornerShape(18.dp))
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
                    Icon(Icons.Filled.Image, contentDescription = null, tint = LightSecondary)
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
                    .then(
                        if (retryLabel != null) {
                            Modifier.semantics {
                                contentDescription = retryLabel
                                role = Role.Button
                            }
                        } else {
                            Modifier
                        }
                    )
                    .clickable(enabled = retryEnabled, onClick = onRetry)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.86f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("失败", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall)
                    Text(
                        retryLabel ?: "重试",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        IconButton(
            onClick = onRemove,
            enabled = !posting,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
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
        modifier = Modifier.size(68.dp),
        shape = RoundedCornerShape(18.dp),
        color = LightSecondaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightSecondary.copy(alpha = 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Add, contentDescription = "继续添加图片", tint = LightSecondary)
        }
    }
}

@Composable
private fun CommentMentionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (UserProfile) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var retry by remember { mutableIntStateOf(0) }
    val usersState = safeCallToState(query.trim(), retry) { repo.searchUsers(query.trim()) }.value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("@ 用户") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DoyuSearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "搜索昵称",
                    height = 48.dp
                )
                when (val state = usersState) {
                    is UiState.Success -> {
                        if (state.data.items.isEmpty()) {
                            EmptyContent(title = "没有匹配用户", message = "换一个关键词再试。", showRetry = false)
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 320.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.data.items, key = { it.userId }) { user ->
                                    CommentPickerRow(
                                        title = user.nickname.ifBlank { "豆屿用户" },
                                        subtitle = if (user.bio.isBlank()) "LV${user.level}" else user.bio,
                                        leading = "@",
                                        onClick = { onSelect(user) }
                                    )
                                }
                            }
                        }
                    }
                    else -> PageStateView(usersState, onRetry = { retry++ })
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun CommentTopicPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Topic) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var retry by remember { mutableIntStateOf(0) }
    val topicsState = safeCallToState(query.trim(), retry) { repo.topics(query.trim()) }.value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("# 话题") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DoyuSearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "搜索话题",
                    height = 48.dp
                )
                when (val state = topicsState) {
                    is UiState.Success -> {
                        if (state.data.items.isEmpty()) {
                            EmptyContent(title = "没有匹配话题", message = "换一个关键词再试。", showRetry = false)
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 320.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.data.items, key = { it.topicId }) { topic ->
                                    CommentPickerRow(
                                        title = topic.name.ifBlank { topic.topicId },
                                        subtitle = "${topic.postCount} 个作品",
                                        leading = "#",
                                        onClick = { onSelect(topic) }
                                    )
                                }
                            }
                        }
                    }
                    else -> PageStateView(topicsState, onRetry = { retry++ })
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun CommentStickerPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Sticker) -> Unit
) {
    var retry by remember { mutableIntStateOf(0) }
    val packsState = safeCallToState(retry) { repo.stickerPacks() }.value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("贴纸表情") },
        text = {
            when (val state = packsState) {
                is UiState.Success -> {
                    val packs = state.data.items
                    if (packs.isEmpty() || packs.all { it.stickers.isEmpty() }) {
                        EmptyContent(title = "暂无贴纸", message = "内置贴纸包还没有可用贴纸。", showRetry = false)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            packs.forEach { pack ->
                                item(key = pack.packId) {
                                    Text(pack.name, style = MaterialTheme.typography.titleSmall)
                                }
                                items(pack.stickers, key = { it.stickerId }) { sticker ->
                                    CommentPickerRow(
                                        title = sticker.name,
                                        subtitle = sticker.emojiText.ifBlank { "点击插入贴纸" },
                                        leading = sticker.emojiText.ifBlank { "贴" },
                                        onClick = { onSelect(sticker) }
                                    )
                                }
                            }
                        }
                    }
                }
                else -> PageStateView(packsState, onRetry = { retry++ })
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun CommentPickerRow(
    title: String,
    subtitle: String,
    leading: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = LightSurfaceVariant.copy(alpha = 0.54f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = LightSecondaryContainer.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        leading.take(2),
                        color = LightSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentListItem(comment: Comment) {
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
                }
                Spacer(Modifier.height(6.dp))
                if (comment.content.isNotBlank()) {
                    Text(comment.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (comment.mentions.isNotEmpty() || comment.topics.isNotEmpty() || comment.stickers.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    CommentAttachmentStrip(comment)
                }
                if (comment.mediaAssets.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    CommentMediaStrip(comment.mediaAssets)
                }
            }
        }
    }
}

@Composable
private fun CommentAttachmentStrip(comment: cn.edu.app.douyu.core.model.Comment) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(comment.mentions, key = { it.userId }) { mention ->
            InlineCommentChip("@${mention.nickname.ifBlank { mention.userId }}")
        }
        items(comment.topics, key = { it.topicId }) { topic ->
            InlineCommentChip("#${topic.name.ifBlank { topic.topicId }}")
        }
        items(comment.stickers, key = { it.stickerId }) { sticker ->
            InlineCommentChip(sticker.emojiText.ifBlank { sticker.name })
        }
    }
}

@Composable
private fun InlineCommentChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = LightSecondaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = LightSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .widthIn(max = 150.dp)
                .padding(horizontal = 9.dp, vertical = 5.dp)
        )
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
    onUploaded: (Uri, String) -> Unit,
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
                onUploaded(image.uri, file.fileId)
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
    ContentStatus.PRIVATE -> "私密"
    ContentStatus.REJECTED -> "审核未通过"
    ContentStatus.DELETED -> "已删除"
}
