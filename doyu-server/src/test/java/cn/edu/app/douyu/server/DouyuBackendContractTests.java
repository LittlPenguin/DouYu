package cn.edu.app.douyu.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.edu.app.douyu.server.common.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DouyuBackendContractTests {
    private static final AtomicInteger LOGIN_COUNTER = new AtomicInteger();


    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SkuRepository skuRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    StickerPackRepository stickerPackRepository;

    @Autowired
    StickerRepository stickerRepository;

    @Test
    void openApiDocsExposeApiV1EndpointsAndUploadContractFields() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        String docs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth", notNullValue()))
                .andExpect(jsonPath("$.info.description", org.hamcrest.Matchers.containsString("OSS upload integration")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode paths = objectMapper.readTree(docs).path("paths");
        Set<String> documentedApiPaths = new TreeSet<>();
        paths.fieldNames().forEachRemaining(path -> {
            if (path.startsWith("/api/v1")) {
                documentedApiPaths.add(path);
            }
        });
        Set<String> mappedApiPaths = new TreeSet<>();
        for (RequestMappingInfo info : requestMappingHandlerMapping.getHandlerMethods().keySet()) {
            for (String pattern : info.getPathPatternsCondition().getPatternValues()) {
                if (pattern.startsWith("/api/v1")) {
                    mappedApiPaths.add(pattern.replaceAll("\\{([^}/]+)}", "{$1}"));
                }
            }
        }

        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/register")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/login")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/sms-code")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/login/sms")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/uploads/presign")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/uploads/confirm")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/users/me/posts")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/users/me/following")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/users/me/followers")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/admin/auth/login")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/refresh")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/logout")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/account/cancel")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/notifications")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/messages/notifications")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/messages/conversations")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/checkins")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/rewards/me")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/badges/me")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/reports")).isFalse();
        org.assertj.core.api.Assertions.assertThat(documentedApiPaths).containsAll(mappedApiPaths);

        String token = login("13800000000", "AGE_18_PLUS");
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":2048,"fileName":"input.png"}
                """);
        JsonNode presignData = presign.path("data");
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("fileKey")).isTrue();
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("uploadUrl")).isTrue();
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("headers")).isTrue();
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("expiresIn")).isTrue();
        putLocalUpload(presignData, 2048);

        JsonNode confirmed = postJsonWithToken("/api/v1/uploads/confirm", token, """
                {"fileKey":"%s","usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":2048,"width":120,"height":120}
                """.formatted(presignData.path("fileKey").asText()));
        JsonNode confirmData = confirmed.path("data");
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("fileId")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("fileKey")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("ownerId")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("usage")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("storageKey")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("mimeType")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("sizeBytes")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("auditStatus")).isTrue();
        org.assertj.core.api.Assertions.assertThat(confirmData.path("fileKey").asText()).isEqualTo(confirmData.path("storageKey").asText());

    }

    @Test
    void emailRegisterAndLoginUseUnifiedResponseWithoutRefreshToken() throws Exception {
        JsonNode registered = postJson("/api/v1/auth/register", """
                {"email":" Test.User+Minor@Example.COM ","password":"password123","confirmPassword":"password123","ageGroup":"AGE_16_17","nickname":"娴嬭瘯鐢ㄦ埛"}
                """);
        String accessToken = registered.at("/data/accessToken").asText();
        org.assertj.core.api.Assertions.assertThat(registered.at("/data/refreshToken").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(registered.at("/data/user/email").asText())
                .isEqualTo("test.user+minor@example.com");

        JsonNode login = postJson("/api/v1/auth/login", """
                {"email":"test.user+minor@example.com","password":"password123"}
                """);
        org.assertj.core.api.Assertions.assertThat(login.at("/data/accessToken").asText()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/refreshToken").isMissingNode()).isTrue();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test.user+minor@example.com","password":"password123","confirmPassword":"password123","ageGroup":"AGE_16_17"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("CONFLICT")));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test.user+minor@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", notNullValue()))
                .andExpect(jsonPath("$.data.isMinor", equalTo(true)));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"rt_removed\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"rt_removed\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadPresignAndConfirmCreateImmediatelyUsableFileAsset() throws Exception {
        String token = login("13800000002", "AGE_18_PLUS");
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":2048,"fileName":"input.png"}
                """);
        putLocalUpload(presign.path("data"), 2048);

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileKey":"%s","usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":2048,"width":120,"height":120}
                """.formatted(presign.at("/data/fileKey").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId", notNullValue()))
                .andExpect(jsonPath("$.data.auditStatus", equalTo("PASS")))
                .andExpect(jsonPath("$.data.publicUrl", notNullValue()));
    }

    @Test
    void profileUpdatePersistsManualRegionAndReturnsResolvedAvatarUrl() throws Exception {
        String token = login("13800000003", "AGE_18_PLUS");
        String avatarFileId = confirmedFile(token, "AVATAR", 96, 96);

        JsonNode updated = patchJsonWithToken("/api/v1/users/me", token, """
                {"nickname":"region user","bio":"region bio","avatarFileId":"%s","region":"杭州"}
                """.formatted(avatarFileId));
        org.assertj.core.api.Assertions.assertThat(updated.at("/data/region").asText()).isEqualTo("杭州");
        org.assertj.core.api.Assertions.assertThat(updated.at("/data/avatarUrl").asText())
                .contains("/uploads/assets/avatar/")
                .isNotEqualTo(avatarFileId);

        JsonNode me = getJsonWithToken("/api/v1/users/me", token);
        org.assertj.core.api.Assertions.assertThat(me.at("/data/nickname").asText()).isEqualTo("region user");
        org.assertj.core.api.Assertions.assertThat(me.at("/data/bio").asText()).isEqualTo("region bio");
        org.assertj.core.api.Assertions.assertThat(me.at("/data/region").asText()).isEqualTo("杭州");
        org.assertj.core.api.Assertions.assertThat(me.at("/data/avatarUrl").asText())
                .contains("/uploads/assets/avatar/")
                .isNotEqualTo(avatarFileId);
        org.assertj.core.api.Assertions.assertThat(me.at("/data/avatarFileId").isMissingNode()).isTrue();
    }

    @Test
    void userSettingsDefaultToTruePatchPartiallyAndDoNotExposePrivateMessagePreferences() throws Exception {
        JsonNode registered = postJson("/api/v1/auth/register", """
                {"email":"settings-user@example.com","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"设置用户"}
                """);
        String token = registered.at("/data/accessToken").asText();

        JsonNode defaults = getJsonWithToken("/api/v1/users/me/settings", token);
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/allowRecommendation").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/allowFavorites").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/notifyInteractions").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/notifyPublish").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/notifySystem").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/allowStrangerMessages").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(defaults.at("/data/notifyMessages").isMissingNode()).isTrue();

        JsonNode patched = patchJsonWithToken("/api/v1/users/me/settings", token, """
                {"allowRecommendation":false,"allowStrangerMessages":false,"notifyMessages":false,"notifySystem":false}
                """);
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/allowRecommendation").asBoolean()).isFalse();
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/allowFavorites").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/notifyInteractions").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/notifyPublish").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/notifySystem").asBoolean()).isFalse();
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/allowStrangerMessages").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(patched.at("/data/notifyMessages").isMissingNode()).isTrue();

        JsonNode me = getJsonWithToken("/api/v1/users/me", token);
        org.assertj.core.api.Assertions.assertThat(me.at("/data/allowRecommendation").asBoolean()).isFalse();
        org.assertj.core.api.Assertions.assertThat(me.at("/data/notifySystem").asBoolean()).isFalse();
        org.assertj.core.api.Assertions.assertThat(me.at("/data/allowStrangerMessages").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(me.at("/data/notifyMessages").isMissingNode()).isTrue();

        mockMvc.perform(post("/api/v1/auth/account/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void orderCreationIsIdempotentAndDoesNotExposeOrderCenterEndpoints() throws Exception {
        String token = login("13800000004", "AGE_18_PLUS");
        String skuId = ensureSelfOperatedSku("fixture-product-order-red", "fixture-sku-order-red",
                "https://fixture.local/assets/order-red.png", 100);

        JsonNode cartAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":2}
                """.formatted(skuId));
        String cartItemId = cartAdd.at("/data/itemId").asText();

        String orderPayload = """
                {"itemIds":["%s"],"addressSnapshot":{"recipient":"Tester","phone":"13800000000","region":"Hangzhou","detail":"Road 1"}}
                """.formatted(cartItemId);
        JsonNode first = postJsonWithIdempotency("/api/v1/orders", token, "order-key-1", orderPayload);
        JsonNode second = postJsonWithIdempotency("/api/v1/orders", token, "order-key-1", orderPayload);
        String orderId = first.at("/data/orderId").asText();

        org.assertj.core.api.Assertions.assertThat(second.at("/data/orderId").asText()).isEqualTo(orderId);
        org.assertj.core.api.Assertions.assertThat(first.at("/data/addressSnapshot/recipient").asText()).isEqualTo("Tester");
        org.assertj.core.api.Assertions.assertThat(first.at("/data/status").asText()).isEqualTo("CREATED");

        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "order-key-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"skuId":"%s","quantity":999999}],"addressSnapshot":{"recipient":"Tester","phone":"13800000000","region":"Hangzhou","detail":"Road 1"}}
                                """.formatted(skuId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("INVENTORY_NOT_ENOUGH")));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "order-key-address-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"skuId":"%s","quantity":1}],"addressId":"addr_removed"}
                                """.formatted(skuId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));
    }
    @Test
    void playerProductCannotEnterStandardCartOrOrder() throws Exception {
        String token = login("13800000022", "AGE_18_PLUS");
        String playerSkuId = ensurePlayerSku("fixture-product-player-second-hand", "fixture-sku-player-second-hand",
                "PLAYER_SECOND_HAND", "https://fixture.local/assets/player-second-hand.png");
        String selfSkuId = ensureSelfOperatedSku("fixture-product-cart-white", "fixture-sku-cart-white",
                "https://fixture.local/assets/cart-white.png", 100);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuId":"%s","quantity":1}
                                """.formatted(playerSkuId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("CONFLICT")));

        JsonNode selfAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":1}
                """.formatted(selfSkuId));
        String cartItemId = selfAdd.at("/data/itemId").asText();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "order-player-boundary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemIds":["missing_player_cart_item","%s"],"addressSnapshot":{"recipient":"Tester","phone":"13800000000","region":"Hangzhou","detail":"Road 1"}}
                                """.formatted(cartItemId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));
    }

    @Test
    void adminAndReportApisAreRemoved() throws Exception {
        String userToken = login("13800000005", "AGE_18_PLUS");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"POST","targetId":"post_missing","reason":"SPAM","description":"test report"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void communityPostLikeFavoriteAndCommentFlow() throws Exception {
        String token = login("13800000010", "AGE_18_PLUS");

        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"test post","content":"community test content"}
                """);
        String postId = created.at("/data/postId").asText();
        org.assertj.core.api.Assertions.assertThat(postId).isNotEmpty();

        mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.status", equalTo("VISIBLE")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andExpect(jsonPath("$.data.author.userId", notNullValue()));

        String feedContent = mockMvc.perform(get("/api/v1/posts/feed")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode feedItems = objectMapper.readTree(feedContent).at("/data/items");
        org.assertj.core.api.Assertions.assertThat(feedItems)
                .anySatisfy(item -> org.assertj.core.api.Assertions.assertThat(item.path("postId").asText()).isEqualTo(postId));

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(true)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)));

        JsonNode comment = postJsonWithPath("/api/v1/posts/{postId}/comments", token, """
                {"content":"nice bead post"}
                """, postId);
        String commentId = comment.at("/data/commentId").asText();
        org.assertj.core.api.Assertions.assertThat(commentId).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/status").asText()).isEqualTo("VISIBLE");

        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].commentId", equalTo(commentId)))
                .andExpect(jsonPath("$.data.items[0].status", equalTo("VISIBLE")));

        mockMvc.perform(delete("/api/v1/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted", equalTo(true)));

        mockMvc.perform(delete("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(false)));

        mockMvc.perform(delete("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(false)));
    }

    @Test
    void anonymousUsersCanReadPostDetailAndCommentsButCannotMutateInteractions() throws Exception {
        String token = login("13800000062", "AGE_18_PLUS");

        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"anonymous readable post","content":"public detail and comments"}
                """);
        String postId = created.at("/data/postId").asText();
        JsonNode comment = postJsonWithPath("/api/v1/posts/{postId}/comments", token, """
                {"content":"public comment"}
                """, postId);
        String commentId = comment.at("/data/commentId").asText();

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.likeCount", equalTo(0)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(0)))
                .andExpect(jsonPath("$.data.likedByMe", equalTo(false)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(false)))
                .andExpect(jsonPath("$.data.followedAuthorByMe", equalTo(false)));

        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].commentId", equalTo(commentId)))
                .andExpect(jsonPath("$.data.items[0].status", equalTo("VISIBLE")));

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"anonymous mutation"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));
    }

    @Test
    void publishedCommunityPostAppearsInFeedWithTopicNamesAndCoverDimensions() throws Exception {
        String token = login("13800000061", "AGE_18_PLUS");
        String topicId = ensureTopic("fixture-topic-cover-dimensions", "cover dimensions");
        String fileId = confirmedFile(token, "POST_IMAGE", 480, 720);
        String secondFileId = confirmedFile(token, "POST_IMAGE", 320, 240);

        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"ratio cover post","content":"post with real cover dimensions","mediaFileIds":["%s","%s"],"topicIds":["%s"]}
                """.formatted(fileId, secondFileId, topicId));
        String postId = created.at("/data/postId").asText();
        org.assertj.core.api.Assertions.assertThat(created.at("/data/status").asText()).isEqualTo("VISIBLE");
        org.assertj.core.api.Assertions.assertThat(created.at("/data/imageUrls").isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(created.at("/data/imageUrls").size()).isEqualTo(2);
        String firstImageUrl = created.at("/data/imageUrls/0").asText();
        String secondImageUrl = created.at("/data/imageUrls/1").asText();
        org.assertj.core.api.Assertions.assertThat(firstImageUrl).contains("/uploads/assets/post_image/");
        org.assertj.core.api.Assertions.assertThat(secondImageUrl).contains("/uploads/assets/post_image/");
        org.assertj.core.api.Assertions.assertThat(secondImageUrl).isNotEqualTo(firstImageUrl);
        org.assertj.core.api.Assertions.assertThat(created.at("/data/coverImageUrl").asText()).isEqualTo(firstImageUrl);

        String detailContent = mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("VISIBLE")))
                .andExpect(jsonPath("$.data.coverImageUrl", org.hamcrest.Matchers.containsString("/uploads/assets/post_image/")))
                .andExpect(jsonPath("$.data.coverWidth", equalTo(480)))
                .andExpect(jsonPath("$.data.coverHeight", equalTo(720)))
                .andExpect(jsonPath("$.data.topicNames[0]", equalTo("cover dimensions")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode detail = objectMapper.readTree(detailContent).path("data");
        org.assertj.core.api.Assertions.assertThat(detail.at("/imageUrls").size()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(detail.at("/imageUrls/0").asText()).isEqualTo(firstImageUrl);
        org.assertj.core.api.Assertions.assertThat(detail.at("/imageUrls/1").asText()).isEqualTo(secondImageUrl);

        String feedContent = mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.items[0].coverWidth", equalTo(480)))
                .andExpect(jsonPath("$.data.items[0].coverHeight", equalTo(720)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode feedPost = objectMapper.readTree(feedContent).at("/data/items/0");
        org.assertj.core.api.Assertions.assertThat(feedPost.at("/imageUrls").size()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(feedPost.at("/imageUrls/0").asText()).isEqualTo(firstImageUrl);
        org.assertj.core.api.Assertions.assertThat(feedPost.at("/imageUrls/1").asText()).isEqualTo(secondImageUrl);

        String topicContent = mockMvc.perform(get("/api/v1/topics/{topicId}/posts", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.items[0].topicNames[0]", equalTo("cover dimensions")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode topicPost = objectMapper.readTree(topicContent).at("/data/items/0");
        org.assertj.core.api.Assertions.assertThat(topicPost.at("/imageUrls").size()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(topicPost.at("/imageUrls/0").asText()).isEqualTo(firstImageUrl);
        org.assertj.core.api.Assertions.assertThat(topicPost.at("/imageUrls/1").asText()).isEqualTo(secondImageUrl);
    }

    @Test
    void loginAndCommunitySampleContractFieldsStayAligned() throws Exception {
        JsonNode login = postJson("/api/v1/auth/register", """
                {"email":"contract-user@example.com","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"contract-user"}
                """);
        org.assertj.core.api.Assertions.assertThat(login.at("/data/accessToken").asText()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/refreshToken").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/expiresIn").asLong()).isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(login.at("/data/user/avatarUrl").isMissingNode()).isFalse();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/user/avatarFileId").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/user/email").asText()).isEqualTo("contract-user@example.com");
        org.assertj.core.api.Assertions.assertThat(login.path("traceId").asText()).isNotBlank();

        String token = login.at("/data/accessToken").asText();
        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"contract post","content":"community contract content","mediaFileIds":[],"topicIds":[]}
                """);
        JsonNode post = created.path("data");
        org.assertj.core.api.Assertions.assertThat(post.path("postId").asText()).startsWith("post_");
        org.assertj.core.api.Assertions.assertThat(post.path("status").asText()).isEqualTo("VISIBLE");
        org.assertj.core.api.Assertions.assertThat(post.path("mediaFileIds").isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(post.path("topicIds").isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(post.path("author").has("avatarUrl")).isTrue();
        org.assertj.core.api.Assertions.assertThat(created.path("traceId").asText()).isNotBlank();

        JsonNode comment = postJsonWithPath("/api/v1/posts/{postId}/comments", token, """
                {"content":"contract comment"}
                """, post.path("postId").asText());
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/status").asText()).isEqualTo("VISIBLE");
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/author/avatarUrl").isMissingNode()).isFalse();
    }

    @Test
    void communityInteractionsAreAuthGuardedAndValidateResource() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"unauthorized","content":"body"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")))
                .andExpect(jsonPath("$.traceId", notNullValue()));

        mockMvc.perform(post("/api/v1/posts/missing_post/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"comment"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        mockMvc.perform(post("/api/v1/posts/missing_post/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        mockMvc.perform(post("/api/v1/posts/missing_post/favorite"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        String token = login("13800000017", "AGE_18_PLUS");

        mockMvc.perform(post("/api/v1/posts/missing_post/like")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));

        mockMvc.perform(delete("/api/v1/posts/missing_post/like")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));

        mockMvc.perform(post("/api/v1/posts/missing_post/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));

        mockMvc.perform(delete("/api/v1/posts/missing_post/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));
    }

    @Test
    void repeatedCommunityInteractionsRemainIdempotent() throws Exception {
        String token = login("13800000018", "AGE_18_PLUS");
        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"idempotent post","content":"community interaction content"}
                """);
        String postId = created.at("/data/postId").asText();

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(true)))
                .andExpect(jsonPath("$.data.likedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.likeCount", equalTo(1)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(0)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(false)));

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(true)))
                .andExpect(jsonPath("$.data.likedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.likeCount", equalTo(1)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(1)))
                .andExpect(jsonPath("$.data.likeCount", equalTo(1)))
                .andExpect(jsonPath("$.data.likedByMe", equalTo(true)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(1)));

        mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount", equalTo(1)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(1)));
    }

    @Test
    void communityInteractionUsesStoredAggregateCountsInsteadOfResettingSeedCounts() throws Exception {
        String token = login("13800000019", "AGE_18_PLUS");
        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"seed aggregate post","content":"community aggregate content"}
                """);
        String postId = created.at("/data/postId").asText();
        PostEntity post = postRepository.findById(postId).orElseThrow();
        post.setLikeCount(40);
        post.setFavoriteCount(12);
        postRepository.save(post);

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(true)))
                .andExpect(jsonPath("$.data.likedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.likeCount", equalTo(41)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(12)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.likeCount", equalTo(41)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(13)));
    }

    @Test
    void userFollowAndUnfollowWorks() throws Exception {
        String tokenA = login("13800000011", "AGE_18_PLUS");
        String tokenB = login("13800000012", "AGE_18_PLUS");

        JsonNode userB = getJsonWithToken("/api/v1/users/me", tokenB);
        String userBId = userB.at("/data/userId").asText();

        mockMvc.perform(post("/api/v1/users/{userId}/follow", userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followed", equalTo(true)));

        mockMvc.perform(delete("/api/v1/users/{userId}/follow", userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followed", equalTo(false)));
    }

    @Test
    void userCannotFollowThemselves() throws Exception {
        String tokenA = login("13800000019", "AGE_18_PLUS");
        JsonNode userA = getJsonWithToken("/api/v1/users/me", tokenA);
        String userAId = userA.at("/data/userId").asText();

        mockMvc.perform(post("/api/v1/users/{userId}/follow", userAId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rewardCheckinAndBadgeApisAreRemoved() throws Exception {
        String token = login("13800000013", "AGE_18_PLUS");

        mockMvc.perform(post("/api/v1/checkins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/checkins/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/rewards/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/badges/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerCreatesDefaultNotificationsAndCanMarkThemRead() throws Exception {
        String token = login("13800000014", "AGE_18_PLUS");

        String response = mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total", equalTo(3)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode notifications = objectMapper.readTree(response).at("/data/items");
        org.assertj.core.api.Assertions.assertThat(notifications)
                .anySatisfy(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.path("type").asText()).isEqualTo("SYSTEM");
                    org.assertj.core.api.Assertions.assertThat(item.path("title").asText()).isEqualTo("欢迎来到豆屿");
                    org.assertj.core.api.Assertions.assertThat(item.path("content").asText())
                            .isEqualTo("你可以浏览作品、搜索话题，也可以上传自己的拼豆作品。");
                    org.assertj.core.api.Assertions.assertThat(item.path("unread").asBoolean()).isTrue();
                })
                .anySatisfy(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.path("type").asText()).isEqualTo("SYSTEM");
                    org.assertj.core.api.Assertions.assertThat(item.path("title").asText()).isEqualTo("上传作品提示");
                    org.assertj.core.api.Assertions.assertThat(item.path("content").asText())
                            .isEqualTo("底部“上传”可以发布作品；图片会先通过 OSS 上传，成功后再发布。");
                    org.assertj.core.api.Assertions.assertThat(item.path("unread").asBoolean()).isTrue();
                })
                .anySatisfy(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.path("type").asText()).isEqualTo("SYSTEM");
                    org.assertj.core.api.Assertions.assertThat(item.path("title").asText()).isEqualTo("商城下单提示");
                    org.assertj.core.api.Assertions.assertThat(item.path("content").asText())
                            .isEqualTo("商城支持商品浏览、购物车和创建订单；当前不提供支付服务。");
                    org.assertj.core.api.Assertions.assertThat(item.path("unread").asBoolean()).isTrue();
                });

        mockMvc.perform(post("/api/v1/notifications/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read", equalTo(true)));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].unread", equalTo(false)))
                .andExpect(jsonPath("$.data.items[1].unread", equalTo(false)))
                .andExpect(jsonPath("$.data.items[2].unread", equalTo(false)));
    }

    @Test
    void privateMessageConversationApisAreRemoved() throws Exception {
        String tokenA = login("13800000024", "AGE_18_PLUS");

        mockMvc.perform(get("/api/v1/messages/conversations")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/messages/conversations/{conversationId}", "conv_removed")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/messages/conversations/{conversationId}", "conv_removed")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"removed"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void errorScenariosReturnCorrectCodes() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));

        String token = login("13800000015", "AGE_18_PLUS");

        mockMvc.perform(get("/api/v1/posts/nonexistent_post_id")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));

        mockMvc.perform(post("/api/v1/uploads/presign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usage":"INVALID_USAGE","mimeType":"image/png","sizeBytes":1024,"fileName":"test.png"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        mockMvc.perform(post("/api/v1/uploads/presign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":999999999,"fileName":"huge.png"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"INVALID","targetId":"x","reason":"SPAM"}
                                """))
                .andExpect(status().isNotFound());

        // Malformed JSON body 鈫?400 INVALID_ARGUMENT
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        // Missing required field (blank email) 鈫?400 INVALID_ARGUMENT
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        // Nonexistent order 鈫?404 NOT_FOUND
        mockMvc.perform(get("/api/v1/orders/nonexistent_order")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // Nonexistent comment 鈫?404 NOT_FOUND
        mockMvc.perform(delete("/api/v1/comments/nonexistent_comment")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));

        // Verify all error responses have traceId
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.traceId", notNullValue()));
    }

    @Test
    void feedEndpointIsPublicAndReturnsPagination() throws Exception {
        mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.page", equalTo(1)))
                .andExpect(jsonPath("$.data.size", equalTo(20)));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void explicitProductFixturesCoverSelfOperatedPlayerSecondHandAndCustomService() throws Exception {
        ensureSelfOperatedSku("fixture-product-list-self", "fixture-sku-list-self",
                "https://fixture.local/assets/list-self.png", 100);
        ensurePlayerSku("fixture-product-list-second-hand", "fixture-sku-list-second-hand",
                "PLAYER_SECOND_HAND", "https://fixture.local/assets/list-second-hand.png");
        ensurePlayerSku("fixture-product-list-custom", "fixture-sku-list-custom",
                "PLAYER_CUSTOM_SERVICE", "https://fixture.local/assets/list-custom.png");

        String content = mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode products = objectMapper.readTree(content);
        JsonNode items = products.at("/data/items");

        org.assertj.core.api.Assertions.assertThat(items)
                .anySatisfy(item -> org.assertj.core.api.Assertions.assertThat(item.path("type").asText()).isEqualTo("SELF_OPERATED"))
                .anySatisfy(item -> org.assertj.core.api.Assertions.assertThat(item.path("type").asText()).isEqualTo("PLAYER_SECOND_HAND"))
                .anySatisfy(item -> org.assertj.core.api.Assertions.assertThat(item.path("type").asText()).isEqualTo("PLAYER_CUSTOM_SERVICE"));
    }

    @Test
    void productListHidesDraftOrPendingReviewProductsButDetailShowsBoundary() throws Exception {
        ensureSelfOperatedSku("fixture-product-public-list", "fixture-sku-public-list",
                "https://fixture.local/assets/public-list.png", 5);

        Instant now = Instant.now();
        ProductEntity draft = productRepository.findById("fixture-product-draft-hidden").orElseGet(ProductEntity::new);
        if (draft.getId() == null) {
            draft.setId("fixture-product-draft-hidden");
            draft.setCreatedAt(now);
        }
        draft.setType("SELF_OPERATED");
        draft.setSellerId("fixture-seller");
        draft.setTitle("draft hidden product");
        draft.setDescription("draft product should not appear in public list");
        draft.setImageUrl("https://fixture.local/assets/draft-hidden.png");
        draft.setCategoryId("fixture");
        draft.setStatus("DRAFT");
        draft.setAuditStatus("NEED_MANUAL_REVIEW");
        draft.setUpdatedAt(now);
        productRepository.save(draft);

        String content = mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode items = objectMapper.readTree(content).at("/data/items");

        org.assertj.core.api.Assertions.assertThat(items)
                .anySatisfy(item -> org.assertj.core.api.Assertions.assertThat(item.path("productId").asText())
                        .isEqualTo("fixture-product-public-list"));
        for (JsonNode item : items) {
            org.assertj.core.api.Assertions.assertThat(item.path("productId").asText())
                    .isNotEqualTo("fixture-product-draft-hidden");
        }

        mockMvc.perform(get("/api/v1/products/{productId}", "fixture-product-draft-hidden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("DRAFT")))
                .andExpect(jsonPath("$.data.auditStatus", equalTo("NEED_MANUAL_REVIEW")));
    }

    @Test
    void explicitFixturesReturnConfiguredAssetUrlsWithoutSeedAssets() throws Exception {
        String productId = "fixture-product-asset-url";
        String skuId = ensureSelfOperatedSku(productId, "fixture-sku-asset-url",
                "https://fixture.local/assets/product-asset-url.png", 100);
        String postId = ensureVisiblePost("fixture-post-asset-url", "fixture-author-asset-url",
                null, "https://fixture.local/assets/post-asset-url.png");

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl", equalTo("https://fixture.local/assets/product-asset-url.png")));

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverImageUrl", equalTo("https://fixture.local/assets/post-asset-url.png")));

        String token = login("13800000023", "AGE_18_PLUS");
        postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":1}
                """.formatted(skuId));

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].product.imageUrl", equalTo("https://fixture.local/assets/product-asset-url.png")));

        String productsContent = mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode products = objectMapper.readTree(productsContent).at("/data/items");
        org.assertj.core.api.Assertions.assertThat(products)
                .anySatisfy(product -> {
                    org.assertj.core.api.Assertions.assertThat(product.path("productId").asText()).isEqualTo(productId);
                    org.assertj.core.api.Assertions.assertThat(product.path("imageUrl").asText()).doesNotContain(forbiddenAssetRouteSegment());
                });

        String feedContent = mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode posts = objectMapper.readTree(feedContent).at("/data/items");
        org.assertj.core.api.Assertions.assertThat(posts)
                .anySatisfy(post -> {
                    org.assertj.core.api.Assertions.assertThat(post.path("postId").asText()).isEqualTo(postId);
                    org.assertj.core.api.Assertions.assertThat(post.path("coverImageUrl").asText()).doesNotContain(forbiddenAssetRouteSegment());
                });
    }

    @Test
    void openApiDocsContainAllEndpointTags() throws Exception {
        String docs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode paths = objectMapper.readTree(docs).path("paths");

        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/posts/feed")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/posts/{postId}")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/posts/{postId}/like")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/posts/{postId}/comments")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/checkins")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/notifications")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/notifications/read")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/messages/notifications")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/messages/conversations")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/users/{userId}/follow")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/reports")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/admin/auth/login")).isFalse();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/refunds")).isFalse();
    }

    @Test
    void commentsSupportTextImageAndMixedMediaWithValidation() throws Exception {
        String token = login("13800000024", "AGE_18_PLUS");
        String otherToken = login("13800000025", "AGE_18_PLUS");
        String imageFileId = confirmedFile(token, "POST_IMAGE");
        String postId = ensureVisiblePost("fixture-post-comments-media", "fixture-author-comments-media",
                null, "https://fixture.local/assets/comments-media.png");

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"","mediaFileIds":["%s"]}
                                """.formatted(imageFileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", equalTo("")))
                .andExpect(jsonPath("$.data.mediaFileIds[0]", equalTo(imageFileId)))
                .andExpect(jsonPath("$.data.mediaAssets[0].fileId", equalTo(imageFileId)))
                .andExpect(jsonPath("$.data.mediaAssets[0].publicUrl", org.hamcrest.Matchers.containsString("/uploads/assets/post_image/")));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"鍥炬枃璇勮","mediaFileIds":["%s"]}
                                """.formatted(imageFileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", equalTo("鍥炬枃璇勮")))
                .andExpect(jsonPath("$.data.mediaAssets[0].mimeType", equalTo("image/png")));

        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].mediaAssets[0].fileId", equalTo(imageFileId)));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"   ","mediaFileIds":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"澶鍥剧墖","mediaFileIds":["1","2","3","4","5","6","7","8","9","10"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        String otherImageFileId = confirmedFile(otherToken, "POST_IMAGE");
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"涓嶈兘浣跨敤鍒汉鐨勫浘","mediaFileIds":["%s"]}
                                """.formatted(otherImageFileId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));

        String productImageFileId = confirmedFile(token, "PRODUCT_IMAGE");
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"鐢ㄩ€斾笉瀵?,"mediaFileIds":["%s"]}
                                """.formatted(productImageFileId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));
    }

    @Test
    void communityMentionTopicStickerAndInteractionAssetsWork() throws Exception {
        String authorToken = login("13800000026", "AGE_18_PLUS");
        String viewerToken = login("13800000027", "AGE_18_PLUS");
        String mentionedToken = login("13800000028", "AGE_18_PLUS");

        JsonNode authorMe = getJsonWithToken("/api/v1/users/me", authorToken).path("data");
        JsonNode mentionedMe = getJsonWithToken("/api/v1/users/me", mentionedToken).path("data");
        String authorId = authorMe.path("userId").asText();
        String mentionedUserId = mentionedMe.path("userId").asText();
        String topicId = ensureTopic("fixture-topic-interaction", "interaction");
        String stickerId = ensureSticker("fixture-pack-interaction", "fixture-sticker-interaction");

        JsonNode created = postJsonWithToken("/api/v1/posts", authorToken, """
                {"title":"interaction assets post","content":"community interaction asset source","mediaFileIds":[],"topicIds":["%s"]}
                """.formatted(topicId));
        String postId = created.at("/data/postId").asText();
        PostEntity visiblePost = postRepository.findById(postId).orElseThrow();
        visiblePost.setStatus("VISIBLE");
        postRepository.save(visiblePost);

        mockMvc.perform(get("/api/v1/users/search")
                        .header("Authorization", "Bearer " + viewerToken)
                        .queryParam("keyword", "13800000028")
                        .queryParam("page", "1")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].userId", equalTo(mentionedUserId)));

        mockMvc.perform(get("/api/v1/topics")
                        .queryParam("keyword", "interaction")
                        .queryParam("page", "1")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].topicId", equalTo(topicId)));

        mockMvc.perform(get("/api/v1/topics/{topicId}/posts", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].topicIds[0]", equalTo(topicId)));

        String stickerPacksContent = mockMvc.perform(get("/api/v1/sticker-packs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode stickerPacks = objectMapper.readTree(stickerPacksContent);
        org.assertj.core.api.Assertions.assertThat(stickerPacks.at("/data/items"))
                .anySatisfy(pack -> org.assertj.core.api.Assertions.assertThat(pack.path("packId").asText()).isEqualTo("fixture-pack-interaction"));

        JsonNode comment = postJsonWithPath("/api/v1/posts/{postId}/comments", viewerToken, """
                {"content":"@mention #topic sticker","mentionUserIds":["%s"],"topicIds":["%s"],"stickerIds":["%s"]}
                """.formatted(mentionedUserId, topicId, stickerId), postId);
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/mentions/0/userId").asText()).isEqualTo(mentionedUserId);
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/topics/0/topicId").asText()).isEqualTo(topicId);
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/stickers/0/stickerId").asText()).isEqualTo(stickerId);

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + mentionedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].type", equalTo("MENTION")));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"   ","mentionUserIds":["%s"],"topicIds":["%s"],"stickerIds":[]}
                                """.formatted(mentionedUserId, topicId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/users/{userId}/follow", authorId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(true)))
                .andExpect(jsonPath("$.data.followedAuthorByMe", equalTo(true)));

        String feedContent = mockMvc.perform(get("/api/v1/posts/feed")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode feedItems = objectMapper.readTree(feedContent).at("/data/items");
        JsonNode echoedPost = null;
        for (JsonNode item : feedItems) {
            if (postId.equals(item.path("postId").asText())) {
                echoedPost = item;
                break;
            }
        }
        org.assertj.core.api.Assertions.assertThat(echoedPost).isNotNull();
        org.assertj.core.api.Assertions.assertThat(echoedPost.path("likedByMe").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(echoedPost.path("favoritedByMe").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(echoedPost.path("followedAuthorByMe").asBoolean()).isTrue();

        String topicPostContent = mockMvc.perform(get("/api/v1/topics/{topicId}/posts", topicId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode topicItems = objectMapper.readTree(topicPostContent).at("/data/items");
        JsonNode echoedTopicPost = null;
        for (JsonNode item : topicItems) {
            if (postId.equals(item.path("postId").asText())) {
                echoedTopicPost = item;
                break;
            }
        }
        org.assertj.core.api.Assertions.assertThat(echoedTopicPost).isNotNull();
        org.assertj.core.api.Assertions.assertThat(echoedTopicPost.path("likedByMe").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(echoedTopicPost.path("favoritedByMe").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(echoedTopicPost.path("followedAuthorByMe").asBoolean()).isTrue();

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likedByMe", equalTo(false)))
                .andExpect(jsonPath("$.data.favoritedByMe", equalTo(false)))
                .andExpect(jsonPath("$.data.followedAuthorByMe", equalTo(false)));

        mockMvc.perform(get("/api/v1/users/me/liked-posts")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)));
        mockMvc.perform(get("/api/v1/users/me/commented-posts")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)));
        mockMvc.perform(get("/api/v1/users/me/favorite-posts")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)));
        mockMvc.perform(get("/api/v1/users/me/followed-posts")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].authorId", equalTo(authorId)));
    }

    @Test
    void profileStatDetailEndpointsReturnRealPostsFollowingAndFollowers() throws Exception {
        String ownerToken = login("13800000070", "AGE_18_PLUS");
        String followedToken = login("13800000071", "AGE_18_PLUS");
        String followerToken = login("13800000072", "AGE_18_PLUS");
        String ownerId = getJsonWithToken("/api/v1/users/me", ownerToken).at("/data/userId").asText();
        String followedId = getJsonWithToken("/api/v1/users/me", followedToken).at("/data/userId").asText();
        String followerId = getJsonWithToken("/api/v1/users/me", followerToken).at("/data/userId").asText();

        JsonNode created = postJsonWithToken("/api/v1/posts", ownerToken, """
                {"title":"profile stat post","content":"profile stat source","mediaFileIds":[],"topicIds":[]}
                """);
        String postId = created.at("/data/postId").asText();
        PostEntity post = postRepository.findById(postId).orElseThrow();
        post.setStatus("VISIBLE");
        postRepository.save(post);

        mockMvc.perform(post("/api/v1/users/{userId}/follow", followedId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/users/{userId}/follow", ownerId)
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me/posts")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.items[0].authorId", equalTo(ownerId)));
        mockMvc.perform(get("/api/v1/users/me/following")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].userId", equalTo(followedId)));
        mockMvc.perform(get("/api/v1/users/me/followers")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].userId", equalTo(followerId)));

        mockMvc.perform(get("/api/v1/users/me/posts"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/users/me/following"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/users/me/followers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void minorOrUnverifiedUsersCannotPublishPlayerTradeProducts() throws Exception {
        String minorToken = login("13800000006", "AGE_16_17");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .header("Authorization", "Bearer " + minorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + minorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"PLAYER_CUSTOM_SERVICE","title":"minor product","description":"test","sku":{"specName":"default","priceCent":5000,"stock":1}}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));

        String adultToken = login("13800000007", "AGE_18_PLUS");
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adultToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"PLAYER_SECOND_HAND","title":"unverified product","description":"test","sku":{"specName":"default","priceCent":1000,"stock":1}}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));
    }

    private String login(String phone, String ageGroup) throws Exception {
        String email = "user-" + phone + "-" + LOGIN_COUNTER.incrementAndGet() + "@example.com";
        JsonNode login = postJson("/api/v1/auth/register", """
                {"email":"%s","password":"password123","confirmPassword":"password123","ageGroup":"%s","nickname":"娴嬭瘯鐢ㄦ埛"}
                """.formatted(email, ageGroup));
        return login.at("/data/accessToken").asText();
    }

    private String confirmedFile(String token, String usage) throws Exception {
        return confirmedFile(token, usage, 64, 64);
    }

    private String confirmedFile(String token, String usage, int width, int height) throws Exception {
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"%s","mimeType":"image/png","sizeBytes":1024,"fileName":"file.png"}
                """.formatted(usage));
        putLocalUpload(presign.path("data"), 1024);
        JsonNode confirmed = postJsonWithToken("/api/v1/uploads/confirm", token, """
                {"fileKey":"%s","usage":"%s","mimeType":"image/png","sizeBytes":1024,"width":%d,"height":%d}
                """.formatted(presign.at("/data/fileKey").asText(), usage, width, height));
        return confirmed.at("/data/fileId").asText();
    }

    private void putLocalUpload(JsonNode presignData, int sizeBytes) throws Exception {
        mockMvc.perform(put("/uploads/temp/" + presignData.path("fileKey").asText())
                        .contentType("image/png")
                        .content(new byte[sizeBytes]))
                .andExpect(status().isOk());
    }

    @Test
    void globalSearchReturnsRealRetainedResultsAndHonorsFilters() throws Exception {
        String token = login("13800000038", "AGE_18_PLUS");
        String userId = getJsonWithToken("/api/v1/users/me", token).at("/data/userId").asText();
        String topicId = ensureTopic("fixture-topic-search-strawberry", "草莓色卡");
        String postId = ensureVisiblePost("fixture-post-search-strawberry", userId, topicId, null);
        PostEntity post = postRepository.findById(postId).orElseThrow();
        post.setTitle("草莓小熊杯垫");
        post.setContent("适合新手的草莓色拼豆作品");
        postRepository.save(post);

        ensureSearchProduct("fixture-product-search-strawberry", "草莓拼豆套装", "透明板和草莓色豆子", "kit", "材料套装", "ON_SALE", "PASS");
        ensureSearchProduct("fixture-product-search-hidden", "草莓隐藏商品", "不应出现在搜索", "kit", "材料套装", "DRAFT", "PASS");

        mockMvc.perform(get("/api/v1/search")
                        .queryParam("keyword", "草莓")
                        .queryParam("type", "all")
                        .queryParam("page", "1")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.resultType=='POST' && @.targetId=='%s')]".formatted(postId)).exists())
                .andExpect(jsonPath("$.data.items[?(@.resultType=='PRODUCT' && @.targetId=='fixture-product-search-strawberry')]").exists())
                .andExpect(jsonPath("$.data.items[?(@.resultType=='TOPIC' && @.targetId=='%s')]".formatted(topicId)).exists())
                .andExpect(jsonPath("$.data.items[?(@.targetId=='fixture-product-search-hidden')]").doesNotExist());

        mockMvc.perform(get("/api/v1/search")
                        .queryParam("keyword", "草莓")
                        .queryParam("type", "products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].resultType", equalTo("PRODUCT")))
                .andExpect(jsonPath("$.data.items[0].targetId", equalTo("fixture-product-search-strawberry")));

        mockMvc.perform(get("/api/v1/search")
                .queryParam("keyword", "")
                .queryParam("type", "all"))
        .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.data.total", equalTo(0)));
    }

    private JsonNode postJson(String path, String body) throws Exception {
        String content = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode postJsonWithToken(String path, String token, String body) throws Exception {
        String content = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode patchJsonWithToken(String path, String token, String body) throws Exception {
        String content = mockMvc.perform(patch(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode getJsonWithToken(String path, String token) throws Exception {
        String content = mockMvc.perform(get(path)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private String ensureSelfOperatedSku(String productId, String skuId, String imageUrl, int stock) {
        ensureProduct(productId, "SELF_OPERATED", null, imageUrl);
        return ensureSku(skuId, productId, 1200, stock);
    }

    private String ensurePlayerSku(String productId, String skuId, String type, String imageUrl) {
        ensureProduct(productId, type, "fixture-seller", imageUrl);
        return ensureSku(skuId, productId, 3800, 1);
    }

    private void ensureProduct(String productId, String type, String sellerId, String imageUrl) {
        Instant now = Instant.now();
        ProductEntity product = productRepository.findById(productId).orElseGet(ProductEntity::new);
        if (product.getId() == null) {
            product.setId(productId);
            product.setCreatedAt(now);
        }
        product.setType(type);
        product.setSellerId(sellerId);
        product.setTitle("fixture product " + productId);
        product.setDescription("fixture product for backend contract tests");
        product.setImageUrl(imageUrl);
        product.setCategoryId("fixture");
        product.setStatus("ON_SALE");
        product.setAuditStatus("PASS");
        product.setUpdatedAt(now);
        productRepository.save(product);
    }

    private void ensureSearchProduct(String productId, String title, String description, String categoryId,
                                     String categoryName, String status, String auditStatus) {
        Instant now = Instant.now();
        ProductEntity product = productRepository.findById(productId).orElseGet(ProductEntity::new);
        if (product.getId() == null) {
            product.setId(productId);
            product.setCreatedAt(now);
        }
        product.setType("SELF_OPERATED");
        product.setSellerId(null);
        product.setTitle(title);
        product.setDescription(description);
        product.setImageUrl(null);
        product.setCategoryId(categoryId);
        product.setCategoryName(categoryName);
        product.setStatus(status);
        product.setAuditStatus(auditStatus);
        product.setUpdatedAt(now);
        productRepository.save(product);
    }

    private String ensureSku(String skuId, String productId, int priceCent, int stock) {
        Instant now = Instant.now();
        SkuEntity sku = skuRepository.findById(skuId).orElseGet(SkuEntity::new);
        if (sku.getId() == null) {
            sku.setId(skuId);
            sku.setCreatedAt(now);
        }
        sku.setProductId(productId);
        sku.setSpecName("fixture spec");
        sku.setPriceCent(priceCent);
        sku.setStock(stock);
        sku.setLockedStock(0);
        sku.setStatus("ON_SALE");
        sku.setUpdatedAt(now);
        skuRepository.save(sku);
        return skuId;
    }

    private String ensureVisiblePost(String postId, String authorId, String topicId, String coverImageUrl) {
        Instant now = Instant.now();
        PostEntity post = postRepository.findById(postId).orElseGet(PostEntity::new);
        if (post.getId() == null) {
            post.setId(postId);
            post.setCreatedAt(now);
        }
        post.setAuthorId(authorId);
        post.setTitle("fixture post " + postId);
        post.setContent("fixture post for backend contract tests");
        post.setMediaFileIds(null);
        post.setCoverImageUrl(coverImageUrl);
        post.setTopicIds(topicId);
        post.setStatus("VISIBLE");
        post.setLikeCount(0);
        post.setFavoriteCount(0);
        post.setCommentCount(0);
        post.setPinned(false);
        post.setUpdatedAt(now);
        postRepository.save(post);
        return postId;
    }

    private String ensureTopic(String topicId, String keyword) {
        Instant now = Instant.now();
        TopicEntity topic = topicRepository.findById(topicId).orElseGet(TopicEntity::new);
        if (topic.getId() == null) {
            topic.setId(topicId);
            topic.setCreatedAt(now);
        }
        topic.setName(keyword);
        topic.setDescription(keyword + " fixture topic");
        topic.setPostCount(1);
        topic.setUpdatedAt(now);
        topicRepository.save(topic);
        return topicId;
    }

    private String ensureSticker(String packId, String stickerId) {
        Instant now = Instant.now();
        if (stickerPackRepository.findById(packId).isEmpty()) {
            stickerPackRepository.save(new StickerPackEntity(packId, "fixture stickers", 1, now, now));
        }
        if (stickerRepository.findById(stickerId).isEmpty()) {
            stickerRepository.save(new StickerEntity(stickerId, packId, "fixture sticker", "OK", null, 1, now, now));
        }
        return stickerId;
    }

    private String forbiddenAssetRouteSegment() {
        return "/" + "se" + "ed" + "/";
    }

    private JsonNode postJsonWithPath(String pathTemplate, String token, String body, String pathVar) throws Exception {
        String path = pathTemplate.replaceFirst("\\{[^}]+}", pathVar);
        String content = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode postJsonWithIdempotency(String path, String token, String key, String body) throws Exception {
        String content = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }
}
