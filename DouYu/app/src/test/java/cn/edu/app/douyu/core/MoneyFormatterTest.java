package cn.edu.app.douyu.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MoneyFormatterTest {
    @Test
    public void formatsCentsAsYuan() {
        assertEquals("¥0.00", MoneyFormatter.centsToYuan(0));
        assertEquals("¥12.34", MoneyFormatter.centsToYuan(1234));
        assertEquals("--", MoneyFormatter.centsToYuan(null));
    }
}
