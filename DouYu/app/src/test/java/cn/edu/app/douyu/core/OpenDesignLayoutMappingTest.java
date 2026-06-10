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
    public void mapsCurrentOpenDesignPagesToDedicatedXmlLayouts() {
        LayoutMapping[] mappings = new LayoutMapping[]{
                new LayoutMapping("community-home-a.html", R.layout.fragment_community_home),
                new LayoutMapping("commerce-home-a.html", R.layout.fragment_commerce_home),
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
                new LayoutMapping("doyu-design-directions.html", R.layout.activity_main)
        };

        assertEquals(20, mappings.length);
        for (LayoutMapping mapping : mappings) {
            assertTrue(mapping.openDesignPage.endsWith(".html"));
            assertTrue(mapping.layoutId > 0);
        }
    }

    @Test
    public void mainNavigationUsesFourContentTabsPlusUploadAction() throws IOException {
        String mainXml = readUtf8("src/main/res/layout/activity_main.xml");
        String mainActivity = readUtf8("src/main/java/cn/edu/app/douyu/MainActivity.java");
        String intentExtras = readUtf8("src/main/java/cn/edu/app/douyu/core/IntentExtras.java");

        assertViewIdExists("activity_main.xml", mainXml, "@+id/tab_community");
        assertViewIdExists("activity_main.xml", mainXml, "@+id/tab_commerce");
        assertViewIdExists("activity_main.xml", mainXml, "@+id/tab_upload");
        assertViewIdExists("activity_main.xml", mainXml, "@+id/tab_messages");
        assertViewIdExists("activity_main.xml", mainXml, "@+id/tab_profile");
        assertTrue(mainXml.indexOf("@+id/tab_community") < mainXml.indexOf("@+id/tab_commerce"));
        assertTrue(mainXml.indexOf("@+id/tab_commerce") < mainXml.indexOf("@+id/tab_upload"));
        assertTrue(mainXml.indexOf("@+id/tab_upload") < mainXml.indexOf("@+id/tab_messages"));
        assertTrue(mainXml.indexOf("@+id/tab_messages") < mainXml.indexOf("@+id/tab_profile"));
        assertFalse(mainXml.contains("tab_ai"));
        assertFalse(mainActivity.contains("AiFragment"));
        assertFalse(mainActivity.contains("quick_ai"));
        assertFalse(mainActivity.contains("quick_" + "post"));
        assertTrue(mainActivity.contains("R.id.tab_upload"));
        assertTrue(mainActivity.contains("RETURN_ACTION_POST_CREATE"));
        assertTrue(mainActivity.contains("PostCreateActivity"));
        assertTrue(mainActivity.contains("IntentExtras.SECTION_MESSAGES"));
        assertTrue(intentExtras.contains("SECTION_COMMUNITY"));
        assertTrue(intentExtras.contains("SECTION_COMMERCE"));
        assertTrue(intentExtras.contains("SECTION_MESSAGES"));
        assertTrue(intentExtras.contains("SECTION_PROFILE"));
    }

    @Test
    public void uploadAndProfileContractsStayAvailable() throws IOException {
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String profileFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java");
        String profileXml = readUtf8("src/main/res/layout/fragment_profile_home.xml");
        String profileEditActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileEditActivity.java");
        String messagesAdapter = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/MessageHomeAdapter.java");
        String messagesFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/MessagesFragment.java");
        String uiCopy = readUtf8("src/main/java/cn/edu/app/douyu/core/UiCopy.java");

        assertTrue(api.contains("@POST(\"/api/v1/uploads/presign\")"));
        assertTrue(api.contains("@POST(\"/api/v1/uploads/confirm\")"));
        assertTrue(api.contains("@POST(\"/api/v1/posts\")"));
        assertTrue(repository.contains("uploadAvatar"));
        assertTrue(repository.contains("uploadPostImage"));
        assertTrue(repository.contains("createPost"));
        assertTrue(profileEditActivity.contains("uploadAvatar"));
        assertTrue(profileFragment.contains("repository.likedPosts()"));
        assertTrue(profileFragment.contains("repository.favoritePosts()"));
        assertFalse(profileFragment.contains("patternJobs"));
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/tab_liked");
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/tab_favorites");
        assertTrue(profileXml.contains("编辑资料"));
        assertTrue(profileXml.contains("获赞"));
        assertTrue(profileXml.contains("点赞作品"));
        assertTrue(profileXml.contains("收藏作品"));
        assertTrue(profileFragment.contains(".setTitle(\"获赞来源\")"));
        assertTrue(profileFragment.contains("\"点赞\""));
        assertTrue(profileFragment.contains("\"收藏\""));
        assertTrue(profileFragment.contains("\"未命名作品\""));
        assertTrue(messagesAdapter.contains("Row.section(\"通知\""));
        assertTrue(messagesAdapter.contains("Row.section(\"消息\""));
        assertTrue(messagesFragment.contains("暂时无法连接服务"));
        assertTrue(uiCopy.contains("加载失败："));
        assertTrue(uiCopy.contains("重试"));
        assertFalse(profileXml.contains("tab_my_patterns"));
        assertFalse(profileXml.contains("Not signed in"));
        assertFalse(profileXml.contains("Liked posts"));
        assertFalse(profileFragment.contains("Likes source"));
    }

    @Test
    public void notificationDetailLabelsStayChinese() throws IOException {
        String notificationTypes = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/NotificationTypes.java");
        String notificationDetail = readUtf8("src/main/java/cn/edu/app/douyu/feature/message/NotificationDetailActivity.java");

        assertTrue(notificationTypes.contains("审核通知"));
        assertTrue(notificationTypes.contains("系统通知"));
        assertTrue(notificationTypes.contains("互动通知"));
        assertTrue(notificationTypes.contains("通知"));
        assertFalse(notificationTypes.contains("Review notification"));
        assertFalse(notificationTypes.contains("System notification"));
        assertFalse(notificationTypes.contains("Interaction notification"));
        assertTrue(notificationDetail.contains("NotificationTypes.heroLabel(type)"));
    }

    @Test
    public void removedFeatureLayoutsAndEndpointsAreAbsent() throws IOException {
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String manifest = readUtf8("src/main/AndroidManifest.xml");
        String searchXml = readUtf8("src/main/res/layout/activity_search.xml");
        String postCreateXml = readUtf8("src/main/res/layout/activity_post_create.xml");
        String settingsPrivacy = readUtf8("src/main/res/layout/activity_settings_privacy_permissions.xml");

        assertFalse(api.contains("/api/v1/patterns"));
        assertFalse(api.contains("favorite-patterns"));
        assertFalse(repository.contains("createPatternJob"));
        assertFalse(repository.contains("favoritePatterns"));
        assertFalse(manifest.contains(".feature.ai"));
        assertFalse(manifest.contains("PaymentBoundaryActivity"));
        assertTrue(manifest.contains("PostCaptureActivity"));
        assertTrue(manifest.contains("android.permission.CAMERA"));
        assertFalse(searchXml.contains("图纸"));
        assertFalse(postCreateXml.contains("关联图纸"));
        assertFalse(settingsPrivacy.contains("地图"));
        assertFalse(settingsPrivacy.contains("定位"));
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_publish_top");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_bottom_nav");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_nav_community");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_nav_commerce");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_nav_upload");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_nav_messages");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_nav_profile");
        assertTrue(postCreateXml.indexOf("@+id/post_nav_community") < postCreateXml.indexOf("@+id/post_nav_commerce"));
        assertTrue(postCreateXml.indexOf("@+id/post_nav_commerce") < postCreateXml.indexOf("@+id/post_nav_upload"));
        assertTrue(postCreateXml.indexOf("@+id/post_nav_upload") < postCreateXml.indexOf("@+id/post_nav_messages"));
        assertTrue(postCreateXml.indexOf("@+id/post_nav_messages") < postCreateXml.indexOf("@+id/post_nav_profile"));
    }

    @Test
    public void postDetailAndAuthContractsRemainRealBackendDriven() throws IOException {
        String postDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java");
        String loginActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/LoginActivity.java");
        String registerActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/RegisterActivity.java");
        String settingsActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java");

        assertTrue(postDetailActivity.contains("repository.comments(postId)"));
        assertTrue(postDetailActivity.contains("uploadPostImage"));
        assertTrue(postDetailActivity.contains("AuthGate.runOrRequestLogin"));
        String postCreateActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostCreateActivity.java");
        assertTrue(postCreateActivity.contains("uploadPostImage"));
        assertTrue(postCreateActivity.contains("createPost"));
        assertTrue(postCreateActivity.contains("PostCaptureActivity"));
        assertTrue(postCreateActivity.contains("REVIEWING"));
        assertTrue(postCreateActivity.contains("buildFailedActions"));
        assertTrue(postCreateActivity.contains("buildFailedAction(\"重试\""));
        assertTrue(postCreateActivity.contains("buildFailedAction(\"删除\""));
        assertTrue(postCreateActivity.contains("delete.setOnClickListener(v -> removeMedia(media))"));
        assertTrue(postCreateActivity.contains("topPublishButton.setOnClickListener(v -> submitPost())"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_MESSAGES)"));
        assertTrue(loginActivity.contains("repository.login(email, password)"));
        assertTrue(registerActivity.contains("repository.register(email, password, confirmPassword, nickname, ageGroup)"));
        assertTrue(settingsActivity.contains("repository.logout(refreshToken)"));
    }

    private static void assertViewIdExists(String layout, String xml, String id) {
        assertTrue(layout + " missing " + id, xml.contains("android:id=\"" + id + "\""));
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
}
