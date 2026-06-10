package cn.edu.app.douyu.server.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.edu.app.douyu.server.upload.oss.OssProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "douyu.oss.provider=local",
        "douyu.storage.local-path=./target/doyu-upload-idempotency-test-storage",
        "douyu.storage.base-url=http://localhost"
})
class UploadConfirmIdempotencyTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void duplicateConfirmWithSameMetadataReturnsExistingAsset() throws Exception {
        String token = registerToken();
        String fileKey = uniquePostImageKey();
        String payload = confirmPayload(fileKey, 32, 120, 120);

        String firstResponse = mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String fileId = objectMapper.readTree(firstResponse).at("/data/fileId").asText();

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId", equalTo(fileId)))
                .andExpect(jsonPath("$.data.fileKey", equalTo(fileKey)))
                .andExpect(jsonPath("$.data.sizeBytes", equalTo(32)));
    }

    @Test
    void duplicateConfirmWithDifferentMetadataIsRejected() throws Exception {
        String token = registerToken();
        String fileKey = uniquePostImageKey();

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmPayload(fileKey, 32, 120, 120)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmPayload(fileKey, 48, 320, 240)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));
    }

    private String registerToken() throws Exception {
        String email = "upload-idempotency-" + UUID.randomUUID() + "@example.com";
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"upload tester"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.at("/data/accessToken").asText();
    }

    private static String uniquePostImageKey() {
        return "assets/post_image/" + UUID.randomUUID() + "/image.png";
    }

    private static String confirmPayload(String fileKey, int sizeBytes, int width, int height) {
        return """
                {"fileKey":"%s","usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":%d,"width":%d,"height":%d}
                """.formatted(fileKey, sizeBytes, width, height);
    }

    @TestConfiguration
    static class FakeOssProviderConfig {
        @Bean
        @Primary
        OssProvider fakeOssProvider() {
            return new OssProvider() {
                @Override
                public PresignResult presign(String fileKey, String mimeType, long expiresInSeconds) {
                    return new PresignResult("https://oss.example.test/" + fileKey, Map.of());
                }

                @Override
                public ConfirmResult confirm(String fileKey, long expectedSizeBytes) {
                    return new ConfirmResult("https://cdn.example.test/" + fileKey, expectedSizeBytes);
                }

                @Override
                public String getPublicUrl(String fileKey) {
                    return "https://cdn.example.test/" + fileKey;
                }
            };
        }
    }
}
