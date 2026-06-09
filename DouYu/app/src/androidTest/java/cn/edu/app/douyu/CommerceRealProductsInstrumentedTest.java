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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.feature.commerce.ProductDetailActivity;

@RunWith(AndroidJUnit4.class)
public class CommerceRealProductsInstrumentedTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context targetContext = instrumentation.getTargetContext();

    @Test
    public void captureCommerceHomeCategoriesAndRealProductDetail() throws Exception {
        File outputDir = prepareOutputDir();
        String baseUrl = BuildConfig.API_BASE_URL;
        String token = login(baseUrl);
        persistToken(token);

        JSONObject products = getJson(apiUrl(baseUrl, "/api/v1/products?page=1&size=20"), token);
        JSONArray productItems = products.getJSONObject("data").getJSONArray("items");
        assertTrue("Commerce smoke requires at least one imported visible product", productItems.length() > 0);
        String productId = productItems.getJSONObject(0).getString("productId");

        JSONObject categories = getJson(apiUrl(baseUrl, "/api/v1/product-categories"), token);
        JSONArray categoryItems = categories.getJSONObject("data").getJSONArray("items");
        assertTrue("Commerce smoke requires at least one visible product category", categoryItems.length() > 0);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForNetwork();
            scenario.onActivity(activity -> activity.findViewById(R.id.tab_commerce).performClick());
            waitForNetwork();
            takeScreenshot(outputDir, "commerce_real_home");

            scenario.onActivity(activity -> clickChipAt(activity.findViewById(R.id.section_chips), 1));
            waitForNetwork();
            takeScreenshot(outputDir, "commerce_category_1");

            if (categoryItems.length() > 1) {
                scenario.onActivity(activity -> clickChipAt(activity.findViewById(R.id.section_chips), 2));
                waitForNetwork();
                takeScreenshot(outputDir, "commerce_category_2");
            }
        }

        captureActivity(outputDir, "commerce_real_product_detail", new Intent(targetContext, ProductDetailActivity.class)
                .putExtra(IntentExtras.PRODUCT_ID, productId));
    }

    private void clickChipAt(android.view.View view, int index) {
        if (!(view instanceof android.view.ViewGroup)) {
            throw new AssertionError("Commerce category chip group is missing");
        }
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        if (group.getChildCount() <= index) {
            throw new AssertionError("Missing commerce category chip at index " + index);
        }
        group.getChildAt(index).performClick();
    }

    private String login(String baseUrl) throws Exception {
        String phone = "13900002081";
        String email = "commerce-" + phone + "-" + UUID.randomUUID() + "@example.com";
        String body = new JSONObject()
                .put("email", email)
                .put("password", "password123")
                .put("confirmPassword", "password123")
                .put("ageGroup", "AGE_18_PLUS")
                .put("nickname", "商城真机验收")
                .toString();
        String response = post(apiUrl(baseUrl, "/api/v1/auth/register"), body, null);
        return new JSONObject(response).getJSONObject("data").getString("accessToken");
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
        File outputDir = new File(targetContext.getExternalFilesDir(null), "commerce-real-products");
        if (outputDir.exists()) {
            deleteChildren(outputDir);
        } else {
            assertTrue(outputDir.mkdirs());
        }
        Bundle bundle = new Bundle();
        bundle.putString("commerceRealProductsDir", outputDir.getAbsolutePath());
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

    private void waitForNetwork() throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(8000L);
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
