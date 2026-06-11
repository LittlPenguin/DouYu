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
                new LayoutMapping("post-" + "com" + "pose-a.html", R.layout.fragment_post_create),
                new LayoutMapping("post-detail-comment-toolbar-a.html", R.layout.activity_post_detail),
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

        assertEquals(19, mappings.length);
        for (LayoutMapping mapping : mappings) {
            assertTrue(mapping.openDesignPage.endsWith(".html"));
            assertTrue(mapping.layoutId > 0);
        }
    }

    @Test
    public void mainNavigationUsesFiveContentTabsIncludingUpload() throws IOException {
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
        assertTrue(mainActivity.contains("new PostCreateFragment()"));
        assertTrue(mainActivity.contains("R.id.nav_indicator_upload"));
        assertTrue(mainActivity.contains("IntentExtras.SECTION_UPLOAD"));
        assertFalse(mainActivity.contains("PostCreateActivity"));
        assertFalse(mainActivity.contains("Ai" + "Fragment"));
        assertFalse(mainActivity.contains("quick_ai"));
        assertFalse(mainActivity.contains("quick_" + "post"));
        assertTrue(intentExtras.contains("SECTION_COMMUNITY"));
        assertTrue(intentExtras.contains("SECTION_COMMERCE"));
        assertTrue(intentExtras.contains("SECTION_UPLOAD"));
        assertTrue(intentExtras.contains("SECTION_MESSAGES"));
        assertTrue(intentExtras.contains("SECTION_PROFILE"));
    }

    @Test
    public void postCreateFragmentUsesMainShellNavigationAndRealUploadContracts() throws IOException {
        String postCreateXml = readUtf8("src/main/res/layout/fragment_post_create.xml");
        String postCreateFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java");
        String manifest = readUtf8("src/main/AndroidManifest.xml");

        assertFalse(postCreateXml.contains("layout=\"@layout/include_bottom_nav\""));
        assertFalse(postCreateXml.contains("@+id/post_bottom_nav"));
        assertFalse(postCreateXml.contains("@+id/back_button"));
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_publish_top");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_title_input");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_body_input");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_pick_image");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_capture_image");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_image_grid");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_topic_group");
        assertViewIdExists("fragment_post_create.xml", postCreateXml, "@+id/post_publish_button");

        assertFalse(manifest.contains(".feature.community.PostCreateActivity"));
        assertTrue(manifest.contains(".feature.community.PostCaptureActivity"));
        assertFalse(postCreateFragment.contains("openMainSection("));
        assertFalse(postCreateFragment.contains("selectUploadNav"));
        assertFalse(postCreateFragment.contains("R.id.nav_indicator_upload"));
        assertTrue(postCreateFragment.contains("public interface Host"));
        assertTrue(postCreateFragment.contains("navigateToSection(String section)"));
        assertTrue(postCreateFragment.contains("uploadPostImageAsset"));
        assertTrue(postCreateFragment.contains("createPost"));
        assertTrue(postCreateFragment.contains("PostCaptureActivity"));
        assertTrue(postCreateFragment.contains("openCreatedPost(post)"));
        assertTrue(postCreateFragment.contains("new Intent(requireContext(), PostDetailActivity.class)"));
        assertTrue(postCreateFragment.contains("intent.putExtra(IntentExtras.POST_ID, post.postId)"));
        assertTrue(postCreateFragment.contains("styleTopicChip(chip)"));
        assertTrue(postCreateFragment.contains("private ColorStateList topicChipBackgroundColor()"));
        assertTrue(postCreateFragment.contains("private ColorStateList topicChipTextColor()"));
        assertTrue(postCreateFragment.contains("private ColorStateList topicChipStrokeColor()"));
        assertFalse(postCreateFragment.contains("REVIEWING"));
    }

    @Test
    public void uploadAndProfileContractsStayBackendDriven() throws IOException {
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String profileFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileFragment.java");
        String profileXml = readUtf8("src/main/res/layout/fragment_profile_home.xml");
        String profileEditActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/ProfileEditActivity.java");
        String profileEditXml = readUtf8("src/main/res/layout/activity_profile_edit.xml");
        String userProfile = readUtf8("src/main/java/cn/edu/app/douyu/model/UserProfile.java");
        String updateProfileRequest = readUtf8("src/main/java/cn/edu/app/douyu/model/UpdateProfileRequest.java");

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
        assertTrue(profileFragment.contains("repository.likedPosts()"));
        assertTrue(profileFragment.contains("repository.favoritePosts()"));
        assertFalse(profileFragment.contains("patternJobs"));
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/tab_liked");
        assertViewIdExists("fragment_profile_home.xml", profileXml, "@+id/tab_favorites");
    }

    @Test
    public void removedFeatureLayoutsAndEndpointsAreAbsent() throws IOException {
        String api = readUtf8("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = readUtf8("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String manifest = readUtf8("src/main/AndroidManifest.xml");
        String searchXml = readUtf8("src/main/res/layout/activity_search.xml");
        String postCreateXml = readUtf8("src/main/res/layout/fragment_post_create.xml");
        String settingsPrivacy = readUtf8("src/main/res/layout/activity_settings_privacy_permissions.xml");

        assertFalse(api.contains("/api/v1/patterns"));
        assertFalse(api.contains("favorite-patterns"));
        assertFalse(repository.contains("createPattern" + "Job"));
        assertFalse(repository.contains("favoritePatterns"));
        assertFalse(manifest.contains(".feature.ai"));
        assertFalse(manifest.contains("PaymentBoundary" + "Activity"));
        assertTrue(manifest.contains("PostCaptureActivity"));
        assertTrue(manifest.contains("android.permission.CAMERA"));
        assertFalse(searchXml.contains("pattern"));
        assertFalse(postCreateXml.contains("pattern"));
        assertFalse(postCreateXml.contains("@+id/post_bottom_nav"));
        assertFalse(settingsPrivacy.contains("map"));
        assertFalse(settingsPrivacy.contains("location"));
        assertFalse(settingsPrivacy.contains("私信"));
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
    public void postDetailAndAuthContractsRemainRealBackendDriven() throws IOException {
        String postDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java");
        String postDetailXml = readUtf8("src/main/res/layout/activity_post_detail.xml");
        String postCreateFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java");
        String loginActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/LoginActivity.java");
        String registerActivity = readUtf8("src/main/java/cn/edu/app/douyu/auth/RegisterActivity.java");
        String settingsActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java");

        assertTrue(postDetailActivity.contains("repository.post(postId)"));
        assertTrue(postDetailActivity.contains("repository.comments(postId)"));
        assertTrue(postDetailActivity.contains("uploadPostImage"));
        assertTrue(postDetailActivity.contains("AuthGate.runOrRequestLogin"));
        assertTrue(postDetailActivity.contains("toggleLike()"));
        assertTrue(postDetailActivity.contains("toggleFavorite()"));
        assertTrue(postDetailActivity.contains("focusCommentInput()"));
        assertTrue(postDetailActivity.contains("toggleFollow()"));
        assertViewIdExists("activity_post_detail.xml", postDetailXml, "@+id/post_gallery_frame");
        assertViewIdExists("activity_post_detail.xml", postDetailXml, "@+id/post_thumbnail_scroll");
        assertViewIdExists("activity_post_detail.xml", postDetailXml, "@+id/post_thumbnail_strip");
        assertTrue(postCreateFragment.contains("uploadPostImage"));
        assertTrue(postCreateFragment.contains("uploadPostImageAsset"));
        assertTrue(postCreateFragment.contains("publicUrl"));
        assertTrue(postCreateFragment.contains("createPost"));
        assertTrue(postCreateFragment.contains("PostCaptureActivity"));
        assertTrue(postCreateFragment.contains("openCreatedPost(post)"));
        assertTrue(postCreateFragment.contains("new Intent(requireContext(), PostDetailActivity.class)"));
        assertTrue(postCreateFragment.contains("intent.putExtra(IntentExtras.POST_ID, post.postId)"));
        assertTrue(loginActivity.contains("repository.login(email, password)"));
        assertTrue(registerActivity.contains("repository.register(email, password, confirmPassword, nickname, ageGroup)"));
        assertTrue(settingsActivity.contains("store.clear()"));
        assertFalse(settingsActivity.contains("repository.logout("));
        assertFalse(settingsActivity.contains("refreshToken"));
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
