package cn.edu.app.douyu.core;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

public class UiCopyMojibakeTest {
    private static final String[] MOJIBAKE_FRAGMENTS = {
            "閸", "缁", "闁", "鐠", "锟", "涓", "绉", "鍥", "璇", "鎴", "�"
    };

    @Test
    public void uiCopyConstantsDoNotContainMojibakeFragments() throws Exception {
        for (Field field : UiCopy.class.getDeclaredFields()) {
            if (field.getType() == String.class) {
                assertClean(field.getName(), (String) field.get(null));
            }
        }
    }

    private static void assertClean(String name, String value) {
        for (String fragment : MOJIBAKE_FRAGMENTS) {
            assertFalse(name + " contains mojibake fragment " + fragment + ": " + value,
                    value != null && value.contains(fragment));
        }
    }
}
