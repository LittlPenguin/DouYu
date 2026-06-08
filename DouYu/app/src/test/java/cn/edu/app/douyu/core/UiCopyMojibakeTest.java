package cn.edu.app.douyu.core;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

public class UiCopyMojibakeTest {
    private static final String[] MOJIBAKE_FRAGMENTS = {
            chars(0x95b8), chars(0x7f02), chars(0x95c1), chars(0x9420), chars(0x951f),
            chars(0x5a11), chars(0x7ec9), chars(0x95b8), chars(0x9420), chars(0x95b9),
            chars(0x951f), chars(0xfffd)
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

    private static String chars(int... values) {
        char[] chars = new char[values.length];
        for (int i = 0; i < values.length; i++) {
            chars[i] = (char) values[i];
        }
        return new String(chars);
    }
}
