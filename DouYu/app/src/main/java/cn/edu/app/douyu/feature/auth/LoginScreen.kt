package cn.edu.app.douyu.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.SmsCodeRequest
import cn.edu.app.douyu.core.model.SmsLoginRequest
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.ui.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    LoginScreenContent(navController)
}


@Preview
@Composable
private fun LoginScreenPreview(){
    LoginScreenContent(navController = null)
}

@Composable
fun LoginScreenContent(navController: NavHostController?) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var logging by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { DoyuTopBar("手机号登录", canGoBack = true, onBack = { navController?.popBackStack() }) }
    ) { padding ->
        DoyuPage(padding) {
            Text("登录后可以保存图纸、收藏作品、查看订单和同步生成记录。", style = MaterialTheme.typography.bodyLarge)
            DoyuCard {
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
                        TextButton(
                            onClick = {
                                if (phone.length == 11 && !sending) {
                                    sending = true
                                    error = null
                                    scope.launch {
                                        runCatching { DoyuAppContainer.apiClient.authApi.sendSmsCode(SmsCodeRequest(phone)) }
                                            .onFailure { error = it.message }
                                        sending = false
                                    }
                                }
                            },
                            enabled = !sending
                        ) { Text(if (sending) "发送中..." else "获取") }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                DoyuPrimaryButton(
                    text = if (logging) "登录中..." else "登录并进入豆屿",
                    onClick = {
                        if (phone.length == 11 && code.length == 6 && !logging) {
                            logging = true
                            error = null
                            scope.launch {
                                runCatching {
                                    DoyuAppContainer.authSessionManager.loginBySms(
                                        SmsLoginRequest(phone, code)
                                    )
                                }.onSuccess {
                                    navController?.navigate(BottomTab.PROFILE.route) {
                                        popUpTo(BottomTab.PROFILE.route) { inclusive = true }
                                    }
                                }.onFailure { error = it.message }
                                logging = false
                            }
                        }
                    },
                    icon = Icons.AutoMirrored.Filled.Login,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            error?.let {
                DoyuCard {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
            DoyuCard {
                Text("合规提示", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("注册登录时需要确认年龄段。16-17 岁用户会受到卖家发布和高额消费限制。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
