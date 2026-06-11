package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
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
        String mainActivity = read("src/main/java/cn/edu/app/douyu/MainActivity.java");
        String postCreateFragment = read("src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java");
        String manifest = read("src/main/AndroidManifest.xml");

        assertTrue(mainActivity.contains("new PostCreateFragment()"));
        assertFalse(mainActivity.contains("PostCreateActivity.class"));
        assertFalse(postCreateFragment.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP"));
        assertFalse(postCreateFragment.contains("Intent.FLAG_ACTIVITY_SINGLE_TOP"));
        assertFalse(postCreateFragment.contains("openMainSection("));
        assertFalse(manifest.contains(".feature.community.PostCreateActivity"));
    }

    @Test
    public void mainUploadEntryRoutesThroughOpenTab() throws IOException {
        String mainActivity = read("src/main/java/cn/edu/app/douyu/MainActivity.java");
        String onCreateBody = methodBody(mainActivity, "protected void onCreate(Bundle savedInstanceState)");
        String openTabBody = methodBody(mainActivity, "private void openTab(int index)");

        assertTrue(onCreateBody.contains("findViewById(R.id.tab_upload).setOnClickListener(v -> openTab(2))"));
        assertFalse(onCreateBody.contains("findViewById(R.id.tab_upload).setOnClickListener(v -> {\n            AuthGate.runOrRequestLogin"));
        assertTrue(onCreateBody.contains("tabs[0].setOnClickListener(v -> openTab(0))"));
        assertTrue(onCreateBody.contains("tabs[1].setOnClickListener(v -> openTab(1))"));
        assertTrue(onCreateBody.contains("tabs[2].setOnClickListener(v -> openTab(2))"));
        assertTrue(onCreateBody.contains("tabs[3].setOnClickListener(v -> openTab(3))"));
        assertTrue(onCreateBody.contains("tabs[4].setOnClickListener(v -> openTab(4))"));

        assertTrue(openTabBody.contains("if (index == 0)"));
        assertTrue(openTabBody.contains("else if (index == 1)"));
        assertTrue(openTabBody.contains("else if (index == 2)"));
        assertTrue(openTabBody.contains("new PostCreateFragment()"));
        assertFalse(openTabBody.contains("PostCreateActivity.class"));
        assertFalse(openTabBody.contains("AuthGate.runOrRequestLogin"));
        assertTrue(openTabBody.contains("else if (index == 3)"));
        assertTrue(openTabBody.contains("new MessagesFragment()"));
        assertTrue(openTabBody.contains("new ProfileFragment()"));
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }

    private static String methodBody(String content, String signature) {
        int start = content.indexOf(signature);
        assertTrue("Missing method signature: " + signature, start >= 0);
        Matcher matcher = Pattern.compile("\\{").matcher(content);
        matcher.region(start, content.length());
        assertTrue("Missing method body: " + signature, matcher.find());
        int bodyStart = matcher.start();
        int depth = 0;
        for (int i = bodyStart; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return content.substring(bodyStart, i + 1);
                }
            }
        }
        throw new AssertionError("Unclosed method body: " + signature);
    }
}
