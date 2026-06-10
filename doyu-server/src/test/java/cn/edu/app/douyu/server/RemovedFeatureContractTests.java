package cn.edu.app.douyu.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RemovedFeatureContractTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    void backendDoesNotExposeRemovedAiPaymentOrMapContracts() throws Exception {
        String docs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode paths = objectMapper.readTree(docs).path("paths");

        Set<String> mappedApiPaths = new TreeSet<>();
        for (RequestMappingInfo info : requestMappingHandlerMapping.getHandlerMethods().keySet()) {
            for (String pattern : info.getPathPatternsCondition().getPatternValues()) {
                if (pattern.startsWith("/api/v1")) {
                    mappedApiPaths.add(pattern.replaceAll("\\{([^}/]+)}", "{$1}"));
                }
            }
        }

        assertThat(paths.has("/api/v1/patterns/jobs")).isFalse();
        assertThat(paths.has("/api/v1/patterns/jobs/{jobId}")).isFalse();
        assertThat(paths.has("/api/v1/patterns/quota")).isFalse();
        assertThat(paths.has("/api/v1/patterns/{patternId}/favorite")).isFalse();
        assertThat(paths.has("/api/v1/payments")).isFalse();
        assertThat(paths.has("/api/v1/payments/{paymentId}")).isFalse();
        assertThat(paths.has("/api/v1/payments/callbacks/wechat")).isFalse();
        assertThat(paths.has("/api/v1/payments/callbacks/alipay")).isFalse();
        assertThat(paths.has("/api/v1/refunds")).isFalse();
        assertThat(paths.has("/api/v1/uploads/presign")).isTrue();
        assertThat(paths.has("/api/v1/uploads/confirm")).isTrue();
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/patterns"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/payments"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/refunds"));
    }

    @Test
    void backendSourceDoesNotContainRemovedProductionProviderSurfaces() throws IOException {
        assertMissing("src/main/java/cn/edu/app/douyu/server/pattern");
        assertMissing("src/main/java/cn/edu/app/douyu/server/payment");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/PaymentEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/PaymentRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/PatternJobEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/PatternJobRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/PatternAssetEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/PatternAssetRepository.java");

        String config = read("src/main/resources/application.yml");
        assertThat(config).doesNotContain("douyu.ai");
        assertThat(config).contains("douyu:", "oss:", "DOUYU_OSS_PROVIDER");

        String uploadController = read("src/main/java/cn/edu/app/douyu/server/upload/UploadController.java");
        assertThat(uploadController).doesNotContain("AI_INPUT", "PATTERN_OUTPUT");
        assertThat(uploadController).contains("POST_IMAGE", "PRODUCT_IMAGE");

        String security = read("src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java");
        assertThat(security).doesNotContain("/api/v1/payments/callbacks/**");
    }

    private static void assertMissing(String path) {
        assertThat(Files.exists(Path.of(path))).as("Removed artifact must not exist: %s", path).isFalse();
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}
