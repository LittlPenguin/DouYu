package cn.edu.app.douyu.feature.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import android.graphics.BitmapFactory
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.PatternAsset
import cn.edu.app.douyu.core.model.PatternJobStatus
import cn.edu.app.douyu.core.model.UploadConfirmRequest
import cn.edu.app.douyu.core.model.UploadPresignRequest
import cn.edu.app.douyu.core.model.UploadUsage
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.data.safeCallOrNull
import cn.edu.app.douyu.core.network.requireSuccess
import kotlinx.coroutines.launch

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

private enum class UploadState { IDLE, UPLOADING, SUCCESS, FAILED }

@Composable
private fun ImageSelectScreenContent(navController: NavHostController?) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var uploadState by remember { mutableStateOf(UploadState.IDLE) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadedFileId by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            uploadState = UploadState.IDLE
        }
    }

    // Listen for captured URI from camera
    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        val capturedUriStr = savedStateHandle?.get<String>("captured_uri")
        if (capturedUriStr != null) {
            selectedUri = Uri.parse(capturedUriStr)
            uploadState = UploadState.IDLE
            savedStateHandle.remove<String>("captured_uri")
        }
    }

    Scaffold(topBar = { DoyuTopBar("选择图片", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            if (selectedUri == null) {
                // No image selected yet — show selection options
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
            } else {
                // Image selected — show preview
                DoyuCard {
                    SectionHeader("已选图片")
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "选中图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton(
                            "重新选择",
                            onClick = {
                                selectedUri = null
                                uploadState = UploadState.IDLE
                                uploadedFileId = null
                            },
                            icon = Icons.Filled.Refresh,
                            modifier = Modifier.weight(1f)
                        )
                        if (uploadState == UploadState.IDLE || uploadState == UploadState.FAILED) {
                            DoyuPrimaryButton(
                                if (uploadState == UploadState.FAILED) "重试上传" else "上传并继续",
                                onClick = {
                                    val uri = selectedUri ?: return@DoyuPrimaryButton
                                    uploadState = UploadState.UPLOADING
                                    uploadProgress = 0f
                                    scope.launch {
                                        try {
                                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                                ?: throw IllegalStateException("无法读取图片")
                                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                                            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                                            val fileName = "upload_${System.currentTimeMillis()}.jpg"

                                            val uploadApi = DoyuAppContainer.apiClient.uploadApi
                                            val transport = DoyuAppContainer.uploadTransport

                                            val presign = requireSuccess(
                                                uploadApi.presign(
                                                    UploadPresignRequest(
                                                        usage = UploadUsage.AI_INPUT,
                                                        fileName = fileName,
                                                        mimeType = mimeType,
                                                        sizeBytes = bytes.size.toLong()
                                                    )
                                                )
                                            )

                                            transport.upload(presign, bytes) { progress ->
                                                uploadProgress = progress * 0.9f
                                            }

                                            val file = requireSuccess(
                                                uploadApi.confirm(
                                                    UploadConfirmRequest(
                                                        fileKey = presign.fileKey,
                                                        usage = UploadUsage.AI_INPUT,
                                                        mimeType = mimeType,
                                                        sizeBytes = bytes.size.toLong(),
                                                        width = options.outWidth.takeIf { it > 0 },
                                                        height = options.outHeight.takeIf { it > 0 }
                                                    )
                                                )
                                            )

                                            uploadProgress = 1f
                                            uploadedFileId = file.fileId
                                            uploadState = UploadState.SUCCESS
                                        } catch (e: Exception) {
                                            uploadState = UploadState.FAILED
                                        }
                                    }
                                },
                                icon = Icons.Filled.CloudUpload,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Upload progress/status
                if (uploadState == UploadState.UPLOADING) {
                    DoyuCard {
                        Text("上传中...", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "已上传 ${(uploadProgress * 100).toInt()}%",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (uploadState == UploadState.FAILED) {
                    DoyuCard {
                        Text("上传失败", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(4.dp))
                        Text("请检查网络后重试", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (uploadState == UploadState.SUCCESS) {
                    DoyuCard {
                        Text("上传完成", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("fileId: ${uploadedFileId ?: "..."}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DoyuPrimaryButton(
                        "继续设置参数",
                        onClick = { navController?.navigate(AppRoute.AI_PARAMS) },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            DoyuCard {
                SectionHeader("上传流程")
                Text("Photo Picker/拍照 -> /uploads/presign -> 直传对象存储 -> /uploads/confirm 返回 fileId。")
                Spacer(Modifier.height(8.dp))
                Text("创建 AI 任务时只传 inputFileId，不直接传 fileKey。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
                is UiState.Empty -> EmptyContent(
                    "还没有生成过图纸",
                    "选择一张图片，开始生成你的第一张拼豆图纸吧！",
                    showRetry = false
                )
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
