package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

public class SourceMojibakeSpotTest {
    private static final String[] KEY_FILES = {
            "src/main/java/cn/edu/app/douyu/ui/BaseListFragment.java",
            "src/main/java/cn/edu/app/douyu/ui/XmlPageActivity.java",
            "src/main/java/cn/edu/app/douyu/core/UiCopy.java",
            "src/main/java/cn/edu/app/douyu/feature/ai/AiFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/message/MessagesFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/ai/AiFlowActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/commerce/CommerceFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/commerce/ProductDetailActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/message/ConversationActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/message/NotificationDetailActivity.java",
            "src/androidTest/java/cn/edu/app/douyu/VisualSmokeInstrumentedTest.java",
            "src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java",
            "src/main/res/layout/fragment_commerce_home.xml",
            "src/main/res/layout/fragment_ai_home.xml",
            "src/main/res/layout/fragment_messages_home.xml",
            "src/main/res/layout/fragment_profile_home.xml",
            "src/main/res/layout/activity_ai_flow.xml",
            "src/main/res/layout/activity_conversation.xml",
            "src/main/res/layout/activity_notification_detail.xml",
            "src/main/res/layout/activity_post_detail.xml",
            "src/main/res/layout/activity_product_detail.xml",
            "src/main/res/layout/activity_payment_boundary.xml",
            "src/main/res/layout/activity_main.xml",
            "src/main/res/values/strings.xml"
    };

    private static final String[] MOJIBAKE_FRAGMENTS = {
            chars(0x95ab, 0x6c31, 0x7161), chars(0x7459, 0x55da),
            chars(0x95b8), chars(0x95c1), chars(0x9420), chars(0x951f), chars(0xfffd),
            chars(0x7ec9, 0x4f77, 0x4fca), chars(0x9365, 0x5267, 0x710a),
            chars(0x93b4, 0x6220, 0x6b91), chars(0x935f, 0x55d7, 0x7144),
            chars(0x5a11, 0x581f, 0x4f05), chars(0x93c2, 0x677f),
            chars(0x7487, 0xfe3d, 0x510f), chars(0x6d93, 0x5a41, 0x7d36),
            chars(0x93c8, 0xe046, 0x6ae5)
    };

    @Test
    public void criticalJavaAndXmlFilesDoNotContainMojibake() throws IOException {
        for (String file : KEY_FILES) {
            String content = new String(Files.readAllBytes(Path.of(file)), StandardCharsets.UTF_8);
            for (String fragment : MOJIBAKE_FRAGMENTS) {
                assertFalse(file + " contains mojibake fragment " + fragment,
                        content.contains(fragment));
            }
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
