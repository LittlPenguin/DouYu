package cn.edu.app.douyu.feature.message

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.ui.theme.*

private val repo = DoyuAppContainer.messageRepository

@Preview
@Composable
private fun MessageListScreenPreview() { MessageListScreenContent(navController = null) }

@Composable
fun MessageListScreen(navController: NavHostController) { MessageListScreenContent(navController) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageListScreenContent(navController: NavHostController?) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var notificationsRetryCount by remember { mutableIntStateOf(0) }
    var conversationsRetryCount by remember { mutableIntStateOf(0) }
    val notificationsState = safeCallToState(notificationsRetryCount) { repo.notifications() }.value
    val conversationsState = safeCallToState(conversationsRetryCount) { repo.conversations() }.value
    val isUnauthenticated = notificationsState is UiState.RequireLogin || conversationsState is UiState.RequireLogin

    Scaffold(
        topBar = {
            DoyuTopBar("消息") {
                DoyuSegmentedControl(
                    options = listOf("通知", "私信"),
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it },
                    modifier = Modifier.width(164.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isUnauthenticated) {
                MessageLoginPrompt(navController)
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "tabSwitch"
                ) { tab ->
                    when (tab) {
                        0 -> NotificationList(notificationsState, onRetry = { notificationsRetryCount++ })
                        1 -> ConversationList(conversationsState, navController, onRetry = { conversationsRetryCount++ })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationList(state: UiState<*>, onRetry: () -> Unit = {}) {
    when (val ns = state) {
        is UiState.Success -> {
            val items = (ns.data as? PageResponse<cn.edu.app.douyu.core.model.NotificationMessage>)?.items.orEmpty()
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items.forEach { notification ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (notification.type) {
                                            cn.edu.app.douyu.core.model.NotificationType.SYSTEM -> LightTertiaryContainer
                                            cn.edu.app.douyu.core.model.NotificationType.ORDER -> LightSecondaryContainer
                                            else -> LightPrimaryContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (notification.type) {
                                        cn.edu.app.douyu.core.model.NotificationType.SYSTEM -> Icons.Filled.Notifications
                                        cn.edu.app.douyu.core.model.NotificationType.ORDER -> Icons.Filled.ShoppingBag
                                        else -> Icons.Filled.Favorite
                                    },
                                    contentDescription = null,
                                    tint = when (notification.type) {
                                        cn.edu.app.douyu.core.model.NotificationType.SYSTEM -> LightOnTertiaryContainer
                                        cn.edu.app.douyu.core.model.NotificationType.ORDER -> LightOnSecondaryContainer
                                        else -> LightOnPrimaryContainer
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))

                            // Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    notification.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    notification.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            // Unread indicator
                            if (notification.unread) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LightPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
        is UiState.Empty -> EmptyContent("还没有消息", "去社区看看吧", showRetry = false)
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun ConversationList(state: UiState<*>, navController: NavHostController?, onRetry: () -> Unit = {}) {
    when (val cs = state) {
        is UiState.Success -> {
            val items = (cs.data as? PageResponse<cn.edu.app.douyu.core.model.Conversation>)?.items.orEmpty()
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items.forEach { conv ->
                    Surface(
                        onClick = { navController?.navigate(AppRoute.conversation(conv.conversationId)) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(LightSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(12.dp))

                            // Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "会话 ${conv.conversationId}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${conv.userAId} ↔ ${conv.userBId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        is UiState.Empty -> EmptyContent("还没有私信", "去社区看看吧", showRetry = false)
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun MessageLoginPrompt(navController: NavHostController?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BeadPattern(
            modifier = Modifier
                .size(96.dp)
                .padding(12.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text("登录后查看消息", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "接收订单更新、互动通知和卖家私信，不错过每一笔交易动态。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        DoyuPrimaryButton(
            "去登录",
            onClick = { navController?.navigate(AppRoute.LOGIN) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        DoyuCard {
            SectionHeader("通知")
            MessagePlaceholderRow(Icons.Filled.Notifications, "系统与订单通知", "发货提醒、签收确认、系统公告")
            MessagePlaceholderRow(Icons.Filled.Notifications, "互动消息", "点赞、评论、关注提醒")
        }
        DoyuCard {
            SectionHeader("私信")
            MessagePlaceholderRow(Icons.Filled.MailOutline, "卖家与买家对话", "交易沟通、定制需求协商")
            MessagePlaceholderRow(Icons.Filled.MailOutline, "社区好友私信", "拼豆爱好者之间的交流")
        }
    }
}

@Composable
private fun MessagePlaceholderRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

// ── ConversationScreen ──

@Preview
@Composable
private fun ConversationScreenPreview() { ConversationScreenContent(navController = null, conversationId = "conv_001") }

@Composable
fun ConversationScreen(navController: NavHostController, conversationId: String) { ConversationScreenContent(navController, conversationId) }

@Composable
private fun ConversationScreenContent(navController: NavHostController?, conversationId: String) {
    Scaffold(topBar = { DoyuTopBar("会话", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            val chatState = safeCallToState(conversationId) { repo.chat(conversationId) }.value
            when (val cs = chatState) {
                is UiState.Success -> cs.data.items.forEach {
                    val isMine = it.mine
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isMine) LightPrimaryContainer else LightSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(if (isMine) 0.7f else 0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    it.senderName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isMine) LightOnPrimaryContainer else LightPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    it.content,
                                    color = if (isMine) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                is UiState.Empty -> PageStateView(UiState.Empty)
                else -> PageStateView(chatState)
            }
            DoyuCard {
                DisabledFeatureNotice(
                    title = "私信发送待接入",
                    message = "当前会话只读展示，发送和举报入口会在后端链路确认后开放。",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("输入私信") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DoyuOutlinedButton(
                        "举报",
                        onClick = ::disabledClick,
                        enabled = false,
                        icon = Icons.Filled.Report,
                        modifier = Modifier.weight(1f)
                    )
                    DoyuPrimaryButton(
                        "发送",
                        onClick = ::disabledClick,
                        enabled = false,
                        icon = Icons.AutoMirrored.Filled.Send,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
