package cn.edu.app.douyu.server.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import cn.edu.app.douyu.server.upload.oss.LocalOssProvider;
import cn.edu.app.douyu.server.upload.oss.OssProvider;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "douyu.oss.provider=local",
        "douyu.storage.local-path=./target/doyu-upload-contract-test-storage",
        "douyu.storage.base-url=http://localhost"
})
class UploadRealObjectContractTests {
    private static final Path CONTRACT_STORAGE = Path.of("./target/doyu-upload-contract-test-storage");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void confirmWithoutPutIsRejected() throws Exception {
        String token = registerToken();
        JsonNode presign = presign(token, 16);

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmPayload(presign.path("fileKey").asText(), 16)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));
    }

    @Test
    void presignSanitizesClientFileNameBeforeBuildingStorageKey() throws Exception {
        String token = registerToken();
        JsonNode presign = presign(token, 16, "../outside/input.png");
        String fileKey = presign.path("fileKey").asText();

        assertThat(fileKey).doesNotContain("..");
        assertThat(fileKey).doesNotContain("\\");
        assertThat(fileKey).endsWith("/outside-input.png");
    }

    @Test
    void localProviderRejectsUnsafeFileKeyBeforeMovingFiles() throws Exception {
        byte[] imageBytes = "escape-bytes".getBytes(StandardCharsets.UTF_8);
        Path escapedTemp = CONTRACT_STORAGE.resolve("uploads").resolve("escape.png").toAbsolutePath().normalize();
        Path escapedFinal = CONTRACT_STORAGE.resolve("escape.png").toAbsolutePath().normalize();
        Files.deleteIfExists(escapedFinal);
        Files.createDirectories(escapedTemp.getParent());
        Files.write(escapedTemp, imageBytes);
        LocalOssProvider provider = new LocalOssProvider(CONTRACT_STORAGE.toString(), "http://localhost");

        assertThatThrownBy(() -> provider.confirm("../escape.png", imageBytes.length))
                .isInstanceOf(OssProvider.UploadNotCompletedException.class);
        assertThat(Files.exists(escapedTemp)).isTrue();
        assertThat(Files.exists(escapedFinal)).isFalse();
    }

    @Test
    void confirmSizeMismatchIsRejected() throws Exception {
        String token = registerToken();
        JsonNode presign = presign(token, 16);
        String fileKey = presign.path("fileKey").asText();

        mockMvc.perform(put("/uploads/temp/" + fileKey)
                        .contentType("image/png")
                        .content(new byte[]{1}))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmPayload(fileKey, 16)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));
    }

    @Test
    void confirmRejectsUsageThatDoesNotMatchFileKeyPrefix() throws Exception {
        String token = registerToken();
        byte[] imageBytes = "usage-boundary-bytes".getBytes(StandardCharsets.UTF_8);
        JsonNode presign = presign(token, imageBytes.length);
        String fileKey = presign.path("fileKey").asText();

        mockMvc.perform(put("/uploads/temp/" + fileKey)
                        .contentType("image/png")
                        .content(imageBytes))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmPayload(fileKey, "AVATAR", imageBytes.length)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));
    }

    @Test
    void confirmAfterNonEmptyPutCreatesAssetAndServesFile() throws Exception {
        String token = registerToken();
        byte[] imageBytes = "real-image-bytes".getBytes(StandardCharsets.UTF_8);
        JsonNode presign = presign(token, imageBytes.length);
        String fileKey = presign.path("fileKey").asText();

        mockMvc.perform(put("/uploads/temp/" + fileKey)
                        .contentType("image/png")
                        .content(imageBytes))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/uploads/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmPayload(fileKey, imageBytes.length)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId", notNullValue()))
                .andExpect(jsonPath("$.data.sizeBytes", equalTo(imageBytes.length)))
                .andExpect(jsonPath("$.data.publicUrl", equalTo("http://localhost/uploads/" + fileKey)));

        mockMvc.perform(get("/uploads/" + fileKey))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageBytes));
    }

    private JsonNode presign(String token, int sizeBytes) throws Exception {
        return presign(token, sizeBytes, "input.png");
    }

    private JsonNode presign(String token, int sizeBytes, String fileName) throws Exception {
        String response = mockMvc.perform(post("/api/v1/uploads/presign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usage":"POST_IMAGE","mimeType":"image/png","sizeBytes":%d,"fileName":"%s"}
                                """.formatted(sizeBytes, fileName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileKey", notNullValue()))
                .andExpect(jsonPath("$.data.uploadUrl", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private String registerToken() throws Exception {
        String email = "upload-contract-" + UUID.randomUUID() + "@example.com";
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"upload tester"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/accessToken").asText();
    }

    private static String confirmPayload(String fileKey, int sizeBytes) {
        return confirmPayload(fileKey, "POST_IMAGE", sizeBytes);
    }

    private static String confirmPayload(String fileKey, String usage, int sizeBytes) {
        return """
                {"fileKey":"%s","usage":"%s","mimeType":"image/png","sizeBytes":%d,"width":120,"height":120}
                """.formatted(fileKey, usage, sizeBytes);
    }
}
