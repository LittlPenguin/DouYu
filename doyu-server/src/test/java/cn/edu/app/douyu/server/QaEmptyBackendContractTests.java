package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.AdminBootstrapRunner;
import cn.edu.app.douyu.server.common.entity.AdminUserRepository;
import cn.edu.app.douyu.server.common.entity.ConversationRepository;
import cn.edu.app.douyu.server.common.entity.NotificationRepository;
import cn.edu.app.douyu.server.common.entity.PostRepository;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.StickerPackRepository;
import cn.edu.app.douyu.server.common.entity.TopicRepository;
import cn.edu.app.douyu.server.pattern.ai.AiVisionProviderRouter;
import cn.edu.app.douyu.server.pattern.ai.StubAiVisionProvider;
import cn.edu.app.douyu.server.payment.PaymentCallbackVerifier;
import cn.edu.app.douyu.server.payment.StubPaymentCallbackVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:douyu_qa_empty_contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.data.redis.repositories.enabled=false",
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("qa-empty")
class QaEmptyBackendContractTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Environment environment;

    @Autowired
    AdminUserRepository adminUserRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    StickerPackRepository stickerPackRepository;

    @Autowired
    ConversationRepository conversationRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    Map<String, ApplicationRunner> applicationRunners;

    @Autowired
    PaymentCallbackVerifier paymentCallbackVerifier;

    @Autowired
    AiVisionProviderRouter aiVisionProviderRouter;

    @Test
    void qaEmptyProfileIsExplicitlyMarkedAsIsolatedEmptyRuntime() {
        org.assertj.core.api.Assertions.assertThat(Arrays.asList(environment.getActiveProfiles()))
                .contains("qa-empty");
        org.assertj.core.api.Assertions.assertThat(environment.getProperty("douyu.qa-empty.enabled", Boolean.class))
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(environment.getProperty("douyu.qa-empty.database-role"))
                .isEqualTo("isolated-empty");
    }

    @Test
    void qaEmptyStartsWithOnlySchemaAndAdminBootstrapNoContentSeed() throws Exception {
        org.assertj.core.api.Assertions.assertThat(adminUserRepository.findByUsername("admin")).isPresent();
        org.assertj.core.api.Assertions.assertThat(postRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(productRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(topicRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(stickerPackRepository.count()).isZero();

        mockMvc.perform(get("/api/v1/posts/feed").queryParam("page", "1").queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.total", equalTo(0)));
        mockMvc.perform(get("/api/v1/products").queryParam("page", "1").queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.total", equalTo(0)));
        mockMvc.perform(get("/api/v1/topics").queryParam("page", "1").queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.total", equalTo(0)));
        mockMvc.perform(get("/api/v1/sticker-packs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.total", equalTo(0)));
    }

    @Test
    void runtimeKeepsAdminBootstrapButDoesNotRegisterSeedOrDemoInitializers() {
        org.assertj.core.api.Assertions.assertThat(applicationRunners.values())
                .anySatisfy(runner -> org.assertj.core.api.Assertions.assertThat(runner)
                        .isInstanceOf(AdminBootstrapRunner.class));
        org.assertj.core.api.Assertions.assertThat(applicationRunners.values())
                .allSatisfy(runner -> org.assertj.core.api.Assertions.assertThat(runner.getClass().getSimpleName())
                        .doesNotContain("DataInitializer")
                        .doesNotContain("Seed")
                        .doesNotContain("Demo"));
    }

    @Test
    void qaEmptyKeepsStubProviderBoundaries() {
        org.assertj.core.api.Assertions.assertThat(paymentCallbackVerifier)
                .isInstanceOf(StubPaymentCallbackVerifier.class);
        org.assertj.core.api.Assertions.assertThat(aiVisionProviderRouter.getProviderNames())
                .contains(StubAiVisionProvider.class.getSimpleName());
    }

    @Test
    void qaEmptyExposesExplicitMessageFixtureWithoutRuntimeSeed() throws Exception {
        org.assertj.core.api.Assertions.assertThat(conversationRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(notificationRepository.count()).isZero();

        String token = login("13900001001");
        String userId = getMeUserId(token);

        mockMvc.perform(post("/api/v1/qa-empty/fixtures/message-thread")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.data.notificationId").isNotEmpty())
                .andExpect(jsonPath("$.data.userAId", equalTo(userId)))
                .andExpect(jsonPath("$.data.notificationTitle", equalTo("验收通知")))
                .andExpect(jsonPath("$.data.notificationBody", equalTo("真实后端通知详情内容")));

        org.assertj.core.api.Assertions.assertThat(conversationRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(notificationRepository.count()).isEqualTo(2);
    }

    private String login(String phone) throws Exception {
        mockMvc.perform(post("/api/v1/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk());
        String response = mockMvc.perform(post("/api/v1/auth/login/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"%s","code":"123456","ageGroup":"AGE_18_PLUS","nickname":"QA Smoke"}
                                """.formatted(phone)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/accessToken").asText();
    }

    private String getMeUserId(String token) throws Exception {
        String response = mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.at("/data/userId").asText();
    }
}
