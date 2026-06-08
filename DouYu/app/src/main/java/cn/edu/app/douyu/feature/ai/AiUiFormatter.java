package cn.edu.app.douyu.feature.ai;

import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;

public final class AiUiFormatter {
    private AiUiFormatter() {
    }

    public static String statusLabel(String status) {
        if ("PENDING".equals(status)) {
            return "排队中";
        }
        if ("PROCESSING".equals(status)) {
            return "生成中";
        }
        if ("SUCCEEDED".equals(status)) {
            return "已完成";
        }
        if ("FAILED".equals(status)) {
            return "生成失败";
        }
        if ("CANCELED".equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }

    public static int progressPercent(Double progress, String status) {
        double value;
        if (progress == null) {
            if ("SUCCEEDED".equals(status)) {
                value = 1.0d;
            } else if ("PROCESSING".equals(status)) {
                value = 0.5d;
            } else {
                value = 0.0d;
            }
        } else {
            value = progress;
        }
        int percent = (int) Math.round(value * 100.0d);
        return Math.max(0, Math.min(100, percent));
    }

    public static boolean canCancel(String status) {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }

    public static boolean hasPattern(PatternJob job) {
        return job != null
                && ((job.patternId != null && !job.patternId.isEmpty())
                || (job.patternAsset != null && canFavorite(job.patternAsset)));
    }

    public static boolean canFavorite(PatternAsset pattern) {
        return pattern != null && pattern.patternId != null && !pattern.patternId.isEmpty();
    }

    public static String failureReason(String reason) {
        if (reason == null || reason.isEmpty()) {
            return "后端未返回失败原因，请返回后重试或重新选择图片。";
        }
        return reason;
    }

    public static String valueOrFallback(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
