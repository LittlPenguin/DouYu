package cn.edu.app.douyu.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cn.edu.app.douyu.model.AddressSnapshot;
import cn.edu.app.douyu.model.AuthSession;
import cn.edu.app.douyu.model.CartItemRequest;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentMediaAsset;
import cn.edu.app.douyu.model.CommentRequest;
import cn.edu.app.douyu.model.CreateOrderRequest;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.Order;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.PostInteraction;
import cn.edu.app.douyu.model.PostRequest;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductCategory;
import cn.edu.app.douyu.model.LoginRequest;
import cn.edu.app.douyu.model.ReadReceipt;
import cn.edu.app.douyu.model.RegisterRequest;
import cn.edu.app.douyu.model.SearchResult;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.UpdateCartRequest;
import cn.edu.app.douyu.model.UpdateProfileRequest;
import cn.edu.app.douyu.model.UpdateUserSettingsRequest;
import cn.edu.app.douyu.model.UploadConfirmRequest;
import cn.edu.app.douyu.model.UploadPresignRequest;
import cn.edu.app.douyu.model.UploadPresignResponse;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.model.UserSettings;
import cn.edu.app.douyu.model.FollowResult;
import cn.edu.app.douyu.network.ApiException;
import cn.edu.app.douyu.network.ApiResponse;
import cn.edu.app.douyu.network.DoyuApi;
import okhttp3.MediaType;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Android 数据仓库：封装真实后端 API、上传流程、图片 URL 修正和统一错误处理。
 */
public class DoyuRepository {
    // 列表接口默认读取第一页，每页 20 条，页面暂不直接暴露分页参数。
    private static final int FIRST_PAGE = 1;
    private static final int PAGE_SIZE = 20;
    // Retrofit 业务接口、独立上传客户端和 API 地址共同支撑真实后端数据链路。
    private final DoyuApi api;
    private final OkHttpClient uploadClient;
    private final HttpUrl apiBaseUrl;

    public DoyuRepository(DoyuApi api) {
        this(api, new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build());
    }

    public DoyuRepository(DoyuApi api, OkHttpClient uploadClient) {
        this(api, uploadClient, null);
    }

    public DoyuRepository(DoyuApi api, OkHttpClient uploadClient, String apiBaseUrl) {
        this.api = api;
        this.uploadClient = uploadClient;
        // apiBaseUrl 用于把后端返回的 localhost 图片地址改写成设备可访问的主机。
        this.apiBaseUrl = apiBaseUrl == null || apiBaseUrl.trim().isEmpty()
                ? null
                : HttpUrl.parse(apiBaseUrl);
    }

    // 社区与搜索：拉取真实 Feed、话题、帖子详情、评论和互动状态。
    public PageResponse<Post> feed() throws IOException {
        return normalizePostPage(body(api.feed(FIRST_PAGE, PAGE_SIZE)));
    }

