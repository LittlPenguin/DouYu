package cn.edu.app.douyu.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.Badge
import cn.edu.app.douyu.core.model.CheckinStatus
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.data.safeCallOrNull

private val repo = DoyuAppContainer.profileRepository

@Preview
@Composable
private fun ProfileScreenPreview() { ProfileScreenContent(navController = null) }

@Composable
fun ProfileScreen(navController: NavHostController) { ProfileScreenContent(navController) }

@Composable
private fun ProfileScreenContent(navController: NavHostController?) {
    val dashboardState = safeCallToState { repo.dashboard() }.value
    Scaffold(
        topBar = {
            DoyuTopBar("我的", action = {
                IconButton(onClick = { navController?.navigate(AppRoute.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "设置")
                }
            })
        }
    ) { padding ->
        DoyuPage(padding) {
            when (val state = dashboardState) {
                is UiState.Success -> {
                    val dashboard = state.data
                    DoyuCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BeadCluster(58.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(dashboard.user.nickname, style = MaterialTheme.typography.headlineSmall)
                                Text(dashboard.user.bio, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("${dashboard.reward.levelCode} · ${dashboard.reward.points} 积分 · ${dashboard.reward.experience} 经验", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("作品", "${dashboard.user.followerCount}", Modifier.weight(1f))
                        MetricCard("图纸", "${dashboard.patternCount}", Modifier.weight(1f))
                        MetricCard("订单", "${dashboard.orderCount}", Modifier.weight(1f))
                    }
                }
                UiState.RequireLogin -> {
                    PageStateView(UiState.RequireLogin)
                }
                else -> PageStateView(dashboardState)
            }
            DoyuCard {
                SectionHeader("我的资产")
                ProfileAction("我的拼豆", Icons.Filled.GridView) { navController?.navigate(AppRoute.MY_PATTERNS) }
                ProfileAction("生成记录", Icons.Filled.AutoAwesome) { navController?.navigate(AppRoute.PATTERN_HISTORY) }
                ProfileAction("我的订单", Icons.AutoMirrored.Filled.ReceiptLong) { }
                ProfileAction("签到与等级", Icons.Filled.WorkspacePremium) { }
            }
            DoyuCard {
                var checkin by remember { mutableStateOf<CheckinStatus?>(null) }
                var badges by remember { mutableStateOf<List<Badge>>(emptyList()) }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        checkin = safeCallOrNull { repo.checkinStatus() }
                        badges = safeCallOrNull { repo.badges() } ?: emptyList()
                    }
                }
                SectionHeader("签到与徽章")
                Text("今日签到：${if (checkin?.checkedToday == true) "已签到" else "未签到"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                badges.forEach {
                    Text("${it.name} · ${if (it.achieved) "已获得" else "未获得"}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            DoyuOutlinedButton("登录页占位", onClick = { navController?.navigate(AppRoute.LOGIN) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    DoyuCard(modifier = modifier, contentPadding = PaddingValues(12.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileAction(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@Preview
@Composable
private fun MyPatternsScreenPreview() { MyPatternsScreenContent(navController = null) }

@Composable
fun MyPatternsScreen(navController: NavHostController) { MyPatternsScreenContent(navController) }

@Composable
private fun MyPatternsScreenContent(navController: NavHostController?) {
    val patternsState = safeCallToState { repo.patterns() }.value
    Scaffold(topBar = { DoyuTopBar("我的拼豆", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = patternsState) {
                is UiState.Success -> state.data.forEach {
                    DoyuCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BeadPattern(Modifier.size(58.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(it.title, style = MaterialTheme.typography.titleMedium)
                                Text("${it.patternId} · ${it.widthCells} x ${it.heightCells} · ${it.totalBeads} 颗", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                else -> PageStateView(patternsState)
            }
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() { SettingsScreenContent(navController = null) }

@Composable
fun SettingsScreen(navController: NavHostController) { SettingsScreenContent(navController) }

@Composable
private fun SettingsScreenContent(navController: NavHostController?) {
    Scaffold(topBar = { DoyuTopBar("设置", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                SectionHeader("合规入口")
                listOf("隐私政策", "用户协议", "权限说明", "第三方 SDK 清单", "账号注销", "客服与反馈").forEach {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            }
            DoyuCard {
                Text("权限策略", style = MaterialTheme.typography.titleMedium)
                Text("相机仅在拍照时申请；相册优先使用 Photo Picker；不默认申请定位、蓝牙、Wi-Fi 或广泛存储权限。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuOutlinedButton("退出登录", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
