package cn.edu.app.douyu.network;

import cn.edu.app.douyu.model.AuthSession;
import cn.edu.app.douyu.model.CartItemRequest;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.Comment;
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
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit 接口总表：声明 Android 访问 /api/v1 后端的全部业务 endpoint。
 */
public interface DoyuApi {
    // 社区与搜索：Feed、话题、帖子详情、评论和互动。
    @GET("/api/v1/posts/feed")
    Call<ApiResponse<PageResponse<Post>>> feed(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/topics")
    Call<ApiResponse<PageResponse<Topic>>> topics(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/search")
    Call<ApiResponse<PageResponse<SearchResult>>> search(@Query("keyword") String keyword,
                                                         @Query("type") String type,
                                                         @Query("page") int page,
                                                         @Query("size") int size);

    @GET("/api/v1/topics/{topicId}/posts")
    Call<ApiResponse<PageResponse<Post>>> topicPosts(@Path("topicId") String topicId, @Query("page") int page, @Query("size") int size);

    @GET("/api/v1/posts/{postId}")
    Call<ApiResponse<Post>> post(@Path("postId") String postId);

    @POST("/api/v1/posts")
    Call<ApiResponse<Post>> createPost(@Body PostRequest request);

    @GET("/api/v1/posts/{postId}/comments")
    Call<ApiResponse<PageResponse<Comment>>> comments(@Path("postId") String postId, @Query("page") int page, @Query("size") int size);

    @POST("/api/v1/posts/{postId}/comments")
    Call<ApiResponse<Comment>> createComment(@Path("postId") String postId, @Body CommentRequest request);

    @POST("/api/v1/posts/{postId}/like")
    Call<ApiResponse<PostInteraction>> likePost(@Path("postId") String postId);

    @DELETE("/api/v1/posts/{postId}/like")
    Call<ApiResponse<PostInteraction>> unlikePost(@Path("postId") String postId);

    @POST("/api/v1/posts/{postId}/favorite")
    Call<ApiResponse<PostInteraction>> favoritePost(@Path("postId") String postId);

    @DELETE("/api/v1/posts/{postId}/favorite")
    Call<ApiResponse<PostInteraction>> unfavoritePost(@Path("postId") String postId);

    // 商城与订单：商品、分类、购物车和下单接口。
    @GET("/api/v1/products")
    Call<ApiResponse<PageResponse<Product>>> products(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/products")
    Call<ApiResponse<PageResponse<Product>>> products(@Query("page") int page, @Query("size") int size, @Query("categoryId") String categoryId);

    @GET("/api/v1/product-categories")
    Call<ApiResponse<PageResponse<ProductCategory>>> productCategories();

    @GET("/api/v1/products/{productId}")
    Call<ApiResponse<Product>> product(@Path("productId") String productId);

    @GET("/api/v1/cart")
    Call<ApiResponse<CartResponse>> cart();

    @POST("/api/v1/cart/items")
    Call<ApiResponse<CartResponse.Item>> addCartItem(@Body CartItemRequest request);

    @PATCH("/api/v1/cart/items/{itemId}")
    Call<ApiResponse<CartResponse.Item>> updateCartItem(@Path("itemId") String itemId, @Body UpdateCartRequest request);

    @DELETE("/api/v1/cart/items/{itemId}")
    Call<ApiResponse<java.util.Map<String, Object>>> deleteCartItem(@Path("itemId") String itemId);

    @POST("/api/v1/orders")
    Call<ApiResponse<Order>> createOrder(@Header("Idempotency-Key") String idempotencyKey, @Body CreateOrderRequest request);

    // 消息通知：拉取通知列表并标记已读。
    @GET("/api/v1/notifications")
    Call<ApiResponse<PageResponse<NotificationMessage>>> notifications(@Query("page") int page, @Query("size") int size);

    @POST("/api/v1/notifications/read")
    Call<ApiResponse<ReadReceipt>> markNotificationsRead();

    // 用户/Profile：当前用户、设置、关注关系和个人作品列表。
    @GET("/api/v1/users/me")
    Call<ApiResponse<UserProfile>> me();

    @GET("/api/v1/users/search")
    Call<ApiResponse<PageResponse<UserProfile>>> searchUsers(@Query("keyword") String keyword, @Query("page") int page, @Query("size") int size);

    @POST("/api/v1/users/{userId}/follow")
    Call<ApiResponse<FollowResult>> followUser(@Path("userId") String userId);

    @DELETE("/api/v1/users/{userId}/follow")
    Call<ApiResponse<FollowResult>> unfollowUser(@Path("userId") String userId);

    @PATCH("/api/v1/users/me")
    Call<ApiResponse<UserProfile>> updateMe(@Body UpdateProfileRequest request);

    @GET("/api/v1/users/me/settings")
    Call<ApiResponse<UserSettings>> userSettings();

    @PATCH("/api/v1/users/me/settings")
    Call<ApiResponse<UserSettings>> updateUserSettings(@Body UpdateUserSettingsRequest request);

    @GET("/api/v1/users/me/posts")
    Call<ApiResponse<PageResponse<Post>>> myPosts(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/following")
    Call<ApiResponse<PageResponse<UserProfile>>> followingUsers(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/followers")
    Call<ApiResponse<PageResponse<UserProfile>>> followerUsers(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/liked-posts")
    Call<ApiResponse<PageResponse<Post>>> likedPosts(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/favorite-posts")
    Call<ApiResponse<PageResponse<Post>>> favoritePosts(@Query("page") int page, @Query("size") int size);

    // 上传：先 presign 拿临时上传地址，再 confirm 生成可绑定业务的 FileAsset。
    @POST("/api/v1/uploads/presign")
    Call<ApiResponse<UploadPresignResponse>> uploadPresign(@Body UploadPresignRequest request);

    @POST("/api/v1/uploads/confirm")
    Call<ApiResponse<FileAsset>> uploadConfirm(@Body UploadConfirmRequest request);

    // 认证：注册和登录返回 AuthSession，token 后续由 OkHttp 拦截器带上。
    @POST("/api/v1/auth/register")
    Call<ApiResponse<AuthSession>> register(@Body RegisterRequest request);

    @POST("/api/v1/auth/login")
    Call<ApiResponse<AuthSession>> login(@Body LoginRequest request);
}
