package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.entity.PostRepository;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.StickerPackRepository;
import cn.edu.app.douyu.server.common.entity.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:douyu_default_no_seed_contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DefaultRuntimeNoSeedContractTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    StickerPackRepository stickerPackRepository;

    @Autowired(required = false)
    Map<String, ApplicationRunner> applicationRunners;

    @Test
    void defaultRuntimeDoesNotRegisterAdminSeedOrDemoInitializers() {
        org.assertj.core.api.Assertions.assertThat(applicationRunners == null ? Map.<String, ApplicationRunner>of() : applicationRunners)
                .allSatisfy((name, runner) -> org.assertj.core.api.Assertions.assertThat(runner.getClass().getSimpleName())
                        .doesNotContain("AdminBootstrap")
                        .doesNotContain("DataInitializer")
                        .doesNotContain("Seed")
                        .doesNotContain("Demo"));
    }

    @Test
    void defaultRuntimeStartsWithoutContentSeedRows() throws Exception {
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
    void defaultRuntimeDoesNotExposeQaEmptyFixtureEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/qa-empty/fixtures/message-thread"))
                .andExpect(status().is4xxClientError());
    }
}
