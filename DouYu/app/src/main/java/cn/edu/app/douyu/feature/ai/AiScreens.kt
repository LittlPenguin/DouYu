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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.edu.app.douyu.core.data.MockPatternRepository
import cn.edu.app.douyu.core.model.PatternAsset
import cn.edu.app.douyu.core.model.PatternJobStatus
import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.ui.*

private val repo = MockPatternRepository()

@Composable
fun AiHomeScreen(navController: NavHostController) {
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
                DoyuPrimaryButton("选择图片开始", onClick = { navController.navigate(AppRoute.IMAGE_SELECT) }, icon = Icons.Filled.AddPhotoAlternate, modifier = Modifier.fillMaxWidth())
            }
            val job = repo.featuredJob()
            DoyuCard {
                SectionHeader("当前任务", "查看记录") { navController.navigate(AppRoute.PATTERN_HISTORY) }
                Text(job.inputName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { job.progress / 100f }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("处理中 ${job.progress}% · 高峰期会展示排队进度", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                DoyuOutlinedButton("查看任务进度", onClick = { navController.navigate(AppRoute.aiProgress(job.id)) }, icon = Icons.Filled.Pending, modifier = Modifier.fillMaxWidth())
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

@Composable
fun ImageSelectScreen(navController: NavHostController) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        navController.navigate(AppRoute.AI_PARAMS)
    }
    Scaffold(topBar = { DoyuTopBar("选择图片", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                Text("优先使用 Android Photo Picker，只读取你主动选择的图片。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                DoyuPrimaryButton(
                    text = "从相册选择",
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    icon = Icons.Filled.PhotoLibrary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                DoyuOutlinedButton("拍照入口预留", onClick = {}, icon = Icons.Filled.PhotoCamera, modifier = Modifier.fillMaxWidth())
            }
            DoyuCard {
                SectionHeader("上传前会处理")
                Text("后续会接入裁剪、方向修正、压缩、清晰度和主体大小检查。当前 MVP 先打通页面边界。")
            }
            PageStateView(UiState.WeakNetwork)
        }
    }
}

@Composable
fun AiParamsScreen(navController: NavHostController) {
    Scaffold(topBar = { DoyuTopBar("图纸参数", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            ParamSection("豆子规格", listOf("2.6mm", "5mm"))
            ParamSection("成品尺寸", listOf("小挂件", "中等摆件", "大幅作品"))
            ParamSection("难度", listOf("新手", "普通", "进阶"))
            ParamSection("色卡", listOf("豆屿通用 48 色", "低饱和新手色", "已有材料优先"))
            ParamSection("风格", listOf("还原", "可爱", "二次元", "低色数", "头像图标"))
            DoyuPrimaryButton("创建 AI 任务", onClick = { navController.navigate(AppRoute.aiProgress("job_001")) }, icon = Icons.Filled.AutoAwesome, modifier = Modifier.fillMaxWidth())
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

@Composable
fun AiProgressScreen(navController: NavHostController, jobId: String) {
    val job = repo.history().firstOrNull { it.id == jobId } ?: repo.featuredJob()
    Scaffold(topBar = { DoyuTopBar("生成进度", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            DoyuCard {
                BeadPattern(Modifier.size(132.dp).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                Text("正在生成可拼图纸", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { job.progress / 100f }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("任务 $jobId · ${job.status} · ${job.progress}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuCard {
                Text("服务端状态为准", style = MaterialTheme.typography.titleMedium)
                Text("客户端后续会轮询 `/api/v1/patterns/jobs/{jobId}`，成功后展示图纸结果。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DoyuPrimaryButton("查看图纸结果", onClick = { navController.navigate(AppRoute.patternResult(job.patternId ?: "pattern_001")) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PatternResultScreen(navController: NavHostController, patternId: String) {
    val pattern = repo.pattern(patternId)
    Scaffold(topBar = { DoyuTopBar("图纸结果", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
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
                SectionHeader("推荐材料")
                pattern.materials.forEach {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(it.name, fontWeight = FontWeight.SemiBold)
                            Text("${it.quantity} · ${if (it.inStock) "有货" else "缺货"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatPriceCent(it.priceCent), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DoyuOutlinedButton("保存到我的拼豆", onClick = {}, modifier = Modifier.weight(1f))
                DoyuPrimaryButton("加入购物车", onClick = { navController.navigate(AppRoute.CART) }, modifier = Modifier.weight(1f))
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

@Composable
fun PatternHistoryScreen(navController: NavHostController) {
    Scaffold(topBar = { DoyuTopBar("生成记录", canGoBack = true, onBack = { navController.popBackStack() }) }) { padding ->
        DoyuPage(padding) {
            repo.history().forEach { job ->
                DoyuCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(job.inputName, style = MaterialTheme.typography.titleMedium)
                            Text("${job.beadSize} · ${job.difficulty} · ${job.style}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TagChip(if (job.status == PatternJobStatus.SUCCEEDED) "已完成" else "处理中")
                    }
                    Spacer(Modifier.height(10.dp))
                    DoyuOutlinedButton("查看", onClick = { navController.navigate(AppRoute.patternResult(job.patternId ?: "pattern_001")) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
