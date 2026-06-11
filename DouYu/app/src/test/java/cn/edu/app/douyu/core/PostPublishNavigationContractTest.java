package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class PostPublishNavigationContractTest {
    @Test
    public void publishSuccessClearsPostDraftAndOpensDetailReturningToCommunity() throws IOException {
        String postCreateFragment = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostCreateFragment.java");

        assertTrue(postCreateFragment.contains("resetPostDraftAfterPublish()"));
        assertTrue(postCreateFragment.contains("titleInput.setText(\"\")"));
        assertTrue(postCreateFragment.contains("bodyInput.setText(\"\")"));
        assertTrue(postCreateFragment.contains("pendingMedia.clear()"));
        assertTrue(postCreateFragment.contains("selectedTopics.clear()"));
        assertTrue(postCreateFragment.contains("((Chip) child).setChecked(false)"));
        assertTrue(postCreateFragment.contains("intent.putExtra(IntentExtras.RETURN_TO, IntentExtras.SECTION_COMMUNITY)"));
    }

    @Test
    public void postDetailBackUsesCommunityReturnTargetOnlyWhenRequested() throws IOException {
        String postDetailActivity = readUtf8("src/main/java/cn/edu/app/douyu/feature/community/PostDetailActivity.java");

        assertTrue(postDetailActivity.contains("returnToCommunityIfRequested()"));
        assertTrue(postDetailActivity.contains("IntentExtras.SECTION_COMMUNITY.equals(extra(IntentExtras.RETURN_TO))"));
        assertTrue(postDetailActivity.contains("new Intent(this, MainActivity.class)"));
        assertTrue(postDetailActivity.contains("intent.putExtra(IntentExtras.SECTION, IntentExtras.SECTION_COMMUNITY)"));
        assertTrue(postDetailActivity.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP"));
        assertTrue(postDetailActivity.contains("startActivity(intent)"));
        assertTrue(postDetailActivity.contains("finish()"));
    }

    private static String readUtf8(String relativePath) throws IOException {
        return new String(Files.readAllBytes(Path.of(relativePath)), StandardCharsets.UTF_8);
    }
}
