package cn.edu.app.douyu.core;

import java.text.DecimalFormat;

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
