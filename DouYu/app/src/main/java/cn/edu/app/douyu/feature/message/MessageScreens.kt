package cn.edu.app.douyu.feature.message

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.ApiException
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.model.ChatMessage
import cn.edu.app.douyu.core.model.Conversation
import cn.edu.app.douyu.core.model.ConversationDetail
import cn.edu.app.douyu.core.model.NotificationMessage
import cn.edu.app.douyu.core.model.NotificationType
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.ui.theme.*
import kotlinx.coroutines.launch

private val repo = DoyuAppContainer.messageRepository

fun messageHomeTabLabels(): List<String> = listOf("私信", "通知")

fun messageHomeHeroCopy(): List<String> =
    listOf("新消息进入私信", "未读对话", "对话输入中、未互关剩余条数、禁发态只在私信里展示。")

fun messageHomePreviewSectionLabels(): List<String> = listOf("未读对话", "通知预览")

fun conversationStatusLabel(mutualFollow: Boolean, remaining: Int, canSend: Boolean): String = when {
    mutualFollow -> "互关"
    !canSend || remaining <= 0 -> "禁发"
    else -> "${remaining.coerceAtMost(3)}/3"
}

fun shouldBlockConversationAfterSendError(message: String): Boolean =
    message.contains("NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED")

fun shouldBlockConversationAfterSendError(error: Throwable): Boolean =
    (error as? ApiException)?.code == "NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED" ||
        error.message?.let(::shouldBlockConversationAfterSendError) == true

fun nonMutualLimitExceededMessage(): String =
    "未互关私信已达 3 条上限，互相关注后可继续聊天。"

fun conversationComposerCanSend(serverCanSend: Boolean?, blockedByLimit: Boolean): Boolean =
    serverCanSend != false && !blockedByLimit

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
    var refreshDrag by remember { mutableFloatStateOf(0f) }
    val notificationsState: UiState<PageResponse<NotificationMessage>> =
        safeCallToState(notificationsRetryCount) { repo.notifications() }.value
    val conversationsState: UiState<PageResponse<Conversation>> =
        safeCallToState(conversationsRetryCount) { repo.conversations() }.value
    val isUnauthenticated = notificationsState is UiState.RequireLogin || conversationsState is UiState.RequireLogin
    val currentRefreshing = when (selectedTab) {
        0 -> conversationsState is UiState.Loading
        else -> notificationsState is UiState.Loading
    }

    Scaffold(
        topBar = {
            DoyuMainTopBar(
                title = "消息",
                onSearch = { navController?.navigate(AppRoute.SEARCH) },
                onOpenSettings = { navController?.navigate(AppRoute.SETTINGS) },
                onOpenAi = { navController?.navigate(BottomTab.AI.route) },
                onCreatePost = { navController?.navigate(AppRoute.POST_CREATE) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            MessageTabsHeader(
                selectedTab = selectedTab,
                onSelected = { selectedTab = it }
            )
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentRefreshing, tab) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (refreshDrag > 90f && !currentRefreshing) {
                                            if (tab == 0) conversationsRetryCount++ else notificationsRetryCount++
                                        }
                                        refreshDrag = 0f
                                    },
                                    onDragCancel = { refreshDrag = 0f },
                                    onDrag = { change, dragAmount ->
                                        if (dragAmount.y > 0) refreshDrag += dragAmount.y
                                        change.consume()
                                    }
                                )
                            }
                    ) {
                        when (tab) {
                            0 -> MessageOverviewList(
                                conversationsState = conversationsState,
                                notificationsState = notificationsState,
                                navController = navController,
                                onConversationsRetry = { conversationsRetryCount++ },
                                onNotificationsRetry = { notificationsRetryCount++ }
                            )
                            1 -> NotificationList(
                                state = notificationsState,
                                navController = navController,
                                onRetry = { notificationsRetryCount++ }
                            )
                        }
                        if (currentRefreshing) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = LightPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageOverviewList(
    conversationsState: UiState<PageResponse<Conversation>>,
    notificationsState: UiState<PageResponse<NotificationMessage>>,
    navController: NavHostController?,
    onConversationsRetry: () -> Unit,
    onNotificationsRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MessageHomeHeroStrip()
        }
        item {
            val labels = messageHomePreviewSectionLabels()
            SectionHeader(
                title = labels[0],
                subtitle = "互关状态、剩余条数和禁发态只属于私信会话",
                action = "刷新",
                onAction = onConversationsRetry
            )
        }
        when (val cs = conversationsState) {
            is UiState.Success -> {
                val conversations = cs.data.items
                if (conversations.isEmpty()) {
                    item {
                        MessageInlineState("还没有私信", "有交易或社区沟通后，会话会显示在这里。")
                    }
                } else {
                    items(conversations.take(3), key = { "overview-${it.conversationId}" }) { conversation ->
                        ConversationRow(conversation, navController)
                    }
                }
            }

            is UiState.Empty -> item {
                MessageInlineState("还没有私信", "有交易或社区沟通后，会话会显示在这里。")
            }

            is UiState.Loading -> item {
                MessageInlineState("正在加载私信", "正在同步会话、未读和互关限制。")
            }

            is UiState.RequireLogin -> item {
                MessageInlineState("登录后查看私信", "登录后可查看会话、未读和互关限制。")
            }

            is UiState.Forbidden -> item {
                MessageInlineState("暂无私信权限", "当前账号暂时无法查看私信。")
            }

            is UiState.WeakNetwork -> item {
                MessageInlineState("私信加载较慢", "检查网络后可重试。", action = "重试", onAction = onConversationsRetry)
            }

            is UiState.Error -> item {
                MessageInlineState("私信加载失败", cs.message, action = "重试", onAction = onConversationsRetry)
            }
        }

        item {
            val labels = messageHomePreviewSectionLabels()
            SectionHeader(
                title = labels[1],
                subtitle = "通知只展示事件摘要，不承载输入状态",
                action = "刷新",
                onAction = onNotificationsRetry
            )
        }
        when (val ns = notificationsState) {
            is UiState.Success -> {
                val notifications = ns.data.items
                if (notifications.isEmpty()) {
                    item {
                        MessageInlineState("还没有通知", "订单、互动和系统提醒会显示在这里。")
                    }
                } else {
                    items(notifications.take(2), key = { "preview-${it.notificationId}" }) { notification ->
                        NotificationRow(notification, navController)
                    }
                }
            }

            is UiState.Empty -> item {
                MessageInlineState("还没有通知", "订单、互动和系统提醒会显示在这里。")
            }

            is UiState.Loading -> item {
                MessageInlineState("正在加载通知", "正在同步互动、审核和系统提醒。")
            }

            is UiState.RequireLogin -> item {
                MessageInlineState("登录后查看通知", "登录后可查看订单、互动和系统提醒。")
            }

            is UiState.Forbidden -> item {
                MessageInlineState("暂无通知权限", "当前账号暂时无法查看通知。")
            }

            is UiState.WeakNetwork -> item {
                MessageInlineState("通知加载较慢", "检查网络后可重试。", action = "重试", onAction = onNotificationsRetry)
            }

            is UiState.Error -> item {
                MessageInlineState("通知加载失败", ns.message, action = "重试", onAction = onNotificationsRetry)
            }
        }
    }
}

