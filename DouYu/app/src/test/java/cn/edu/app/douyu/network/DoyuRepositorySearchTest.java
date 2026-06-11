package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.SearchResult;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

public class DoyuRepositorySearchTest {
    @Test
    public void searchPassesKeywordAndTypeToBackendSearchEndpoint() throws Exception {
        SearchApiState state = new SearchApiState();
        DoyuRepository repository = new DoyuRepository(fakeApi(state));

        PageResponse<SearchResult> page = repository.search("草莓", "products");

        assertEquals("草莓", state.keyword);
        assertEquals("products", state.type);
        assertEquals(1, state.page);
        assertEquals(20, state.size);
        assertEquals(1, page.items.size());
        assertEquals(SearchResult.TYPE_PRODUCT, page.items.get(0).resultType);
        assertEquals("prod_1", page.items.get(0).targetId);
    }

    private static DoyuApi fakeApi(SearchApiState state) {
        return (DoyuApi) Proxy.newProxyInstance(
                DoyuApi.class.getClassLoader(),
                new Class<?>[]{DoyuApi.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "FakeSearchApi";
                    }
                    if ("search".equals(method.getName())) {
                        state.keyword = (String) args[0];
                        state.type = (String) args[1];
                        state.page = (int) args[2];
                        state.size = (int) args[3];
                        SearchResult result = new SearchResult();
                        result.resultType = SearchResult.TYPE_PRODUCT;
                        result.targetId = "prod_1";
                        result.title = "草莓套装";
                        PageResponse<SearchResult> page = new PageResponse<>();
                        page.items = List.of(result);
                        page.page = 1;
                        page.size = 20;
                        page.total = 1;
                        page.hasMore = false;
                        return new SingleResponseCall<>(Response.success(ok(page)));
                    }
                    throw new AssertionError("Unexpected API call: " + method);
                });
    }

    private static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = "OK";
        response.data = data;
        return response;
    }

    private static final class SearchApiState {
        String keyword;
        String type;
        int page;
        int size;
    }
}
