package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.ui.formatPriceCent
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun formatsCentAmountAsChineseYuan() {
        assertEquals("¥29.90", formatPriceCent(2990))
        assertEquals("¥0.00", formatPriceCent(0))
        assertEquals("¥128.05", formatPriceCent(12805))
    }
}
