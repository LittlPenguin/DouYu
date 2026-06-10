package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SourceMojibakeSpotTest {
    private static final String[] KEY_FILES = {
            "src/main/java/cn/edu/app/douyu/ui/BaseListFragment.java",
            "src/main/java/cn/edu/app/douyu/ui/XmlPageActivity.java",
            "src/main/java/cn/edu/app/douyu/core/UiCopy.java",
            "src/main/java/cn/edu/app/douyu/feature/message/MessagesFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/message/MessageHomeAdapter.java",
            "src/main/java/cn/edu/app/douyu/feature/message/NotificationTypes.java",
            "src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/profile/ProfileAssetAdapter.java",
            "src/main/java/cn/edu/app/douyu/feature/community/PostCreateActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/community/PostCaptureActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/commerce/CommerceFragment.java",
            "src/main/java/cn/edu/app/douyu/feature/commerce/ProductDetailActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/message/ConversationActivity.java",
            "src/main/java/cn/edu/app/douyu/feature/message/NotificationDetailActivity.java",
            "src/androidTest/java/cn/edu/app/douyu/VisualSmokeInstrumentedTest.java",
            "src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java",
            "src/main/res/layout/fragment_commerce_home.xml",
            "src/main/res/layout/fragment_messages_home.xml",
            "src/main/res/layout/fragment_profile_home.xml",
            "src/main/res/layout/item_profile_asset.xml",
            "src/main/res/layout/activity_conversation.xml",
            "src/main/res/layout/activity_notification_detail.xml",
            "src/main/res/layout/activity_post_create.xml",
            "src/main/res/layout/activity_post_capture.xml",
            "src/main/res/layout/activity_post_detail.xml",
            "src/main/res/layout/activity_product_detail.xml",
            "src/main/res/layout/activity_main.xml",
            "src/main/res/drawable/ic_action_settings.xml",
            "src/main/res/drawable/ic_tab_upload.xml",
            "src/main/res/drawable/ic_tab_messages.xml",
            "src/main/res/drawable/ic_tab_profile.xml",
            "src/main/res/values/strings.xml"
    };

    private static final String[] OPEN_DESIGN_FILES = {
            "../doc/development/open-design/community-home-a.html",
            "../doc/development/open-design/commerce-home-a.html",
            "../doc/development/open-design/messages-a.html",
            "../doc/development/open-design/profile-a.html",
            "../doc/development/open-design/post-" + "com" + "pose-a.html",
            "../doc/development/open-design/index.html",
            "../doc/development/open-design/doyu-design-directions.html",
            "../doc/development/open-design/design-decision.md"
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

    private static final String[] ENGLISH_UI_REGRESSIONS = {
            "Not signed in",
            "Sign in to view",
            "Likes source",
            "Liked posts",
            "Favorite posts",
            "Load failed",
            "Service is temporarily unavailable",
            "Cannot connect to the service",
            "No liked posts yet.",
            "No favorite posts yet.",
            "Review notification",
            "System notification",
            "Interaction notification"
    };

    @Test
    public void criticalJavaAndXmlFilesDoNotContainMojibake() throws IOException {
        for (String file : KEY_FILES) {
            assertTrue(file + " must exist", Files.exists(Path.of(file)));
            String content = new String(Files.readAllBytes(Path.of(file)), StandardCharsets.UTF_8);
            for (String fragment : MOJIBAKE_FRAGMENTS) {
                assertFalse(file + " contains mojibake fragment " + fragment,
                        content.contains(fragment));
            }
            for (String fragment : ENGLISH_UI_REGRESSIONS) {
                assertFalse(file + " contains English UI regression " + fragment,
                        content.contains(fragment));
            }
        }
    }

    @Test
    public void retainedOpenDesignFilesDoNotContainMojibakeOrRemovedEntryLinks() throws IOException {
        String replacement = chars(0xfffd);
        for (String file : OPEN_DESIGN_FILES) {
            Path path = Path.of("..").resolve(file).normalize();
            assertTrue(file + " must exist", Files.exists(path));
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            assertFalse(file + " contains replacement character", content.contains(replacement));
            assertFalse(file + " links removed AI page", content.contains("ai-home-a.html"));
            assertFalse(file + " links removed future page", content.contains("future-capability-ui-a.html"));
            assertFalse(file + " keeps old post entry wording", content.contains("顶部新增菜单进入上传帖子"));
            assertFalse(file + " keeps old bottom-tab wording", content.contains("不作为底部主 Tab"));
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
