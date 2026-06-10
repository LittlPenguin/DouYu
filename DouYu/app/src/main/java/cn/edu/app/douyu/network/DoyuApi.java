package cn.edu.app.douyu.network;

import cn.edu.app.douyu.model.AuthSession;
import cn.edu.app.douyu.model.ChatMessage;
import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentRequest;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.PostInteraction;
import cn.edu.app.douyu.model.PostRequest;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductCategory;
import cn.edu.app.douyu.model.LoginRequest;
import cn.edu.app.douyu.model.ReadReceipt;
import cn.edu.app.douyu.model.RefreshRequest;
import cn.edu.app.douyu.model.RegisterRequest;
import cn.edu.app.douyu.model.SendMessageRequest;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.UpdateProfileRequest;
import cn.edu.app.douyu.model.UploadConfirmRequest;
import cn.edu.app.douyu.model.UploadPresignRequest;
import cn.edu.app.douyu.model.UploadPresignResponse;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.model.FollowResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DoyuApi {
    @GET("/api/v1/posts/feed")
    Call<ApiResponse<PageResponse<Post>>> feed(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/topics")
    Call<ApiResponse<PageResponse<Topic>>> topics(@Query("page") int page, @Query("size") int size);

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

    @GET("/api/v1/products")
    Call<ApiResponse<PageResponse<Product>>> products(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/products")
    Call<ApiResponse<PageResponse<Product>>> products(@Query("page") int page, @Query("size") int size, @Query("categoryId") String categoryId);

    @GET("/api/v1/product-categories")
    Call<ApiResponse<PageResponse<ProductCategory>>> productCategories();

    @GET("/api/v1/products/{productId}")
    Call<ApiResponse<Product>> product(@Path("productId") String productId);

    @GET("/api/v1/messages/notifications")
    Call<ApiResponse<PageResponse<NotificationMessage>>> notifications(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/messages/conversations")
    Call<ApiResponse<PageResponse<Conversation>>> conversations(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/messages/conversations/{conversationId}")
    Call<ApiResponse<ConversationDetail>> conversation(@Path("conversationId") String conversationId);

    @POST("/api/v1/messages/conversations/{conversationId}")
    Call<ApiResponse<ChatMessage>> sendMessage(@Path("conversationId") String conversationId, @Body SendMessageRequest request);

    @POST("/api/v1/messages/notifications/read")
    Call<ApiResponse<ReadReceipt>> markNotificationsRead();

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

    @POST("/api/v1/uploads/presign")
    Call<ApiResponse<UploadPresignResponse>> uploadPresign(@Body UploadPresignRequest request);

    @POST("/api/v1/uploads/confirm")
    Call<ApiResponse<FileAsset>> uploadConfirm(@Body UploadConfirmRequest request);

    @POST("/api/v1/auth/register")
    Call<ApiResponse<AuthSession>> register(@Body RegisterRequest request);

    @POST("/api/v1/auth/login")
    Call<ApiResponse<AuthSession>> login(@Body LoginRequest request);

    @POST("/api/v1/auth/logout")
    Call<ApiResponse<java.util.Map<String, Object>>> logout(@Body RefreshRequest request);

    @GET("/api/v1/auth/session")
    Call<ApiResponse<AuthSession>> session();
}
