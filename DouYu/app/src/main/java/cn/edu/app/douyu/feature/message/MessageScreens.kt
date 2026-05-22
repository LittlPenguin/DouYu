package cn.edu.app.douyu.feature.message

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.ChatMessage
import cn.edu.app.douyu.core.model.Conversation
import cn.edu.app.douyu.core.model.NotificationMessage
import cn.edu.app.douyu.core.model.NotificationType
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.ui.theme.*

private val repo = DoyuAppContainer.messageRepository

@Preview
@Composable
private fun MessageListScreenPreview() { MessageListScreenContent(navController = null) }

@Composable
fun MessageListScreen(navController: NavHostController) { MessageListScreenContent(navController) }

@Composable
private fun MessageListScreenContent(navController: NavHostController?) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var notificationsRetryCount by remember { mutableIntStateOf(0) }
    var conversationsRetryCount by remember { mutableIntStateOf(0) }
    val notificationsState: UiState<PageResponse<NotificationMessage>> =
        safeCallToState(notificationsRetryCount) { repo.notifications() }.value
    val conversationsState: UiState<PageResponse<Conversation>> =
        safeCallToState(conversationsRetryCount) { repo.conversations() }.value
    val isUnauthenticated = notificationsState is UiState.RequireLogin || conversationsState is UiState.RequireLogin

    Scaffold(
        topBar = {
            MessageTabsHeader(
                selectedTab = selectedTab,
                onSelected = { selectedTab = it }
            )
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
private fun MessageTabsHeader(selectedTab: Int, onSelected: (Int) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MessageTab("通知", selectedTab == 0, onClick = { onSelected(0) })
                Spacer(Modifier.width(36.dp))
                MessageTab("私信", selectedTab == 1, onClick = { onSelected(1) })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
        }
    }
}

@Composable
private fun MessageTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clickable(onClick = onClick)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (selected) LightPrimary else Color.Transparent)
        )
    }
}

@Composable
private fun NotificationList(
    state: UiState<PageResponse<NotificationMessage>>,
    onRetry: () -> Unit = {}
) {
    when (val ns = state) {
        is UiState.Success -> {
            val items = ns.data.items
            if (items.isEmpty()) {
                EmptyContent("还没有通知", "订单、互动和系统提醒会显示在这里。", showRetry = false)
                return
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.notificationId }) { notification ->
                    NotificationRow(notification)
                }
            }
        }
        is UiState.Empty -> EmptyContent("还没有通知", "订单、互动和系统提醒会显示在这里。", showRetry = false)
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun NotificationRow(notification: NotificationMessage) {
    val visual = notificationVisual(notification.type)
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (notification.unread) 1.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 78.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(58.dp)
                    .clip(CircleShape)
                    .background(if (notification.unread) LightPrimary else Color.Transparent)
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(visual.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    visual.icon,
                    contentDescription = null,
                    tint = visual.contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    notification.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                notificationTimeLabel(notification),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ConversationList(
    state: UiState<PageResponse<Conversation>>,
    navController: NavHostController?,
    onRetry: () -> Unit = {}
) {
    when (val cs = state) {
        is UiState.Success -> {
            val items = cs.data.items
            if (items.isEmpty()) {
                EmptyContent("还没有私信", "有交易或社区沟通后，会话会显示在这里。", showRetry = false)
                return
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.conversationId }) { conversation ->
                    ConversationRow(conversation, navController)
                }
            }
        }
        is UiState.Empty -> EmptyContent("还没有私信", "有交易或社区沟通后，会话会显示在这里。", showRetry = false)
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, navController: NavHostController?) {
    val openConversation = navController?.let { controller ->
        { controller.navigate(AppRoute.conversation(conversation.conversationId)) }
    }
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 78.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conversationTitle(conversation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    ReadOnlyBadge()
                }
                Text(
                    "只读会话，进入查看历史消息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    conversationTimeLabel(conversation.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (openConversation != null) {
        Surface(
            onClick = openConversation,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    } else {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun ReadOnlyBadge() {
    Surface(
        shape = CircleShape,
        color = LightSurfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            "只读",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun MessageLoginPrompt(navController: NavHostController?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BeadPattern(
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("登录后查看消息", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            "接收订单更新、互动通知和卖家私信，不错过每一笔交易动态。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        DoyuPrimaryButton(
            "去登录",
            onClick = { navController?.navigate(AppRoute.LOGIN) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        DoyuCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            SectionHeader("通知")
            MessagePlaceholderRow(Icons.Filled.Notifications, "系统与订单通知", "发货提醒、签收确认、系统公告")
            MessagePlaceholderRow(Icons.Filled.Notifications, "互动消息", "点赞、评论、关注提醒")
        }
        Spacer(Modifier.height(12.dp))
        DoyuCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
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
            .heightIn(min = 52.dp)
            .padding(vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
            val chatState: UiState<PageResponse<ChatMessage>> =
                safeCallToState(conversationId) { repo.chat(conversationId) }.value
            when (val cs = chatState) {
                is UiState.Success -> {
                    if (cs.data.items.isEmpty()) {
                        EmptyContent("还没有会话内容", "这条私信会话目前没有历史消息。", showRetry = false)
                    } else {
                        cs.data.items.forEach { ChatBubble(it) }
                    }
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
                    label = { Text("私信发送未开放") },
                    trailingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isMine = message.mine
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (isMine) LightPrimaryContainer else LightSurfaceVariant,
            modifier = Modifier.fillMaxWidth(if (isMine) 0.72f else 0.86f)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    message.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isMine) LightOnPrimaryContainer else LightPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMine) LightOnPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private data class NotificationVisual(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

private fun notificationVisual(type: NotificationType): NotificationVisual = when (type) {
    NotificationType.ORDER -> NotificationVisual(Icons.Filled.ShoppingBag, LightSecondaryContainer, LightOnSecondaryContainer)
    NotificationType.COMMENT,
    NotificationType.LIKE,
    NotificationType.FAVORITE,
    NotificationType.FOLLOW -> NotificationVisual(Icons.Filled.Favorite, LightPrimaryContainer, LightOnPrimaryContainer)
    NotificationType.SYSTEM,
    NotificationType.AI_TASK,
    NotificationType.REPORT -> NotificationVisual(Icons.Filled.Notifications, LightTertiaryContainer, LightOnTertiaryContainer)
}

private fun notificationTimeLabel(notification: NotificationMessage): String =
    notification.createdAt?.let(::compactTimeLabel) ?: if (notification.unread) "刚刚" else "昨天"

private fun conversationTimeLabel(updatedAt: String?): String =
    updatedAt?.let(::compactTimeLabel) ?: "最近"

private fun compactTimeLabel(value: String): String {
    val dateTimeSeparator = value.indexOf('T')
    if (dateTimeSeparator >= 0 && value.length >= dateTimeSeparator + 6) {
        return value.substring(dateTimeSeparator + 1, dateTimeSeparator + 6)
    }
    if (value.length >= 10) return value.substring(5, 10).replace('-', '/')
    return value
}

private fun conversationTitle(conversation: Conversation): String =
    if (conversation.userBId.contains("support", ignoreCase = true)) {
        "豆屿客服"
    } else {
        "拼豆同好 ${conversation.userBId.takeLast(3).uppercase()}"
    }
