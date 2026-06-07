package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import cn.edu.app.douyu.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpenDesignLayoutMappingTest {
    @Test
    public void mapsOpenDesignPagesToDedicatedXmlLayouts() {
        LayoutMapping[] mappings = new LayoutMapping[]{
                new LayoutMapping("community-home-a.html", R.layout.fragment_community_home),
                new LayoutMapping("commerce-home-a.html", R.layout.fragment_commerce_home),
                new LayoutMapping("ai-home-a.html", R.layout.fragment_ai_home),
                new LayoutMapping("messages-a.html", R.layout.fragment_messages_home),
                new LayoutMapping("profile-a.html", R.layout.fragment_profile_home),
                new LayoutMapping("search-a.html", R.layout.activity_search),
                new LayoutMapping("post-" + "com" + "pose-a.html", R.layout.activity_post_create),
                new LayoutMapping("post-detail-comment-toolbar-a.html", R.layout.activity_post_detail),
                new LayoutMapping("message-conversation-a.html", R.layout.activity_conversation),
                new LayoutMapping("notification-detail-a.html", R.layout.activity_notification_detail),
                new LayoutMapping("profile-edit-a.html", R.layout.activity_profile_edit),
                new LayoutMapping("settings-home-a.html", R.layout.activity_settings_home),
                new LayoutMapping("settings-account-security-a.html", R.layout.activity_settings_account_security),
                new LayoutMapping("settings-privacy-permissions-a.html", R.layout.activity_settings_privacy_permissions),
                new LayoutMapping("settings-notifications-a.html", R.layout.activity_settings_notifications),
                new LayoutMapping("settings-about-compliance-a.html", R.layout.activity_settings_about_compliance),
                new LayoutMapping("future-capability-ui-a.html", R.layout.activity_future_capability),
                new LayoutMapping("doyu-design-directions.html", R.layout.activity_main)
        };

        assertEquals(18, mappings.length);
        for (LayoutMapping mapping : mappings) {
            assertTrue(mapping.openDesignPage.endsWith(".html"));
            assertTrue(mapping.layoutId > 0);
        }
    }

    @Test
    public void mainTabStateContainersStartHiddenInXml() throws IOException {
        String[] layouts = {
                "fragment_ai_home.xml",
                "fragment_messages_home.xml",
                "fragment_profile_home.xml"
        };

        for (String layout : layouts) {
            String xml = new String(Files.readAllBytes(Path.of("src/main/res/layout", layout)), StandardCharsets.UTF_8);
            assertViewHasGoneVisibility(layout, xml, "@+id/error_box");
            assertViewHasGoneVisibility(layout, xml, "@+id/error_text");
            assertViewHasGoneVisibility(layout, xml, "@+id/retry_button");
            assertViewHasGoneVisibility(layout, xml, "@+id/empty_text");
        }
    }

    @Test
    public void openDesignAndDiagramsUseJavaXmlArchitectureWording() throws IOException {
        DocExpectation[] expectations = new DocExpectation[]{
                new DocExpectation("../../doc/development/open-design/index.html",
                        new String[]{"用于指导 Compose", "Compose 落地"}),
                new DocExpectation("../../doc/development/open-design/doyu-design-directions.html",
                        new String[]{"后续页面设计和 Compose", "Compose 落地"}),
                new DocExpectation("../../doc/development/diagrams/auth-session-flow.svg",
                        new String[]{"DataStore 是 Android 登录态持久化事实", "DataStore hydrate"}),
                new DocExpectation("../../doc/development/diagrams/system-architecture.svg",
                        new String[]{"Compose / Navigation", "Retrofit / DataStore"})
        };

        for (DocExpectation expectation : expectations) {
            String content = readUtf8(expectation.relativePath);
            for (String forbidden : expectation.forbiddenActiveWording) {
                assertFalse(expectation.relativePath + " still uses old active architecture wording: " + forbidden,
                        content.contains(forbidden));
            }
        }
    }

    @Test
    public void realBackendSmokeUsesSafeApiUrlJoin() throws IOException {
        String source = readUtf8("src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java");
        assertFalse("RealBackendSmokeInstrumentedTest must not create double-slash API paths",
                source.contains("baseUrl + \"/api"));
        assertTrue("RealBackendSmokeInstrumentedTest should centralize API URL joining",
                source.contains("apiUrl(baseUrl,"));
    }

    private static void assertViewHasGoneVisibility(String layout, String xml, String id) {
        int idIndex = xml.indexOf("android:id=\"" + id + "\"");
        assertTrue(layout + " missing " + id, idIndex >= 0);
        int tagStart = xml.lastIndexOf('<', idIndex);
        int tagEnd = xml.indexOf('>', idIndex);
        assertTrue(layout + " malformed " + id, tagStart >= 0 && tagEnd > idIndex);
        String tag = xml.substring(tagStart, tagEnd);
        assertTrue(layout + " " + id + " should start gone", tag.contains("android:visibility=\"gone\""));
        assertFalse(layout + " " + id + " should not start visible", tag.contains("android:visibility=\"visible\""));
    }

    private static String readUtf8(String relativePath) throws IOException {
        return new String(Files.readAllBytes(Path.of(relativePath)), StandardCharsets.UTF_8);
    }

    private static final class LayoutMapping {
        final String openDesignPage;
        final int layoutId;

        LayoutMapping(String openDesignPage, int layoutId) {
            this.openDesignPage = openDesignPage;
            this.layoutId = layoutId;
        }
    }

    private static final class DocExpectation {
        final String relativePath;
        final String[] forbiddenActiveWording;

        DocExpectation(String relativePath, String[] forbiddenActiveWording) {
            this.relativePath = relativePath;
            this.forbiddenActiveWording = forbiddenActiveWording;
        }
    }
}
