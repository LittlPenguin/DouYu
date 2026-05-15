package cn.edu.app.douyu.feature.message

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState

private val repo = DoyuAppContainer.messageRepository

@Preview
@Composable
private fun MessageListScreenPreview() { MessageListScreenContent(navController = null) }

@Composable
fun MessageListScreen(navController: NavHostController) { MessageListScreenContent(navController) }

@Composable
private fun MessageListScreenContent(navController: NavHostController?) {
    Scaffold(topBar = { DoyuTopBar("消息") }) { padding ->
        DoyuPage(padding) {
            SectionHeader("通知")
            val notificationsState = safeCallToState { repo.notifications() }
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
            val conversationsState = safeCallToState { repo.conversations() }
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

@Preview
@Composable
private fun ConversationScreenPreview() { ConversationScreenContent(navController = null, conversationId = "conv_001") }

@Composable
fun ConversationScreen(navController: NavHostController, conversationId: String) { ConversationScreenContent(navController, conversationId) }

@Composable
private fun ConversationScreenContent(navController: NavHostController?, conversationId: String) {
    Scaffold(topBar = { DoyuTopBar("会话", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            val chatState = safeCallToState { repo.chat(conversationId) }
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
