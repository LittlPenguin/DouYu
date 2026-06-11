package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.entity.NotificationRepository;
import cn.edu.app.douyu.server.common.entity.PostRepository;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.StickerPackRepository;
import cn.edu.app.douyu.server.common.entity.TopicRepository;
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
    PostRepository postRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    StickerPackRepository stickerPackRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired(required = false)
    Map<String, ApplicationRunner> applicationRunners;

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
    void qaEmptyStartsWithOnlySchemaAndNoContentSeed() throws Exception {
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
    void runtimeDoesNotRegisterAdminSeedOrDemoInitializers() {
        org.assertj.core.api.Assertions.assertThat(applicationRunners == null ? Map.<String, ApplicationRunner>of() : applicationRunners)
                .allSatisfy((name, runner) -> org.assertj.core.api.Assertions.assertThat(runner.getClass().getSimpleName())
                        .doesNotContain("AdminBootstrap")
                        .doesNotContain("DataInitializer")
                        .doesNotContain("Seed")
                        .doesNotContain("Demo"));
    }

    @Test
    void qaEmptyDoesNotExposeMessageFixtureAndRegistersDefaultNotifications() throws Exception {
        org.assertj.core.api.Assertions.assertThat(notificationRepository.count()).isZero();

        String token = login("13900001001");

        mockMvc.perform(post("/api/v1/qa-empty/fixtures/message-thread")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", equalTo(3)))
                .andExpect(jsonPath("$.data.items[0].type", equalTo("SYSTEM")));

        org.assertj.core.api.Assertions.assertThat(notificationRepository.count()).isEqualTo(3);
    }

    private String login(String phone) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"qa-%s@example.com","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"QA Smoke"}
                                """.formatted(phone)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/accessToken").asText();
    }
}
