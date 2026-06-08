package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.SkuEntity;
import cn.edu.app.douyu.server.common.entity.SkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:douyu_commerce_real_products_contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.data.redis.repositories.enabled=false",
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommerceRealProductsContractTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SkuRepository skuRepository;

    @BeforeEach
    void clearCommerceFixtures() {
        skuRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void publicProductsFilterByCategoryAndExposeMasonryFields() throws Exception {
        upsertProduct("prod_beads_red", "beads", "豆子", "SELF_OPERATED",
                "https://oss.example.test/commerce/red-beads.jpg", 1200, 900,
                "ON_SALE", "PASS", 1800, 42);
        upsertProduct("prod_tools_tweezer", "tools", "工具", "SELF_OPERATED",
                "https://oss.example.test/commerce/tweezer.jpg", 800, 1200,
                "ON_SALE", "PASS", 2600, 9);
        upsertProduct("prod_hidden_review", "beads", "豆子", "SELF_OPERATED",
                "https://oss.example.test/commerce/hidden.jpg", 640, 640,
                "DRAFT", "NEED_MANUAL_REVIEW", 1000, 1);

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("page", "1")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.total", equalTo(2)))
                .andExpect(jsonPath("$.data.items[0].imageWidth").isNumber())
                .andExpect(jsonPath("$.data.items[0].imageHeight").isNumber())
                .andExpect(jsonPath("$.data.items[0].priceCents").isNumber())
                .andExpect(jsonPath("$.data.items[0].stock").isNumber());

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("page", "1")
                        .queryParam("size", "20")
                        .queryParam("categoryId", "beads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productId", equalTo("prod_beads_red")))
                .andExpect(jsonPath("$.data.items[0].categoryId", equalTo("beads")))
                .andExpect(jsonPath("$.data.items[0].categoryName", equalTo("豆子")))
                .andExpect(jsonPath("$.data.items[0].imageWidth", equalTo(1200)))
                .andExpect(jsonPath("$.data.items[0].imageHeight", equalTo(900)))
                .andExpect(jsonPath("$.data.items[0].stock", equalTo(42)));
    }

    @Test
    void productCategoriesReturnVisibleProductCountsOnly() throws Exception {
        upsertProduct("prod_beads_blue", "beads", "豆子", "SELF_OPERATED",
                "https://oss.example.test/commerce/blue-beads.jpg", 900, 900,
                "ON_SALE", "PASS", 1900, 20);
        upsertProduct("prod_boards_clear", "boards", "板子", "SELF_OPERATED",
                "https://oss.example.test/commerce/clear-board.jpg", 1000, 700,
                "ON_SALE", "PASS", 2200, 8);
        upsertProduct("prod_boards_hidden", "boards", "板子", "SELF_OPERATED",
                "https://oss.example.test/commerce/hidden-board.jpg", 1000, 700,
                "ON_SALE", "NEED_MANUAL_REVIEW", 2200, 8);

        mockMvc.perform(get("/api/v1/product-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].categoryId", equalTo("beads")))
                .andExpect(jsonPath("$.data.items[0].name", equalTo("豆子")))
                .andExpect(jsonPath("$.data.items[0].productCount", equalTo(1)))
                .andExpect(jsonPath("$.data.items[1].categoryId", equalTo("boards")))
                .andExpect(jsonPath("$.data.items[1].name", equalTo("板子")))
                .andExpect(jsonPath("$.data.items[1].productCount", equalTo(1)));
    }

    private void upsertProduct(String productId, String categoryId, String categoryName, String type,
                               String imageUrl, int imageWidth, int imageHeight,
                               String status, String auditStatus, int priceCent, int stock) {
        Instant now = Instant.now();
        ProductEntity product = productRepository.findById(productId).orElseGet(ProductEntity::new);
        if (product.getId() == null) {
            product.setId(productId);
            product.setCreatedAt(now);
        }
        product.setType(type);
        product.setSellerId(null);
        product.setTitle("fixture " + productId);
        product.setDescription("fixture commerce product");
        product.setImageUrl(imageUrl);
        product.setImageWidth(imageWidth);
        product.setImageHeight(imageHeight);
        product.setCategoryId(categoryId);
        product.setCategoryName(categoryName);
        product.setStatus(status);
        product.setAuditStatus(auditStatus);
        product.setUpdatedAt(now);
        productRepository.save(product);

        SkuEntity sku = skuRepository.findById(productId + "_sku").orElseGet(SkuEntity::new);
        if (sku.getId() == null) {
            sku.setId(productId + "_sku");
            sku.setCreatedAt(now);
        }
        sku.setProductId(productId);
        sku.setSpecName("默认规格");
        sku.setPriceCent(priceCent);
        sku.setStock(stock);
        sku.setLockedStock(0);
        sku.setStatus("ON_SALE");
        sku.setUpdatedAt(now);
        skuRepository.save(sku);
    }
}
