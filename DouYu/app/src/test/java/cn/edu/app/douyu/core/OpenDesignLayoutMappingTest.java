package cn.edu.app.douyu.core;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilderFactory;

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
                new LayoutMapping("profile-posts-a.html", R.layout.activity_profile_posts),
                new LayoutMapping("profile-following-a.html", R.layout.activity_profile_users),
                new LayoutMapping("profile-followers-a.html", R.layout.activity_profile_users),
                new LayoutMapping("login-a.html", R.layout.activity_login),
                new LayoutMapping("register-a.html", R.layout.activity_register),
                new LayoutMapping("settings-home-a.html", R.layout.activity_settings_home),
                new LayoutMapping("settings-account-security-a.html", R.layout.activity_settings_account_security),
                new LayoutMapping("settings-privacy-permissions-a.html", R.layout.activity_settings_privacy_permissions),
                new LayoutMapping("settings-notifications-a.html", R.layout.activity_settings_notifications),
                new LayoutMapping("settings-about-compliance-a.html", R.layout.activity_settings_about_compliance),
                new LayoutMapping("future-capability-ui-a.html", R.layout.activity_future_capability),
                new LayoutMapping("doyu-design-directions.html", R.layout.activity_main)
        };

        assertEquals(23, mappings.length);
        for (LayoutMapping mapping : mappings) {
            assertTrue(mapping.openDesignPage.endsWith(".html"));
            assertTrue(mapping.layoutId > 0);
        }
    }

    @Test
    public void launcherIconUsesTBLogoSourceAndExpectedDensitySizes() throws Exception {
        String manifest = readUtf8("src/main/AndroidManifest.xml");

        assertTrue("Launcher icon must keep the manifest entry used by production builds",
                manifest.contains("android:icon=\"@mipmap/tb_launcher\""));
        assertTrue("Round launcher icon must keep the manifest entry used by production builds",
                manifest.contains("android:roundIcon=\"@mipmap/tb_launcher_round\""));
        assertEquals("Drawable TB logo must be copied from assets/TBLogo.png",
                sha256(Path.of("../../assets/TBLogo.png")),
                sha256(Path.of("src/main/res/drawable/tb_logo.png")));

        LauncherDensity[] densities = new LauncherDensity[]{
                new LauncherDensity("mipmap-mdpi", 48),
                new LauncherDensity("mipmap-hdpi", 72),
                new LauncherDensity("mipmap-xhdpi", 96),
                new LauncherDensity("mipmap-xxhdpi", 144),
                new LauncherDensity("mipmap-xxxhdpi", 192)
        };

        for (LauncherDensity density : densities) {
            assertPngDimensions(Path.of("src/main/res", density.directory, "tb_launcher.png"), density.size);
            assertPngDimensions(Path.of("src/main/res", density.directory, "tb_launcher_round.png"), density.size);
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
    public void postDetailLayoutContainsOpenDesignDetailStructure() throws Exception {
        String xml = new String(Files.readAllBytes(Path.of("src/main/res/layout", "activity_post_detail.xml")), StandardCharsets.UTF_8);
        Document document = parseXml(xml);

        assertTrue(xml.contains("@+id/post_gallery_section"));
        assertTrue(xml.contains("@+id/post_carousel_count"));
        assertTrue(xml.contains("左右滑动查看作品图片"));
        assertTrue(xml.contains("@+id/post_thumbnail_strip"));
        assertTrue(xml.contains("@+id/post_like_action"));
        assertTrue(xml.contains("@+id/post_comment_action"));
        assertTrue(xml.contains("@+id/post_favorite_action"));
        assertTrue(xml.contains("@+id/post_static_comment_trigger"));
        assertTrue(xml.contains("@+id/post_comment_editor"));
        assertTrue(xml.contains("@+id/post_comments_container"));
        assertTrue(xml.contains("@+id/post_comment_empty"));
        assertTrue(xml.contains("@+id/post_comment_error_box"));
        assertTrue(xml.contains("@+id/post_comment_input"));
        assertTrue(xml.contains("@+id/post_comment_tool_image"));
        assertTrue(xml.contains("@+id/post_comment_tool_mention"));
        assertTrue(xml.contains("@+id/post_comment_tool_topic"));
        assertTrue(xml.contains("@+id/post_comment_media_strip"));
        assertTrue(xml.contains("@+id/post_comment_chip_row"));
        assertViewHasGoneVisibility("activity_post_detail.xml", xml, "@+id/post_comment_editor");
        assertViewHasGoneVisibility("activity_post_detail.xml", xml, "@+id/post_comment_overlay_container");
        assertViewHasGoneVisibility("activity_post_detail.xml", xml, "@+id/post_comment_media_scroll");
        assertViewHasGoneVisibility("activity_post_detail.xml", xml, "@+id/post_comment_chip_scroll");

        Element editor = requireElementById(document, "@+id/post_comment_editor");
        Element overlay = requireElementById(document, "@+id/post_comment_overlay_container");
        Element staticBar = requireElementById(document, "@+id/post_comment_bar");
        assertTrue("Expanded post detail input must live in the keyboard overlay container",
                hasAncestorWithId(editor, "@+id/post_comment_overlay_container"));
        assertFalse("Expanded post detail input must not sit inside the scrolling comment section",
                hasAncestorWithId(editor, "@+id/post_comment_section"));
        assertFalse("Static comment bar must not contain the real send button",
                subtreeContainsId(staticBar, "@+id/post_comment_send"));
        assertFalse("Static comment bar must not contain image comment tools",
                subtreeContainsId(staticBar, "@+id/post_comment_tool_image"));
        assertFalse("Static comment bar must not contain mention tools",
                subtreeContainsId(staticBar, "@+id/post_comment_tool_mention"));
        assertFalse("Static comment bar must not contain topic tools",
                subtreeContainsId(staticBar, "@+id/post_comment_tool_topic"));
        assertTrue("Real comment input should remain inside the overlay subtree",
                subtreeContainsId(overlay, "@+id/post_comment_input"));
        assertTrue("Real comment send should remain inside the overlay subtree",
                subtreeContainsId(overlay, "@+id/post_comment_send"));
        assertTrue("Comment image draft strip should live in the overlay subtree",
                subtreeContainsId(overlay, "@+id/post_comment_media_strip"));
        assertTrue("Selected @/# chips should live in the overlay subtree",
                subtreeContainsId(overlay, "@+id/post_comment_chip_row"));
        assertFalse("Static comment bar must not contain the image draft strip",
                subtreeContainsId(staticBar, "@+id/post_comment_media_strip"));
        assertTrue("Post detail actions should live in the bottom static comment entry",
                xml.indexOf("@+id/post_like_action") > xml.indexOf("@+id/post_comment_bar"));
        assertFalse("Post detail must not keep a separate engagement card above comments",
                xml.contains("@+id/post_engagement_section"));
        assertFalse(xml.contains("详情顺序遵循 Open Design"));
    }

    @Test
    public void postDetailActivityUsesImeInsetsForCommentKeyboard() throws IOException {
        String manifest = readUtf8("src/main/AndroidManifest.xml");
        String postDetailActivity = tagContaining(manifest, ".feature.community.PostDetailActivity");
        String activity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java");
        String systemBarInsets = readUtf8("src/main/java/cn/edu/app/douyu/core/SystemBarInsets.java");

        assertTrue("Post detail comment editor must let IME insets own keyboard positioning",
                postDetailActivity.contains("android:windowSoftInputMode=\"adjustNothing\""));
        assertTrue("Post detail should enforce adjustNothing at runtime for device compatibility",
                activity.contains("SOFT_INPUT_ADJUST_NOTHING"));
        assertFalse("Post detail must not combine adjustResize with manual IME padding",
                activity.contains("SOFT_INPUT_ADJUST_RESIZE"));
        assertTrue("Post detail must use the shared SystemBarInsets helper for input insets",
                activity.contains("SystemBarInsets.applyToContentWithBottomContainers"));
        assertTrue("Post detail must receive IME visibility changes so manual keyboard hide restores the static bar",
                activity.contains("SystemBarInsets.applyToContentWithBottomContainers(this, this::onImeVisibilityChanged"));
        assertTrue("Post detail must collapse the real input when blank scroll content is tapped",
                activity.contains("post_detail_scroll") && activity.contains("setOnClickListener(v -> collapseCommentInputIfVisible())"));
        assertTrue("Post detail must expose a single guarded collapse path for blank taps and IME hide",
                activity.contains("collapseCommentInputIfVisible"));
        assertTrue("Post detail must restore the static input when IME becomes hidden",
                activity.contains("onImeVisibilityChanged") && activity.contains("!visible"));
        assertFalse("PostDetailActivity must not directly override the root insets listener",
                activity.contains("setOnApplyWindowInsetsListener"));
        assertFalse("Post detail keyboard input must not scroll to the comment section",
                activity.contains("smoothScrollTo(0, findViewById(R.id.post_comment_section).getTop())"));
        assertTrue("SystemBarInsets must handle IME insets for the bottom input",
                systemBarInsets.contains("WindowInsetsCompat.Type.ime()"));
        assertTrue("SystemBarInsets must avoid double-counting navigation and IME bottom insets",
                systemBarInsets.contains("Math.max(ime.bottom, bars.bottom)"));
        assertTrue("SystemBarInsets must notify pages when IME visibility changes",
                systemBarInsets.contains("ImeVisibilityListener") && systemBarInsets.contains("listener.onImeVisibilityChanged"));
        assertTrue("SystemBarInsets must preserve original padding when replacing the base listener",
                systemBarInsets.contains("originalPadding(root)"));
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

    @Test
    public void realBackendSmokeCoversPostDetailCommentFlow() throws IOException {
        String source = readUtf8("src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java");

        assertTrue("Post detail real backend smoke must capture the first loaded detail state",
                source.contains("\"post_detail_real_home\""));
        assertTrue("Post detail real backend smoke must capture the focused comment input state",
                source.contains("\"post_detail_keyboard_\" + \"com\" + \"poser\""));
        assertTrue("Post detail real backend smoke must capture refreshed comments after submitting a real comment",
                source.contains("\"post_detail_comments_after_submit\""));
        assertTrue("Post detail real backend smoke must assert the static bar hides when the real input opens",
                source.contains("R.id.post_comment_bar") && source.contains("View.GONE"));
        assertTrue("Post detail real backend smoke must assert the real input shows when the keyboard opens",
                source.contains("R.id.post_comment_editor") && source.contains("View.VISIBLE"));
        assertTrue("Post detail real backend smoke must submit a real comment through the backend API",
                source.contains("createComment("));
        assertTrue("Post detail repair must have a focused real-device smoke entry",
                source.contains("captureRealBackendPostDetailOnly"));
        assertTrue("Post detail extended smoke must cover the real comment editor paths",
                source.contains("captureRealBackendPostDetailComposer"));
        assertTrue("Post detail extended smoke must add an uploaded comment image through the real flow",
                source.contains("testAddCommentImage"));
        assertTrue("Post detail extended smoke must use the backend-returned image URL for the photo viewer",
                source.contains("commentImageUrls(newest)"));
        assertTrue("Post detail extended smoke must verify blank taps collapse the real input",
                source.contains("testTapBlankArea") && source.contains("\"post_detail_editor_after_blank_tap\""));
        assertFalse("Real backend smoke must not use seed asset URLs for evidence",
                source.contains("/" + "seed" + "/") || source.contains("doyu-" + "public"));
        assertFalse("Real backend fixture copy must be readable Chinese, not mojibake",
                source.contains(chars(0x942a, 0x71b8, 0x6e80))
                        || source.contains(chars(0x6960, 0x5c7e, 0x6579))
                        || source.contains(chars(0x7487, 0xfe3d, 0x510f)));
    }

    @Test
    public void commentImageUploadUsesPresignedHeaders() throws IOException {
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");

        assertTrue("Comment image upload must send the headers returned by the presign API",
                repository.contains("uploadPresignedBytes(presign.uploadUrl, bytes, mimeType, presign.headers)"));
        assertFalse("Comment image upload must not bypass presign headers with a raw Retrofit PUT",
                repository.contains("api.uploadPut(presign.uploadUrl"));
    }

    @Test
    public void messagesHomeShowsNotificationsBeforeMessagesWithoutPrivateQuotaBadges() throws IOException {
        String openDesign = readUtf8("../../doc/development/open-design/messages-a.html");
        String fragmentXml = readUtf8("src/main/res/layout/fragment_messages_home.xml");
        String conversationItemXml = readUtf8("src/main/res/layout/item_conversation.xml");
        String messagesFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/MessagesFragment.java");
        String conversationAdapter = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/ConversationAdapter.java");
        String conversationActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/ConversationActivity.java");

        assertTrue("Open Design messages home must show notification section before messages section",
                openDesign.indexOf("class=\"pill ok\">通知</span>") >= 0
                        && openDesign.indexOf("class=\"pill warn\">消息</span>")
                        > openDesign.indexOf("class=\"pill ok\">通知</span>"));
        assertFalse("Open Design messages home must remove the private/notification tab copy",
                openDesign.contains("私信 / 通知 Tab") || openDesign.contains("<div class=\"tabs\""));
        assertFalse("Open Design messages home must remove the old unread-conversation hero",
                openDesign.contains("未读对话") || openDesign.contains("对话输入中、未互关剩余条数、禁发态只在私信里展示"));
        assertFalse("Open Design messages rows must not show private quota or send-state badges",
                openDesign.contains(">互关<") || openDesign.contains(">1/3<") || openDesign.contains(">禁发<")
                        || openDesign.contains("未互关剩余") || openDesign.contains("超过 3 条"));

        assertFalse("Messages home XML must not keep private/notification tabs",
                fragmentXml.contains("@+id/tab_private") || fragmentXml.contains("@+id/tab_notify"));
        assertFalse("Messages home XML must not keep the removed hero copy ids",
                fragmentXml.contains("@+id/messages_hero_title") || fragmentXml.contains("@+id/messages_hero_desc"));
        assertViewIdExists("fragment_messages_home.xml", fragmentXml, "@+id/summary_list");
        assertViewIdExists("fragment_messages_home.xml", fragmentXml, "@+id/loading");
        assertViewIdExists("fragment_messages_home.xml", fragmentXml, "@+id/empty_text");
        assertViewIdExists("fragment_messages_home.xml", fragmentXml, "@+id/error_box");
        assertFalse("MessagesFragment must not keep tab state or tab rendering",
                messagesFragment.contains("TAB_PRIVATE")
                        || messagesFragment.contains("TAB_NOTIFY")
                        || messagesFragment.contains("currentTab")
                        || messagesFragment.contains("renderTabs"));
        assertTrue("MessagesFragment must load notifications and conversations into a single home adapter",
                messagesFragment.contains("MessageHomeAdapter")
                        && messagesFragment.contains("repository.notifications()")
                        && messagesFragment.contains("repository.conversations()"));

        assertFalse("Message rows on home must not expose a right-side conversation status badge",
                conversationItemXml.contains("@+id/conversation_status"));
        assertFalse("Home conversation adapter must not calculate or render mutual-follow quota state",
                conversationAdapter.contains("mutualFollow")
                        || conversationAdapter.contains("remainingNonMutualMessages")
                        || conversationAdapter.contains("canSend")
                        || conversationAdapter.contains("bg_pill_ok")
                        || conversationAdapter.contains("bg_pill_warn")
                        || conversationAdapter.contains("bg_pill_stop")
                        || conversationAdapter.contains("/3"));
        assertTrue("Conversation detail must keep the authoritative private-message send-state fields",
                conversationActivity.contains("mutualFollow")
                        && conversationActivity.contains("remainingNonMutualMessages")
                        && conversationActivity.contains("canSend"));
    }

    @Test
    public void authEmailLoginRegisterAndLoggedOutBoundariesAreMapped() throws IOException {
        String loginDesign = readUtf8("../../doc/development/open-design/login-a.html");
        String registerDesign = readUtf8("../../doc/development/open-design/register-a.html");
        String loggedOutIndex = readUtf8("../../doc/development/open-design/index-logged-out.html");
        String loggedInIndex = readUtf8("../../doc/development/open-design/index.html");
        String profileDesign = readUtf8("../../doc/development/open-design/profile-a.html");
        String manifest = readUtf8("src/main/AndroidManifest.xml");
        String loginXml = readUtf8("src/main/res/layout/activity_login.xml");
        String registerXml = readUtf8("src/main/res/layout/activity_register.xml");
        String settingsXml = readUtf8("src/main/res/layout/activity_settings_home.xml");
        String accountSecurityXml = readUtf8("src/main/res/layout/activity_settings_account_security.xml");
        String uiCopy = readUtf8("src/main/java/cn/edu/app/douyu/core/UiCopy.java");
        String authGate = readUtf8("src/main/java/cn/edu/app/douyu/auth/AuthGate.java");
        String sessionStore = readUtf8("src/main/java/cn/edu/app/douyu/auth/SessionStore.java");
        String loginActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/LoginActivity.java");
        String registerActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/RegisterActivity.java");
        String settingsActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java");
        String mainActivity = readUtf8("src/main/java/cn/edu/app/douyu/MainActivity.java");
        String profileFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java");
        String postDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java");
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String apiModuleMap = readUtf8("../../doc/development/diagrams/api-module-map.svg");
        String realBackendSmoke = readUtf8("src/androidTest/java/cn/edu/app/douyu/RealBackendSmokeInstrumentedTest.java");
        String commerceRealProducts = readUtf8("src/androidTest/java/cn/edu/app/douyu/CommerceRealProductsInstrumentedTest.java");

        assertTrue("Logged-in Open Design index must link to the logged-out index",
                loggedInIndex.contains("index-logged-out.html"));
        assertTrue("Logged-in Open Design index must link login and register pages",
                loggedInIndex.contains("login-a.html") && loggedInIndex.contains("register-a.html"));
        assertTrue("Logged-out Open Design index must expose login/register entry points",
                loggedOutIndex.contains("login-a.html") && loggedOutIndex.contains("register-a.html"));
        assertTrue("Logged-out Open Design must document the protected-action login dialog",
                loggedOutIndex.contains("需要登录")
                        && loggedOutIndex.contains("登录后可以继续使用此功能。")
                        && loggedOutIndex.contains("去登录 / 取消"));
        assertTrue("Login Open Design must use the real email login endpoint",
                loginDesign.contains("/api/v1/auth/login") && loginDesign.contains("邮箱"));
        assertTrue("Register Open Design must use the real email register endpoint",
                registerDesign.contains("/api/v1/auth/register")
                        && registerDesign.contains("确认密码")
                        && registerDesign.contains("注册并登录"));
        assertTrue("Profile Open Design must keep the exact logged-out data copy",
                profileDesign.contains("需要登录后才能查看此页面的数据。"));
        assertFalse("Profile Open Design must remove the old extra logged-out explanation",
                profileDesign.contains("当前展示登录边界，不使用本地假内容。"));

        assertTrue("Manifest must register LoginActivity",
                manifest.contains(".auth.LoginActivity"));
        assertTrue("Manifest must register RegisterActivity",
                manifest.contains(".auth.RegisterActivity"));
        assertViewIdExists("activity_login.xml", loginXml, "@+id/login_email");
        assertViewIdExists("activity_login.xml", loginXml, "@+id/login_password");
        assertViewIdExists("activity_login.xml", loginXml, "@+id/login_submit");
        assertViewIdExists("activity_login.xml", loginXml, "@+id/login_open_register");
        assertViewIdExists("activity_register.xml", registerXml, "@+id/register_email");
        assertViewIdExists("activity_register.xml", registerXml, "@+id/register_password");
        assertViewIdExists("activity_register.xml", registerXml, "@+id/register_confirm_password");
        assertViewIdExists("activity_register.xml", registerXml, "@+id/register_submit");
        assertViewIdExists("activity_settings_home.xml", settingsXml, "@+id/settings_logout_action");
        assertTrue("Settings account security copy must describe email auth, not phone auth",
                accountSecurityXml.contains("邮箱") && !accountSecurityXml.contains("手机号"));

        assertTrue("UiCopy login-required text must be exact",
                uiCopy.contains("需要登录后才能查看此页面的数据。"));
        assertFalse("UiCopy must not keep the removed logged-out trailing sentence",
                uiCopy.contains("当前展示登录边界"));
        assertTrue("AuthGate must show the required confirmation copy",
                authGate.contains("需要登录")
                        && authGate.contains("登录后可以继续使用此功能。")
                        && authGate.contains("去登录")
                        && authGate.contains("取消"));
        assertTrue("SessionStore must share the same preference and token keys used by the API interceptor",
                sessionStore.contains("\"doyu_session\"")
                        && sessionStore.contains("\"accessToken\"")
                        && sessionStore.contains("\"refreshToken\""));
        assertTrue("LoginActivity must call the real login API and save AuthSession",
                loginActivity.contains("repository.login(email, password)")
                        && loginActivity.contains("new SessionStore(this).save(session)")
                        && loginActivity.contains("Patterns.EMAIL_ADDRESS"));
        assertTrue("RegisterActivity must call the real register API and enforce password confirmation",
                registerActivity.contains("repository.register(email, password, confirmPassword, nickname, ageGroup)")
                        && registerActivity.contains("!password.equals(confirmPassword)")
                        && registerActivity.contains("new SessionStore(this).save(session)"));
        assertTrue("SettingsActivity must confirm logout, call repository.logout, and clear SessionStore on success",
                settingsActivity.contains("确认退出登录？")
                        && settingsActivity.contains("repository.logout(refreshToken)")
                        && settingsActivity.contains("store.clear()"));
        assertTrue("SettingsActivity must refresh the bottom auth action after returning from LoginActivity",
                settingsActivity.contains("protected void onResume()")
                        && settingsActivity.contains("super.onResume()")
                        && settingsActivity.contains("bindLogoutAction();"));
        assertTrue("Upload work action must go through AuthGate",
                mainActivity.contains("AuthGate.runOrRequestLogin")
                        && mainActivity.contains("RETURN_ACTION_POST_CREATE"));
        assertTrue("Profile edit action must go through AuthGate",
                profileFragment.contains("AuthGate.runOrRequestLogin")
                        && profileFragment.contains("RETURN_ACTION_PROFILE_EDIT"));
        assertTrue("Post detail comment/write actions must go through AuthGate",
                postDetailActivity.contains("AuthGate.runOrRequestLogin")
                        && postDetailActivity.contains("RETURN_ACTION_COMMENT"));
        assertTrue("DoyuApi must expose email register, login, and logout endpoints",
                api.contains("@POST(\"/api/v1/auth/register\")")
                        && api.contains("@POST(\"/api/v1/auth/login\")")
                        && api.contains("@POST(\"/api/v1/auth/logout\")"));
        assertFalse("DoyuApi must not expose removed SMS auth endpoints",
                api.contains("sms-code") || api.contains("login/sms"));
        assertTrue("DoyuRepository must expose login/register/logout methods",
                repository.contains("AuthSession login(")
                        && repository.contains("AuthSession register(")
                        && repository.contains("void logout("));
        assertTrue("API module diagram must document current email auth endpoints",
                apiModuleMap.contains("register / login / refresh / logout"));
        assertFalse("API module diagram must not keep removed SMS auth wording",
                apiModuleMap.contains("sms-code"));
        assertTrue("Real backend smoke must generate a unique email for repeatable device review",
                realBackendSmoke.contains("UUID.randomUUID()")
                        && realBackendSmoke.contains("\"smoke-\" + phone + \"-\""));
        assertTrue("Commerce real-products smoke must generate a unique email for repeatable device review",
                commerceRealProducts.contains("UUID.randomUUID()")
                        && commerceRealProducts.contains("\"commerce-\" + phone + \"-\""));
    }

    @Test
    public void profileStatsNavigateToRealListsAndLikesExplanationDialog() throws IOException {
        String profileOpenDesign = readUtf8("../../doc/development/open-design/profile-a.html");
        String profilePostsOpenDesign = readUtf8("../../doc/development/open-design/profile-posts-a.html");
        String profileFollowingOpenDesign = readUtf8("../../doc/development/open-design/profile-following-a.html");
        String profileFollowersOpenDesign = readUtf8("../../doc/development/open-design/profile-followers-a.html");
        String profileXml = readUtf8("src/main/res/layout/fragment_profile_home.xml");
        String postsXml = readUtf8("src/main/res/layout/activity_profile_posts.xml");
        String usersXml = readUtf8("src/main/res/layout/activity_profile_users.xml");
        String manifest = readUtf8("src/main/AndroidManifest.xml");
        String fragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java");
        String postsActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfilePostsActivity.java");
        String usersActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileUsersActivity.java");
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String profileEditActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileEditActivity.java");

        assertTrue("Profile Open Design must link posts stat to the dedicated page",
                profileOpenDesign.contains("profile-posts-a.html"));
        assertTrue("Profile Open Design must link following stat to the dedicated page",
                profileOpenDesign.contains("profile-following-a.html"));
        assertTrue("Profile Open Design must link followers stat to the dedicated page",
                profileOpenDesign.contains("profile-followers-a.html"));
        assertTrue("Profile Open Design must include the likes source explanation dialog",
                profileOpenDesign.contains("获赞来自你发布作品收到的赞")
                        && profileOpenDesign.contains("like-source-dialog"));
        assertTrue("Profile posts design must describe the real /me/posts data source",
                profilePostsOpenDesign.contains("/api/v1/users/me/posts"));
        assertTrue("Profile following design must describe the real /me/following data source",
                profileFollowingOpenDesign.contains("/api/v1/users/me/following"));
        assertTrue("Profile followers design must describe the real /me/followers data source",
                profileFollowersOpenDesign.contains("/api/v1/users/me/followers"));

        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/stat_liked_cell");
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/stat_posts_cell");
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/stat_following_cell");
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/stat_followers_cell");
        assertViewIdExists("activity_profile_posts.xml", postsXml, "@+id/profile_posts_list");
        assertViewIdExists("activity_profile_users.xml", usersXml, "@+id/profile_users_list");
        assertTrue("Manifest must register ProfilePostsActivity",
                manifest.contains(".feature.profile.ProfilePostsActivity"));
        assertTrue("Manifest must register ProfileUsersActivity",
                manifest.contains(".feature.profile.ProfileUsersActivity"));

        assertTrue("ProfileFragment must show the likes source explanation dialog",
                fragment.contains("showLikesSourceDialog")
                        && fragment.contains("获赞来自你发布作品收到的赞"));
        assertTrue("ProfileFragment must navigate the posts stat into ProfilePostsActivity",
                fragment.contains("ProfilePostsActivity"));
        assertTrue("ProfileFragment must navigate following and followers into ProfileUsersActivity",
                fragment.contains("ProfileUsersActivity")
                        && fragment.contains("MODE_FOLLOWING")
                        && fragment.contains("MODE_FOLLOWERS"));
        assertTrue("ProfilePostsActivity must load the real current-user posts endpoint",
                postsActivity.contains("repository.myPosts()"));
        assertTrue("ProfileUsersActivity must load real following and followers endpoints",
                usersActivity.contains("repository.followingUsers()")
                        && usersActivity.contains("repository.followerUsers()"));
        assertTrue("DoyuApi must declare the real Profile stat endpoints",
                api.contains("@GET(\"/api/v1/users/me/posts\")")
                        && api.contains("@GET(\"/api/v1/users/me/following\")")
                        && api.contains("@GET(\"/api/v1/users/me/followers\")"));
        assertTrue("DoyuRepository must expose the real Profile stat endpoint methods",
                repository.contains("myPosts()")
                        && repository.contains("followingUsers()")
                        && repository.contains("followerUsers()"));
        assertTrue("Profile edit must keep the real avatar upload and save contract",
                profileEditActivity.contains("uploadAvatar")
                        && profileEditActivity.contains("repository.updateMe(nickname, bio, avatarFileId)")
                        && profileEditActivity.contains("nickname.isEmpty()"));
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

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(Files.readAllBytes(path));
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            builder.append(String.format("%02x", value & 0xff));
        }
        return builder.toString();
    }

    private static void assertPngDimensions(Path path, int expectedSize) throws IOException {
        assertTrue(path + " must exist", Files.exists(path));
        byte[] bytes = Files.readAllBytes(path);
        assertTrue(path + " must be a readable PNG", bytes.length >= 24);
        assertEquals(path + " width", expectedSize, pngDimension(bytes, 16));
        assertEquals(path + " height", expectedSize, pngDimension(bytes, 20));
    }

    private static int pngDimension(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private static Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static Element requireElementById(Document document, String id) {
        Element element = findElementById(document.getDocumentElement(), id);
        assertTrue("Missing XML element " + id, element != null);
        return element;
    }

    private static Element findElementById(Element element, String id) {
        if (id.equals(element.getAttribute("android:id"))) {
            return element;
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element match = findElementById((Element) child, id);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static boolean hasAncestorWithId(Element element, String id) {
        Node parent = element.getParentNode();
        while (parent instanceof Element) {
            Element parentElement = (Element) parent;
            if (id.equals(parentElement.getAttribute("android:id"))) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private static boolean subtreeContainsId(Element element, String id) {
        if (id.equals(element.getAttribute("android:id"))) {
            return true;
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && subtreeContainsId((Element) child, id)) {
                return true;
            }
        }
        return false;
    }

    private static String chars(int... values) {
        char[] chars = new char[values.length];
        for (int i = 0; i < values.length; i++) {
            chars[i] = (char) values[i];
        }
        return new String(chars);
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

    private static final class LauncherDensity {
        final String directory;
        final int size;

        LauncherDensity(String directory, int size) {
            this.directory = directory;
            this.size = size;
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
