package cn.edu.app.douyu.feature.message

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.ui.theme.DoyuTextMuted

private val repo = DoyuAppContainer.messageRepository

@Preview
@Composable
private fun MessageListScreenPreview() { MessageListScreenContent(navController = null) }

@Composable
fun MessageListScreen(navController: NavHostController) { MessageListScreenContent(navController) }

@Composable
private fun MessageListScreenContent(navController: NavHostController?) {
    val notificationsState = safeCallToState { repo.notifications() }.value
    val conversationsState = safeCallToState { repo.conversations() }.value
    val isUnauthenticated = notificationsState is UiState.RequireLogin || conversationsState is UiState.RequireLogin

    Scaffold(topBar = { DoyuTopBar("消息") }) { padding ->
        DoyuPage(padding) {
            if (isUnauthenticated) {
                MessageLoginPrompt(navController)
            } else {
                SectionHeader("通知")
                when (val ns = notificationsState) {
                    is UiState.Success -> ns.data.items.forEach {
                        DoyuCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(it.title, style = MaterialTheme.typography.titleMedium)
                                    Text(it.content, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (it.unread) BeadDot(MaterialTheme.colorScheme.primary, size = 12.dp)
                            }
                        }
                    }
                    is UiState.Empty -> PageStateView(UiState.Empty)
                    else -> PageStateView(notificationsState)
                }
                SectionHeader("私信")
                when (val cs = conversationsState) {
                    is UiState.Success -> cs.data.items.forEach {
                        DoyuCard {
                            Surface(onClick = { navController?.navigate(AppRoute.conversation(it.conversationId)) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BeadCluster(38.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("会话 ${it.conversationId}", style = MaterialTheme.typography.titleMedium)
                                        Text("${it.userAId} ↔ ${it.userBId}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Empty -> PageStateView(UiState.Empty)
                    else -> PageStateView(conversationsState)
                }
            }
        }
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
            color = DoyuTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        DoyuPrimaryButton(
            "去登录",
            onClick = { navController?.navigate(AppRoute.LOGIN) },
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.height(24.dp))
    DoyuCard {
        SectionHeader("通知")
        MessagePlaceholderRow(Icons.Filled.Notifications, "系统与订单通知", "发货提醒、签收确认、审核结果")
        MessagePlaceholderRow(Icons.Filled.Notifications, "互动消息", "点赞、评论、关注提醒")
    }
    DoyuCard {
        SectionHeader("私信")
        MessagePlaceholderRow(Icons.Filled.MailOutline, "卖家与买家对话", "交易沟通、定制需求协商")
        MessagePlaceholderRow(Icons.Filled.MailOutline, "社区好友私信", "拼豆爱好者之间的交流")
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
        Icon(icon, contentDescription = null, tint = DoyuTextMuted, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = DoyuTextMuted)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DoyuTextMuted.copy(alpha = 0.6f))
        }
    }
}

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
                    DoyuCard(modifier = Modifier.fillMaxWidth(if (it.mine) 0.86f else 1f)) {
                        Text(it.senderName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(it.content)
                    }
                }
                is UiState.Empty -> PageStateView(UiState.Empty)
                else -> PageStateView(chatState)
            }
            DoyuCard {
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("输入私信") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DoyuOutlinedButton("举报", onClick = {}, icon = Icons.Filled.Report, modifier = Modifier.weight(1f))
                    DoyuPrimaryButton("发送", onClick = {}, icon = Icons.AutoMirrored.Filled.Send, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
