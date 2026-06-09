package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.SkuRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:douyu_dev_commerce_import_contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.data.redis.repositories.enabled=false",
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevCommerceImportContractTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SkuRepository skuRepository;

    @Test
    void devProfileImportsVisibleCommerceProductExplicitly() throws Exception {
        String token = login("13900002080");

        mockMvc.perform(put("/api/v1/dev/commerce/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId":"commerce_real_beads_001",
                                  "type":"SELF_OPERATED",
                                  "title":"混色拼豆补充包",
                                  "description":"真实 OSS 图片导入的 dev 验收商品",
                                  "imageUrl":"https://oss.example.test/commerce/beads-001.jpg",
                                  "imageWidth":1200,
                                  "imageHeight":900,
                                  "categoryId":"beads",
                                  "categoryName":"豆子",
                                  "sku":{
                                    "skuId":"commerce_real_beads_001_sku",
                                    "specName":"1000 粒装",
                                    "priceCent":1880,
                                    "stock":36
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId", equalTo("commerce_real_beads_001")))
                .andExpect(jsonPath("$.data.status", equalTo("ON_SALE")))
                .andExpect(jsonPath("$.data.auditStatus", equalTo("PASS")))
                .andExpect(jsonPath("$.data.categoryId", equalTo("beads")))
                .andExpect(jsonPath("$.data.imageWidth", equalTo(1200)))
                .andExpect(jsonPath("$.data.imageHeight", equalTo(900)));

        ProductEntity product = productRepository.findById("commerce_real_beads_001").orElseThrow();
        assertThat(product.getStatus()).isEqualTo("ON_SALE");
        assertThat(product.getAuditStatus()).isEqualTo("PASS");
        assertThat(product.getCategoryName()).isEqualTo("豆子");
        assertThat(product.getImageWidth()).isEqualTo(1200);
        assertThat(product.getImageHeight()).isEqualTo(900);
        assertThat(skuRepository.findByProductId("commerce_real_beads_001")).hasSize(1);

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("categoryId", "beads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].productId", equalTo("commerce_real_beads_001")));
    }

    @Test
    void devCommerceImportRequiresLogin() throws Exception {
        mockMvc.perform(put("/api/v1/dev/commerce/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String phone) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"commerce-%s@example.com","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"商城导入"}
                                """.formatted(phone)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/accessToken").asText();
    }
}
