package cn.edu.app.douyu.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.ui.*

@Composable
fun LoginScreen(navController: NavHostController) {
    Scaffold(
        topBar = { DoyuTopBar("手机号登录", canGoBack = true, onBack = { navController.popBackStack() }) }
    ) { padding ->
        DoyuPage(padding) {
            Text("登录后可以保存图纸、收藏作品、查看订单和同步生成记录。", style = MaterialTheme.typography.bodyLarge)
            DoyuCard {
                var phone by remember { mutableStateOf("") }
                var code by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(11) },
                    label = { Text("手机号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.take(6) },
                    label = { Text("验证码") },
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = {}) { Text("获取") }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                DoyuPrimaryButton(
                    text = "登录并进入豆屿",
                    onClick = { navController.navigate(BottomTab.PROFILE.route) },
                    icon = Icons.AutoMirrored.Filled.Login,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DoyuCard {
                Text("合规提示", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("注册登录时需要确认年龄段。16-17 岁用户会受到卖家发布和高额消费限制。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
