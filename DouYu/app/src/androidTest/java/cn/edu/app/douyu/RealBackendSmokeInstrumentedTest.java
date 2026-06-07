package cn.edu.app.douyu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.feature.ai.AiFlowActivity;
import cn.edu.app.douyu.feature.commerce.ProductDetailActivity;
import cn.edu.app.douyu.feature.community.PostDetailActivity;
import cn.edu.app.douyu.feature.message.ConversationActivity;
import cn.edu.app.douyu.feature.message.NotificationDetailActivity;

@RunWith(AndroidJUnit4.class)
public class RealBackendSmokeInstrumentedTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context targetContext = instrumentation.getTargetContext();

    @Test
    public void captureRealBackendDetailScreens() throws Exception {
        File outputDir = prepareOutputDir();
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl, "13900001000", "AGE_18_PLUS");
        persistToken(token);

        String postId = createPost(baseUrl, token);
        String productId = createProduct(baseUrl, token);
        PatternJobFixture patternFixture = createPatternJob(baseUrl, token);
        String jobData = patternFixture.jobResponse;
        String jobId = new JSONObject(jobData).getJSONObject("data").getString("jobId");
        String patternId = waitForPattern(baseUrl, token, jobId);
        String fixture = createMessageFixture(baseUrl, token);
        String conversationId = new JSONObject(fixture).getJSONObject("data").getString("conversationId");
        String notificationId = new JSONObject(fixture).getJSONObject("data").getString("notificationId");
        String notificationTitle = new JSONObject(fixture).getJSONObject("data").getString("notificationTitle");
        String notificationBody = new JSONObject(fixture).getJSONObject("data").getString("notificationBody");

        captureActivity(outputDir, "real_post_detail", new Intent(targetContext, PostDetailActivity.class)
                .putExtra(IntentExtras.POST_ID, postId));
        captureActivity(outputDir, "real_product_detail", new Intent(targetContext, ProductDetailActivity.class)
                .putExtra(IntentExtras.PRODUCT_ID, productId));
        captureActivity(outputDir, "real_ai_flow", new Intent(targetContext, AiFlowActivity.class)
                .putExtra(IntentExtras.UPLOADED_FILE_ID, patternFixture.fileId)
                .putExtra(IntentExtras.JOB_ID, jobId)
                .putExtra(IntentExtras.PATTERN_ID, patternId));
        captureActivity(outputDir, "real_conversation", new Intent(targetContext, ConversationActivity.class)
                .putExtra(IntentExtras.CONVERSATION_ID, conversationId));
        captureActivity(outputDir, "real_notification_detail", new Intent(targetContext, NotificationDetailActivity.class)
                .putExtra(IntentExtras.NOTIFICATION_ID, notificationId)
                .putExtra(IntentExtras.TITLE, notificationTitle)
                .putExtra(IntentExtras.BODY, notificationBody));
    }

    private String login(String baseUrl, String phone, String ageGroup) throws Exception {
        post(apiUrl(baseUrl, "/api/v1/auth/sms-code"), new JSONObject().put("phone", phone).toString(), null);
        String body = new JSONObject()
                .put("phone", phone)
                .put("code", "123456")
                .put("ageGroup", ageGroup)
                .put("nickname", "QA Smoke")
                .toString();
        String response = post(apiUrl(baseUrl, "/api/v1/auth/login/sms"), body, null);
        return new JSONObject(response).getJSONObject("data").getString("accessToken");
    }

    private String createPost(String baseUrl, String token) throws Exception {
        JSONObject body = new JSONObject()
                .put("title", "QA real post")
                .put("content", "QA real post content")
                .put("mediaFileIds", new org.json.JSONArray())
                .put("topicIds", new org.json.JSONArray())
                .put("linkedPatternId", JSONObject.NULL);
        String response = post(apiUrl(baseUrl, "/api/v1/posts"), body.toString(), token);
        return new JSONObject(response).getJSONObject("data").getString("postId");
    }

    private String createProduct(String baseUrl, String token) throws Exception {
        JSONObject sku = new JSONObject()
                .put("specName", "QA smoke sku")
                .put("priceCent", 12800)
                .put("stock", 8);
        JSONObject body = new JSONObject()
                .put("type", "SELF_OPERATED")
                .put("title", "QA smoke product")
                .put("description", "QA smoke product description")
                .put("sku", sku);
        String response = post(apiUrl(baseUrl, "/api/v1/products"), body.toString(), token);
        return new JSONObject(response).getJSONObject("data").getString("productId");
    }

    private PatternJobFixture createPatternJob(String baseUrl, String token) throws Exception {
        String fileId = uploadAndConfirm(baseUrl, token);
        JSONObject body = new JSONObject()
                .put("inputFileId", fileId)
                .put("beadSize", "MM_2_6")
                .put("targetSize", "16x16")
                .put("difficulty", "BEGINNER")
                .put("paletteId", "default")
                .put("style", "CUTE");
        return new PatternJobFixture(fileId, post(apiUrl(baseUrl, "/api/v1/patterns/jobs"), body.toString(), token));
    }

    private String waitForPattern(String baseUrl, String token, String jobId) throws Exception {
        for (int i = 0; i < 60; i++) {
            JSONObject response = getJson(apiUrl(baseUrl, "/api/v1/patterns/jobs/" + jobId), token);
            JSONObject data = response.getJSONObject("data");
            if (data.has("patternId") && !data.isNull("patternId")) {
                return data.getString("patternId");
            }
            Thread.sleep(1000L);
        }
        throw new IllegalStateException("Pattern job did not finish in time");
    }

    private String uploadAndConfirm(String baseUrl, String token) throws Exception {
        JSONObject presignRequest = new JSONObject()
                .put("usage", "AI_INPUT")
                .put("mimeType", "image/png")
                .put("sizeBytes", 1024)
                .put("fileName", "qa-smoke.png");
        JSONObject presign = postJson(apiUrl(baseUrl, "/api/v1/uploads/presign"), presignRequest.toString(), token);
        String fileKey = presign.getJSONObject("data").getString("fileKey");
        JSONObject confirmRequest = new JSONObject()
                .put("fileKey", fileKey)
                .put("usage", "AI_INPUT")
                .put("mimeType", "image/png")
                .put("sizeBytes", 1024)
                .put("width", 120)
                .put("height", 120);
        JSONObject confirmed = postJson(apiUrl(baseUrl, "/api/v1/uploads/confirm"), confirmRequest.toString(), token);
        return confirmed.getJSONObject("data").getString("fileId");
    }

    private String createMessageFixture(String baseUrl, String token) throws Exception {
        return post(apiUrl(baseUrl, "/api/v1/qa-empty/fixtures/message-thread"), "", token);
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
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setRequestMethod("POST");
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

    private void waitForScreen() throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(3000L);
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

    private static final class PatternJobFixture {
        final String fileId;
        final String jobResponse;

        PatternJobFixture(String fileId, String jobResponse) {
            this.fileId = fileId;
            this.jobResponse = jobResponse;
        }
    }
}
