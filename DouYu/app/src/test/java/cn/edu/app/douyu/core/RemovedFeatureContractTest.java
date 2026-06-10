package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemovedFeatureContractTest {
    @Test
    public void androidRuntimeDoesNotExposeAiMapApiOrPaymentPages() throws IOException {
        assertNoJavaSources("src/main/java/cn/edu/app/douyu/feature/ai");
        assertMissing("src/main/res/layout/fragment_ai_home.xml");
        assertMissing("src/main/res/layout/activity_ai_flow.xml");
        assertMissing("src/main/res/layout/activity_camera.xml");
        assertMissing("src/main/res/layout/activity_payment_boundary.xml");
        assertMissing("src/main/res/drawable/ic_action_ai.xml");
        assertMissing("src/main/res/drawable/ic_tab_" + "ai.xml");

        String manifest = read("src/main/AndroidManifest.xml");
        String mainActivity = read("src/main/java/cn/edu/app/douyu/MainActivity.java");
        String api = read("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = read("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String settingsActivity = read("src/main/java/cn/edu/app/douyu/feature/profile/SettingsActivity.java");
        String settingsHome = read("src/main/res/layout/activity_settings_home.xml");
        String settingsPrivacy = read("src/main/res/layout/activity_settings_privacy_permissions.xml");
        String profileEdit = read("src/main/res/layout/activity_profile_edit.xml");
        String visualSmoke = read("src/androidTest/java/cn/edu/app/douyu/VisualSmokeInstrumentedTest.java");

        assertDoesNotContain(manifest, ".feature.ai");
        assertDoesNotContain(manifest, "PaymentBoundary" + "Activity");
        assertDoesNotContain(mainActivity, "Ai" + "Fragment");
        assertDoesNotContain(mainActivity, "tab_" + "ai");
        assertDoesNotContain(mainActivity, "quick_ai");
        assertDoesNotContain(api, "/api/v1/patterns");
        assertDoesNotContain(api, "favorite-patterns");
        assertDoesNotContain(api, "AiQuota");
        assertDoesNotContain(repository, "createPattern" + "Job");
        assertDoesNotContain(repository, "favoritePatterns");
        assertDoesNotContain(repository, "aiQuota");
        assertMissing("src/main/res/layout/activity_settings_about_" + "compliance.xml");
        assertDoesNotContain(settingsActivity, "about_" + "compliance");
        assertDoesNotContain(settingsActivity, "activity_settings_about_" + "compliance");
        assertDoesNotContain(settingsHome, "settings_about_" + "compliance");
        assertDoesNotContain(visualSmoke, "settings_about_" + "compliance");
        assertDoesNotContain(visualSmoke, "about_" + "compliance");
        assertDoesNotContain(settingsHome, "地图 API");
        assertDoesNotContain(settingsHome, "支付 API");
        assertDoesNotContain(settingsPrivacy, "地图 API");
        assertDoesNotContain(settingsPrivacy, "位置能力");
        assertDoesNotContain(profileEdit, "地图定位");
        assertContains(settingsActivity, "help_about");
        assertContains(settingsActivity, "activity_settings_help_about");
        assertContains(settingsHome, "settings_help_about");
        assertContains(visualSmoke, "settings_help_about");
    }

    @Test
    public void workspaceDocsAndDesignArtifactsDoNotKeepRemovedFeaturePages() {
        assertMissing("../../doc/development/open-design/ai-home-a.html");
        assertMissing("../../doc/development/open-design/future-capability-ui-a.html");
        assertMissing("../../doc/development/open-design/settings-about-" + "compliance-a.html");
        assertMissing("../../doc/development/diagrams/ai-home-wireframe.svg");
        assertMissing("../../doc/development/07-ai-pattern-generation.md");
        assertMissing("../../doc/development/08-commerce-payment.md");
        assertMissing("../../doc/development/15-ai-pattern-provider-selection.md");
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }

    private static void assertMissing(String path) {
        assertFalse("Removed feature artifact must not exist: " + path, Files.exists(Path.of(path)));
    }

    private static void assertNoJavaSources(String path) throws IOException {
        Path root = Path.of(path);
        if (!Files.exists(root)) {
            return;
        }
        try (java.util.stream.Stream<Path> files = Files.walk(root)) {
            assertFalse("Removed feature source directory must not keep Java files: " + path,
                    files.anyMatch(file -> file.toString().endsWith(".java")));
        }
    }

    private static void assertDoesNotContain(String content, String forbidden) {
        assertFalse("Removed feature reference remains: " + forbidden, content.contains(forbidden));
    }

    private static void assertContains(String content, String expected) {
        assertTrue("Expected retained reference missing: " + expected, content.contains(expected));
    }
}
