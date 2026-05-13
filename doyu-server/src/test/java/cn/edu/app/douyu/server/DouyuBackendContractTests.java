package cn.edu.app.douyu.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
                {"inputFileId":"%s","beadSize":"MM_2_6","targetSize":"SMALL","difficulty":"BEGINNER","paletteId":"default","style":"CUTE"}
                """.formatted(fileId));
        String jobId = created.at("/data/jobId").asText();

        mockMvc.perform(get("/api/v1/patterns/jobs/{jobId}", jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", equalTo("SUCCEEDED")))
                .andExpect(jsonPath("$.data.patternId", notNullValue()))
                .andExpect(jsonPath("$.data.materials.totalBeads", equalTo(256)));
    }

    @Test
    void orderPaymentAndRefundAreIdempotentAndGuardInventoryAndRefundAmount() throws Exception {
        String token = login("13800000004", "AGE_18_PLUS");

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuId":"sku_bead_red","quantity":2}
                                """))
                .andExpect(status().isOk());

        String orderPayload = """
                {"items":[{"skuId":"sku_bead_red","quantity":2}],"address":{"receiver":"张三","phone":"13800000004","detail":"杭州测试地址"}}
                """;
        JsonNode first = postJsonWithIdempotency("/api/v1/orders", token, "order-key-1", orderPayload);
        JsonNode second = postJsonWithIdempotency("/api/v1/orders", token, "order-key-1", orderPayload);
        String orderId = first.at("/data/orderId").asText();

        org.assertj.core.api.Assertions.assertThat(second.at("/data/orderId").asText()).isEqualTo(orderId);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "order-key-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"skuId":"sku_bead_red","quantity":999999}],"address":{"receiver":"张三","phone":"13800000004","detail":"杭州测试地址"}}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("INVENTORY_NOT_ENOUGH")));

        JsonNode payment = postJsonWithIdempotency("/api/v1/payments", token, "pay-key-1", """
                {"orderId":"%s","channel":"WECHAT_APP"}
                """.formatted(orderId));
        String paymentId = payment.at("/data/paymentId").asText();

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
