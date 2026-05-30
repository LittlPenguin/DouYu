package cn.edu.app.douyu.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
        org.assertj.core.api.Assertions.assertThat(confirmData.hasNonNull("auditStatus")).isTrue();

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

        JsonNode cartAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"sku_bead_red","quantity":2}
                """);
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
                {"skuId":"sku_bead_red","quantity":999999}
                """);
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

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuId":"sku_player_second_hand_kit","quantity":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("CONFLICT")));

        JsonNode selfAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"sku_bead_white","quantity":1}
                """);
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

        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].commentId", equalTo(commentId)));

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
        org.assertj.core.api.Assertions.assertThat(comment.at("/data/status").asText()).isEqualTo("REVIEWING");
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
                .andExpect(jsonPath("$.data.liked", equalTo(true)));

        mockMvc.perform(post("/api/v1/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked", equalTo(true)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)));

        mockMvc.perform(post("/api/v1/posts/{postId}/favorite", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited", equalTo(true)));

        mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount", equalTo(1)))
                .andExpect(jsonPath("$.data.favoriteCount", equalTo(1)));
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
    void productQaSeedDataCoversSelfOperatedPlayerSecondHandAndCustomService() throws Exception {
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
    void seedAssetUrlsAreReturnedByProductPostAndCartApis() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", "prod_bead_red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl", equalTo("http://localhost:8081/seed/commerce/bead-red.jpg")));

        mockMvc.perform(get("/api/v1/posts/{postId}", "post_seed_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverImageUrl", equalTo("http://localhost:8081/seed/community/newbie-guide.jpg")));

        String token = login("13800000023", "AGE_18_PLUS");
        postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"sku_bead_red","quantity":1}
                """);

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].product.imageUrl", equalTo("http://localhost:8081/seed/commerce/bead-red.jpg")));

        mockMvc.perform(get("/seed/commerce/bead-red.jpg"))
                .andExpect(status().isOk());

        String productsContent = mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode products = objectMapper.readTree(productsContent).at("/data/items");
        org.assertj.core.api.Assertions.assertThat(products).hasSize(6);
        org.assertj.core.api.Assertions.assertThat(products)
                .allSatisfy(product -> org.assertj.core.api.Assertions.assertThat(product.path("imageUrl").asText()).startsWith("http://localhost:8081/seed/commerce/"));

        String feedContent = mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode posts = objectMapper.readTree(feedContent).at("/data/items");
        org.assertj.core.api.Assertions.assertThat(posts).hasSize(4);
        org.assertj.core.api.Assertions.assertThat(posts)
                .allSatisfy(post -> org.assertj.core.api.Assertions.assertThat(post.path("coverImageUrl").asText()).startsWith("http://localhost:8081/seed/community/"));
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
        JsonNode presign = postJsonWithToken("/api/v1/uploads/presign", token, """
                {"usage":"%s","mimeType":"image/png","sizeBytes":1024,"fileName":"file.png"}
                """.formatted(usage));
        JsonNode confirmed = postJsonWithToken("/api/v1/uploads/confirm", token, """
                {"fileKey":"%s","usage":"%s","mimeType":"image/png","sizeBytes":1024,"width":64,"height":64}
                """.formatted(presign.at("/data/fileKey").asText(), usage));
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
