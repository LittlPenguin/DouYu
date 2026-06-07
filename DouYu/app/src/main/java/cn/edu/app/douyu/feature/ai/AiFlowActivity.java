package cn.edu.app.douyu.feature.ai;

import android.content.Intent;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class AiFlowActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_ai_flow;
    }

    @Override
    protected String title() {
        return "AI 图纸流程";
    }

    @Override
    protected void bindViews() {
        String uploadedFileId = extra(IntentExtras.UPLOADED_FILE_ID);
        String jobId = extra(IntentExtras.JOB_ID);
        String patternId = extra(IntentExtras.PATTERN_ID);
        setText(R.id.ai_uploaded_file_id, valueOrFallback(uploadedFileId, "未选择上传文件"));
        if (!jobId.isEmpty()) {
            setText(R.id.ai_job_id, "正在加载任务状态：" + jobId);
            setText(R.id.ai_pattern_id, valueOrFallback(patternId, "等待任务返回结果图纸"));
            loadDetail(
                    repository -> repository.patternJob(jobId),
                    this::renderJob,
                    (state, message) -> setText(R.id.ai_job_id, message)
            );
        } else if (!patternId.isEmpty()) {
            setText(R.id.ai_job_id, "未提供 jobId，仅加载图纸详情。");
            setText(R.id.ai_pattern_id, "正在加载图纸：" + patternId);
            loadDetail(
                    repository -> repository.pattern(patternId),
                    this::renderPattern,
                    (state, message) -> setText(R.id.ai_pattern_id, message)
            );
        } else {
            setText(R.id.ai_job_id, "缺少 jobId，无法请求 AI 任务状态。");
            setText(R.id.ai_pattern_id, "缺少 patternId，暂无结果图纸。");
        }
        findViewById(R.id.ai_open_camera).setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));
    }

    private void renderJob(PatternJob job) {
        if (job == null) {
            setText(R.id.ai_job_id, "任务不存在或无权访问。");
            return;
        }
        setText(R.id.ai_uploaded_file_id,
                "上传文件：" + first(job.inputName, job.inputFileId, extra(IntentExtras.UPLOADED_FILE_ID), "未知"));
        setText(R.id.ai_job_id,
                "任务 ID：" + valueOrFallback(job.jobId, "未知") + "\n"
                        + "状态：" + valueOrFallback(job.status, "未知") + "\n"
                        + "进度：" + progress(job.progress) + "\n"
                        + "规格：" + valueOrFallback(job.beadSize, "未知") + " / "
                        + valueOrFallback(job.targetSize, "未知") + "\n"
                        + "难度：" + valueOrFallback(job.difficulty, "未知") + "\n"
                        + "色卡：" + valueOrFallback(job.paletteName, "未知") + "\n"
                        + "失败原因：" + valueOrFallback(job.failureReason, "无"));
        if (job.patternAsset != null) {
            renderPattern(job.patternAsset);
        } else {
            setText(R.id.ai_pattern_id, "结果图纸：" + valueOrFallback(job.patternId, "暂未生成"));
        }
    }

    private void renderPattern(PatternAsset pattern) {
        if (pattern == null) {
            setText(R.id.ai_pattern_id, "图纸不存在或无权访问。");
            return;
        }
        setText(R.id.ai_pattern_id,
                "图纸 ID：" + valueOrFallback(pattern.patternId, "未知") + "\n"
                        + "标题：" + valueOrFallback(pattern.title, "拼豆图纸") + "\n"
                        + "状态：" + valueOrFallback(pattern.status, "未知") + "\n"
                        + "规格：" + valueOrFallback(pattern.beadSize, "未知") + "\n"
                        + "尺寸：" + cellCount(pattern.widthCells) + " x " + cellCount(pattern.heightCells) + "\n"
                        + "总豆数：" + cellCount(pattern.totalBeads) + "\n"
                        + "色卡：" + valueOrFallback(pattern.paletteName, "未知"));
    }

    private static String progress(Double progress) {
        if (progress == null) {
            return "未知";
        }
        return Math.round(progress * 100.0d) + "%";
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
