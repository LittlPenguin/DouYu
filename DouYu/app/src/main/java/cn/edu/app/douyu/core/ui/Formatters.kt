package cn.edu.app.douyu.core.ui

import java.util.Locale

fun formatPriceCent(amountCent: Int): String {
    return String.format(Locale.US, "¥%.2f", amountCent / 100.0)
}
