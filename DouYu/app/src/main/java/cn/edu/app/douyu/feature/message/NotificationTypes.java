package cn.edu.app.douyu.feature.message;

import java.util.Locale;

import cn.edu.app.douyu.R;

/**
 * Maps backend notification {@code type} strings to icons, hero pill labels and styles.
 * Categories follow the open-design notification taxonomy: 审核 / 互动 / 任务 / 系统.
 */
final class NotificationTypes {
    private NotificationTypes() {
    }

    static int iconRes(String type) {
        String key = upper(type);
        if (contains(key, "AT", "MENTION")) {
            return R.drawable.ic_notify_at;
        }
        if (contains(key, "PATTERN", "AI", "JOB", "TASK")) {
            return R.drawable.ic_action_ai;
        }
        return R.drawable.ic_notify_bell;
    }

    static String heroLabel(String type) {
        String key = upper(type);
        if (contains(key, "AUDIT", "REVIEW")) {
            return "审核通知";
        }
        if (contains(key, "SYSTEM", "COMPLIANCE", "SECURITY")) {
            return "系统通知";
        }
        if (contains(key, "PATTERN", "AI", "JOB", "TASK")) {
            return "任务通知";
        }
        if (contains(key, "LIKE", "FAVORITE", "COLLECT", "COMMENT", "AT", "MENTION")) {
            return "互动通知";
        }
        return "通知";
    }

    static int heroPillBg(String type) {
        String key = upper(type);
        if (contains(key, "AUDIT", "REVIEW")) {
            return R.drawable.bg_pill_warn;
        }
        if (contains(key, "PATTERN", "AI", "JOB", "TASK", "LIKE", "FAVORITE", "COLLECT", "COMMENT", "AT", "MENTION")) {
            return R.drawable.bg_pill_ok;
        }
        return R.drawable.bg_chip_plain;
    }

    static int heroPillColor(String type) {
        String key = upper(type);
        if (contains(key, "AUDIT", "REVIEW")) {
            return R.color.doyu_warn;
        }
        if (contains(key, "PATTERN", "AI", "JOB", "TASK", "LIKE", "FAVORITE", "COLLECT", "COMMENT", "AT", "MENTION")) {
            return R.color.doyu_mint_deep;
        }
        return R.color.doyu_text_muted;
    }

    /** Renders an ISO-8601 instant (e.g. 2026-06-07T12:30:00Z) as HH:mm, or empty when absent. */
    static String shortTime(String iso) {
        if (iso == null || iso.length() < 16 || iso.charAt(10) != 'T') {
            return "";
        }
        return iso.substring(11, 16);
    }

    private static String upper(String type) {
        return type == null ? "" : type.toUpperCase(Locale.ROOT);
    }

    private static boolean contains(String key, String... needles) {
        for (String needle : needles) {
            if (key.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
