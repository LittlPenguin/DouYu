package cn.edu.app.douyu.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchContractTest {
    @Test
    public void searchActivityAndLayoutUseRealBackendSearch() throws IOException {
        String activity = read("src/main/java/cn/edu/app/douyu/feature/community/SearchActivity.java");
        String layout = read("src/main/res/layout/activity_search.xml");
        String api = read("src/main/java/cn/edu/app/douyu/network/DoyuApi.java");
        String repository = read("src/main/java/cn/edu/app/douyu/data/DoyuRepository.java");
        String surface = activity + layout;

        assertTrue(activity.contains("repository.search("));
        assertTrue(activity.contains("SearchResult.TYPE_POST"));
        assertTrue(activity.contains("PostDetailActivity"));
        assertTrue(activity.contains("ProductDetailActivity"));
        assertTrue(activity.contains("SearchResultAdapter"));
        assertTrue(layout.contains("@+id/search_input"));
        assertTrue(layout.contains("@+id/search_clear"));
        assertTrue(layout.contains("@+id/search_cancel"));
        assertTrue(layout.contains("@+id/search_results"));
        assertTrue(layout.contains("@+id/search_retry"));
        assertTrue(layout.contains("@+id/search_tab_all"));
        assertTrue(layout.contains("@+id/search_tab_posts"));
        assertTrue(layout.contains("@+id/search_tab_products"));
        assertTrue(layout.contains("@+id/search_tab_users"));
        assertTrue(layout.contains("@+id/search_tab_topics"));
        assertTrue(api.contains("@GET(\"/api/v1/search\")"));
        assertTrue(repository.contains("search(String keyword, String type)"));

        assertFalse(surface.contains("UI-only"));
        assertFalse(surface.contains("待接入"));
        assertFalse(surface.contains("开发态"));
        assertFalse(surface.contains("等待搜索接口"));
        assertFalse(surface.contains("本地假结果"));
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }
}
