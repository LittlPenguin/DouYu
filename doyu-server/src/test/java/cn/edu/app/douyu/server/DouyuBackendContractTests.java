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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DouyuBackendContractTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    ConversationRepository conversationRepository;

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
    void openApiDocsExposeApiV1EndpointsAndUploadPatternContractFields() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        String docs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth", notNullValue()))
                .andExpect(jsonPath("$.info.description", org.hamcrest.Matchers.containsString("Stub Provider")))
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

        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/auth/sms-code")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/uploads/presign")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/uploads/confirm")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/patterns/jobs")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/payments/callbacks/wechat")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/admin/auth/login")).isTrue();
        org.assertj.core.api.Assertions.assertThat(documentedApiPaths).containsAll(mappedApiPaths);

        String token = login("13800000000", "AGE_18_PLUS");
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"AI_INPUT","mimeType":"image/png","sizeBytes":2048,"fileName":"input.png"}
                """);
        JsonNode presignData = presign.path("data");
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("fileKey")).isTrue();
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("uploadUrl")).isTrue();
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("headers")).isTrue();
        org.assertj.core.api.Assertions.assertThat(presignData.hasNonNull("expiresIn")).isTrue();

        JsonNode confirmed = postJsonWithToken("/api/v1/uploads/confirm", token, """
                {"fileKey":"%s","usage":"AI_INPUT","mimeType":"image/png","sizeBytes":2048,"width":120,"height":120}
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

        JsonNode created = postJsonWithToken("/api/v1/patterns/jobs", token, """
                {"inputFileId":"%s","beadSize":"MM_2_6","targetSize":"16x16","difficulty":"BEGINNER","paletteId":"default","style":"CUTE"}
                """.formatted(confirmData.path("fileId").asText()));
        org.assertj.core.api.Assertions.assertThat(created.at("/data/inputFileId").asText()).isEqualTo(confirmData.path("fileId").asText());
    }

    @Test
    void smsLoginRefreshAndLogoutUseUnifiedResponseAndRevokeRefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"13800000001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.traceId", notNullValue()));

        JsonNode login = postJson("/api/v1/auth/login/sms", """
                {"phone":"13800000001","code":"123456","ageGroup":"AGE_16_17","nickname":"测试用户"}
                """);
        String accessToken = login.at("/data/accessToken").asText();
        String refreshToken = login.at("/data/refreshToken").asText();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", notNullValue()))
                .andExpect(jsonPath("$.data.isMinor", equalTo(true)));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED")));
    }

    @Test
    void uploadPresignAndConfirmCreateFileAssetAndModerationRecord() throws Exception {
        String token = login("13800000002", "AGE_18_PLUS");
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"AI_INPUT","mimeType":"image/png","sizeBytes":2048,"fileName":"input.png"}
                """);

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileKey":"%s","usage":"AI_INPUT","mimeType":"image/png","sizeBytes":2048,"width":120,"height":120}
                                """.formatted(presign.at("/data/fileKey").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId", notNullValue()))
                .andExpect(jsonPath("$.data.auditStatus", equalTo("NEED_MANUAL_REVIEW")));
    }

    @Test
    void patternJobMovesFromPendingToSucceededAndCreatesPatternAsset() throws Exception {
        String token = login("13800000003", "AGE_18_PLUS");
        String fileId = confirmedFile(token, "AI_INPUT");

        JsonNode created = postJsonWithToken("/api/v1/patterns/jobs", token, """
                {"inputFileId":"%s","beadSize":"MM_2_6","targetSize":"16x16","difficulty":"BEGINNER","paletteId":"default","style":"CUTE"}
                """.formatted(fileId));
        String jobId = created.at("/data/jobId").asText();

        // Poll for async completion
        String status = "PENDING";
        for (int i = 0; i < 50; i++) {
            JsonNode polled = getJsonWithToken("/api/v1/patterns/jobs/" + jobId, token);
            status = polled.at("/data/status").asText();
            if ("SUCCEEDED".equals(status) || "FAILED".equals(status)) break;
            Thread.sleep(100);
        }
        org.assertj.core.api.Assertions.assertThat(status).isEqualTo("SUCCEEDED");

        mockMvc.perform(get("/api/v1/patterns/jobs/{jobId}", jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCEEDED")))
                .andExpect(jsonPath("$.data.patternId", notNullValue()))
                .andExpect(jsonPath("$.data.progress", equalTo(1.0)))
                .andExpect(jsonPath("$.data.patternAsset.materials.totalBeads", equalTo(256)));
    }

    @Test
    void orderPaymentAndRefundAreIdempotentAndGuardInventoryAndRefundAmount() throws Exception {
        String token = login("13800000004", "AGE_18_PLUS");
        String skuId = ensureSelfOperatedSku("fixture-product-order-red", "fixture-sku-order-red",
                "https://fixture.local/assets/order-red.png", 100);

        JsonNode cartAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":2}
                """.formatted(skuId));
        String cartItemId = cartAdd.at("/data/itemId").asText();

        String orderPayload = """
                {"itemIds":["%s"],"addressId":"addr_test_1"}
                """.formatted(cartItemId);
        JsonNode first = postJsonWithIdempotency("/api/v1/orders", token, "order-key-1", orderPayload);
        JsonNode second = postJsonWithIdempotency("/api/v1/orders", token, "order-key-1", orderPayload);
        String orderId = first.at("/data/orderId").asText();

        org.assertj.core.api.Assertions.assertThat(second.at("/data/orderId").asText()).isEqualTo(orderId);
        org.assertj.core.api.Assertions.assertThat(first.at("/data/addressSnapshot").isObject()).isTrue();
        org.assertj.core.api.Assertions.assertThat(first.at("/data/status").asText()).isEqualTo("WAITING_PAYMENT");

        // Add another cart item with huge quantity for inventory test
        JsonNode cartAdd2 = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":999999}
                """.formatted(skuId));
        String cartItemId2 = cartAdd2.at("/data/itemId").asText();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "order-key-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itemIds":["%s"],"addressId":"addr_test_1"}
                                """.formatted(cartItemId2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("INVENTORY_NOT_ENOUGH")));

        JsonNode payment = postJsonWithIdempotency("/api/v1/payments", token, "pay-key-1", """
                {"orderId":"%s","channel":"WECHAT_APP"}
                """.formatted(orderId));
        String paymentId = payment.at("/data/paymentId").asText();
        org.assertj.core.api.Assertions.assertThat(payment.at("/data/orderId").asText()).isEqualTo(orderId);
        org.assertj.core.api.Assertions.assertThat(payment.at("/data/status").asText()).isEqualTo("CREATED");
        org.assertj.core.api.Assertions.assertThat(payment.at("/data/amountCent").asInt()).isEqualTo(first.at("/data/payableAmountCent").asInt());

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId", equalTo(paymentId)))
                .andExpect(jsonPath("$.data.status", equalTo("CREATED")))
                .andExpect(jsonPath("$.data.amountCent", equalTo(first.at("/data/payableAmountCent").asInt())));

        mockMvc.perform(post("/api/v1/payments/callbacks/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentId":"%s","channelTradeNo":"wx_trade_001","paid":true}
                                """.formatted(paymentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCEEDED")));

        mockMvc.perform(post("/api/v1/payments/callbacks/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentId":"%s","channelTradeNo":"wx_trade_001","paid":true}
                                """.formatted(paymentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCEEDED")));

        mockMvc.perform(post("/api/v1/refunds")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "refund-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","paymentId":"%s","amountCent":999999,"reason":"测试超额退款"}
                                """.formatted(orderId, paymentId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("CONFLICT")));
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
                                {"itemIds":["missing_player_cart_item","%s"],"addressId":"addr_test_1"}
                                """.formatted(cartItemId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));
    }

    @Test
    void adminApisRequireAdminTokenAndAdminProcessingWritesOperationLog() throws Exception {
        String userToken = login("13800000005", "AGE_18_PLUS");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));

        JsonNode adminLogin = postJson("/api/v1/admin/auth/login", """
                {"username":"admin","password":"admin123"}
                """);
        String adminToken = adminLogin.at("/data/accessToken").asText();

        JsonNode report = postJsonWithToken("/api/v1/reports", userToken, """
                {"targetType":"POST","targetId":"post_missing","reason":"SPAM","description":"测试举报"}
                """);
        String reportId = report.at("/data/reportId").asText();

        mockMvc.perform(post("/api/v1/admin/reports/{reportId}/process", reportId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"RESOLVED","reason":"已处理"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("RESOLVED")));

        mockMvc.perform(get("/api/v1/admin/operation-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0]", hasKey("targetId")));
    }

    @Test
    void communityPostLikeFavoriteAndCommentFlow() throws Exception {
        String token = login("13800000010", "AGE_18_PLUS");

        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"测试帖子","content":"拼豆社区测试内容"}
                """);
        String postId = created.at("/data/postId").asText();
        org.assertj.core.api.Assertions.assertThat(postId).isNotEmpty();

        mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.status", equalTo("REVIEWING")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andExpect(jsonPath("$.data.author.userId", notNullValue()));

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(true)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)));

        JsonNode comment = postJsonWithPath("/api/v1/posts/{postId}/comments", token, """
                {"content":"好可爱的拼豆！"}
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
    void approvedCommunityPostAppearsInFeedWithTopicNamesAndCoverDimensions() throws Exception {
        String token = login("13800000061", "AGE_18_PLUS");
        String topicId = ensureTopic("fixture-topic-cover-dimensions", "cover dimensions");
        String fileId = confirmedFile(token, "POST_IMAGE", 480, 720);

        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"ratio cover post","content":"post with real cover dimensions","mediaFileIds":["%s"],"topicIds":["%s"]}
                """.formatted(fileId, topicId));
        String postId = created.at("/data/postId").asText();

        JsonNode adminLogin = postJson("/api/v1/admin/auth/login", """
                {"username":"admin","password":"admin123"}
                """);
        String adminToken = adminLogin.at("/data/accessToken").asText();
        mockMvc.perform(post("/api/v1/admin/posts/{postId}/audit", postId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approved":true,"reason":"fixture approved"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("VISIBLE")));

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("VISIBLE")))
                .andExpect(jsonPath("$.data.coverImageUrl", org.hamcrest.Matchers.containsString("/stub/post_image/")))
                .andExpect(jsonPath("$.data.coverWidth", equalTo(480)))
                .andExpect(jsonPath("$.data.coverHeight", equalTo(720)))
                .andExpect(jsonPath("$.data.topicNames[0]", equalTo("cover dimensions")));

        mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.items[0].coverWidth", equalTo(480)))
                .andExpect(jsonPath("$.data.items[0].coverHeight", equalTo(720)));

        mockMvc.perform(get("/api/v1/topics/{topicId}/posts", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].postId", equalTo(postId)))
                .andExpect(jsonPath("$.data.items[0].topicNames[0]", equalTo("cover dimensions")));
    }

    @Test
    void loginAndCommunitySampleContractFieldsStayAligned() throws Exception {
        JsonNode login = postJson("/api/v1/auth/login/sms", """
                {"phone":"13800000016","code":"123456","ageGroup":"AGE_18_PLUS","nickname":"contract-user"}
                """);
        org.assertj.core.api.Assertions.assertThat(login.at("/data/accessToken").asText()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/refreshToken").asText()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/expiresIn").asLong()).isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(login.at("/data/user/avatarUrl").isMissingNode()).isFalse();
        org.assertj.core.api.Assertions.assertThat(login.at("/data/user/avatarFileId").isMissingNode()).isTrue();
        org.assertj.core.api.Assertions.assertThat(login.path("traceId").asText()).isNotBlank();

        String token = login.at("/data/accessToken").asText();
        JsonNode created = postJsonWithToken("/api/v1/posts", token, """
                {"title":"contract post","content":"community contract content","mediaFileIds":[],"topicIds":[]}
                """);
        JsonNode post = created.path("data");
        org.assertj.core.api.Assertions.assertThat(post.path("postId").asText()).startsWith("post_");
        org.assertj.core.api.Assertions.assertThat(post.path("status").asText()).isEqualTo("REVIEWING");
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
    void rewardCheckinAndStatusWork() throws Exception {
        String token = login("13800000013", "AGE_18_PLUS");

        mockMvc.perform(post("/api/v1/checkins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkedToday", equalTo(true)))
                .andExpect(jsonPath("$.data.alreadyChecked", equalTo(false)))
                .andExpect(jsonPath("$.data.streakDays", equalTo(1)))
                .andExpect(jsonPath("$.data.rewardPoints", equalTo(5)))
                .andExpect(jsonPath("$.data.points", equalTo(5)));

        mockMvc.perform(post("/api/v1/checkins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alreadyChecked", equalTo(true)))
                .andExpect(jsonPath("$.data.streakDays", equalTo(1)))
                .andExpect(jsonPath("$.data.rewardPoints", equalTo(0)));

        mockMvc.perform(get("/api/v1/checkins/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkedToday", equalTo(true)));

        mockMvc.perform(get("/api/v1/rewards/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.points", equalTo(5)))
                .andExpect(jsonPath("$.data.levelCode", equalTo("LV1")));

        mockMvc.perform(get("/api/v1/badges/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].badgeId", equalTo("badge_newbie")));
    }

    @Test
    void messageNotificationAndConversationFlow() throws Exception {
        String token = login("13800000014", "AGE_18_PLUS");

        mockMvc.perform(get("/api/v1/messages/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());

        mockMvc.perform(post("/api/v1/messages/notifications/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read", equalTo(true)));

        mockMvc.perform(get("/api/v1/messages/conversations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void nonMutualConversationAllowsThreeMessagesThenRequiresMutualFollow() throws Exception {
        String tokenA = login("13800000024", "AGE_18_PLUS");
        String tokenB = login("13800000025", "AGE_18_PLUS");
        String userAId = getJsonWithToken("/api/v1/users/me", tokenA).at("/data/userId").asText();
        String userBId = getJsonWithToken("/api/v1/users/me", tokenB).at("/data/userId").asText();

        ConversationEntity conversation = new ConversationEntity();
        conversation.setId("conv_contract_non_mutual_limit");
        conversation.setUserAId(userAId);
        conversation.setUserBId(userBId);
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/messages/conversations/{conversationId}", conversation.getId())
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content":"hello %d"}
                                    """.formatted(i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.mine", equalTo(true)))
                    .andExpect(jsonPath("$.data.senderId", equalTo(userAId)));
        }

        mockMvc.perform(post("/api/v1/messages/conversations/{conversationId}", conversation.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"blocked"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED")));

        mockMvc.perform(post("/api/v1/users/{userId}/follow", userBId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followedByMe", equalTo(true)));
        mockMvc.perform(post("/api/v1/users/{userId}/follow", userAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mutualFollow", equalTo(true)));

        mockMvc.perform(post("/api/v1/messages/conversations/{conversationId}", conversation.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"mutual follow message"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", equalTo("mutual follow message")));

        mockMvc.perform(get("/api/v1/messages/conversations/{conversationId}", conversation.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversation.mutualFollow", equalTo(true)))
                .andExpect(jsonPath("$.data.conversation.canSend", equalTo(true)))
                .andExpect(jsonPath("$.data.conversation.lastMessage", equalTo("mutual follow message")))
                .andExpect(jsonPath("$.data.messages[0].content", equalTo("hello 1")))
                .andExpect(jsonPath("$.data.messages[3].content", equalTo("mutual follow message")));
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
                                {"usage":"AI_INPUT","mimeType":"image/png","sizeBytes":999999999,"fileName":"huge.png"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"INVALID","targetId":"x","reason":"SPAM"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        // Malformed JSON body → 400 INVALID_ARGUMENT
        mockMvc.perform(post("/api/v1/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        // Missing required field (blank phone) → 400 INVALID_ARGUMENT
        mockMvc.perform(post("/api/v1/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        // Nonexistent order → 404 NOT_FOUND
        mockMvc.perform(get("/api/v1/orders/nonexistent_order")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("NOT_FOUND")));

        // Nonexistent comment → 404 NOT_FOUND
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
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/checkins")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/messages/notifications")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/messages/conversations")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/users/{userId}/follow")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/reports")).isTrue();
        org.assertj.core.api.Assertions.assertThat(paths.has("/api/v1/refunds")).isTrue();
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
                .andExpect(jsonPath("$.data.mediaAssets[0].publicUrl", org.hamcrest.Matchers.containsString("/stub/post_image/")));

        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"图文评论","mediaFileIds":["%s"]}
                                """.formatted(imageFileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", equalTo("图文评论")))
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
                                {"content":"太多图片","mediaFileIds":["1","2","3","4","5","6","7","8","9","10"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        String otherImageFileId = confirmedFile(otherToken, "POST_IMAGE");
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"不能使用别人的图","mediaFileIds":["%s"]}
                                """.formatted(otherImageFileId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));

        String aiFileId = confirmedFile(token, "AI_INPUT");
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"用途不对","mediaFileIds":["%s"]}
                                """.formatted(aiFileId)))
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

        mockMvc.perform(get("/api/v1/messages/notifications")
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
    void minorOrUnverifiedUsersCannotPublishPlayerTradeProducts() throws Exception {
        String minorToken = login("13800000006", "AGE_16_17");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + minorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"PLAYER_CUSTOM_SERVICE","title":"接头像定制","description":"测试","sku":{"specName":"默认","priceCent":5000,"stock":1}}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));

        String adultToken = login("13800000007", "AGE_18_PLUS");
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adultToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"PLAYER_SECOND_HAND","title":"二手豆子","description":"测试","sku":{"specName":"默认","priceCent":1000,"stock":1}}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", equalTo("FORBIDDEN")));
    }

    private String login(String phone, String ageGroup) throws Exception {
        mockMvc.perform(post("/api/v1/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk());
        JsonNode login = postJson("/api/v1/auth/login/sms", """
                {"phone":"%s","code":"123456","ageGroup":"%s","nickname":"测试用户"}
                """.formatted(phone, ageGroup));
        return login.at("/data/accessToken").asText();
    }

    private String confirmedFile(String token, String usage) throws Exception {
        return confirmedFile(token, usage, 64, 64);
    }

    private String confirmedFile(String token, String usage, int width, int height) throws Exception {
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"%s","mimeType":"image/png","sizeBytes":1024,"fileName":"file.png"}
                """.formatted(usage));
        JsonNode confirmed = postJsonWithToken("/api/v1/uploads/confirm", token, """
                {"fileKey":"%s","usage":"%s","mimeType":"image/png","sizeBytes":1024,"width":%d,"height":%d}
                """.formatted(presign.at("/data/fileKey").asText(), usage, width, height));
        return confirmed.at("/data/fileId").asText();
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
        post.setLinkedPatternId(null);
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
