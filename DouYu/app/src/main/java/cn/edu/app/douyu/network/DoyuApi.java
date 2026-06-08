package cn.edu.app.douyu.network;

import cn.edu.app.douyu.model.AuthSession;
import cn.edu.app.douyu.model.ChatMessage;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PatternAsset;
import cn.edu.app.douyu.model.PatternJob;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ReadReceipt;
import cn.edu.app.douyu.model.SendMessageRequest;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.UpdateProfileRequest;
import cn.edu.app.douyu.model.UploadConfirmRequest;
import cn.edu.app.douyu.model.UploadPresignRequest;
import cn.edu.app.douyu.model.UploadPresignResponse;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.model.AiQuota;
import cn.edu.app.douyu.model.ConfirmUploadRequest;
import cn.edu.app.douyu.model.CreatePatternJobRequest;
import cn.edu.app.douyu.model.FavoriteResult;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.PresignUploadRequest;
import cn.edu.app.douyu.model.PresignUploadResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

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

    @POST("/api/v1/uploads/presign")
    Call<ApiResponse<PresignUploadResponse>> uploadPresign(@Body PresignUploadRequest request);

    @POST("/api/v1/uploads/confirm")
    Call<ApiResponse<FileAsset>> uploadConfirm(@Body ConfirmUploadRequest request);

    @POST("/api/v1/patterns/jobs")
    Call<ApiResponse<PatternJob>> createPatternJob(@Body CreatePatternJobRequest request);

    @POST("/api/v1/patterns/jobs/{jobId}/cancel")
    Call<ApiResponse<PatternJob>> cancelPatternJob(@Path("jobId") String jobId);

    @POST("/api/v1/patterns/{patternId}/favorite")
    Call<ApiResponse<FavoriteResult>> favoritePattern(@Path("patternId") String patternId);

    @GET("/api/v1/patterns/quota")
    Call<ApiResponse<AiQuota>> aiQuota();

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

    @PATCH("/api/v1/users/me")
    Call<ApiResponse<UserProfile>> updateMe(@Body UpdateProfileRequest request);

    @GET("/api/v1/users/me/liked-posts")
    Call<ApiResponse<PageResponse<Post>>> likedPosts(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/favorite-posts")
    Call<ApiResponse<PageResponse<Post>>> favoritePosts(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/me/favorite-patterns")
    Call<ApiResponse<PageResponse<PatternAsset>>> favoritePatterns(@Query("page") int page, @Query("size") int size);

    @POST("/api/v1/uploads/presign")
    Call<ApiResponse<UploadPresignResponse>> uploadPresign(@Body UploadPresignRequest request);

    @PUT
    Call<ResponseBody> uploadPut(@Url String url, @Body RequestBody body);

    @POST("/api/v1/uploads/confirm")
    Call<ApiResponse<FileAsset>> uploadConfirm(@Body UploadConfirmRequest request);

    @GET("/api/v1/auth/session")
    Call<ApiResponse<AuthSession>> session();
}
