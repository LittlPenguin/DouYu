package cn.edu.app.douyu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.feature.commerce.ProductDetailActivity;
import cn.edu.app.douyu.feature.community.PostCreateActivity;
import cn.edu.app.douyu.feature.community.PostDetailActivity;
import cn.edu.app.douyu.feature.community.SearchActivity;
import cn.edu.app.douyu.feature.message.ConversationActivity;
import cn.edu.app.douyu.feature.message.NotificationDetailActivity;
import cn.edu.app.douyu.feature.profile.ProfileEditActivity;
import cn.edu.app.douyu.feature.profile.SettingsActivity;

@RunWith(AndroidJUnit4.class)
public class VisualSmokeInstrumentedTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void captureJavaXmlScreens() throws Exception {
        File outputDir = prepareOutputDir();
        captureMainTabs(outputDir);
        captureActivity(outputDir, "search", new Intent(targetContext, SearchActivity.class));
        captureActivity(outputDir, "post_create", new Intent(targetContext, PostCreateActivity.class));
        captureActivity(outputDir, "post_detail", new Intent(targetContext, PostDetailActivity.class)
                .putExtra(IntentExtras.POST_ID, "post_visual_check"));
        captureActivity(outputDir, "product_detail", new Intent(targetContext, ProductDetailActivity.class)
                .putExtra(IntentExtras.PRODUCT_ID, "product_visual_check"));
        captureActivity(outputDir, "conversation", new Intent(targetContext, ConversationActivity.class)
                .putExtra(IntentExtras.CONVERSATION_ID, "conv_visual_check"));
        captureActivity(outputDir, "notification_detail", new Intent(targetContext, NotificationDetailActivity.class)
                .putExtra(IntentExtras.NOTIFICATION_ID, "notif_visual_check")
                .putExtra(IntentExtras.TITLE, "通知事件")
                .putExtra(IntentExtras.BODY, "通知详情视觉验收"));
        captureActivity(outputDir, "profile_edit", new Intent(targetContext, ProfileEditActivity.class));
        captureActivity(outputDir, "settings_home", new Intent(targetContext, SettingsActivity.class));
        captureActivity(outputDir, "settings_account_security", new Intent(targetContext, SettingsActivity.class)
                .putExtra(IntentExtras.SECTION, "account_security"));
        captureActivity(outputDir, "settings_privacy_permissions", new Intent(targetContext, SettingsActivity.class)
                .putExtra(IntentExtras.SECTION, "privacy_permissions"));
        captureActivity(outputDir, "settings_notifications", new Intent(targetContext, SettingsActivity.class)
                .putExtra(IntentExtras.SECTION, "notifications"));
        captureActivity(outputDir, "settings_help_about", new Intent(targetContext, SettingsActivity.class)
                .putExtra(IntentExtras.SECTION, "help_about"));
    }

    private void captureMainTabs(File outputDir) throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForNetworkBoundary();
            takeScreenshot(outputDir, "main_community");
            captureTab(scenario, outputDir, "main_commerce", R.id.tab_commerce);
            takeScreenshot(outputDir, "main_upload_action_visible");
            captureTab(scenario, outputDir, "main_messages", R.id.tab_messages);
            captureTab(scenario, outputDir, "main_profile", R.id.tab_profile);
        }
    }

    private void captureTab(
            ActivityScenario<MainActivity> scenario,
            File outputDir,
            String name,
            int itemId
    ) throws Exception {
        scenario.onActivity(activity -> {
            activity.findViewById(itemId).performClick();
        });
        waitForNetworkBoundary();
        takeScreenshot(outputDir, name);
    }

    private void captureActivity(File outputDir, String name, Intent intent) throws Exception {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<?> ignored = ActivityScenario.launch(intent)) {
            waitForScreen();
            takeScreenshot(outputDir, name);
        }
    }

    private File prepareOutputDir() throws IOException {
        File outputDir = new File(targetContext.getExternalFilesDir(null), "visual-smoke");
        if (outputDir.exists()) {
            deleteChildren(outputDir);
        } else {
            assertTrue(outputDir.mkdirs());
        }
        Bundle bundle = new Bundle();
        bundle.putString("visualSmokeDir", outputDir.getAbsolutePath());
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

    private void waitForScreen() throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(2500L);
        instrumentation.waitForIdleSync();
    }

    private void waitForNetworkBoundary() throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(12000L);
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
