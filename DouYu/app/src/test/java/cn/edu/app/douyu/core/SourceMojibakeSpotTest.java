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
            "閫氱煡", "瑙嗚",
            "閸", "闁", "鐠", "锟", "�", "绉佷俊", "鍥剧焊", "鎴戠殑",
            "鍟嗗煄", "娑堟伅", "鏂板", "璇︽儏", "涓婁紶", "鏈櫥"
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
}
