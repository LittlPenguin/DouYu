package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class MainNavigationContractTest {
    @Test
    public void mainActivityHandlesInitialAndReusedSectionNavigation() throws IOException {
        String mainActivity = read("src/main/java/cn/edu/app/douyu/MainActivity.java");

        assertTrue(mainActivity.contains("handleSectionIntent(getIntent())"));
        assertTrue(mainActivity.contains("protected void onNewIntent(Intent intent)"));
        assertTrue(mainActivity.contains("setIntent(intent)"));
        assertTrue(mainActivity.contains("handleSectionIntent(intent)"));
        assertTrue(mainActivity.contains("sectionToTabIndex("));
        assertTrue(mainActivity.contains("IntentExtras.SECTION_COMMUNITY"));
        assertTrue(mainActivity.contains("IntentExtras.SECTION_COMMERCE"));
        assertTrue(mainActivity.contains("IntentExtras.SECTION_MESSAGES"));
        assertTrue(mainActivity.contains("IntentExtras.SECTION_PROFILE"));
    }

    @Test
    public void uploadPageReturnsToExistingMainShellWithSectionExtras() throws IOException {
        String postCreateActivity = read("src/main/java/cn/edu/app/douyu/feature/community/PostCreateActivity.java");

        assertTrue(postCreateActivity.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP"));
        assertTrue(postCreateActivity.contains("Intent.FLAG_ACTIVITY_SINGLE_TOP"));
        assertTrue(postCreateActivity.contains("intent.putExtra(IntentExtras.SECTION, section)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_COMMUNITY)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_COMMERCE)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_MESSAGES)"));
        assertTrue(postCreateActivity.contains("openMainSection(IntentExtras.SECTION_PROFILE)"));
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }
}
