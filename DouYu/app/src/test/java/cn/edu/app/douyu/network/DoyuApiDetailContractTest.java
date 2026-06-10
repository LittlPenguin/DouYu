package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentRequest;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PostInteraction;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.PostRequest;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductCategory;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.ConfirmUploadRequest;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.PresignUploadRequest;
import cn.edu.app.douyu.model.PresignUploadResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DoyuApiDetailContractTest {
    @Test
    public void conversationDetailUsesBackendDetailEnvelope() throws Exception {
        Method method = DoyuApi.class.getMethod("conversation", String.class);

        assertEquals("/api/v1/messages/conversations/{conversationId}", method.getAnnotation(GET.class).value());
        assertCallDataType(method, ConversationDetail.class);
    }

    @Test
    public void uploadEndpointsUseExistingBackendEndpoints() throws Exception {
        Method presign = DoyuApi.class.getMethod("uploadPresign", PresignUploadRequest.class);
        Method confirm = DoyuApi.class.getMethod("uploadConfirm", ConfirmUploadRequest.class);

        assertEquals("/api/v1/uploads/presign", presign.getAnnotation(POST.class).value());
        assertCallDataType(presign, PresignUploadResponse.class);
        assertEquals("/api/v1/uploads/confirm", confirm.getAnnotation(POST.class).value());
        assertCallDataType(confirm, FileAsset.class);
    }

    @Test
    public void communityTopicsAndTopicPostsUsePublicBackendEndpoints() throws Exception {
        Method topics = DoyuApi.class.getMethod("topics", int.class, int.class);
        Method topicPosts = DoyuApi.class.getMethod("topicPosts", String.class, int.class, int.class);

        assertEquals("/api/v1/topics", topics.getAnnotation(GET.class).value());
        assertPageItemType(topics, Topic.class);
        assertEquals("/api/v1/topics/{topicId}/posts", topicPosts.getAnnotation(GET.class).value());
        assertPageItemType(topicPosts, Post.class);
    }

    @Test
    public void commerceCategoriesAndFilteredProductsUseBackendEndpoints() throws Exception {
        Method categories = DoyuApi.class.getMethod("productCategories");
        Method products = DoyuApi.class.getMethod("products", int.class, int.class, String.class);

        assertEquals("/api/v1/product-categories", categories.getAnnotation(GET.class).value());
        assertPageItemType(categories, ProductCategory.class);
        assertEquals("/api/v1/products", products.getAnnotation(GET.class).value());
        assertPageItemType(products, Product.class);
        assertEquals("categoryId", products.getParameters()[2].getAnnotation(Query.class).value());
    }

    @Test
    public void postCommentsUseBackendCommentEndpoints() throws Exception {
        Method comments = DoyuApi.class.getMethod("comments", String.class, int.class, int.class);
        Method createComment = DoyuApi.class.getMethod("createComment", String.class, CommentRequest.class);

        assertEquals("/api/v1/posts/{postId}/comments", comments.getAnnotation(GET.class).value());
        assertPageItemType(comments, Comment.class);
        assertEquals("/api/v1/posts/{postId}/comments", createComment.getAnnotation(POST.class).value());
        assertCallDataType(createComment, Comment.class);
    }

    @Test
    public void createPostUsesBackendReviewingPostEndpoint() throws Exception {
        Method createPost = DoyuApi.class.getMethod("createPost", PostRequest.class);

        assertEquals("/api/v1/posts", createPost.getAnnotation(POST.class).value());
        assertCallDataType(createPost, Post.class);
    }

    @Test
    public void postInteractionEndpointsUseBackendPaths() throws Exception {
        Method like = DoyuApi.class.getMethod("likePost", String.class);
        Method unlike = DoyuApi.class.getMethod("unlikePost", String.class);
        Method favorite = DoyuApi.class.getMethod("favoritePost", String.class);
        Method unfavorite = DoyuApi.class.getMethod("unfavoritePost", String.class);

        assertEquals("/api/v1/posts/{postId}/like", like.getAnnotation(POST.class).value());
        assertCallDataType(like, PostInteraction.class);
        assertEquals("/api/v1/posts/{postId}/like", unlike.getAnnotation(DELETE.class).value());
        assertCallDataType(unlike, PostInteraction.class);
        assertEquals("/api/v1/posts/{postId}/favorite", favorite.getAnnotation(POST.class).value());
        assertCallDataType(favorite, PostInteraction.class);
        assertEquals("/api/v1/posts/{postId}/favorite", unfavorite.getAnnotation(DELETE.class).value());
        assertCallDataType(unfavorite, PostInteraction.class);
    }

    private static void assertCallDataType(Method method, Class<?> expectedDataType) {
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof ParameterizedType);
        ParameterizedType callType = (ParameterizedType) returnType;
        assertEquals(Call.class, callType.getRawType());
        ParameterizedType apiResponseType = (ParameterizedType) callType.getActualTypeArguments()[0];
        assertEquals(ApiResponse.class, apiResponseType.getRawType());
        assertEquals(expectedDataType, apiResponseType.getActualTypeArguments()[0]);
    }

    private static void assertPageItemType(Method method, Class<?> expectedItemType) {
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof ParameterizedType);
        ParameterizedType callType = (ParameterizedType) returnType;
        assertEquals(Call.class, callType.getRawType());
        ParameterizedType apiResponseType = (ParameterizedType) callType.getActualTypeArguments()[0];
        assertEquals(ApiResponse.class, apiResponseType.getRawType());
        ParameterizedType pageType = (ParameterizedType) apiResponseType.getActualTypeArguments()[0];
        assertEquals(PageResponse.class, pageType.getRawType());
        assertEquals(expectedItemType, pageType.getActualTypeArguments()[0]);
    }
}
