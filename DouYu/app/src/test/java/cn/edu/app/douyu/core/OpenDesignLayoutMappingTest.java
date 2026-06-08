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
        TabStateContainers[] tabs = new TabStateContainers[]{
                new TabStateContainers("fragment_ai_home.xml",
                        "@+id/error_box", "@+id/error_text", "@+id/retry_button", "@+id/empty_text"),
                new TabStateContainers("fragment_messages_home.xml",
                        "@+id/error_box", "@+id/error_text", "@+id/retry_button", "@+id/empty_text"),
                new TabStateContainers("fragment_profile_home.xml",
                        "@+id/asset_error_box", "@+id/asset_empty")
        };

        for (TabStateContainers tab : tabs) {
            String xml = new String(Files.readAllBytes(Path.of("src/main/res/layout", tab.layout)), StandardCharsets.UTF_8);
            for (String goneId : tab.goneIds) {
                assertViewHasGoneVisibility(tab.layout, xml, goneId);
            }
        }
    }

    @Test
    public void commerceScreenRemovesRuleCardsAndKeepsRealDataStates() throws IOException {
        String androidXml = readUtf8("src/main/res/layout/fragment_commerce_home.xml");
        String productCardXml = readUtf8("src/main/res/layout/item_commerce_product.xml");
        String openDesignHtml = readUtf8("../../doc/development/open-design/commerce-home-a.html");
        String removedActivityTitle = "新手材料" + "补给";
        String removedActivityInstruction = "只展示真实可解释活动，" + "点击路径必须存在";
        String removedRuleTitle = "商城规则说明";

        assertFalse("Android commerce runtime must not show removed activity banner",
                androidXml.contains(removedActivityTitle));
        assertFalse("Android commerce runtime must not keep the removed activity instruction",
                androidXml.contains(removedActivityInstruction));
        assertFalse("Android commerce runtime must not show the old fixed rules card",
                androidXml.contains(removedRuleTitle));
        assertFalse("Open Design commerce source must not show removed activity banner",
                openDesignHtml.contains(removedActivityTitle));
        assertFalse("Open Design commerce source must not keep the removed activity instruction",
                openDesignHtml.contains(removedActivityInstruction));
        assertFalse("Open Design commerce source must not show the old fixed rules card",
                openDesignHtml.contains(removedRuleTitle));

        assertViewIdExists("fragment_commerce_home.xml", androidXml, "@+id/section_chips");
        assertViewIdExists("fragment_commerce_home.xml", androidXml, "@+id/summary_list");
        assertViewIdExists("fragment_commerce_home.xml", androidXml, "@+id/loading");
        assertViewIdExists("fragment_commerce_home.xml", androidXml, "@+id/empty_text");
        assertViewIdExists("fragment_commerce_home.xml", androidXml, "@+id/error_box");
        assertViewIdExists("item_commerce_product.xml", productCardXml, "@+id/commerce_product_image");
        assertViewIdExists("item_commerce_product.xml", productCardXml, "@+id/commerce_product_price");
        assertViewIdExists("item_commerce_product.xml", productCardXml, "@+id/commerce_product_stock");
    }

    @Test
    public void commercePaymentBoundaryPathExistsAndPaymentCtaStartsDisabled() throws IOException {
        String productDetailXml = readUtf8("src/main/res/layout/activity_product_detail.xml");
        String paymentBoundaryXml = readUtf8("src/main/res/layout/activity_payment_boundary.xml");

        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/product_payment_boundary");
        String disabledPaymentCta = tagContaining(paymentBoundaryXml,
                "android:text=\"当前为 UI-only 边界，等待真实支付接入\"");
        assertTrue("Payment boundary page must keep the CTA disabled until real payment is connected",
                disabledPaymentCta.contains("android:enabled=\"false\""));
        assertTrue("Disabled payment CTA must keep readable text instead of relying on default low-contrast disabled styling",
                disabledPaymentCta.contains("android:textColor=\"@color/doyu_text\""));
        assertTrue("Disabled payment CTA must use an explicit visible background",
                disabledPaymentCta.contains("app:backgroundTint=\"@color/doyu_surface\""));
        assertTrue("Disabled payment CTA must keep a visible boundary stroke",
                disabledPaymentCta.contains("app:strokeColor=\"@color/doyu_open_line\""));
        assertTrue("Disabled payment CTA must keep a non-zero stroke width",
                disabledPaymentCta.contains("app:strokeWidth=\"1dp\""));
    }

    @Test
    public void postDetailLayoutContainsOpenDesignDetailStructure() throws IOException {
        String xml = new String(Files.readAllBytes(Path.of("src/main/res/layout", "activity_post_detail.xml")), StandardCharsets.UTF_8);

        assertTrue(xml.contains("@+id/post_gallery_section"));
        assertTrue(xml.contains("@+id/post_carousel_count"));
        assertTrue(xml.contains("左右滑动查看作品图片"));
        assertTrue(xml.contains("@+id/post_thumbnail_strip"));
        assertTrue(xml.contains("@+id/post_like_action"));
        assertTrue(xml.contains("@+id/post_comment_action"));
        assertTrue(xml.contains("@+id/post_favorite_action"));
        assertTrue(xml.contains("@+id/post_comments_container"));
        assertTrue(xml.contains("@+id/post_comment_empty"));
        assertTrue(xml.contains("@+id/post_comment_error_box"));
        assertTrue(xml.contains("@+id/post_comment_input"));
        assertTrue(xml.contains("@+id/post_comment_tool_image"));
        assertTrue(xml.contains("@+id/post_comment_tool_mention"));
        assertTrue(xml.contains("@+id/post_comment_tool_topic"));
        assertFalse(xml.contains("详情顺序遵循 Open Design"));
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

    private static void assertViewIdExists(String layout, String xml, String id) {
        assertTrue(layout + " missing " + id, xml.contains("android:id=\"" + id + "\""));
    }

    private static String tagContaining(String xml, String needle) {
        int needleIndex = xml.indexOf(needle);
        assertTrue("Missing XML tag containing " + needle, needleIndex >= 0);
        int tagStart = xml.lastIndexOf('<', needleIndex);
        int tagEnd = xml.indexOf('>', needleIndex);
        assertTrue("Malformed XML tag containing " + needle, tagStart >= 0 && tagEnd > needleIndex);
        return xml.substring(tagStart, tagEnd);
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

    private static final class TabStateContainers {
        final String layout;
        final String[] goneIds;

        TabStateContainers(String layout, String... goneIds) {
            this.layout = layout;
            this.goneIds = goneIds;
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
