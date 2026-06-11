package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentRequest;
import cn.edu.app.douyu.model.PageResponse;
import cn.edu.app.douyu.model.PostInteraction;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.PostRequest;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductCategory;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.CartItemRequest;
import cn.edu.app.douyu.model.CreateOrderRequest;
import cn.edu.app.douyu.model.Order;
import cn.edu.app.douyu.model.NotificationMessage;
import cn.edu.app.douyu.model.ReadReceipt;
import cn.edu.app.douyu.model.SearchResult;
import cn.edu.app.douyu.model.UpdateCartRequest;
import cn.edu.app.douyu.model.UpdateUserSettingsRequest;
import cn.edu.app.douyu.model.Topic;
import cn.edu.app.douyu.model.UserSettings;
import cn.edu.app.douyu.model.FileAsset;
import cn.edu.app.douyu.model.UploadConfirmRequest;
import cn.edu.app.douyu.model.UploadPresignRequest;
import cn.edu.app.douyu.model.UploadPresignResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DoyuApiDetailContractTest {
    @Test
    public void notificationApisUseCurrentBackendEndpoints() throws Exception {
        Method notifications = DoyuApi.class.getMethod("notifications", int.class, int.class);
        Method markRead = DoyuApi.class.getMethod("markNotificationsRead");

        assertEquals("/api/v1/notifications", notifications.getAnnotation(GET.class).value());
        assertPageItemType(notifications, NotificationMessage.class);
        assertEquals("/api/v1/notifications/read", markRead.getAnnotation(POST.class).value());
        assertCallDataType(markRead, ReadReceipt.class);
    }

    @Test
    public void uploadEndpointsUseExistingBackendEndpoints() throws Exception {
        Method presign = DoyuApi.class.getMethod("uploadPresign", UploadPresignRequest.class);
        Method confirm = DoyuApi.class.getMethod("uploadConfirm", UploadConfirmRequest.class);

        assertEquals("/api/v1/uploads/presign", presign.getAnnotation(POST.class).value());
        assertCallDataType(presign, UploadPresignResponse.class);
        assertEquals("/api/v1/uploads/confirm", confirm.getAnnotation(POST.class).value());
        assertCallDataType(confirm, FileAsset.class);
        assertEquals(1, countDeclaredMethods("uploadPresign"));
        assertEquals(1, countDeclaredMethods("uploadConfirm"));
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
    public void commerceCartAndOrderApisUseBackendPurchaseEndpoints() throws Exception {
        Method cart = DoyuApi.class.getMethod("cart");
        Method addCartItem = DoyuApi.class.getMethod("addCartItem", CartItemRequest.class);
        Method updateCartItem = DoyuApi.class.getMethod("updateCartItem", String.class, UpdateCartRequest.class);
        Method deleteCartItem = DoyuApi.class.getMethod("deleteCartItem", String.class);
        Method createOrder = DoyuApi.class.getMethod("createOrder", String.class, CreateOrderRequest.class);

        assertEquals("/api/v1/cart", cart.getAnnotation(GET.class).value());
        assertCallDataType(cart, CartResponse.class);
        assertEquals("/api/v1/cart/items", addCartItem.getAnnotation(POST.class).value());
        assertCallDataType(addCartItem, CartResponse.Item.class);
        assertEquals("/api/v1/cart/items/{itemId}", updateCartItem.getAnnotation(PATCH.class).value());
        assertCallDataType(updateCartItem, CartResponse.Item.class);
        assertEquals("/api/v1/cart/items/{itemId}", deleteCartItem.getAnnotation(DELETE.class).value());
        assertEquals("/api/v1/orders", createOrder.getAnnotation(POST.class).value());
        assertEquals("Idempotency-Key", createOrder.getParameters()[0].getAnnotation(Header.class).value());
        assertCallDataType(createOrder, Order.class);
    }

    @Test
    public void userSettingsApisUseBackendSettingsEndpoints() throws Exception {
        Method getSettings = DoyuApi.class.getMethod("userSettings");
        Method updateSettings = DoyuApi.class.getMethod("updateUserSettings", UpdateUserSettingsRequest.class);

        assertEquals("/api/v1/users/me/settings", getSettings.getAnnotation(GET.class).value());
        assertCallDataType(getSettings, UserSettings.class);
        assertEquals("/api/v1/users/me/settings", updateSettings.getAnnotation(PATCH.class).value());
        assertCallDataType(updateSettings, UserSettings.class);
    }

    @Test
    public void searchApiUsesBackendSearchEndpoint() throws Exception {
        Method search = DoyuApi.class.getMethod("search", String.class, String.class, int.class, int.class);

        assertEquals("/api/v1/search", search.getAnnotation(GET.class).value());
        assertEquals("keyword", search.getParameters()[0].getAnnotation(Query.class).value());
        assertEquals("type", search.getParameters()[1].getAnnotation(Query.class).value());
        assertPageItemType(search, SearchResult.class);
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

    private static int countDeclaredMethods(String name) {
        int count = 0;
        for (Method method : DoyuApi.class.getDeclaredMethods()) {
            if (name.equals(method.getName())) {
                count++;
            }
        }
        return count;
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
