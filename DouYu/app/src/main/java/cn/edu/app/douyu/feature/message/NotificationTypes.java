package cn.edu.app.douyu.feature.message;

import java.util.Locale;

import cn.edu.app.douyu.R;

final class NotificationTypes {
    private NotificationTypes() {
    }

    static int iconRes(String type) {
        String key = upper(type);
        if (contains(key, "AT", "MENTION")) {
            return R.drawable.ic_notify_at;
        }
        return R.drawable.ic_notify_bell;
    }

    static String heroLabel(String type) {
        String key = upper(type);
        if (contains(key, "SYSTEM", "SECURITY")) {
            return "系统通知";
        }
        if (contains(key, "LIKE", "FAVORITE", "COLLECT", "COMMENT", "AT", "MENTION")) {
            return "互动通知";
        }
        return "通知";
    }

    static int heroPillBg(String type) {
        String key = upper(type);
        if (contains(key, "LIKE", "FAVORITE", "COLLECT", "COMMENT", "AT", "MENTION")) {
            return R.drawable.bg_pill_ok;
        }
        return R.drawable.bg_chip_plain;
    }

    static int heroPillColor(String type) {
        String key = upper(type);
        if (contains(key, "LIKE", "FAVORITE", "COLLECT", "COMMENT", "AT", "MENTION")) {
            return R.color.doyu_mint_deep;
        }
        return R.color.doyu_text_muted;
    }

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