    public PageResponse<Topic> topics() throws IOException {
        return body(api.topics(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<SearchResult> search(String keyword, String type) throws IOException {
        String safeKeyword = keyword == null ? "" : keyword;
        String safeType = type == null || type.trim().isEmpty() ? "all" : type;
        return normalizeSearchPage(body(api.search(safeKeyword, safeType, FIRST_PAGE, PAGE_SIZE)));
    }

    public PageResponse<Post> topicPosts(String topicId) throws IOException {
        return normalizePostPage(body(api.topicPosts(topicId, FIRST_PAGE, PAGE_SIZE)));
    }

    public Post post(String postId) throws IOException {
        return normalizePost(body(api.post(postId)));
    }

    public Post createPost(String title, String content, java.util.List<String> mediaFileIds, java.util.List<String> topicIds) throws IOException {
        return normalizePost(body(api.createPost(new PostRequest(title, content, mediaFileIds, topicIds))));
    }

    public PageResponse<Comment> comments(String postId) throws IOException {
        return normalizeCommentPage(body(api.comments(postId, FIRST_PAGE, PAGE_SIZE)));
    }

    public Comment createComment(String postId, CommentRequest request) throws IOException {
        return normalizeComment(body(api.createComment(postId, request)));
    }

    public PostInteraction likePost(String postId) throws IOException {
        return body(api.likePost(postId));
    }

    public PostInteraction unlikePost(String postId) throws IOException {
        return body(api.unlikePost(postId));
    }

    public PostInteraction favoritePost(String postId) throws IOException {
        return body(api.favoritePost(postId));
    }

    public PostInteraction unfavoritePost(String postId) throws IOException {
        return body(api.unfavoritePost(postId));
    }

    // 商城与订单：商品、分类、购物车和订单都通过后端接口完成。
    public PageResponse<Product> products() throws IOException {
        return body(api.products(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<Product> products(String categoryId) throws IOException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return products();
        }
        return body(api.products(FIRST_PAGE, PAGE_SIZE, categoryId));
    }

    public PageResponse<ProductCategory> productCategories() throws IOException {
        return body(api.productCategories());
    }

    public Product product(String productId) throws IOException {
        return body(api.product(productId));
    }

    public CartResponse cart() throws IOException {
        return body(api.cart());
    }

    public CartResponse.Item addCartItem(String skuId, int quantity) throws IOException {
        return body(api.addCartItem(new CartItemRequest(skuId, quantity)));
    }

    public CartResponse.Item updateCartItem(String itemId, int quantity) throws IOException {
        return body(api.updateCartItem(itemId, new UpdateCartRequest(quantity)));
    }

    public void deleteCartItem(String itemId) throws IOException {
        body(api.deleteCartItem(itemId));
    }

    public Order createImmediateOrder(String skuId, int quantity, AddressSnapshot addressSnapshot, String remark) throws IOException {
        return body(api.createOrder(
                orderIdempotencyKey(),
                CreateOrderRequest.immediate(skuId, quantity, addressSnapshot, remark)));
    }

    public Order createCartOrder(List<String> itemIds, AddressSnapshot addressSnapshot, String remark) throws IOException {
        return body(api.createOrder(
                orderIdempotencyKey(),
                CreateOrderRequest.fromCart(itemIds, addressSnapshot, remark)));
    }

    // presign 后的上传地址需要直接 PUT 文件 bytes，不能走普通 JSON Retrofit 请求。
    public void uploadPresignedBytes(String uploadUrl, byte[] bytes, String mimeType, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(resolveLoopbackUrlToApiHost(uploadUrl))
                .put(RequestBody.create(MediaType.parse(mimeType), bytes));
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey() != null && header.getValue() != null) {
                    builder.header(header.getKey(), header.getValue());
                }
            }
        }
        try (okhttp3.Response response = uploadClient.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new ApiException(response.code(), "上传失败：" + response.code());
            }
        }
    }

    // 将 localhost/127.0.0.1 上传或图片地址改写为 API host，便于模拟器/真机访问。
    private String resolveLoopbackUrlToApiHost(String url) {
        HttpUrl parsedUrl = HttpUrl.parse(url);
        if (parsedUrl == null || apiBaseUrl == null) {
            return url;
        }
        if (!isLoopbackHost(parsedUrl.host()) || isLoopbackHost(apiBaseUrl.host())) {
            return url;
        }
        return parsedUrl.newBuilder()
                .scheme(apiBaseUrl.scheme())
                .host(apiBaseUrl.host())
                .port(apiBaseUrl.port())
                .build()
                .toString();
    }

    private static boolean isLoopbackHost(String host) {
        if (host == null) {
            return false;
        }
        String normalized = host.trim().toLowerCase(java.util.Locale.ROOT);
        return "localhost".equals(normalized)
                || "127.0.0.1".equals(normalized)
                || "::1".equals(normalized)
                || "0:0:0:0:0:0:0:1".equals(normalized);
    }

    // 消息通知：读取通知并把未读通知标记为已读。
    public PageResponse<NotificationMessage> notifications() throws IOException {
        return body(api.notifications(FIRST_PAGE, PAGE_SIZE));
    }

    public ReadReceipt markNotificationsRead() throws IOException {
        return body(api.markNotificationsRead());
    }

    // Profile 与设置：当前用户资料、头像、关注关系、个人作品和偏好设置。
    public UserProfile me() throws IOException {
        return normalizeProfile(body(api.me()));
    }

