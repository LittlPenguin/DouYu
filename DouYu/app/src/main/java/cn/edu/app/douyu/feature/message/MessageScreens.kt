package cn.edu.app.douyu.feature.message

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.MockMessageRepository
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*

private val repo = MockMessageRepository()

@Composable
fun MessageListScreen(navController: NavHostController) {
    Scaffold(topBar = { DoyuTopBar("消息") }) { padding ->
        DoyuPage(padding) {
            SectionHeader("通知")
            repo.notifications().items.forEach {
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
            SectionHeader("私信")
            repo.conversations().items.forEach {
                DoyuCard {
                    Surface(onClick = { navController.navigate(AppRoute.conversation(it.conversationId)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BeadCluster(38.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(it.peerName, style = MaterialTheme.typography.titleMedium)
                                Text(it.lastMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (it.unreadCount > 0) TagChip(it.unreadCount.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationScreen(navController: NavHostController, conversationId: String) {
    val conversation = repo.conversations().items.firstOrNull { it.conversationId == conversationId }
    Scaffold(topBar = { DoyuTopBar(conversation?.peerName ?: "会话", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            conversation?.riskHint?.let {
                DoyuCard {
                    Text("风险提示", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            repo.chat(conversationId).items.forEach {
                DoyuCard(modifier = Modifier.fillMaxWidth(if (it.mine) 0.86f else 1f)) {
                    Text(it.senderName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(it.content)
                }
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
