package cn.edu.app.douyu.network;

import cn.edu.app.douyu.model.AuthSession;
import cn.edu.app.douyu.model.ChatMessage;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.UpdateProfileRequest;
import cn.edu.app.douyu.model.UserProfile;
import retrofit2.Call;
import retrofit2.http.Body;
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

    @GET("/api/v1/products")
    Call<ApiResponse<PageResponse<Product>>> products(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/products/{productId}")
    Call<ApiResponse<Product>> product(@Path("productId") String productId);

    @GET("/api/v1/patterns/jobs")
    Call<ApiResponse<PageResponse<PatternJob>>> patternJobs(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/patterns/jobs/{jobId}")
    Call<ApiResponse<PatternJob>> patternJob(@Path("jobId") String jobId);

    @GET("/api/v1/patterns/{patternId}")
    Call<ApiResponse<PatternAsset>> pattern(@Path("patternId") String patternId);

    @GET("/api/v1/messages/notifications")
    Call<ApiResponse<PageResponse<NotificationMessage>>> notifications(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/messages/conversations")
    Call<ApiResponse<PageResponse<Conversation>>> conversations(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/messages/conversations/{conversationId}")
    Call<ApiResponse<ConversationDetail>> conversation(@Path("conversationId") String conversationId);

    @GET("/api/v1/users/me")
    Call<ApiResponse<UserProfile>> me();

    @PATCH("/api/v1/users/me")
    Call<ApiResponse<UserProfile>> updateMe(@Body UpdateProfileRequest request);

    @GET("/api/v1/users/me/liked-posts")
    Call<ApiResponse<PageResponse<Post>>> likedPosts(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/favorite-patterns")
    Call<ApiResponse<PageResponse<PatternAsset>>> favoritePatterns(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/auth/session")
    Call<ApiResponse<AuthSession>> session();
}
