package cn.edu.app.douyu.feature.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import android.graphics.BitmapFactory
import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.core.ui.*
import cn.edu.app.douyu.core.data.safeCallToState
import cn.edu.app.douyu.core.data.safeCallOrNull
import cn.edu.app.douyu.core.network.requireSuccess
import cn.edu.app.douyu.ui.theme.*
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
    Scaffold(
        topBar = {
            DoyuTopBar("AI 拼图") {
                IconButton(onClick = { navController?.navigate(AppRoute.PATTERN_HISTORY) }) {
                    Icon(Icons.Filled.History, contentDescription = "历史", tint = LightPrimary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            DoyuHeroCard(
                title = "照片变拼豆图纸",
                subtitle = "上传你的照片，AI一键生成专属拼豆图纸，让回忆变得可触摸，轻松开启手工之旅。",
                ctaText = "开始创作",
                onCtaClick = { navController?.navigate(AppRoute.IMAGE_SELECT) },
                badge = "智能图纸引擎"
            )

            // Current Task Card
            var jobRetryCount by remember { mutableIntStateOf(0) }
            val jobState = safeCallToState(jobRetryCount) { repo.featuredJob() }.value
            when (val state = jobState) {
                is UiState.Success -> {
                    val job = state.data
                    CurrentTaskCard(
                        fileName = job.inputName ?: job.inputFileId,
                        progress = job.progress,
                        onViewProgress = { navController?.navigate(AppRoute.aiProgress(job.jobId)) }
                    )
                }
                is UiState.Empty -> {
                    // No current task - show nothing
                }
                else -> { /* loading or error */ }
            }

            // Inspiration Tags
            SectionHeader("创作灵感")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("头像", "宠物", "二次元", "节日", "情侣").forEach { tag ->
                    TagChip(tag)
                }
            }

            // History Section
            SectionHeader("创作历史", action = "查看全部") {
                navController?.navigate(AppRoute.PATTERN_HISTORY)
            }
            var historyRetryCount by remember { mutableIntStateOf(0) }
            val historyState = safeCallToState(historyRetryCount) { repo.history() }.value
            when (val state = historyState) {
                is UiState.Success -> {
                    state.data.items.take(4).forEach { job ->
                        HistoryJobCard(
                            title = job.inputName ?: job.inputFileId,
                            status = statusLabel(job.status),
                            onClick = {
                                if (job.status == PatternJobStatus.SUCCEEDED && job.patternId != null) {
                                    navController?.navigate(AppRoute.patternResult(job.patternId))
                                } else {
                                    navController?.navigate(AppRoute.aiProgress(job.jobId))
                                }
                            }
                        )
                    }
                }
                is UiState.Empty -> {
                    EmptyContent(
                        "还没有生成过图纸",
                        "选择一张图片，开始生成你的第一张拼豆图纸吧！",
                        showRetry = false
                    )
                }
                else -> PageStateView(historyState, onRetry = { historyRetryCount++ })
            }
        }
    }
}

@Composable
private fun CurrentTaskCard(
    fileName: String,
    progress: Int,
    onViewProgress: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "taskPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(LightPrimary.copy(alpha = pulseAlpha))
                )
                Spacer(Modifier.width(8.dp))
                Text("正在生成中", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = LightPrimaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        "$progress%",
                        style = MaterialTheme.typography.labelMedium,
                        color = LightPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = LightPrimary,
                trackColor = LightPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(Modifier.height(12.dp))
            DoyuOutlinedButton("查看进度", onViewProgress, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun HistoryJobCard(title: String, status: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(LightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = LightPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── ImageSelectScreen ──

@Preview
@Composable
private fun ImageSelectScreenPreview() { ImageSelectScreenContent(navController = null) }

@Composable
fun ImageSelectScreen(navController: NavHostController) { ImageSelectScreenContent(navController) }

private enum class UploadState { IDLE, UPLOADING, SUCCESS, FAILED, REQUIRE_LOGIN }

@Composable
private fun ImageSelectScreenContent(navController: NavHostController?) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var uploadState by remember { mutableStateOf(UploadState.IDLE) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadedFileId by remember { mutableStateOf<String?>(null) }
    var previewRotation by remember { mutableFloatStateOf(0f) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            uploadState = UploadState.IDLE
            uploadedFileId = null
            previewRotation = 0f
        }
    }

    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        val capturedUriStr = savedStateHandle?.get<String>("captured_uri")
        if (capturedUriStr != null) {
            selectedUri = Uri.parse(capturedUriStr)
            uploadState = UploadState.IDLE
            uploadedFileId = null
            previewRotation = 0f
            savedStateHandle.remove<String>("captured_uri")
        }
    }

    Scaffold(topBar = { DoyuTopBar("选择图片", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            if (selectedUri == null) {
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
                DoyuCard {
                    SectionHeader("已选图片")
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "选中图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp, max = 420.dp)
                            .rotate(previewRotation)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        DoyuOutlinedButton(
                            "左转",
                            onClick = { previewRotation -= 90f },
                            icon = Icons.AutoMirrored.Filled.RotateLeft,
                            modifier = Modifier.weight(1f)
                        )
                        DoyuOutlinedButton(
                            "右转",
                            onClick = { previewRotation += 90f },
                            icon = Icons.AutoMirrored.Filled.RotateRight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "拍照图片按原始比例预览；如系统相册方向不一致，可先旋转预览再继续。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton(
                            "重新选择",
                            onClick = {
                                selectedUri = null
                                uploadState = UploadState.IDLE
                                uploadedFileId = null
                                previewRotation = 0f
                            },
                            icon = Icons.Filled.Refresh,
                            modifier = Modifier.weight(1f)
                        )
                        if (uploadState == UploadState.IDLE || uploadState == UploadState.FAILED || uploadState == UploadState.REQUIRE_LOGIN) {
                            DoyuPrimaryButton(
                                if (uploadState == UploadState.FAILED) "重试上传" else "上传并继续",
                                onClick = {
                                    val uri = selectedUri ?: return@DoyuPrimaryButton
                                    if (!canStartAiUpload(DoyuAppContainer.isLoggedIn, hasSelectedImage = true)) {
                                        uploadState = UploadState.REQUIRE_LOGIN
                                        return@DoyuPrimaryButton
                                    }
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

                if (uploadState == UploadState.UPLOADING) {
                    DoyuCard {
                        Text("上传中...", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { uploadProgress }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text("已上传 ${(uploadProgress * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (uploadState == UploadState.FAILED) {
                    DoyuCard {
                        Text("上传失败", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(4.dp))
                        Text("请检查网络后重试", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (uploadState == UploadState.REQUIRE_LOGIN) {
                    DoyuCard {
                        Text("需要登录", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("登录后才能上传图片并生成图纸。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        DoyuPrimaryButton(
                            "去登录",
                            onClick = { navController?.navigate(aiUploadLoginRoute()) },
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (uploadState == UploadState.SUCCESS) {
                    DoyuCard {
                        Text("上传完成", style = MaterialTheme.typography.titleMedium, color = LightPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("fileId: ${uploadedFileId ?: "..."}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    uploadedFileId?.let { fileId ->
                        AiParamsContent(navController = navController, uploadedFileId = fileId)
                    }
                }
            }
        }
    }
}

// ── AiParamsScreen ──

@Preview
@Composable
private fun AiParamsScreenPreview() { AiParamsScreenContent(navController = null, uploadedFileId = "file_preview") }

@Composable
fun AiParamsScreen(navController: NavHostController, uploadedFileId: String) { AiParamsScreenContent(navController, uploadedFileId) }

@Composable
private fun AiParamsScreenContent(navController: NavHostController?, uploadedFileId: String) {
    Scaffold(topBar = { DoyuTopBar("图纸参数", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            AiParamsContent(navController = navController, uploadedFileId = uploadedFileId)
        }
    }
}

@Composable
private fun AiParamsContent(navController: NavHostController?, uploadedFileId: String) {
    val scope = rememberCoroutineScope()

    var beadSize by remember { mutableStateOf("2.6mm") }
    var targetSize by remember { mutableStateOf("小挂件") }
    var difficulty by remember { mutableStateOf("新手") }
    var palette by remember { mutableStateOf("豆屿通用 48 色") }
    var style by remember { mutableStateOf("还原") }
    var creating by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ParamSection("豆子规格", listOf("2.6mm", "5mm"), beadSize) { beadSize = it }
            ParamSection("成品尺寸", listOf("小挂件", "中等摆件", "大幅作品"), targetSize) { targetSize = it }
            ParamSection("难度", listOf("新手", "普通", "进阶"), difficulty) { difficulty = it }
            ParamSection("色卡", listOf("豆屿通用 48 色", "低饱和新手色", "已有材料优先"), palette) { palette = it }
            ParamSection("风格", listOf("还原", "可爱", "二次元", "低色数", "头像图标"), style) { style = it }

            Spacer(Modifier.height(8.dp))
            Text(
                "预计需要 30 秒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            createError?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))

            DoyuPrimaryButton(
                if (creating) "创建中..." else "开始生成",
                onClick = {
                    if (creating) return@DoyuPrimaryButton
                    creating = true
                    createError = null
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
                            createError = "创建失败：${e.message ?: "请稍后重试"}"
                        }
                    }
                },
                icon = Icons.Filled.AutoAwesome,
                modifier = Modifier.fillMaxWidth(),
                enabled = !creating
            )
    }
}

@Composable
private fun ParamSection(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    DoyuCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { option ->
                        val isSelected = option == selected
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelect(option) },
                            label = { Text(option) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LightPrimaryContainer,
                                selectedLabelColor = LightOnPrimaryContainer
                            )
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ── AiProgressScreen ──

@Preview
@Composable
private fun AiProgressScreenPreview() { AiProgressScreenContent(navController = null, jobId = "job_001") }

@Composable
fun AiProgressScreen(navController: NavHostController, jobId: String) { AiProgressScreenContent(navController, jobId) }

@Composable
private fun AiProgressScreenContent(navController: NavHostController?, jobId: String) {
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<PatternJob?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pollRevision by remember { mutableIntStateOf(0) }
    var canceling by remember { mutableStateOf(false) }
    var cancelError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "progressPulse")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (loading && job == null) {
                CircularProgressIndicator(color = LightPrimary)
            } else if (error != null && job == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("加载失败", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    DoyuPrimaryButton("重试", onClick = { pollRevision++ })
                }
            } else if (job != null) {
                val current = job!!
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    cancelError?.let {
                        DisabledFeatureNotice(
                            title = "取消失败",
                            message = it,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Animation area
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(LightSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        BeadPattern(Modifier.size(120.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { current.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        color = LightPrimary,
                        trackColor = LightPrimaryContainer
                    )

                    Spacer(Modifier.height(12.dp))

                    // Percentage
                    Text(
                        "${current.progress}%",
                        style = MaterialTheme.typography.displayMedium,
                        color = LightPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    // Status text
                    val statusText = when {
                        current.progress < 30 -> "分析图片中..."
                        current.progress < 80 -> "生成图纸中..."
                        else -> "即将完成..."
                    }
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "任务 $jobId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(Modifier.height(32.dp))

                    // Action buttons based on status
                    when (current.status) {
                        PatternJobStatus.SUCCEEDED -> {
                            if (current.patternId != null) {
                                DoyuPrimaryButton(
                                    "查看图纸结果",
                                    onClick = { navController?.navigate(AppRoute.patternResult(current.patternId)) },
                                    icon = Icons.Filled.Visibility,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        PatternJobStatus.FAILED -> {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = LightError.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("生成失败", style = MaterialTheme.typography.titleMedium, color = LightError)
                                    Spacer(Modifier.height(4.dp))
                                    Text(current.failureReason ?: "AI 生成失败，请重试", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DoyuOutlinedButton("取消", onClick = {
                                    canceling = true
                                    cancelError = null
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                requireSuccess(DoyuAppContainer.apiClient.patternApi.cancelJob(jobId))
                                            }
                                            navController?.popBackStack(BottomTab.AI.route, inclusive = false)
                                        } catch (e: Exception) {
                                            cancelError = e.message ?: "请稍后重试"
                                        } finally { canceling = false }
                                    }
                                }, enabled = !canceling, modifier = Modifier.weight(1f))
                                DoyuPrimaryButton("重试", onClick = { pollRevision++ }, icon = Icons.Filled.Refresh, modifier = Modifier.weight(1f))
                            }
                        }
                        PatternJobStatus.CANCELED, PatternJobStatus.REJECTED -> {
                            Text(
                                if (current.status == PatternJobStatus.CANCELED) "任务已取消" else "任务被拒绝",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (current.failureReason != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(current.failureReason!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(16.dp))
                            DoyuPrimaryButton("返回", onClick = { navController?.popBackStack(BottomTab.AI.route, inclusive = false) }, modifier = Modifier.fillMaxWidth())
                        }
                        else -> {
                            TextButton(onClick = {
                                canceling = true
                                cancelError = null
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            requireSuccess(DoyuAppContainer.apiClient.patternApi.cancelJob(jobId))
                                        }
                                        navController?.popBackStack(BottomTab.AI.route, inclusive = false)
                                    } catch (e: Exception) {
                                        cancelError = e.message ?: "请稍后重试"
                                    } finally { canceling = false }
                                }
                            }, enabled = !canceling) {
                                Text("取消任务")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── PatternResultScreen ──

@Preview
@Composable
private fun PatternResultScreenPreview() { PatternResultScreenContent(navController = null, patternId = "pattern_001") }

@Composable
fun PatternResultScreen(navController: NavHostController, patternId: String) { PatternResultScreenContent(navController, patternId) }

@Composable
private fun PatternResultScreenContent(navController: NavHostController?, patternId: String) {
    val scope = rememberCoroutineScope()
    val patternState = safeCallToState(patternId) { repo.pattern(patternId) }.value
    var favorited by remember { mutableStateOf(false) }
    var favoriteMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { DoyuTopBar("图纸结果", canGoBack = true, onBack = { navController?.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            when (val state = patternState) {
                is UiState.Success -> {
                    val pattern = state.data
                    PatternSummary(pattern)

                    if (pattern.previewFileId != null) {
                        DoyuCard {
                            SectionHeader("预览图")
                            Box(
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.small).background(LightSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) { Text("预览图加载中...", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }

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

                    DoyuPrimaryButton(
                        if (favorited) "已收藏" else "保存到我的图纸",
                        onClick = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        requireSuccess(DoyuAppContainer.apiClient.patternApi.favoritePattern(patternId))
                                    }
                                    favorited = true
                                    favoriteMessage = "已保存到我的图纸。"
                                } catch (e: Exception) {
                                    favoriteMessage = "保存失败：${e.message ?: "请稍后重试"}"
                                }
                            }
                        },
                        icon = if (favorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !favorited
                    )
                    favoriteMessage?.let {
                        Text(
                            it,
                            color = if (favorited) LightPrimary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    DisabledFeatureNotice(
                        title = "材料购买待接入",
                        message = "图纸材料清单已展示，自动加购、PDF 导出和带图纸发帖将在真实链路接入后开放。"
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DoyuOutlinedButton(
                            "加入购物车",
                            onClick = ::disabledClick,
                            enabled = false,
                            icon = Icons.Filled.ShoppingCart,
                            modifier = Modifier.weight(1f)
                        )
                        DoyuOutlinedButton(
                            "去购物车",
                            onClick = { navController?.navigate(AppRoute.CART) },
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pattern.pdfFileId != null) {
                        Spacer(Modifier.height(10.dp))
                        DoyuOutlinedButton(
                            "导出 PDF",
                            onClick = ::disabledClick,
                            enabled = false,
                            icon = Icons.Filled.PictureAsPdf,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    DoyuOutlinedButton(
                        "分享到社区",
                        onClick = ::disabledClick,
                        enabled = false,
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
                    .background(LightSurfaceVariant, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) { BeadPattern(Modifier.size(72.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(pattern.title, style = MaterialTheme.typography.titleLarge)
                Text("${pattern.widthCells} x ${pattern.heightCells} 格 · ${pattern.totalBeads} 颗", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(pattern.paletteName, color = LightPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── PatternHistoryScreen ──

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
                    HistoryJobCard(
                        title = job.inputName ?: job.inputFileId,
                        status = statusLabel(job.status),
                        onClick = {
                            if (job.status == PatternJobStatus.SUCCEEDED && job.patternId != null) {
                                navController?.navigate(AppRoute.patternResult(job.patternId))
                            } else {
                                navController?.navigate(AppRoute.aiProgress(job.jobId))
                            }
                        }
                    )
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
    PatternJobStatus.REJECTED -> "失败"
    PatternJobStatus.CANCELED -> "已取消"
}
