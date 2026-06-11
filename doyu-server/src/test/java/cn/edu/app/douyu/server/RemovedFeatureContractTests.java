package cn.edu.app.douyu.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
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

    @Autowired
    JdbcTemplate jdbcTemplate;

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
        assertThat(paths.has("/api/v1/auth/refresh")).isFalse();
        assertThat(paths.has("/api/v1/auth/logout")).isFalse();
        assertThat(paths.has("/api/v1/auth/account/cancel")).isFalse();
        assertThat(paths.has("/api/v1/notifications")).isTrue();
        assertThat(paths.has("/api/v1/messages/notifications")).isFalse();
        assertThat(paths.has("/api/v1/messages/conversations")).isFalse();
        assertThat(paths.has("/api/v1/checkins")).isFalse();
        assertThat(paths.has("/api/v1/checkins/status")).isFalse();
        assertThat(paths.has("/api/v1/rewards/me")).isFalse();
        assertThat(paths.has("/api/v1/badges/me")).isFalse();
        assertThat(paths.has("/api/v1/reports")).isFalse();
        assertThat(paths.has("/api/v1/admin/auth/login")).isFalse();
        assertThat(paths.has("/api/v1/admin/users")).isFalse();
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/patterns"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/payments"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/refunds"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/admin"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/reports"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/messages"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/checkins"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/rewards"));
        assertThat(mappedApiPaths).noneMatch(path -> path.startsWith("/api/v1/badges"));
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
        assertMissing("src/main/java/cn/edu/app/douyu/server/admin");
        assertMissing("src/main/java/cn/edu/app/douyu/server/reward");
        assertMissing("src/main/java/cn/edu/app/douyu/server/moderation");
        assertMissing("src/main/java/cn/edu/app/douyu/server/message/MessageController.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/message/QaEmptyMessageFixtureController.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/AdminBootstrapRunner.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/AdminUserEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/AdminUserRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/AdminOperationLogEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/AdminOperationLogRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/RefreshTokenEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/RefreshTokenRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/ConversationEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/ConversationRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/RewardAccountEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/RewardAccountRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/CheckinRecordEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/CheckinRecordRepository.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/ReportEntity.java");
        assertMissing("src/main/java/cn/edu/app/douyu/server/common/entity/ReportRepository.java");

        String config = read("src/main/resources/application.yml");
        assertThat(config).doesNotContain("douyu.ai", "refresh-token-ttl", "admin:");
        assertThat(config).contains("douyu:", "oss:", "DOUYU_OSS_PROVIDER");

        String uploadController = read("src/main/java/cn/edu/app/douyu/server/upload/UploadController.java");
        assertThat(uploadController).doesNotContain("AI_INPUT", "PATTERN_OUTPUT");
        assertThat(uploadController).contains("POST_IMAGE", "PRODUCT_IMAGE");

        String security = read("src/main/java/cn/edu/app/douyu/server/common/SecurityConfig.java");
        assertThat(security).doesNotContain(
                "/api/v1/payments/callbacks/**",
                "/api/v1/auth/refresh",
                "/api/v1/admin/auth/login",
                "/api/v1/admin/**");

        String authService = read("src/main/java/cn/edu/app/douyu/server/auth/AuthService.java");
        String userController = read("src/main/java/cn/edu/app/douyu/server/user/UserController.java");
        String userEntity = read("src/main/java/cn/edu/app/douyu/server/common/entity/UserEntity.java");
        assertThat(authService + userController + userEntity)
                .doesNotContain("allowStrangerMessages", "notifyMessages", "allow_stranger_messages", "notify_messages");
    }

    @Test
    void pruneMigrationRemovesObsoleteTablesAndKeepsRetainedRuntimeTables() {
        assertTablesDoNotExist(
                "refresh_tokens",
                "conversations",
                "messages",
                "reward_accounts",
                "checkin_records",
                "reward_ledgers",
                "reports",
                "moderation_records",
                "admin_users",
                "admin_operation_logs",
                "ai_usage",
                "payments",
                "refunds",
                "pattern_assets",
                "pattern_jobs"
        );

        assertTablesExist(
                "notifications",
                "users",
                "posts",
                "comments",
                "products",
                "skus",
                "cart_items",
                "orders",
                "order_items",
                "file_assets"
        );
    }

    private static void assertMissing(String path) {
        assertThat(Files.exists(Path.of(path))).as("Removed artifact must not exist: %s", path).isFalse();
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    private void assertTablesDoNotExist(String... tableNames) {
        for (String tableName : tableNames) {
            assertThat(tableExists(tableName)).as("Removed table must not exist: %s", tableName).isFalse();
        }
    }

    private void assertTablesExist(String... tableNames) {
        for (String tableName : tableNames) {
            assertThat(tableExists(tableName)).as("Retained table must exist: %s", tableName).isTrue();
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where lower(table_schema) = 'public'
                  and lower(table_name) = ?
                """, Integer.class, tableName.toLowerCase(Locale.ROOT));
        return count != null && count > 0;
    }
}
