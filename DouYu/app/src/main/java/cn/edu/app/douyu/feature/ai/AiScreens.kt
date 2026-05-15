package cn.edu.app.douyu.feature.ai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.PatternAsset
import cn.edu.app.douyu.core.model.PatternJobStatus
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.data.safeCallOrNull

private val repo = DoyuAppContainer.patternRepository

@Composable
fun AiHomeScreen(navController: NavHostController) {
    AiHomeScreenContent(navController)
}

@Preview
@Composable
private fun AiHomeScreenPreview() {
    AiHomeScreenContent(navController = null)
}

@Composable
private fun AiHomeScreenContent(navController: NavHostController?) {
    Scaffold(topBar = { DoyuTopBar("AI 拼图") }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("照片变拼豆图纸", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(6.dp))
                        Text("生成预览图、网格图、色号清单和材料建议。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    BeadPattern(Modifier.size(76.dp))
                }
                Spacer(Modifier.height(16.dp))
                DoyuPrimaryButton("选择图片开始", onClick = { navController?.navigate(AppRoute.IMAGE_SELECT) }, icon = Icons.Filled.AddPhotoAlternate, modifier = Modifier.fillMaxWidth())
            }
            val jobState = safeCallToState { repo.featuredJob() }
            DoyuCard {
                SectionHeader("当前任务", "查看记录") { navController?.navigate(AppRoute.PATTERN_HISTORY) }
                when (val state = jobState) {
                    is UiState.Success -> {
                        val job = state.data
                        Text(job.inputName ?: job.inputFileId, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { job.progress / 100f }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text("处理中 ${job.progress}% · 高峰期会展示排队进度", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        DoyuOutlinedButton("查看任务进度", onClick = { navController?.navigate(AppRoute.aiProgress(job.jobId)) }, icon = Icons.Filled.Pending, modifier = Modifier.fillMaxWidth())
                    }
                    is UiState.Empty -> {
                        Text("暂无任务", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("选择图片开始生成你的第一张拼豆图纸", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> PageStateView(jobState)
                }
            }
            DoyuCard {
                SectionHeader("新手友好参数")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip("2.6mm")
                    TagChip("低色数")
                    TagChip("小挂件")
                    TagChip("可爱化")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ImageSelectScreenPreview() { ImageSelectScreenContent(navController = null) }

@Composable
fun ImageSelectScreen(navController: NavHostController) { ImageSelectScreenContent(navController) }

@Composable
private fun ImageSelectScreenContent(navController: NavHostController?) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            navController?.navigate(AppRoute.AI_PARAMS)
        }
    }
    Scaffold(topBar = { DoyuTopBar("选择图片", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Text("选择一张图片，AI 会帮你转成拼豆图纸。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                DoyuPrimaryButton(
                    text = "从相册选择",
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    icon = Icons.Filled.PhotoLibrary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                DoyuOutlinedButton(
                    "拍照",
                    onClick = { navController?.navigate(AppRoute.CAMERA_CAPTURE) },
                    icon = Icons.Filled.PhotoCamera,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DoyuCard {
                SectionHeader("上传前会处理")
                Text("联调顺序固定：Photo Picker/拍照 -> /uploads/presign -> 直传对象存储或 Stub 上传 URL -> /uploads/confirm 返回 fileId。")
                Spacer(Modifier.height(8.dp))
                Text("创建 AI 任务时只传 inputFileId，不直接传 fileKey。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PageStateView(UiState.WeakNetwork)
        }
    }
}

@Preview
@Composable
private fun AiParamsScreenPreview() { AiParamsScreenContent(navController = null) }

@Composable
fun AiParamsScreen(navController: NavHostController) { AiParamsScreenContent(navController) }

@Composable
private fun AiParamsScreenContent(navController: NavHostController?) {
    Scaffold(topBar = { DoyuTopBar("图纸参数", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            ParamSection("豆子规格", listOf("2.6mm", "5mm"))
            ParamSection("成品尺寸", listOf("小挂件", "中等摆件", "大幅作品"))
            ParamSection("难度", listOf("新手", "普通", "进阶"))
            ParamSection("色卡", listOf("豆屿通用 48 色", "低饱和新手色", "已有材料优先"))
            ParamSection("风格", listOf("还原", "可爱", "二次元", "低色数", "头像图标"))
            DoyuCard {
                Text("将用已确认的 fileId 创建任务", style = MaterialTheme.typography.titleMedium)
                Text("POST /patterns/jobs 请求包含 inputFileId、beadSize、targetSize、difficulty、paletteId、style。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuPrimaryButton("创建 AI 任务", onClick = { navController?.navigate(AppRoute.aiProgress("job_001")) }, icon = Icons.Filled.AutoAwesome, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParamSection(title: String, options: List<String>) {
    DoyuCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { index, option ->
                FilterChip(
                    selected = index == 0,
                    onClick = {},
                    label = { Text(option) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun AiProgressScreenPreview() { AiProgressScreenContent(navController = null, jobId = "job_001") }

@Composable
fun AiProgressScreen(navController: NavHostController, jobId: String) { AiProgressScreenContent(navController, jobId) }

@Composable
private fun AiProgressScreenContent(navController: NavHostController?, jobId: String) {
    val jobState = safeCallToState { repo.job(jobId) }
    Scaffold(topBar = { DoyuTopBar("生成进度", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = jobState) {
                is UiState.Success -> {
                    val job = state.data
                    DoyuCard {
                        BeadPattern(Modifier.size(132.dp).align(Alignment.CenterHorizontally))
                        Spacer(Modifier.height(16.dp))
                        Text("正在生成可拼图纸", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { job.progress / 100f }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text("任务 $jobId · ${job.status} · ${job.progress}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (job.status == PatternJobStatus.SUCCEEDED && job.patternId != null) {
                        DoyuPrimaryButton("查看图纸结果", onClick = { navController?.navigate(AppRoute.patternResult(job.patternId)) }, modifier = Modifier.fillMaxWidth())
                    } else if (job.status == PatternJobStatus.FAILED) {
                        PageStateView(UiState.Error(job.failureReason ?: "AI 生成失败"))
                        DoyuOutlinedButton("重试", onClick = {}, modifier = Modifier.fillMaxWidth())
                    } else {
                        DoyuOutlinedButton("继续查询任务状态", onClick = {}, modifier = Modifier.fillMaxWidth())
                    }
                }
                else -> PageStateView(jobState)
            }
        }
    }
}

@Preview
@Composable
private fun PatternResultScreenPreview() { PatternResultScreenContent(navController = null, patternId = "pattern_001") }

@Composable
fun PatternResultScreen(navController: NavHostController, patternId: String) { PatternResultScreenContent(navController, patternId) }

@Composable
private fun PatternResultScreenContent(navController: NavHostController?, patternId: String) {
    val patternState = safeCallToState { repo.pattern(patternId) }
    Scaffold(topBar = { DoyuTopBar("图纸结果", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = patternState) {
                is UiState.Success -> {
                    val pattern = state.data
                    PatternSummary(pattern)
                    DoyuCard {
                        SectionHeader("色号清单")
                        pattern.colorStats.forEach {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                BeadDot(Color(it.hex), size = 22.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("${it.colorCode} ${it.displayName}", modifier = Modifier.weight(1f))
                                Text("${it.beadCount} 颗", fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    DoyuCard {
                        SectionHeader("材料清单")
                        val materials = pattern.materials
                        if (materials != null) {
                            Text("共 ${materials.totalBeads} 颗", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            materials.colors.forEach {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${it.colorCode} ${it.displayName}", modifier = Modifier.weight(1f))
                                    Text("${it.beadCount} 颗", fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        } else {
                            Text("暂无材料信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton("保存到我的拼豆", onClick = {}, modifier = Modifier.weight(1f))
                        DoyuPrimaryButton("加入购物车", onClick = { navController?.navigate(AppRoute.CART) }, modifier = Modifier.weight(1f))
                    }
                }
                else -> PageStateView(patternState)
            }
        }
    }
}

@Composable
private fun PatternSummary(pattern: PatternAsset) {
    DoyuCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) { BeadPattern(Modifier.size(72.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(pattern.title, style = MaterialTheme.typography.titleLarge)
                Text("${pattern.widthCells} x ${pattern.heightCells} 格 · ${pattern.totalBeads} 颗", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(pattern.paletteName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview
@Composable
private fun PatternHistoryScreenPreview() { PatternHistoryScreenContent(navController = null) }

@Composable
fun PatternHistoryScreen(navController: NavHostController) { PatternHistoryScreenContent(navController) }

@Composable
private fun PatternHistoryScreenContent(navController: NavHostController?) {
    val historyState = safeCallToState { repo.history() }
    Scaffold(topBar = { DoyuTopBar("生成记录", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = historyState) {
                is UiState.Success -> state.data.items.forEach { job ->
                    DoyuCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(job.inputName ?: job.inputFileId, style = MaterialTheme.typography.titleMedium)
                                Text("${job.inputFileId} · ${job.beadSize} · ${job.difficulty} · ${job.style}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TagChip(statusLabel(job.status))
                        }
                        Spacer(Modifier.height(10.dp))
                        DoyuOutlinedButton(
                            "查看",
                            onClick = {
                                if (job.status == PatternJobStatus.SUCCEEDED && job.patternId != null) {
                                    navController?.navigate(AppRoute.patternResult(job.patternId))
                                } else {
                                    navController?.navigate(AppRoute.aiProgress(job.jobId))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> PageStateView(historyState)
            }
        }
    }
}

private fun statusLabel(status: PatternJobStatus): String = when (status) {
    PatternJobStatus.PENDING -> "排队中"
    PatternJobStatus.PROCESSING -> "处理中"
    PatternJobStatus.SUCCEEDED -> "已完成"
    PatternJobStatus.FAILED -> "失败"
    PatternJobStatus.REJECTED -> "审核拒绝"
    PatternJobStatus.CANCELED -> "已取消"
}
