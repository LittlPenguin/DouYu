package cn.edu.app.douyu.data;

import java.io.IOException;

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
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.UpdateProfileRequest;
import cn.edu.app.douyu.model.UploadConfirmRequest;
import cn.edu.app.douyu.model.UploadPresignRequest;
import cn.edu.app.douyu.model.UploadPresignResponse;
import cn.edu.app.douyu.model.UserProfile;
import cn.edu.app.douyu.network.ApiException;
import cn.edu.app.douyu.network.ApiResponse;
import cn.edu.app.douyu.network.DoyuApi;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DoyuRepository {
    private static final int FIRST_PAGE = 1;
    private static final int PAGE_SIZE = 20;
    private final DoyuApi api;

    public DoyuRepository(DoyuApi api) {
        this.api = api;
    }

    public PageResponse<Post> feed() throws IOException {
        return body(api.feed(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<Topic> topics() throws IOException {
        return body(api.topics(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<Post> topicPosts(String topicId) throws IOException {
        return body(api.topicPosts(topicId, FIRST_PAGE, PAGE_SIZE));
    }

    public Post post(String postId) throws IOException {
        return body(api.post(postId));
    }

    public PageResponse<Product> products() throws IOException {
        return body(api.products(FIRST_PAGE, PAGE_SIZE));
    }

    public Product product(String productId) throws IOException {
        return body(api.product(productId));
    }

    public PageResponse<PatternJob> patternJobs() throws IOException {
        return body(api.patternJobs(FIRST_PAGE, PAGE_SIZE));
    }

    public PatternJob patternJob(String jobId) throws IOException {
        return body(api.patternJob(jobId));
    }

    public PatternAsset pattern(String patternId) throws IOException {
        return body(api.pattern(patternId));
    }

    public PageResponse<NotificationMessage> notifications() throws IOException {
        return body(api.notifications(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<Conversation> conversations() throws IOException {
        return body(api.conversations(FIRST_PAGE, PAGE_SIZE));
    }

    public ConversationDetail conversation(String conversationId) throws IOException {
        return body(api.conversation(conversationId));
    }

    public UserProfile me() throws IOException {
        return body(api.me());
    }

    public UserProfile updateMe(String nickname, String bio) throws IOException {
        return updateMe(nickname, bio, null);
    }

    public UserProfile updateMe(String nickname, String bio, String avatarFileId) throws IOException {
        return body(api.updateMe(new UpdateProfileRequest(nickname, avatarFileId, bio)));
    }

    /**
     * Uploads an avatar image through the real presign -> PUT -> confirm flow and
     * returns the resulting fileId for use in {@link #updateMe(String, String, String)}.
     */
    public String uploadAvatar(byte[] bytes, String mimeType, String fileName, Integer width, Integer height) throws IOException {
        UploadPresignResponse presign = body(api.uploadPresign(
                new UploadPresignRequest("AVATAR", mimeType, bytes.length, fileName)));
        if (presign == null || presign.uploadUrl == null || presign.fileKey == null) {
            throw new ApiException("上传地址无效");
        }
        RequestBody putBody = RequestBody.create(bytes, MediaType.parse(mimeType));
        Response<ResponseBody> putResponse = api.uploadPut(presign.uploadUrl, putBody).execute();
        try {
            if (!putResponse.isSuccessful()) {
                throw new ApiException(putResponse.code(), "头像上传失败 HTTP " + putResponse.code());
            }
        } finally {
            if (putResponse.body() != null) {
                putResponse.body().close();
            }
        }
        FileAsset asset = body(api.uploadConfirm(
                new UploadConfirmRequest(presign.fileKey, "AVATAR", mimeType, bytes.length, width, height)));
        if (asset == null || asset.fileId == null) {
            throw new ApiException("头像确认失败");
        }
        return asset.fileId;
    }

    public PageResponse<Post> likedPosts() throws IOException {
        return body(api.likedPosts(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<Post> favoritePosts() throws IOException {
        return body(api.favoritePosts(FIRST_PAGE, PAGE_SIZE));
    }

    public PageResponse<PatternAsset> favoritePatterns() throws IOException {
        return body(api.favoritePatterns(FIRST_PAGE, PAGE_SIZE));
    }

    public AuthSession session() throws IOException {
        return body(api.session());
    }

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
