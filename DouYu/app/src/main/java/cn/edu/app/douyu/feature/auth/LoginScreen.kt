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

private fun isValidPhone(phone: String): Boolean {
    return phone.matches(Regex("^1[3-9]\\d{9}$"))
}

@Composable
fun LoginScreen(navController: NavHostController) {
    LoginScreenContent(navController)
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(navController = null)
}

@Composable
fun LoginScreenContent(navController: NavHostController?) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var logging by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCodeDialog by remember { mutableStateOf(false) }
    var sentCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = { DoyuTopBar("手机号登录", canGoBack = true, onBack = { navController?.popBackStack() }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        DoyuPage(padding) {
            Text("登录后可以保存图纸、收藏作品、查看订单和同步生成记录。", style = MaterialTheme.typography.bodyLarge)

            DoyuCard {
                // 手机号输入
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(11) },
                    label = { Text("手机号") },
                    singleLine = true,
                    isError = phone.isNotEmpty() && !isValidPhone(phone),
                    supportingText = if (phone.isNotEmpty() && !isValidPhone(phone)) {
                        { Text("请输入正确的手机号") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 验证码输入 + 获取按钮
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
                                        // 尝试调用后端发送验证码
                                        runCatching {
                                            DoyuAppContainer.apiClient.authApi.sendSmsCode(SmsCodeRequest(phone))
                                        }.onSuccess {
                                            sentCode = "123456"
                                            showCodeDialog = true
                                        }.onFailure {
                                            error = ErrorMessages.fromException(it as Exception)
                                        }
                                        sending = false
                                    }
                                }
                            },
                            enabled = !sending && isValidPhone(phone)
                        ) {
                            Text(if (sending) "发送中..." else "获取验证码")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

                // 登录按钮
                DoyuPrimaryButton(
                    text = if (logging) "登录中..." else "登录并进入豆屿",
                    onClick = {
                        if (isValidPhone(phone) && code.length == 6 && !logging) {
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
                                }.onFailure {
                                    error = ErrorMessages.fromException(it as Exception)
                                }
                                logging = false
                            }
                        }
                    },
                    icon = Icons.AutoMirrored.Filled.Login,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isValidPhone(phone) && code.length == 6 && !logging
                )
            }

            // 错误提示
            error?.let {
                DoyuCard {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // 验证码弹窗
    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = { Text("验证码") },
            text = {
                Column {
                    Text("验证码已发送至 $phone")
                    Spacer(Modifier.height(8.dp))
                    Text("验证码：$sentCode", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("请手动输入验证码", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodeDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}
