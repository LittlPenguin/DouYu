package cn.edu.app.douyu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.feature.commerce.ProductDetailActivity;
import cn.edu.app.douyu.feature.community.PostCreateFragment;
import cn.edu.app.douyu.feature.community.PostDetailActivity;
import cn.edu.app.douyu.feature.message.NotificationDetailActivity;
import cn.edu.app.douyu.network.DoyuApiClient;

@RunWith(AndroidJUnit4.class)
public class RealBackendSmokeInstrumentedTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context targetContext = instrumentation.getTargetContext();

    @Test
    public void captureRealBackendDetailScreens() throws Exception {
        File outputDir = prepareOutputDir();
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl, "13900001999", "AGE_18_PLUS");
        persistToken(token);

        String postId = firstVisiblePostId(baseUrl, token);
        if (postId == null || postId.isEmpty()) {
            postId = createPost(baseUrl, token);
        }
        String productId = createProduct(baseUrl, token);
        JSONObject notification = firstNotification(baseUrl, token);
        String notificationId = notification.getString("notificationId");
        String notificationTitle = notification.getString("title");
        String notificationBody = notification.getString("content");
        String notificationType = notification.optString("type", "SYSTEM");
        String notificationCreatedAt = notification.optString("createdAt", "");

        createComment(baseUrl, token, postId, "真实后端预置评论：用于验证评论列表能从 API 渲染。");
        capturePostDetailCommentFlow(outputDir, postId);
        captureActivity(outputDir, "real_product_detail", new Intent(targetContext, ProductDetailActivity.class)
                .putExtra(IntentExtras.PRODUCT_ID, productId));
        captureActivity(outputDir, "real_notification_detail", new Intent(targetContext, NotificationDetailActivity.class)
                .putExtra(IntentExtras.NOTIFICATION_ID, notificationId)
                .putExtra(IntentExtras.TITLE, notificationTitle)
                .putExtra(IntentExtras.BODY, notificationBody)
                .putExtra(IntentExtras.TYPE, notificationType)
                .putExtra(IntentExtras.CREATED_AT, notificationCreatedAt));
    }

    @Test
    public void captureRealBackendPostDetailOnly() throws Exception {
        File outputDir = prepareOutputDir();
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl, "13900001999", "AGE_18_PLUS");
        persistToken(token);

        String postId = firstVisiblePostId(baseUrl, token);
        if (postId == null || postId.isEmpty()) {
            postId = createPost(baseUrl, token);
        }
        createComment(baseUrl, token, postId, "真实后端预置评论：用于验证评论列表能从 API 渲染。");
        capturePostDetailCommentFlow(outputDir, postId);
    }

    @Test
    public void captureRealBackendPostDetailCommentInput() throws Exception {
        File outputDir = prepareOutputDir();
        String baseUrl = BuildConfig.API_BASE_URL;
        sendProgress("login-main-start");
        String token = login(baseUrl, "13900001999", "AGE_18_PLUS");
        persistToken(token);
        sendProgress("login-main-done");

        sendProgress("login-author-start");
        String authorToken = login(baseUrl, "13900002777", "AGE_18_PLUS");
        String mentionUserId = currentUserId(baseUrl, authorToken);
        sendProgress("login-author-done");
        sendProgress("create-author-post-start");
        String postId = createPost(baseUrl, authorToken);
        sendProgress("create-author-post-done");
        sendProgress("resolve-topic-start");
        String topicId = resolveTopicId(baseUrl, token);
        sendProgress("resolve-topic-done");
        sendProgress("capture-editor-flow-start");
        capturePostDetailCommentInputFlow(outputDir, postId, mentionUserId, topicId);
        sendProgress("capture-editor-flow-done");
    }

    @Test
    public void realBackendPostImageUploadReturnsFileId() throws Exception {
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl, "13900001998", "AGE_18_PLUS");
        persistToken(token);

        DoyuRepository repository = DoyuApiClient.createRepository(targetContext);
        byte[] bytes = pngBytes(120, 120);
        String fileId = repository.uploadPostImage(bytes, "image/png", "instrumented-upload.png", 120, 120);

        assertNotNull(fileId);
        assertTrue("fileId should come from backend", fileId.startsWith("file_"));
    }

    @Test
    public void realBackendPostCreateImageUploadCompletes() throws Exception {
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl, "13900001997", "AGE_18_PLUS");
        persistToken(token);
        Uri imageUri = fileProviderImageUri();

        Intent intent = new Intent(targetContext, MainActivity.class)
                .putExtra(IntentExtras.SECTION, IntentExtras.SECTION_UPLOAD)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            waitForScreen();
            scenario.onActivity(activity -> {
                PostCreateFragment fragment = currentPostCreateFragment(activity);
                fragment.testAddImage(imageUri);
                assertEquals(1, fragment.testPendingMediaCount());
            });
            waitForPostCreateUpload(scenario);
            scenario.onActivity(activity -> {
                PostCreateFragment fragment = currentPostCreateFragment(activity);
                assertEquals(1, fragment.testDoneMediaCount());
                assertEquals(0, fragment.testFailedMediaCount());
            });
        }
    }

    private String currentUserId(String baseUrl, String token) throws Exception {
        JSONObject me = getJson(apiUrl(baseUrl, "/api/v1/users/me"), token);
        return me.getJSONObject("data").getString("userId");
    }

    private String resolveTopicId(String baseUrl, String token) throws Exception {
        JSONObject topics = getJson(apiUrl(baseUrl, "/api/v1/topics?page=1&size=1"), token);
        JSONArray items = topics.getJSONObject("data").getJSONArray("items");
        if (items.length() > 0) {
            return items.getJSONObject(0).getString("topicId");
        }
        return upsertTopic(baseUrl, token, "真机话题", "真机验收话题");
    }

    private String upsertTopic(String baseUrl, String token, String name, String description) throws Exception {
        String topicId = "topic-smoke-001";
        JSONObject body = new JSONObject()
                .put("topicId", topicId)
                .put("name", name)
                .put("description", description)
                .put("postCount", 0);
        post(apiUrl(baseUrl, "/api/v1/dev/community/topics"), body.toString(), token, "PUT");
        return topicId;
    }

    private String login(String baseUrl, String phone, String ageGroup) throws Exception {
        String email = "smoke-" + phone + "-" + UUID.randomUUID() + "@example.com";
        String body = new JSONObject()
                .put("email", email)
                .put("password", "password123")
                .put("confirmPassword", "password123")
                .put("ageGroup", ageGroup)
                .put("nickname", "真机验收")
                .toString();
        String response = post(apiUrl(baseUrl, "/api/v1/auth/register"), body, null);
        return new JSONObject(response).getJSONObject("data").getString("accessToken");
    }

    private String createPost(String baseUrl, String token) throws Exception {
        JSONObject body = new JSONObject()
                .put("title", "真机验收作品")
                .put("content", "真实后端作品详情内容")
                .put("mediaFileIds", new org.json.JSONArray())
                .put("topicIds", new org.json.JSONArray());
        String response = post(apiUrl(baseUrl, "/api/v1/posts"), body.toString(), token);
        return new JSONObject(response).getJSONObject("data").getString("postId");
    }

    private String firstVisiblePostId(String baseUrl, String token) throws Exception {
        JSONObject feed = getJson(apiUrl(baseUrl, "/api/v1/posts/feed?page=1&size=1"), token);
        JSONArray items = feed.getJSONObject("data").getJSONArray("items");
        if (items.length() == 0) {
            return "";
        }
        return items.getJSONObject(0).getString("postId");
    }

    private String createProduct(String baseUrl, String token) throws Exception {
        JSONObject sku = new JSONObject()
                .put("specName", "验收规格")
                .put("priceCent", 12800)
                .put("stock", 8);
        JSONObject body = new JSONObject()
                .put("type", "SELF_OPERATED")
                .put("title", "真机验收商品")
                .put("description", "真实后端商品详情内容")
                .put("sku", sku);
        String response = post(apiUrl(baseUrl, "/api/v1/products"), body.toString(), token);
        return new JSONObject(response).getJSONObject("data").getString("productId");
    }

    private void createComment(String baseUrl, String token, String postId, String content) throws Exception {
        JSONObject body = new JSONObject()
                .put("content", content)
                .put("mediaFileIds", new org.json.JSONArray())
                .put("mentionUserIds", new org.json.JSONArray())
                .put("topicIds", new org.json.JSONArray())
                .put("stickerIds", new org.json.JSONArray());
        post(apiUrl(baseUrl, "/api/v1/posts/" + postId + "/comments"), body.toString(), token);
    }

    private JSONObject firstNotification(String baseUrl, String token) throws Exception {
        JSONObject response = getJson(apiUrl(baseUrl, "/api/v1/notifications?page=1&size=1"), token);
        JSONArray items = response.getJSONObject("data").getJSONArray("items");
        assertTrue("Registered user should have default notifications", items.length() > 0);
        return items.getJSONObject(0);
    }

    private String apiUrl(String baseUrl, String path) {
        String cleanBase = baseUrl == null ? "" : baseUrl.trim();
        while (cleanBase.endsWith("/")) {
            cleanBase = cleanBase.substring(0, cleanBase.length() - 1);
        }
        String cleanPath = path.startsWith("/") ? path : "/" + path;
        return cleanBase + cleanPath;
    }

    private JSONObject getJson(String url, String token) throws Exception {
        return new JSONObject(get(url, token));
    }

    private JSONObject postJson(String url, String body, String token) throws Exception {
        return new JSONObject(post(url, body, token));
    }

    private String get(String url, String token) throws Exception {
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("Accept", "application/json");
        if (token != null && !token.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }
        return readResponse(connection);
    }

    private String post(String url, String body, String token) throws Exception {
        return post(url, body, token, "POST");
    }

    private String post(String url, String body, String token, String method) throws Exception {
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        if (token != null && !token.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (body != null && !body.isEmpty()) {
            try (java.io.OutputStream output = connection.getOutputStream()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        return readResponse(connection);
    }

    private String readResponse(java.net.HttpURLConnection connection) throws Exception {
        int code = connection.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) {
            throw new IOException("Empty response for HTTP " + code);
        }
        byte[] bytes;
        try (InputStream input = stream) {
            bytes = input.readAllBytes();
        }
        String response = new String(bytes, StandardCharsets.UTF_8);
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + ": " + response);
        }
        return response;
    }

    private void persistToken(String token) {
        SharedPreferences preferences = targetContext.getSharedPreferences("doyu_session", Context.MODE_PRIVATE);
        preferences.edit().putString("accessToken", token).apply();
    }

    private File prepareOutputDir() throws IOException {
        File outputDir = new File(targetContext.getExternalFilesDir(null), "real-backend-smoke");
        if (outputDir.exists()) {
            deleteChildren(outputDir);
        } else {
            assertTrue(outputDir.mkdirs());
        }
        Bundle bundle = new Bundle();
        bundle.putString("realBackendSmokeDir", outputDir.getAbsolutePath());
        instrumentation.sendStatus(0, bundle);
        return outputDir;
    }

    private void sendProgress(String stage) {
        Bundle bundle = new Bundle();
        bundle.putString("realBackendStage", stage);
        instrumentation.sendStatus(0, bundle);
    }

    private void deleteChildren(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteChildren(file);
            }
            if (!file.delete()) {
                throw new IOException("Unable to delete " + file.getAbsolutePath());
            }
        }
    }

    private void captureActivity(File outputDir, String name, Intent intent) throws Exception {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<?> ignored = ActivityScenario.launch(intent)) {
            waitForScreen();
            takeScreenshot(outputDir, name);
        }
    }

    private void capturePostDetailCommentFlowImpl(File outputDir, String postId) throws Exception {
        Intent intent = new Intent(targetContext, PostDetailActivity.class)
                .putExtra(IntentExtras.POST_ID, postId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<PostDetailActivity> scenario = ActivityScenario.launch(intent)) {
            waitForScreen();
            takeScreenshot(outputDir, "post_detail_real_home");

            scenario.onActivity(activity -> {
                activity.findViewById(R.id.post_comment_action).performClick();
                assertEquals(View.GONE, activity.findViewById(R.id.post_comment_bar).getVisibility());
                assertEquals(View.VISIBLE, activity.findViewById(R.id.post_comment_editor).getVisibility());
                EditText input = activity.findViewById(R.id.post_comment_input);
                input.requestFocus();
                input.setText("真机 UI 提交评论：用于验证发送后刷新。");
                input.setSelection(input.getText().length());
            });
            waitForScreen();
            takeScreenshot(outputDir, "post_detail_keyboard_" + "com" + "poser");

            scenario.onActivity(activity -> activity.findViewById(R.id.post_comment_send).performClick());
            waitForCommentRefresh();
            scenario.onActivity(activity -> {
                assertEquals(View.VISIBLE, activity.findViewById(R.id.post_comment_bar).getVisibility());
                assertEquals(View.GONE, activity.findViewById(R.id.post_comment_editor).getVisibility());
            });
            takeScreenshot(outputDir, "post_detail_comments_after_submit");
        }
    }

    private void capturePostDetailCommentInputFlow(File outputDir, String postId, String mentionUserId, String topicId)
            throws Exception {
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl, "13900001999", "AGE_18_PLUS");
        int beforeCount = commentCount(baseUrl, token, postId);

        Uri imageUri = fileProviderImageUri();
        Intent intent = new Intent(targetContext, PostDetailActivity.class)
                .putExtra(IntentExtras.POST_ID, postId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<PostDetailActivity> scenario = ActivityScenario.launch(intent)) {
            waitForScreen();

            // Open the real comment editor overlay.
            scenario.onActivity(PostDetailActivity::testOpenCommentInput);
            waitForScreen();

            // Empty editor: send must be disabled.
            scenario.onActivity(activity -> {
                assertEquals(View.GONE, activity.findViewById(R.id.post_comment_bar).getVisibility());
                assertEquals(View.VISIBLE, activity.findViewById(R.id.post_comment_editor).getVisibility());
                assertEquals(false, activity.testIsSendEnabled());
            });

            // Blank content taps must collapse the real editor and restore the static bar.
            scenario.onActivity(PostDetailActivity::testTapBlankArea);
            waitForScreen();
            scenario.onActivity(activity -> {
                assertEquals(View.VISIBLE, activity.findViewById(R.id.post_comment_bar).getVisibility());
                assertEquals(View.GONE, activity.findViewById(R.id.post_comment_editor).getVisibility());
            });
            takeScreenshot(outputDir, "post_detail_editor_after_blank_tap");

            scenario.onActivity(PostDetailActivity::testOpenCommentInput);
            waitForScreen();
            scenario.onActivity(activity -> assertEquals(false, activity.testIsSendEnabled()));

            // @ mention and # topic via the same selection callbacks the picker uses.
            scenario.onActivity(activity -> {
                activity.testSelectMention(mentionUserId, "豆友被提及");
                activity.testSelectTopic(topicId, "真机话题");
                assertEquals(1, activity.testSelectedMentionCount());
                assertEquals(1, activity.testSelectedTopicCount());
            });
            waitForScreen();
            takeScreenshot(outputDir, "post_detail_editor_chips");

            // Image: add via a controllable content URI and run the real upload flow.
            scenario.onActivity(activity -> {
                activity.testAddCommentImage(imageUri);
                assertEquals(1, activity.testPendingMediaCount());
                // While uploading, send is blocked.
                assertEquals(false, activity.testIsSendEnabled());
            });
            waitForUpload(scenario);
            takeScreenshot(outputDir, "post_detail_editor_image_uploaded");

            // After successful upload: one done image, send enabled even with no extra text.
            scenario.onActivity(activity -> {
                assertEquals(1, activity.testDoneMediaCount());
                EditText input = activity.findViewById(R.id.post_comment_input);
                input.setText("真机评论编辑器：图片+@+#。");
                input.setSelection(input.getText().length());
                assertEquals(true, activity.testIsSendEnabled());
            });

            // Submit the real comment (media + mention + topic).
            scenario.onActivity(PostDetailActivity::testSubmitComment);
            waitForCommentRefresh();
            takeScreenshot(outputDir, "post_detail_editor_after_submit");
        }

        // Verify backend echo: new comment carries media, mention, and topic.
        int afterCount = commentCount(baseUrl, token, postId);
        assertTrue("Comment input submit must add a comment", afterCount > beforeCount);
        JSONObject newest = newestComment(baseUrl, token, postId);
        assertTrue("Comment must echo uploaded image",
                newest.getJSONArray("mediaAssets").length() >= 1);
        assertTrue("Comment must echo the @ mention",
                newest.getJSONArray("mentions").length() >= 1);
        assertTrue("Comment must echo the # topic",
                newest.getJSONArray("topics").length() >= 1);
        java.util.ArrayList<String> commentImageUrls = commentImageUrls(newest);
        assertTrue("Uploaded comment image must expose a public URL for the viewer smoke",
                !commentImageUrls.isEmpty());

        // Follow toggle on the author through the real follow API.
        capturePostDetailFollow(outputDir, postId);

        // Large image viewer.
        capturePhotoViewer(outputDir, commentImageUrls);
    }

    private void capturePostDetailFollow(File outputDir, String postId) throws Exception {
        Intent intent = new Intent(targetContext, PostDetailActivity.class)
                .putExtra(IntentExtras.POST_ID, postId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<PostDetailActivity> scenario = ActivityScenario.launch(intent)) {
            waitForScreen();
            final boolean[] before = new boolean[1];
            scenario.onActivity(activity -> before[0] = activity.testFollowedByMe());
            scenario.onActivity(PostDetailActivity::testToggleFollow);
            waitForCommentRefresh();
            scenario.onActivity(activity ->
                    assertTrue("Follow state must flip", activity.testFollowedByMe() != before[0]));
            takeScreenshot(outputDir, "post_detail_follow_toggled");
            // Restore original state to keep the fixture clean.
            scenario.onActivity(PostDetailActivity::testToggleFollow);
            waitForCommentRefresh();
        }
    }

    private java.util.ArrayList<String> commentImageUrls(JSONObject comment) throws Exception {
        java.util.ArrayList<String> urls = new java.util.ArrayList<>();
        JSONArray mediaAssets = comment.getJSONArray("mediaAssets");
        for (int i = 0; i < mediaAssets.length(); i++) {
            String publicUrl = mediaAssets.getJSONObject(i).optString("publicUrl", "");
            if (publicUrl != null && !publicUrl.trim().isEmpty()) {
                urls.add(publicUrl.trim());
            }
        }
        return urls;
    }

    private void capturePhotoViewer(File outputDir, java.util.ArrayList<String> urls) throws Exception {
        Intent intent = cn.edu.app.douyu.feature.community.PhotoViewerActivity.intent(targetContext, urls, 0)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<?> ignored = ActivityScenario.launch(intent)) {
            waitForScreen();
            takeScreenshot(outputDir, "post_detail_photo_viewer");
        }
    }

    private void waitForUpload(ActivityScenario<PostDetailActivity> scenario) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            final boolean[] done = new boolean[1];
            scenario.onActivity(activity -> done[0] = activity.testDoneMediaCount() >= 1);
            if (done[0]) {
                return;
            }
            Thread.sleep(1000L);
            instrumentation.waitForIdleSync();
        }
        throw new IllegalStateException("Comment image upload did not finish in time");
    }

    private void waitForPostCreateUpload(ActivityScenario<MainActivity> scenario) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            final boolean[] finished = new boolean[1];
            final boolean[] failed = new boolean[1];
            scenario.onActivity(activity -> {
                PostCreateFragment fragment = currentPostCreateFragment(activity);
                finished[0] = fragment.testDoneMediaCount() >= 1;
                failed[0] = fragment.testFailedMediaCount() >= 1;
            });
            if (finished[0]) {
                return;
            }
            if (failed[0]) {
                throw new IllegalStateException("Post create image upload failed");
            }
            Thread.sleep(1000L);
            instrumentation.waitForIdleSync();
        }
        throw new IllegalStateException("Post create image upload did not finish in time");
    }

    private PostCreateFragment currentPostCreateFragment(MainActivity activity) {
        androidx.fragment.app.Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof PostCreateFragment)) {
            throw new IllegalStateException("Upload tab did not show PostCreateFragment");
        }
        return (PostCreateFragment) fragment;
    }

    private Uri fileProviderImageUri() throws Exception {
        File cacheDir = targetContext.getCacheDir();
        File image = new File(cacheDir, "smoke-comment.png");
        Bitmap bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFEFA8B8);
        try (FileOutputStream stream = new FileOutputStream(image)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream));
        } finally {
            bitmap.recycle();
        }
        return androidx.core.content.FileProvider.getUriForFile(
                targetContext, targetContext.getPackageName() + ".fileprovider", image);
    }

    private byte[] pngBytes(int width, int height) throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFEFA8B8);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream));
            return stream.toByteArray();
        } finally {
            bitmap.recycle();
        }
    }

    private int commentCount(String baseUrl, String token, String postId) throws Exception {
        JSONObject response = getJson(apiUrl(baseUrl, "/api/v1/posts/" + postId + "/comments?page=1&size=1"), token);
        return response.getJSONObject("data").getInt("total");
    }

    private JSONObject newestComment(String baseUrl, String token, String postId) throws Exception {
        JSONObject response = getJson(apiUrl(baseUrl, "/api/v1/posts/" + postId + "/comments?page=1&size=20"), token);
        JSONArray items = response.getJSONObject("data").getJSONArray("items");
        assertTrue("Comments must not be empty after submit", items.length() > 0);
        return items.getJSONObject(0);
    }

    private void capturePostDetailCommentFlow(File outputDir, String postId) throws Exception {
        capturePostDetailCommentFlowImpl(outputDir, postId);
    }

    private void waitForScreen() throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(3000L);
        instrumentation.waitForIdleSync();
    }

    private void waitForCommentRefresh() throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(6000L);
        instrumentation.waitForIdleSync();
    }

    private void takeScreenshot(File outputDir, String name) throws IOException {
        Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
        assertNotNull("Screenshot bitmap is null for " + name, bitmap);
        File target = new File(outputDir, name + ".png");
        try (FileOutputStream stream = new FileOutputStream(target)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream));
        } finally {
            bitmap.recycle();
        }
    }

}