    public UserProfile updateMe(String nickname, String bio) throws IOException {
        return updateMe(nickname, bio, null);
    }

    public UserProfile updateMe(String nickname, String bio, String avatarFileId) throws IOException {
        return normalizeProfile(body(api.updateMe(new UpdateProfileRequest(nickname, avatarFileId, bio))));
    }

    public UserProfile updateMe(String nickname, String bio, String avatarFileId, String region) throws IOException {
        return normalizeProfile(body(api.updateMe(new UpdateProfileRequest(nickname, avatarFileId, bio, region))));
    }

    public UserSettings userSettings() throws IOException {
        return body(api.userSettings());
    }

    public UserSettings updateUserSettings(UpdateUserSettingsRequest request) throws IOException {
        return body(api.updateUserSettings(request));
    }

    public PageResponse<Post> myPosts() throws IOException {
        return normalizePostPage(body(api.myPosts(FIRST_PAGE, PAGE_SIZE)));
    }

    public PageResponse<UserProfile> followingUsers() throws IOException {
        return normalizeProfilePage(body(api.followingUsers(FIRST_PAGE, PAGE_SIZE)));
    }

    public PageResponse<UserProfile> followerUsers() throws IOException {
        return normalizeProfilePage(body(api.followerUsers(FIRST_PAGE, PAGE_SIZE)));
    }

    /**
     * Uploads an avatar image through the real presign -> PUT -> confirm flow and
     * returns the resulting fileId for use in {@link #updateMe(String, String, String)}.
     */
    public String uploadAvatar(byte[] bytes, String mimeType, String fileName, Integer width, Integer height) throws IOException {
        return uploadImage("AVATAR", bytes, mimeType, fileName, width, height);
    }

    /**
     * Uploads a comment image through the real presign -> PUT -> confirm flow with
     * usage POST_IMAGE and returns the resulting fileId for comment mediaFileIds.
     */
    public String uploadPostImage(byte[] bytes, String mimeType, String fileName, Integer width, Integer height) throws IOException {
        return uploadImage("POST_IMAGE", bytes, mimeType, fileName, width, height);
    }

    public FileAsset uploadPostImageAsset(byte[] bytes, String mimeType, String fileName, Integer width, Integer height) throws IOException {
        return uploadImageAsset("POST_IMAGE", bytes, mimeType, fileName, width, height);
    }

    // 返回 fileId 给发帖、评论或资料更新接口绑定图片资产。
    private String uploadImage(String usage, byte[] bytes, String mimeType, String fileName, Integer width, Integer height) throws IOException {
        return uploadImageAsset(usage, bytes, mimeType, fileName, width, height).fileId;
    }

    // 完整上传链路：presign 获取上传地址 -> PUT 文件 bytes -> confirm 落 FileAsset。
    private FileAsset uploadImageAsset(String usage, byte[] bytes, String mimeType, String fileName, Integer width, Integer height) throws IOException {
        UploadPresignResponse presign = body(api.uploadPresign(
                new UploadPresignRequest(usage, mimeType, bytes.length, fileName)));
        if (presign == null || presign.uploadUrl == null || presign.fileKey == null) {
            throw new ApiException("上传地址无效");
        }
        uploadPresignedBytes(presign.uploadUrl, bytes, mimeType, presign.headers);
        FileAsset asset = body(api.uploadConfirm(
                new UploadConfirmRequest(presign.fileKey, usage, mimeType, bytes.length, width, height)));
        if (asset == null || asset.fileId == null) {
            throw new ApiException("图片确认失败");
        }
        asset.publicUrl = normalizeImageUrl(asset.publicUrl);
        return asset;
    }

    // 归一化帖子列表里的封面和多图地址，保证 Glide 能加载真实后端图片。
    private PageResponse<Post> normalizePostPage(PageResponse<Post> page) {
        if (page == null || page.items == null) {
            return page;
        }
        for (Post post : page.items) {
            normalizePost(post);
        }
        return page;
    }

