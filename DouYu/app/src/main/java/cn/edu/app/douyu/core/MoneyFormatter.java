package cn.edu.app.douyu.core;

import java.text.DecimalFormat;
/**
 * 金额格式工具：把后端以分为单位的价格转换成页面展示金额。
 */

public final class MoneyFormatter {
    private static final DecimalFormat YUAN = new DecimalFormat("0.00");

    private MoneyFormatter() {
    }

    public static String centsToYuan(Integer cents) {
        if (cents == null) {
            return "--";
        }
        return "¥" + YUAN.format(cents / 100.0d);
    }
}
