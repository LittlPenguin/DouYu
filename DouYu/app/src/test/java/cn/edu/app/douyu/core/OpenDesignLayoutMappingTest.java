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
        String bottomNavXml = readUtf8("src/main/res/layout/include_bottom_nav.xml");
        String mainActivity = readUtf8("src/main/java/cn/edu/app/douyu/MainActivity.java");
        String intentExtras = readUtf8("src/main/java/cn/edu/app/douyu/core/IntentExtras.java");

        assertTrue(mainXml.contains("layout=\"@layout/include_bottom_nav\""));
        assertViewIdExists("include_bottom_nav.xml", bottomNavXml, "@+id/tab_community");
        assertViewIdExists("include_bottom_nav.xml", bottomNavXml, "@+id/tab_commerce");
        assertViewIdExists("include_bottom_nav.xml", bottomNavXml, "@+id/tab_upload");
        assertViewIdExists("include_bottom_nav.xml", bottomNavXml, "@+id/tab_messages");
        assertViewIdExists("include_bottom_nav.xml", bottomNavXml, "@+id/tab_profile");
        assertContainsInOrder(bottomNavXml,
                "@+id/tab_community",
                "@+id/tab_commerce",
                "@+id/tab_upload",
                "@+id/tab_messages",
                "@+id/tab_profile");
        assertFalse(bottomNavXml.contains("tab_" + "ai"));
        assertFalse(mainActivity.contains("R.id.nav_indicator_upload"));
        assertTrue(bottomNavXml.contains("android:id=\"@+id/nav_indicator_upload\""));
        assertTrue(bottomNavXml.contains("android:visibility=\"gone\""));
        assertFalse(mainActivity.contains("Ai" + "Fragment"));
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
    public void postCreateUsesSharedBottomNavigationAndHighlightsUpload() throws IOException {
        String postCreateXml = readUtf8("src/main/res/layout/activity_post_create.xml");
        String bottomNavXml = readUtf8("src/main/res/layout/include_bottom_nav.xml");
        String postCreateActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostCreateActivity.java");

        assertTrue(postCreateXml.contains("layout=\"@layout/include_bottom_nav\""));
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_bottom_nav");
        assertFalse(postCreateXml.contains("post_nav_community"));
        assertFalse(postCreateXml.contains("post_nav_commerce"));
        assertFalse(postCreateXml.contains("post_nav_upload"));
        assertFalse(postCreateXml.contains("post_nav_messages"));
        assertFalse(postCreateXml.contains("post_nav_profile"));
        assertContainsInOrder(bottomNavXml,
                "@+id/tab_community",
                "@+id/tab_commerce",
                "@+id/tab_upload",
                "@+id/tab_messages",
                "@+id/tab_profile");
        assertTrue(postCreateActivity.contains("selectUploadNav"));
        assertTrue(postCreateActivity.contains("R.id.tab_upload"));
        assertTrue(postCreateActivity.contains("R.id.nav_indicator_upload"));
        assertTrue(postCreateActivity.contains("import android.content.res.ColorStateList;"));
        assertTrue(postCreateActivity.contains("styleTopicChip(chip)"));
        assertTrue(postCreateActivity.contains("private void styleTopicChip(Chip chip)"));
        assertTrue(postCreateActivity.contains("private ColorStateList topicChipBackgroundColor()"));
        assertTrue(postCreateActivity.contains("private ColorStateList topicChipTextColor()"));
        assertTrue(postCreateActivity.contains("private ColorStateList topicChipStrokeColor()"));
        assertTrue(postCreateActivity.contains("android.R.attr.state_checked"));
        assertTrue(postCreateActivity.contains("R.color.doyu_petal_deep"));
        assertTrue(postCreateActivity.contains("R.color.white"));
        assertTrue(postCreateActivity.contains("R.color.doyu_surface"));
        assertTrue(postCreateActivity.contains("R.color.doyu_text_muted"));
        assertTrue(postCreateActivity.contains("R.color.doyu_open_line"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_COMMUNITY)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_COMMERCE)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_MESSAGES)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_PROFILE)"));
    }

    @Test
    public void uploadAndProfileContractsStayAvailable() throws IOException {
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String profileFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java");
        String profileXml = readUtf8("src/main/res/layout/fragment_profile_home.xml");
        String profileEditActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileEditActivity.java");
        String profileEditXml = readUtf8("src/main/res/layout/activity_profile_edit.xml");
        String userProfile = readUtf8("src/main/java/cn/edu/app/douyu/model/UserProfile.java");
        String updateProfileRequest = readUtf8("src/main/java/cn/edu/app/douyu/model/UpdateProfileRequest.java");
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
        assertTrue(profileEditActivity.contains("regionInput"));
        assertTrue(profileEditActivity.contains("REGION_CHOICES"));
        assertTrue(repository.contains("String region"));
        assertTrue(repository.contains("new UpdateProfileRequest(nickname, avatarFileId, bio, region)"));
        assertTrue(userProfile.contains("public String region;"));
        assertTrue(updateProfileRequest.contains("public String region;"));
        assertViewIdExists("activity_profile_edit.xml", profileEditXml, "@+id/region_input");
        assertViewIdExists("activity_profile_edit.xml", profileEditXml, "@+id/region_choices");
        assertFalse(profileEditXml.contains("@+id/age_value"));
        assertFalse(profileEditXml.contains("@+id/interest_tags"));
        assertFalse(profileEditXml.contains("年龄段"));
        assertFalse(profileEditXml.contains("兴趣标签"));
        assertFalse(profileEditXml.contains("UI-only，不保存到后端"));
        assertFalse(profileEditXml.contains("兴趣标签为 UI-only"));
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

        assertFalse(notificationTypes.contains("审核通知"));
        assertFalse(notificationTypes.contains("AUDIT"));
        assertFalse(notificationTypes.contains("REVIEW"));
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
        assertFalse(repository.contains("createPattern" + "Job"));
        assertFalse(repository.contains("favoritePatterns"));
        assertFalse(manifest.contains(".feature.ai"));
        assertFalse(manifest.contains("PaymentBoundary" + "Activity"));
        assertTrue(manifest.contains("PostCaptureActivity"));
        assertTrue(manifest.contains("android.permission.CAMERA"));
        assertFalse(searchXml.contains("图纸"));
        assertFalse(postCreateXml.contains("关联图纸"));
        assertFalse(settingsPrivacy.contains("地图"));
        assertFalse(settingsPrivacy.contains("定位"));
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_publish_top");
        assertViewIdExists("activity_post_create.xml", postCreateXml, "@+id/post_bottom_nav");
        assertFalse(postCreateXml.contains("post_nav_"));
    }

    @Test
    public void commercePurchaseCartAndOrderUiUsesRealBackendContracts() throws IOException {
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String productDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/commerce/ProductDetailActivity.java");
        String productDetailXml = readUtf8("src/main/res/layout/activity_product_detail.xml");
        String manifest = readUtf8("src/main/AndroidManifest.xml");
        Path cartActivityPath = Path.of("src/main/java/cn/edu/app/douyu/feature/commerce/CartActivity.java");
        Path cartXmlPath = Path.of("src/main/res/layout/activity_cart.xml");
        Path cartItemXmlPath = Path.of("src/main/res/layout/item_cart.xml");

        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/purchase_quantity_decrease");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/purchase_quantity_value");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/purchase_quantity_increase");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/add_to_cart_button");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/buy_now_button");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/open_cart_button");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/address_recipient_input");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/address_phone_input");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/address_region_input");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/address_detail_input");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/order_remark_input");
        assertViewIdExists("activity_product_detail.xml", productDetailXml, "@+id/purchase_status");
        assertTrue(productDetailActivity.contains("PurchaseFormState.firstPurchasableSku(product)"));
        assertTrue(productDetailActivity.contains("PurchaseFormState.clampQuantity"));
        assertTrue(productDetailActivity.contains("repository.addCartItem"));
        assertTrue(productDetailActivity.contains("repository.createImmediateOrder"));
        assertTrue(productDetailActivity.contains("AuthGate.runOrRequestLogin"));

        assertTrue("missing CartActivity.java", Files.exists(cartActivityPath));
        assertTrue("missing activity_cart.xml", Files.exists(cartXmlPath));
        assertTrue("missing item_cart.xml", Files.exists(cartItemXmlPath));
        String cartActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/commerce/CartActivity.java");
        String cartXml = readUtf8("src/main/res/layout/activity_cart.xml");
        String cartItemXml = readUtf8("src/main/res/layout/item_cart.xml");

        assertTrue(manifest.contains(".feature.commerce.CartActivity"));
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_list");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_checkout_button");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_total");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_address_recipient_input");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_address_phone_input");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_address_region_input");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_address_detail_input");
        assertViewIdExists("activity_cart.xml", cartXml, "@+id/cart_order_remark_input");
        assertViewIdExists("item_cart.xml", cartItemXml, "@+id/cart_item_selected");
        assertViewIdExists("item_cart.xml", cartItemXml, "@+id/cart_item_quantity_decrease");
        assertViewIdExists("item_cart.xml", cartItemXml, "@+id/cart_item_quantity_increase");
        assertViewIdExists("item_cart.xml", cartItemXml, "@+id/cart_item_delete");
        assertTrue(cartActivity.contains("repository.cart()"));
        assertTrue(cartActivity.contains("repository.updateCartItem"));
        assertTrue(cartActivity.contains("repository.deleteCartItem"));
        assertTrue(cartActivity.contains("repository.createCartOrder"));
        assertTrue(cartActivity.contains("AuthGate.runOrRequestLogin"));
        assertFalse(api.toLowerCase(java.util.Locale.ROOT).contains("payment"));
        assertFalse(api.toLowerCase(java.util.Locale.ROOT).contains("refund"));
        assertFalse(productDetailActivity.toLowerCase(java.util.Locale.ROOT).contains("payment"));
        assertFalse(cartActivity.toLowerCase(java.util.Locale.ROOT).contains("payment"));
    }

    @Test
    public void commercePurchaseCartAndOrderUiCopyIsChinese() throws IOException {
        String productDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/commerce/ProductDetailActivity.java");
        String cartActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/commerce/CartActivity.java");
        String productDetailXml = readUtf8("src/main/res/layout/activity_product_detail.xml");
        String cartXml = readUtf8("src/main/res/layout/activity_cart.xml");
        String cartItemXml = readUtf8("src/main/res/layout/item_cart.xml");
        String uiSurface = productDetailActivity + productDetailXml + cartActivity + cartXml + cartItemXml;

        assertTrue(productDetailXml.contains("android:text=\"购买\""));
        assertTrue(productDetailXml.contains("android:text=\"数量\""));
        assertTrue(productDetailXml.contains("android:hint=\"收货人\""));
        assertTrue(productDetailXml.contains("android:hint=\"手机号\""));
        assertTrue(productDetailXml.contains("android:hint=\"收货地址\""));
        assertTrue(productDetailXml.contains("android:text=\"加入购物车\""));
        assertTrue(productDetailXml.contains("android:text=\"提交订单\""));
        assertTrue(productDetailXml.contains("android:text=\"查看购物车\""));
        assertTrue(cartXml.contains("android:text=\"收货信息\""));
        assertTrue(cartXml.contains("android:text=\"已选 0 件 - --\""));
        assertTrue(cartXml.contains("android:text=\"提交订单\""));
        assertTrue(cartXml.contains("android:text=\"重试\""));
        assertTrue(cartItemXml.contains("android:contentDescription=\"选择购物车商品\""));
        assertTrue(cartItemXml.contains("android:text=\"删除\""));
        assertTrue(productDetailActivity.contains("创建订单成功"));
        assertTrue(cartActivity.contains("购物车"));
        assertTrue(cartActivity.contains("请选择至少一件可购买商品"));
        assertFalse(uiSurface.contains("Add to cart"));
        assertFalse(uiSurface.contains("Create order"));
        assertFalse(uiSurface.contains("Shipping address"));
        assertFalse(uiSurface.contains("Selected "));
        assertFalse(uiSurface.contains("Recipient"));
        assertFalse(uiSurface.contains("Address detail"));
    }

    @Test
    public void postDetailAndAuthContractsRemainRealBackendDriven() throws IOException {
        String postDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java");
        String postDetailXml = readUtf8("src/main/res/layout/activity_post_detail.xml");
        String loginActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/LoginActivity.java");
        String registerActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/RegisterActivity.java");
        String settingsActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java");

        assertTrue(postDetailActivity.contains("repository.post(postId)"));
        assertTrue(postDetailActivity.contains("repository.comments(postId)"));
        assertTrue(postDetailActivity.contains("uploadPostImage"));
        assertTrue(postDetailActivity.contains("AuthGate.runOrRequestLogin"));
        assertTrue(postDetailActivity.contains("bindGallerySwipe()"));
        assertTrue(postDetailActivity.contains("GALLERY_SWIPE_MIN_DISTANCE_DP"));
        assertTrue(postDetailActivity.contains("galleryTouchStartX"));
        assertTrue(postDetailActivity.contains("moveGallery(1)"));
        assertTrue(postDetailActivity.contains("moveGallery(-1)"));
        assertTrue(postDetailActivity.contains("thumbnailScroll.post"));
        assertTrue(postDetailActivity.contains("R.drawable.bg_thumbnail_selected"));
        assertTrue(postDetailActivity.contains("import android.widget.FrameLayout;"));
        assertTrue(postDetailActivity.contains("private View buildGalleryThumbnail(int index)"));
        assertTrue(postDetailActivity.contains("FrameLayout container = new FrameLayout(this)"));
        assertTrue(postDetailActivity.contains("container.setPadding(dp(3), dp(3), dp(3), dp(3))"));
        assertTrue(postDetailActivity.contains("container.setBackgroundResource(index == galleryIndex ? R.drawable.bg_thumbnail_selected : R.drawable.bg_image_placeholder)"));
        assertTrue(postDetailActivity.contains("thumb.setClipToOutline(true)"));
        assertFalse(postDetailActivity.contains("thumb.setBackgroundResource(index == galleryIndex ? R.drawable.bg_thumbnail_selected : R.drawable.bg_image_placeholder)"));
        assertTrue(postDetailActivity.contains("toggleLike()"));
        assertTrue(postDetailActivity.contains("toggleFavorite()"));
        assertTrue(postDetailActivity.contains("focusCommentInput()"));
        assertTrue(postDetailActivity.contains("toggleFollow()"));
        assertViewIdExists("activity_post_detail.xml", postDetailXml, "@+id/post_gallery_frame");
        assertViewIdExists("activity_post_detail.xml", postDetailXml, "@+id/post_thumbnail_scroll");
        assertViewIdExists("activity_post_detail.xml", postDetailXml, "@+id/post_thumbnail_strip");
        assertTrue(postDetailXml.contains("android:clipToOutline=\"true\""));
        assertTrue(postDetailXml.contains("android:scaleType=\"fitCenter\""));
        assertFalse(postDetailXml.contains("android:scaleType=\"centerCrop\""));
        String postCreateActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostCreateActivity.java");
        assertTrue(postCreateActivity.contains("uploadPostImage"));
        assertTrue(postCreateActivity.contains("uploadPostImageAsset"));
        assertTrue(postCreateActivity.contains("publicUrl"));
        assertTrue(postCreateActivity.contains("createPost"));
        assertTrue(postCreateActivity.contains("PostCaptureActivity"));
        assertTrue(postCreateActivity.contains("openCreatedPost(post)"));
        assertTrue(postCreateActivity.contains("new Intent(this, PostDetailActivity.class)"));
        assertTrue(postCreateActivity.contains("intent.putExtra(IntentExtras.POST_ID, post.postId)"));
        assertFalse(postCreateActivity.contains("REVIEWING"));
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

    private static void assertContainsInOrder(String value, String... tokens) {
        int previous = -1;
        for (String token : tokens) {
            int current = value.indexOf(token);
            assertTrue("missing " + token, current >= 0);
            assertTrue(token + " appears out of order", current > previous);
            previous = current;
        }
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