    // 单条帖子同样需要修正封面和图库 URL。
    private Post normalizePost(Post post) {
        if (post == null) {
            return null;
        }
        post.coverImageUrl = normalizeImageUrl(post.coverImageUrl);
        if (post.imageUrls != null) {
            List<String> normalized = new ArrayList<>(post.imageUrls.size());
            for (String url : post.imageUrls) {
                normalized.add(normalizeImageUrl(url));
            }
            post.imageUrls = normalized;
        }
        return post;
    }

    // 评论图片也来自后端 FileAsset，需要修正 publicUrl。
    private PageResponse<Comment> normalizeCommentPage(PageResponse<Comment> page) {
        if (page == null || page.items == null) {
            return page;
        }
        for (Comment comment : page.items) {
            normalizeComment(comment);
        }
        return page;
    }

    private Comment normalizeComment(Comment comment) {
        if (comment == null || comment.mediaAssets == null) {
            return comment;
        }
        for (CommentMediaAsset asset : comment.mediaAssets) {
            if (asset != null) {
                asset.publicUrl = normalizeImageUrl(asset.publicUrl);
            }
        }
        return comment;
    }

    // 用户头像 URL 在 Profile、关注列表和搜索结果中统一修正。
    private PageResponse<UserProfile> normalizeProfilePage(PageResponse<UserProfile> page) {
        if (page == null || page.items == null) {
            return page;
        }
        for (UserProfile profile : page.items) {
            normalizeProfile(profile);
        }
        return page;
    }

    // 搜索结果可能返回帖子、商品或用户图片，这里统一修正预览图地址。
    private PageResponse<SearchResult> normalizeSearchPage(PageResponse<SearchResult> page) {
        if (page == null || page.items == null) {
            return page;
        }
        for (SearchResult result : page.items) {
            if (result != null) {
                result.imageUrl = normalizeImageUrl(result.imageUrl);
            }
        }
        return page;
    }

    private UserProfile normalizeProfile(UserProfile profile) {
        if (profile != null) {
            profile.avatarUrl = normalizeImageUrl(profile.avatarUrl);
        }
        return profile;
    }

    private String normalizeImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        return resolveLoopbackUrlToApiHost(url.trim());
    }

    // 下单使用幂等 key，避免用户重复点击导致后端创建重复订单。
    private static String orderIdempotencyKey() {
        return "android-order-" + UUID.randomUUID().toString().replace("-", "");
    }

    // 用户关系与个人内容：搜索用户、关注/取关、点赞和收藏列表。
    public PageResponse<UserProfile> searchUsers(String keyword) throws IOException {
        return normalizeProfilePage(body(api.searchUsers(keyword == null ? "" : keyword, FIRST_PAGE, PAGE_SIZE)));
    }

    public FollowResult followUser(String userId) throws IOException {
        return body(api.followUser(userId));
    }

    public FollowResult unfollowUser(String userId) throws IOException {
        return body(api.unfollowUser(userId));
    }

    public PageResponse<Post> likedPosts() throws IOException {
        return normalizePostPage(body(api.likedPosts(FIRST_PAGE, PAGE_SIZE)));
    }

    public PageResponse<Post> favoritePosts() throws IOException {
        return normalizePostPage(body(api.favoritePosts(FIRST_PAGE, PAGE_SIZE)));
    }

    // 认证：注册和登录返回会话信息，由调用方保存 token。
    public AuthSession login(String email, String password) throws IOException {
        return body(api.login(new LoginRequest(email, password)));
    }

    public AuthSession register(String email, String password, String confirmPassword, String nickname, String ageGroup) throws IOException {
        return body(api.register(new RegisterRequest(email, password, confirmPassword, nickname, ageGroup)));
    }

    // 统一解析 ApiResponse：处理 HTTP 错误、空响应和后端业务错误，只把 data 返回给页面。
    public static <T> T body(Call<ApiResponse<T>> call) throws IOException {
        Response<ApiResponse<T>> response = call.execute();
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), "HTTP " + response.code());
        }
        ApiResponse<T> apiResponse = response.body();
        if (apiResponse == null) {
            throw new ApiException("empty response");
        }
        if (!apiResponse.success()) {
            throw new ApiException(apiResponse.message == null ? apiResponse.code : apiResponse.message);
        }
        return apiResponse.data;
    }
}
