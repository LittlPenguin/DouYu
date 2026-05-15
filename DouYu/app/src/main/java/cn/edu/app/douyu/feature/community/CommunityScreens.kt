package cn.edu.app.douyu.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState

private val repo = DoyuAppContainer.communityRepository


@Preview
@Composable
private fun CommunityFeedScreenPreview() { CommunityFeedScreenContent(navController = null) }

@Composable
fun CommunityFeedScreen(navController: NavHostController) { CommunityFeedScreenContent(navController) }

@Composable
private fun CommunityFeedScreenContent(navController: NavHostController?) {
    Scaffold(
        topBar = {
            DoyuTopBar("豆屿", action = {
                IconButton(onClick = { navController?.navigate(AppRoute.POST_CREATE) }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "发帖")
                }
            })
        }
    ) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("把喜欢拼成圈子", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text("今日热门：宠物头像、生日礼物、新手低色数图纸", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    BeadCluster(48.dp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TagChip("推荐")
                TagChip("关注", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f))
                TagChip("新手教程", color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f))
            }
            val feedState = safeCallToState { repo.feed() }
            when (val state = feedState) {
                is UiState.Success -> state.data.items.forEach { post ->
                    PostCard(post, onClick = { navController?.navigate(AppRoute.postDetail(post.postId)) })
                }
                is UiState.Empty -> EmptyContent(
                    "还没有帖子",
                    "去发一条吧，成为豆屿第一位分享者！",
                    showRetry = false
                )
                else -> PageStateView(feedState)
            }
            PageStatePreviewRow()
        }
    }
}

@Composable
private fun PostCard(post: Post, onClick: () -> Unit) {
    DoyuCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BeadCluster(38.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(post.author.nickname, style = MaterialTheme.typography.titleMedium)
                Text(post.topicNames.joinToString(prefix = "#", separator = " #"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            if (post.status == ContentStatus.REVIEWING) {
                TagChip("审核中", color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(Modifier.padding(14.dp)) {
                ColorSwatchStrip(post.mediaColors)
                Spacer(Modifier.height(12.dp))
                Text(post.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(post.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconText(Icons.Filled.FavoriteBorder, post.likeCount.toString())
            IconText(Icons.Filled.BookmarkBorder, post.favoriteCount.toString())
            IconText(Icons.Filled.ChatBubbleOutline, post.commentCount.toString())
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

@Composable
private fun PageStatePreviewRow() {
    DoyuCard {
        Text("页面状态组件", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("空", "失败", "弱网", "未登录").forEach {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(it, fontWeight = FontWeight.Medium)
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
    val postState = safeCallToState { repo.post(postId) }
    Scaffold(topBar = { DoyuTopBar("作品详情", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = postState) {
                is UiState.Success -> {
                    PostCard(state.data, onClick = {})
                    SectionHeader("评论")
                    val commentsState = safeCallToState { repo.comments(postId) }
                    when (val cs = commentsState) {
                        is UiState.Success -> cs.data.items.forEach {
                            DoyuCard {
                                Text(it.author.nickname, style = MaterialTheme.typography.titleMedium)
                                Text(it.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is UiState.Empty -> PageStateView(UiState.Empty)
                        else -> PageStateView(commentsState)
                    }
                    DoyuPrimaryButton("收藏图纸", onClick = { navController?.navigate(AppRoute.patternResult(state.data.linkedPatternId ?: "pattern_001")) }, modifier = Modifier.fillMaxWidth())
                    DoyuOutlinedButton("举报内容", onClick = {}, icon = Icons.Filled.Report, modifier = Modifier.fillMaxWidth())
                }
                else -> PageStateView(postState)
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
    Scaffold(topBar = { DoyuTopBar("发布作品", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("标题") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("正文、教程或踩坑经验") }, minLines = 5, modifier = Modifier.fillMaxWidth())
            }
            DoyuCard {
                SectionHeader("图片与图纸")
                DoyuOutlinedButton("添加图片并确认 fileId", onClick = { navController?.navigate(AppRoute.IMAGE_SELECT) }, icon = Icons.Filled.AddPhotoAlternate, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Text("发布接口使用 mediaFileIds；图片先走 /uploads/presign、直传、/uploads/confirm，发布后可能进入审核中。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuPrimaryButton("提交发布", onClick = {}, modifier = Modifier.fillMaxWidth())
            PageStateView(UiState.Reviewing)
        }
    }
}
