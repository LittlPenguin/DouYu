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
import cn.edu.app.douyu.core.model.BeadSize
import cn.edu.app.douyu.core.model.CreatePatternJobRequest
import cn.edu.app.douyu.core.model.PatternAsset
import cn.edu.app.douyu.core.model.PatternDifficulty
import cn.edu.app.douyu.core.model.PatternJobStatus
import cn.edu.app.douyu.core.model.PatternStyle
import cn.edu.app.douyu.core.model.UploadConfirmRequest
import cn.edu.app.douyu.core.model.UploadPresignRequest
import cn.edu.app.douyu.core.model.UploadUsage
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.data.safeCallOrNull
import cn.edu.app.douyu.core.network.requireSuccess
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val jobState = safeCallToState { repo.featuredJob() }.value
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
                        onClick = { navController?.navigate(AppRoute.aiParams(uploadedFileId!!)) },
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
private fun AiParamsScreenPreview() { AiParamsScreenContent(navController = null, uploadedFileId = "file_preview") }

@Composable
fun AiParamsScreen(navController: NavHostController, uploadedFileId: String) { AiParamsScreenContent(navController, uploadedFileId) }

@Composable
private fun AiParamsScreenContent(navController: NavHostController?, uploadedFileId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var beadSize by remember { mutableStateOf("2.6mm") }
    var targetSize by remember { mutableStateOf("小挂件") }
    var difficulty by remember { mutableStateOf("新手") }
    var palette by remember { mutableStateOf("豆屿通用 48 色") }
    var style by remember { mutableStateOf("还原") }
    var creating by remember { mutableStateOf(false) }

    Scaffold(topBar = { DoyuTopBar("图纸参数", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            ParamSection("豆子规格", listOf("2.6mm", "5mm"), beadSize) { beadSize = it }
            ParamSection("成品尺寸", listOf("小挂件", "中等摆件", "大幅作品"), targetSize) { targetSize = it }
            ParamSection("难度", listOf("新手", "普通", "进阶"), difficulty) { difficulty = it }
            ParamSection("色卡", listOf("豆屿通用 48 色", "低饱和新手色", "已有材料优先"), palette) { palette = it }
            ParamSection("风格", listOf("还原", "可爱", "二次元", "低色数", "头像图标"), style) { style = it }
            DoyuPrimaryButton(
                if (creating) "创建中..." else "创建 AI 任务",
                onClick = {
                    if (creating) return@DoyuPrimaryButton
                    creating = true
                    scope.launch {
                        try {
                            val request = CreatePatternJobRequest(
                                inputFileId = uploadedFileId,
                                beadSize = if (beadSize == "5mm") BeadSize.MM_5 else BeadSize.MM_2_6,
                                targetSize = when (targetSize) {
                                    "中等摆件" -> "MEDIUM_DECOR"
                                    "大幅作品" -> "LARGE_ARTWORK"
                                    else -> "SMALL_CHARM"
                                },
                                difficulty = when (difficulty) {
                                    "普通" -> PatternDifficulty.NORMAL
                                    "进阶" -> PatternDifficulty.ADVANCED
                                    else -> PatternDifficulty.BEGINNER
                                },
                                paletteId = when (palette) {
                                    "低饱和新手色" -> "palette_beginner_low"
                                    "已有材料优先" -> "palette_material_first"
                                    else -> "palette_doyu_48"
                                },
                                style = when (style) {
                                    "可爱" -> PatternStyle.CUTE
                                    "二次元" -> PatternStyle.ANIME
                                    "低色数" -> PatternStyle.LOW_COLOR
                                    "头像图标" -> PatternStyle.ICON
                                    else -> PatternStyle.RESTORE
                                }
                            )
                            val job = withContext(Dispatchers.IO) {
                                requireSuccess(DoyuAppContainer.apiClient.patternApi.createJob(request))
                            }
                            creating = false
                            navController?.navigate(AppRoute.aiProgress(job.jobId))
                        } catch (e: Exception) {
                            creating = false
                            Toast.makeText(context, "创建失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                icon = Icons.Filled.AutoAwesome,
                modifier = Modifier.fillMaxWidth(),
                enabled = !creating
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParamSection(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    DoyuCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<cn.edu.app.douyu.core.model.PatternJob?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pollRevision by remember { mutableIntStateOf(0) }
    var canceling by remember { mutableStateOf(false) }

    LaunchedEffect(jobId, pollRevision) {
        loading = true
        error = null
        while (true) {
            try {
                val fetched = withContext(Dispatchers.IO) {
                    requireSuccess(DoyuAppContainer.apiClient.patternApi.job(jobId))
                }
                job = fetched
                loading = false
                if (fetched.status in listOf(
                        PatternJobStatus.SUCCEEDED,
                        PatternJobStatus.FAILED,
                        PatternJobStatus.CANCELED,
                        PatternJobStatus.REJECTED
                    )
                ) break
            } catch (e: Exception) {
                error = e.message
                loading = false
            }
            delay(2000)
        }
    }

    Scaffold(topBar = { DoyuTopBar("生成进度", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            if (loading && job == null) {
                PageStateView(UiState.Loading)
            } else if (error != null && job == null) {
                PageStateView(UiState.Error(error!!))
                DoyuOutlinedButton("重试", onClick = { pollRevision++ }, modifier = Modifier.fillMaxWidth())
            } else if (job != null) {
                val current = job!!
                DoyuCard {
                    BeadPattern(Modifier.size(132.dp).align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(16.dp))
                    Text("正在生成可拼图纸", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { current.progress / 100f }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            current.progress < 30 -> "分析图片中..."
                            current.progress < 80 -> "生成图纸中..."
                            else -> "即将完成..."
                        } + " ${current.progress}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("任务 $jobId · ${statusLabel(current.status)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (current.status == PatternJobStatus.SUCCEEDED && current.patternId != null) {
                    DoyuPrimaryButton(
                        "查看图纸结果",
                        onClick = { navController?.navigate(AppRoute.patternResult(current.patternId)) },
                        icon = Icons.Filled.Visibility,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (current.status == PatternJobStatus.FAILED) {
                    DoyuCard {
                        Text("生成失败", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(4.dp))
                        Text(current.failureReason ?: "AI 生成失败，请重试", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton(
                            "取消",
                            onClick = {
                                canceling = true
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            requireSuccess(DoyuAppContainer.apiClient.patternApi.cancelJob(jobId))
                                        }
                                        navController?.popBackStack(BottomTab.AI.route, inclusive = false)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "取消失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        canceling = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        DoyuPrimaryButton(
                            "重试",
                            onClick = { pollRevision++ },
                            icon = Icons.Filled.Refresh,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (current.status == PatternJobStatus.CANCELED || current.status == PatternJobStatus.REJECTED) {
                    DoyuCard {
                        Text(
                            if (current.status == PatternJobStatus.CANCELED) "任务已取消" else "任务被拒绝",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (current.failureReason != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(current.failureReason!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    DoyuPrimaryButton(
                        "返回",
                        onClick = { navController?.popBackStack(BottomTab.AI.route, inclusive = false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton(
                            "取消任务",
                            onClick = {
                                canceling = true
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            requireSuccess(DoyuAppContainer.apiClient.patternApi.cancelJob(jobId))
                                        }
                                        navController?.popBackStack(BottomTab.AI.route, inclusive = false)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "取消失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        canceling = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val patternState = safeCallToState(patternId) { repo.pattern(patternId) }.value
    var favorited by remember { mutableStateOf(false) }
    var showCartDialog by remember { mutableStateOf(false) }

    if (showCartDialog) {
        val pattern = (patternState as? UiState.Success)?.data
        AlertDialog(
            onDismissRequest = { showCartDialog = false },
            title = { Text("加入购物车") },
            text = {
                val materials = pattern?.materials
                if (materials != null) {
                    Column {
                        Text("将以下材料加入购物车：")
                        Spacer(Modifier.height(8.dp))
                        materials.colors.forEach {
                            Text("${it.colorCode} ${it.displayName} x${it.beadCount}", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("共 ${materials.totalBeads} 颗", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text("暂无材料信息")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCartDialog = false
                    Toast.makeText(context, "已加入购物车", Toast.LENGTH_SHORT).show()
                    navController?.navigate(AppRoute.CART)
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showCartDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(topBar = { DoyuTopBar("图纸结果", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = patternState) {
                is UiState.Success -> {
                    val pattern = state.data
                    PatternSummary(pattern)

                    // Preview image
                    if (pattern.previewFileId != null) {
                        DoyuCard {
                            SectionHeader("预览图")
                            Box(
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("预览图加载中...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Color stats
                    if (pattern.colorStats.isNotEmpty()) {
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
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Text("合计", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                Text("${pattern.totalBeads} 颗", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Materials
                    val materials = pattern.materials
                    if (materials != null && materials.colors.isNotEmpty()) {
                        DoyuCard {
                            SectionHeader("材料清单")
                            Text("共 ${materials.totalBeads} 颗", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            materials.colors.forEach {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${it.colorCode} ${it.displayName}", modifier = Modifier.weight(1f))
                                    Text("${it.beadCount} 颗", fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }

                    // Actions
                    DoyuPrimaryButton(
                        if (favorited) "已收藏" else "保存到我的图纸",
                        onClick = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        requireSuccess(DoyuAppContainer.apiClient.patternApi.favoritePattern(patternId))
                                    }
                                    favorited = true
                                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        icon = if (favorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !favorited
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton("加入购物车", onClick = { showCartDialog = true }, icon = Icons.Filled.ShoppingCart, modifier = Modifier.weight(1f))
                        DoyuOutlinedButton("去购物车", onClick = { navController?.navigate(AppRoute.CART) }, icon = Icons.AutoMirrored.Filled.ArrowForward, modifier = Modifier.weight(1f))
                    }
                    if (pattern.pdfFileId != null) {
                        Spacer(Modifier.height(10.dp))
                        DoyuOutlinedButton("导出 PDF", onClick = { Toast.makeText(context, "PDF 导出功能开发中", Toast.LENGTH_SHORT).show() }, icon = Icons.Filled.PictureAsPdf, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(10.dp))
                    DoyuOutlinedButton(
                        "分享到社区",
                        onClick = { navController?.navigate(AppRoute.POST_CREATE) },
                        icon = Icons.Filled.Share,
                        modifier = Modifier.fillMaxWidth()
                    )
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
    val historyState = safeCallToState { repo.history() }.value
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