@Composable
private fun MessageHomeHeroStrip() {
    val copy = messageHomeHeroCopy()
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = LightTertiaryContainer,
            contentColor = LightTertiary
        ) {
            Text(
                text = copy[0],
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = copy[1],
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = copy[2],
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessageInlineState(
    title: String,
    subtitle: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    DoyuCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (action != null) {
                TextButton(onClick = onAction) {
                    Text(action)
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
                val labels = messageHomeTabLabels()
                MessageTab(labels[0], selectedTab == 0, onClick = { onSelected(0) })
                Spacer(Modifier.width(36.dp))
                MessageTab(labels[1], selectedTab == 1, onClick = { onSelected(1) })
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
    navController: NavHostController?,
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
                    NotificationRow(notification, navController)
                }
            }
        }
        is UiState.Empty -> EmptyContent("还没有通知", "订单、互动和系统提醒会显示在这里。", showRetry = false)
        else -> PageStateView(state, onRetry = onRetry)
    }
}

@Composable
private fun NotificationRow(notification: NotificationMessage, navController: NavHostController?) {
    val visual = notificationVisual(notification.type)
    val route = AppRoute.notificationDetail(
        notificationId = notification.notificationId,
        title = notification.title,
        content = notification.content,
        type = notification.type.name,
        createdAt = notification.createdAt.orEmpty()
    )
    Surface(
        onClick = { navController?.navigate(route) },
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
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "查看通知详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun NotificationDetailScreen(
    navController: NavHostController,
    notificationId: String,
    title: String,
    content: String,
    type: String,
    createdAt: String
) {
    Scaffold(
        topBar = {
            DoyuTopBar("通知详情", canGoBack = true, onBack = { navController.popBackStack() })
        }
    ) { padding ->
        DoyuPage(padding) {
            DoyuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = title.ifBlank { "通知" },
                    subtitle = notificationDetailSubtitle(type, createdAt)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    content.ifBlank { "当前通知没有更多内容。" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                NotificationMetaRow(
                    icon = Icons.Filled.Notifications,
                    title = "通知编号",
                    description = notificationId.ifBlank { "未返回通知编号" },
                    status = "列表内详情"
                )
            }
            DisabledFeatureNotice(
                title = "详情来源说明",
                message = "当前没有独立通知详情后端接口，本页只展示列表已返回的通知数据；后续如需要已读回写、跳转目标或富文本内容，需要先补接口契约。"
            )
        }
    }
}

@Composable
private fun NotificationMetaRow(
    icon: ImageVector,
    title: String,
    description: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(LightSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun notificationDetailSubtitle(type: String, createdAt: String): String =
    listOfNotNull(
        type.takeIf { it.isNotBlank() }?.let { "类型 $it" },
        createdAt.takeIf { it.isNotBlank() }?.let { "时间 ${compactTimeLabel(it)}" }
    ).joinToString(" · ").ifBlank { "来自通知列表的详情" }

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
                    ConversationStatusBadge(conversation)
                }
                Text(
                    conversationPreviewSubtitle(conversation),
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
private fun ConversationStatusBadge(conversation: Conversation) {
    val label = conversationStatusLabel(
        mutualFollow = conversation.mutualFollow,
        remaining = conversation.remainingNonMutualMessages,
        canSend = conversation.canSend
    )
    val color = when (label) {
        "互关" -> LightPrimaryContainer
        "禁发" -> MaterialTheme.colorScheme.errorContainer
        else -> LightTertiaryContainer
    }
    val contentColor = when (label) {
        "互关" -> LightOnPrimaryContainer
        "禁发" -> MaterialTheme.colorScheme.onErrorContainer
        else -> LightTertiary
    }
    Surface(
        shape = CircleShape,
        color = color,
        contentColor = contentColor
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun conversationPreviewSubtitle(conversation: Conversation): String {
    val status = when {
        conversation.mutualFollow -> "互相关注"
        !conversation.canSend || conversation.remainingNonMutualMessages <= 0 -> "超过 3 条，互相关注后可继续聊天"
        else -> "未互关剩余 ${conversation.remainingNonMutualMessages.coerceAtMost(3)} 条"
    }
    return conversation.lastMessage
        .takeIf { it.isNotBlank() }
        ?.let { "$status · $it" }
        ?: status
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
            onClick = { navController?.navigate(AppRoute.login(BottomTab.MESSAGE.route)) },
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
            var refreshKey by remember { mutableIntStateOf(0) }
            var input by remember { mutableStateOf("") }
            var sendState by remember { mutableStateOf<UiState<ChatMessage>?>(null) }
            var sendBlocked by remember { mutableStateOf(false) }
            val sendScope = rememberCoroutineScope()
            val detailState: UiState<ConversationDetail> =
                safeCallToState(conversationId, refreshKey) { repo.conversation(conversationId) }.value
            when (val cs = detailState) {
                is UiState.Success -> {
                    val detail = cs.data
                    val conversation = detail.conversation
                    val messages = detail.messages
                    if (messages.isEmpty()) {
                        EmptyContent("还没有会话内容", "这条私信会话目前没有历史消息。", showRetry = false)
                    } else {
                        messages.forEach { ChatBubble(it) }
                    }
                    ConversationComposer(
                        conversation = conversation,
                        input = input,
                        sendState = sendState,
                        blockedByLimit = sendBlocked,
                        onInputChange = { input = it },
                        onSend = {
                            val content = input.trim()
                            if (content.isBlank()) return@ConversationComposer
                            sendState = UiState.Loading
                            sendScope.launch {
                                sendState = runCatching { repo.sendMessage(conversationId, content) }
                                    .fold(
                                        onSuccess = {
                                            input = ""
                                            sendBlocked = false
                                            refreshKey++
                                            UiState.Success(it)
                                        },
                                        onFailure = {
                                            val message = it.message ?: "发送失败"
                                            if (shouldBlockConversationAfterSendError(it)) {
                                                sendBlocked = true
                                                refreshKey++
                                                UiState.Error(nonMutualLimitExceededMessage())
                                            } else {
                                                UiState.Error(message)
                                            }
                                        }
                                    )
                            }
                        }
                    )
                }
                is UiState.Empty -> PageStateView(UiState.Empty)
                else -> PageStateView(detailState, onRetry = { refreshKey++ })
            }
        }
    }
}

@Composable
private fun ConversationComposer(
    conversation: Conversation?,
    input: String,
    sendState: UiState<ChatMessage>?,
    blockedByLimit: Boolean = false,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val canSend = conversationComposerCanSend(conversation?.canSend, blockedByLimit)
    val helper = when {
        conversation == null -> "正在确认会话关系"
        conversation.mutualFollow -> "你们已互相关注，可以继续交流。"
        conversation.remainingNonMutualMessages > 0 -> "未互关还可发送 ${conversation.remainingNonMutualMessages} 条消息。"
        else -> "互相关注后可继续聊天。"
    }
    DoyuCard {
        Text(helper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            enabled = canSend && sendState !is UiState.Loading,
            label = { Text(if (canSend) "输入私信" else "已达未互关私信上限") },
            trailingIcon = {
                IconButton(onClick = onSend, enabled = canSend && input.isNotBlank() && sendState !is UiState.Loading) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            maxLines = 4
        )
        if (sendState is UiState.Error) {
            Spacer(Modifier.height(6.dp))
            Text(sendState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
    NotificationType.MENTION,
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
    conversation.peerName.ifBlank {
        if (conversation.userBId.contains("support", ignoreCase = true)) {
            "豆屿客服"
        } else {
            "拼豆同好 ${conversation.userBId.takeLast(3).uppercase()}"
        }
    }
