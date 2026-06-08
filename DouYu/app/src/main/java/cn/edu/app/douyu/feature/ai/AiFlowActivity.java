package cn.edu.app.douyu.feature.ai;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.ProgressBar;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.FavoriteResult;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.model.PresignUploadResponse;
import cn.edu.app.douyu.network.ApiException;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class AiFlowActivity extends XmlPageActivity {
    private static final String AI_INPUT_USAGE = "AI_INPUT";
    private static final String DEFAULT_BEAD_SIZE = "MM_5";
    private static final String DEFAULT_TARGET_SIZE = "28x28";
    private static final String DEFAULT_DIFFICULTY = "NORMAL";
    private static final String DEFAULT_PALETTE_ID = "standard";
    private static final String DEFAULT_STYLE = "CLASSIC";

    private Uri selectedImageUri;
    private String selectedFileName;
    private String selectedMimeType;
    private PatternJob currentJob;
    private PatternAsset currentPattern;

    private MaterialButton createJobButton;
    private MaterialButton cancelJobButton;
    private MaterialButton favoritePatternButton;
    private ProgressBar statusProgress;

    @Override
    protected int layoutRes() {
        return R.layout.activity_ai_flow;
    }

    @Override
    protected String title() {
        return "AI 图纸详情";
    }

    @Override
    protected void bindViews() {
        createJobButton = findViewById(R.id.ai_create_job);
        cancelJobButton = findViewById(R.id.ai_cancel_job);
        favoritePatternButton = findViewById(R.id.ai_favorite_pattern);
        statusProgress = findViewById(R.id.ai_status_progress);

        selectedImageUri = getIntent().getData();
        selectedFileName = selectedImageUri == null ? "" : displayName(selectedImageUri);
        selectedMimeType = selectedImageUri == null ? "" : mimeType(selectedImageUri);

        createJobButton.setOnClickListener(v -> createJob());
        cancelJobButton.setOnClickListener(v -> cancelJob());
        favoritePatternButton.setOnClickListener(v -> favoritePattern());
        findViewById(R.id.ai_open_camera).setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));

        bindInitialState();
    }

    private void bindInitialState() {
        String uploadedFileId = extra(IntentExtras.UPLOADED_FILE_ID);
        String jobId = extra(IntentExtras.JOB_ID);
        String patternId = extra(IntentExtras.PATTERN_ID);

        if (selectedImageUri != null) {
            setText(R.id.ai_uploaded_file_id, "已选择图片：" + valueOrFallback(selectedFileName, "本地图片"));
            setText(R.id.ai_input_status, "图片待上传。确认参数后创建真实 AI 图纸任务。");
            createJobButton.setEnabled(true);
        } else {
            setText(R.id.ai_uploaded_file_id, valueOrFallback(readableUploadedFile(uploadedFileId), "未选择上传文件"));
            createJobButton.setEnabled(!uploadedFileId.isEmpty() && jobId.isEmpty() && patternId.isEmpty());
        }

        if (!jobId.isEmpty()) {
            setText(R.id.ai_job_id, "正在加载任务状态：" + jobId);
            setText(R.id.ai_pattern_id, valueOrFallback(patternId, "等待任务返回结果图纸"));
            loadDetail(
                    repository -> repository.patternJob(jobId),
                    this::renderJob,
                    this::renderDetailError
            );
        } else if (!patternId.isEmpty()) {
            setText(R.id.ai_job_id, "未提供 jobId，仅加载图纸详情。");
            setText(R.id.ai_pattern_id, "正在加载图纸：" + patternId);
            loadDetail(
                    repository -> repository.pattern(patternId),
                    this::renderPattern,
                    this::renderDetailError
            );
        } else if (selectedImageUri == null && uploadedFileId.isEmpty()) {
            setText(R.id.ai_status_label, "未创建");
            setText(R.id.ai_job_id, "请从 AI 首页选择相册图片、拍照，或从历史任务进入详情。");
            setText(R.id.ai_status_message, "没有图片或任务 ID 时不会创建本地假任务。");
        }
    }

    private void createJob() {
        createJobButton.setEnabled(false);
        setText(R.id.ai_status_label, "上传中");
        setText(R.id.ai_status_message, "正在上传图片并创建 AI 图纸任务。");
        statusProgress.setProgress(0);
        loadDetail(
                this::createJobWithSelectedInput,
                this::renderJob,
                this::renderDetailError
        );
    }

    private PatternJob createJobWithSelectedInput(DoyuRepository repository) throws Exception {
        String inputFileId = extra(IntentExtras.UPLOADED_FILE_ID);
        if (inputFileId.isEmpty()) {
            if (selectedImageUri == null) {
                throw new ApiException("请先选择图片。");
            }
            byte[] bytes = readBytes(selectedImageUri);
            String mimeType = valueOrFallback(selectedMimeType, "image/jpeg");
            String fileName = valueOrFallback(selectedFileName, "ai-input.jpg");
            PresignUploadResponse presign = repository.uploadPresign(AI_INPUT_USAGE, mimeType, bytes.length, fileName);
            if (presign == null || presign.uploadUrl == null || presign.uploadUrl.isEmpty()
                    || presign.fileKey == null || presign.fileKey.isEmpty()) {
                throw new ApiException("上传预签名信息不完整。");
            }
            repository.uploadPresignedBytes(presign.uploadUrl, bytes, mimeType, presign.headers);
            FileAsset file = repository.uploadConfirm(presign.fileKey, AI_INPUT_USAGE, mimeType, bytes.length, null, null);
            inputFileId = file == null ? "" : file.fileId;
        }
        if (inputFileId == null || inputFileId.isEmpty()) {
            throw new ApiException("上传完成后未获得 fileId。");
        }
        return repository.createPatternJob(
                inputFileId,
                DEFAULT_BEAD_SIZE,
                DEFAULT_TARGET_SIZE,
                DEFAULT_DIFFICULTY,
                DEFAULT_PALETTE_ID,
                DEFAULT_STYLE
        );
    }

    private void cancelJob() {
        if (currentJob == null || currentJob.jobId == null || currentJob.jobId.isEmpty()) {
            return;
        }
        cancelJobButton.setEnabled(false);
        setText(R.id.ai_status_message, "正在取消任务：" + currentJob.jobId);
        loadDetail(
                repository -> repository.cancelPatternJob(currentJob.jobId),
                this::renderJob,
                this::renderDetailError
        );
    }

    private void favoritePattern() {
        if (currentPattern == null || currentPattern.patternId == null || currentPattern.patternId.isEmpty()) {
            return;
        }
        favoritePatternButton.setEnabled(false);
        setText(R.id.ai_pattern_summary, "正在收藏图纸：" + currentPattern.patternId);
        loadDetail(
                repository -> repository.favoritePattern(currentPattern.patternId),
                this::renderFavoriteResult,
                this::renderDetailError
        );
    }

    private void renderJob(PatternJob job) {
        if (job == null) {
            renderDetailError(LoadState.ERROR, "任务不存在或无权访问。");
            return;
        }
        currentJob = job;
        int progress = AiUiFormatter.progressPercent(job.progress, job.status);
        setText(R.id.ai_status_label, AiUiFormatter.statusLabel(job.status));
        setText(R.id.ai_uploaded_file_id,
                "上传文件：" + first(job.inputName, job.inputFileId, extra(IntentExtras.UPLOADED_FILE_ID), "未知"));
        setText(R.id.ai_job_id,
                "任务 ID：" + valueOrFallback(job.jobId, "未知") + "\n"
                        + "状态：" + valueOrFallback(job.status, "未知") + "\n"
                        + "规格：" + valueOrFallback(job.beadSize, "未知") + " / "
                        + valueOrFallback(job.targetSize, "未知") + "\n"
                        + "难度：" + valueOrFallback(job.difficulty, "未知") + "\n"
                        + "色卡：" + valueOrFallback(job.paletteName, "未知") + "\n"
                        + "风格：" + valueOrFallback(job.style, "未知"));
        if ("FAILED".equals(job.status)) {
            setText(R.id.ai_status_message, "失败原因：" + AiUiFormatter.failureReason(job.failureReason));
        } else {
            setText(R.id.ai_status_message, "进度：" + progress + "%。状态以后端任务返回为准。");
        }
        statusProgress.setProgress(progress);
        cancelJobButton.setEnabled(AiUiFormatter.canCancel(job.status));
        createJobButton.setEnabled(false);

        if (job.patternAsset != null) {
            renderPattern(job.patternAsset);
        } else if (job.patternId != null && !job.patternId.isEmpty()) {
            setText(R.id.ai_pattern_id, "正在加载图纸：" + job.patternId);
            loadDetail(
                    repository -> repository.pattern(job.patternId),
                    this::renderPattern,
                    this::renderPatternError
            );
        } else {
            currentPattern = null;
            setText(R.id.ai_pattern_id, "结果图纸：" + valueOrFallback(job.patternId, "暂未生成"));
            setText(R.id.ai_pattern_summary, "任务完成并返回 patternId 后才展示图纸详情。");
            favoritePatternButton.setEnabled(false);
        }
    }

    private void renderPattern(PatternAsset pattern) {
        if (pattern == null) {
            renderPatternError(LoadState.ERROR, "图纸不存在或无权访问。");
            return;
        }
        currentPattern = pattern;
        setText(R.id.ai_pattern_id,
                "图纸 ID：" + valueOrFallback(pattern.patternId, "未知") + "\n"
                        + "标题：" + valueOrFallback(pattern.title, "拼豆图纸") + "\n"
                        + "状态：" + valueOrFallback(pattern.status, "未知"));
        setText(R.id.ai_pattern_summary,
                "规格：" + valueOrFallback(pattern.beadSize, "未知") + "\n"
                        + "尺寸：" + cellCount(pattern.widthCells) + " x " + cellCount(pattern.heightCells) + "\n"
                        + "总豆数：" + cellCount(pattern.totalBeads) + "\n"
                        + "色卡：" + valueOrFallback(pattern.paletteName, "未知"));
        favoritePatternButton.setEnabled(AiUiFormatter.canFavorite(pattern));
    }

    private void renderFavoriteResult(FavoriteResult result) {
        boolean favorited = result != null && Boolean.TRUE.equals(result.favorited);
        favoritePatternButton.setText(favorited ? "已收藏" : "收藏图纸");
        favoritePatternButton.setEnabled(false);
        setText(R.id.ai_pattern_summary, favorited ? "图纸已收藏。收藏状态以后端返回为准。" : "收藏接口未返回成功状态。");
    }

    private void renderDetailError(LoadState state, String message) {
        setText(R.id.ai_status_label, state == LoadState.LOGIN_REQUIRED ? "需登录" : "加载失败");
        setText(R.id.ai_status_message, message);
        statusProgress.setProgress(0);
        cancelJobButton.setEnabled(false);
        createJobButton.setEnabled(selectedImageUri != null || !extra(IntentExtras.UPLOADED_FILE_ID).isEmpty());
    }

    private void renderPatternError(LoadState state, String message) {
        setText(R.id.ai_pattern_id, state == LoadState.LOGIN_REQUIRED ? "需要登录后才能查看图纸详情。" : message);
        setText(R.id.ai_pattern_summary, "图纸详情未加载成功，不使用本地假结果。");
        favoritePatternButton.setEnabled(false);
    }

    private String readableUploadedFile(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.endsWith("_visual_check")) {
            return "视觉验证上传文件";
        }
        return "上传文件：" + value;
    }

    private String displayName(Uri uri) {
        if ("file".equals(uri.getScheme())) {
            File file = new File(uri.getPath() == null ? "" : uri.getPath());
            return file.getName();
        }
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
            // Fallback below keeps the flow usable when metadata is unavailable.
        }
        return "ai-input.jpg";
    }

    private String mimeType(Uri uri) {
        String type = getContentResolver().getType(uri);
        return type == null || type.isEmpty() ? "image/jpeg" : type;
    }

    private byte[] readBytes(Uri uri) throws IOException {
        ContentResolver resolver = getContentResolver();
        try (InputStream input = resolver.openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) {
                throw new ApiException("无法读取所选图片。");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static String cellCount(Integer value) {
        return value == null ? "未知" : String.valueOf(value);
    }

    private static String first(String first, String second, String third, String fallback) {
        if (first != null && !first.isEmpty()) {
            return first;
        }
        if (second != null && !second.isEmpty()) {
            return second;
        }
        if (third != null && !third.isEmpty()) {
            return third;
        }
        return fallback;
    }
}
