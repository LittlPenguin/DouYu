package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import cn.edu.app.douyu.data.DoyuRepository;
import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentMediaAsset;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

public class DoyuRepositoryImageUrlNormalizationTest {
    private static final String API_BASE_URL = "http://10.64.241.153:8081/";

    @Test
    public void feedRewritesLoopbackPostImageUrlsToApiHost() throws Exception {
        Post post = new Post();
        post.coverImageUrl = "http://localhost:8081/uploads/assets/post_image/file_1/post.png";
        post.imageUrls = List.of(
                "http://127.0.0.1:8081/uploads/assets/post_image/file_1/post.png",
                "https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/assets/post_image/file_2/post.png"
        );
        DoyuRepository repository = new DoyuRepository(fakeApi("feed", page(post)), null, API_BASE_URL);

        PageResponse<Post> response = repository.feed();

        assertEquals("http://10.64.241.153:8081/uploads/assets/post_image/file_1/post.png",
                response.items.get(0).coverImageUrl);
        assertEquals(List.of(
                        "http://10.64.241.153:8081/uploads/assets/post_image/file_1/post.png",
                        "https://is-wulong-budget.oss-cn-guangzhou.aliyuncs.com/assets/post_image/file_2/post.png"
                ),
                response.items.get(0).imageUrls);
    }

    @Test
    public void postDetailRewritesLoopbackGalleryUrlsToApiHost() throws Exception {
        Post post = new Post();
        post.coverImageUrl = "http://[::1]:8081/uploads/assets/post_image/file_1/post.png";
        post.imageUrls = List.of("http://localhost:8081/uploads/assets/post_image/file_1/post.png");
        DoyuRepository repository = new DoyuRepository(fakeApi("post", post), null, API_BASE_URL);

        Post response = repository.post("post_1");

        assertEquals("http://10.64.241.153:8081/uploads/assets/post_image/file_1/post.png",
                response.coverImageUrl);
        assertEquals(List.of("http://10.64.241.153:8081/uploads/assets/post_image/file_1/post.png"),
                response.imageUrls);
    }

    @Test
    public void commentsRewriteLoopbackMediaAssetUrlsToApiHost() throws Exception {
        CommentMediaAsset asset = new CommentMediaAsset();
        asset.publicUrl = "http://localhost:8081/uploads/assets/post_image/file_1/comment.png";
        Comment comment = new Comment();
        comment.mediaAssets = List.of(asset);
        DoyuRepository repository = new DoyuRepository(fakeApi("comments", page(comment)), null, API_BASE_URL);

        PageResponse<Comment> response = repository.comments("post_1");

        assertEquals("http://10.64.241.153:8081/uploads/assets/post_image/file_1/comment.png",
                response.items.get(0).mediaAssets.get(0).publicUrl);
    }

    private static DoyuApi fakeApi(String expectedMethodName, Object value) {
        return (DoyuApi) Proxy.newProxyInstance(
                DoyuApi.class.getClassLoader(),
                new Class<?>[]{DoyuApi.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "FakeImageUrlApi";
                    }
                    if (expectedMethodName.equals(method.getName())) {
                        return new SingleResponseCall<>(Response.success(ok(value)));
                    }
                    throw new AssertionError("Unexpected API call: " + method);
                });
    }

    private static <T> PageResponse<T> page(T item) {
        PageResponse<T> page = new PageResponse<>();
        page.items = List.of(item);
        page.page = 1;
        page.size = 20;
        page.total = 1;
        page.hasMore = false;
        return page;
    }

    @SuppressWarnings("unchecked")
    private static <T> ApiResponse<T> ok(Object data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = "OK";
        response.data = (T) data;
        return response;
    }
}
