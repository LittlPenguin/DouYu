package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.entity.ConversationRepository;
import cn.edu.app.douyu.server.common.entity.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:douyu_dev_fixture_contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.data.redis.repositories.enabled=false",
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevMessageFixtureContractTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ConversationRepository conversationRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    void devProfileExposesExplicitMessageFixtureForRealDeviceSmoke() throws Exception {
        org.assertj.core.api.Assertions.assertThat(conversationRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(notificationRepository.count()).isZero();

        String token = login("13900001003");
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
        return objectMapper.readTree(response).at("/data/userId").asText();
    }
}
