package cn.edu.app.douyu.server;

import cn.edu.app.douyu.server.common.entity.CartItemRepository;
import cn.edu.app.douyu.server.common.entity.IdempotencyRecordRepository;
import cn.edu.app.douyu.server.common.entity.OrderItemRepository;
import cn.edu.app.douyu.server.common.entity.OrderRepository;
import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.SkuEntity;
import cn.edu.app.douyu.server.common.entity.SkuRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:douyu_commerce_purchase_contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.data.redis.repositories.enabled=false",
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommercePurchaseContractTests {
    private static final AtomicInteger USER_COUNTER = new AtomicInteger();

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SkuRepository skuRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    IdempotencyRecordRepository idempotencyRecordRepository;

    @BeforeEach
    void clearPurchaseFixtures() {
        idempotencyRecordRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        skuRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void cartMergesQuantitiesButRejectsTotalsAboveAvailableStock() throws Exception {
        String token = registerUser();
        String skuId = upsertProductAndSku("prod_cart_stock", "sku_cart_stock", "SELF_OPERATED", 980, 3);

        JsonNode firstAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":2}
                """.formatted(skuId));

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"skuId":"%s","quantity":2}
                                """.formatted(skuId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("INVENTORY_NOT_ENOUGH")));

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].itemId", equalTo(firstAdd.at("/data/itemId").asText())))
                .andExpect(jsonPath("$.data.items[0].quantity", equalTo(2)))
                .andExpect(jsonPath("$.data.items[0].rowAmountCent", equalTo(1960)))
                .andExpect(jsonPath("$.data.items[0].available", equalTo(true)));
    }

    @Test
    void immediateOrderCreatesCreatedOrderWithAddressSnapshotAndDoesNotExposeDetailEndpoint() throws Exception {
        String token = registerUser();
        String skuId = upsertProductAndSku("prod_buy_now", "sku_buy_now", "SELF_OPERATED", 1200, 5);

        JsonNode order = postJsonWithIdempotency("/api/v1/orders", token, "buy-now-key", """
                {
                  "items":[{"skuId":"%s","quantity":2}],
                  "addressSnapshot":{
                    "recipient":"Bean Buyer",
                    "phone":"13800001111",
                    "region":"Hangzhou",
                    "detail":"No. 1 Bean Street"
                  },
                  "remark":"leave at door"
                }
                """.formatted(skuId));
        JsonNode replay = postJsonWithIdempotency("/api/v1/orders", token, "buy-now-key", """
                {
                  "items":[{"skuId":"%s","quantity":2}],
                  "addressSnapshot":{
                    "recipient":"Bean Buyer",
                    "phone":"13800001111",
                    "region":"Hangzhou",
                    "detail":"No. 1 Bean Street"
                  },
                  "remark":"leave at door"
                }
                """.formatted(skuId));

        assertThat(replay.at("/data/orderId").asText()).isEqualTo(order.at("/data/orderId").asText());
        assertThat(order.at("/data/status").asText()).isEqualTo("CREATED");
        assertThat(order.at("/data/totalAmountCent").asInt()).isEqualTo(2400);
        assertThat(order.at("/data/payableAmountCent").asInt()).isEqualTo(2400);
        assertThat(order.at("/data/addressSnapshot/recipient").asText()).isEqualTo("Bean Buyer");
        assertThat(order.at("/data/addressSnapshot/phone").asText()).isEqualTo("13800001111");
        assertThat(order.at("/data/addressSnapshot/region").asText()).isEqualTo("Hangzhou");
        assertThat(order.at("/data/addressSnapshot/detail").asText()).isEqualTo("No. 1 Bean Street");
        assertThat(order.at("/data/items/0/skuId").asText()).isEqualTo(skuId);
        assertThat(order.at("/data/items/0/quantity").asInt()).isEqualTo(2);
        assertThat(order.at("/data/items/0/rowAmountCent").asInt()).isEqualTo(2400);
        assertThat(order.at("/data/payment").isMissingNode()).isTrue();

        mockMvc.perform(get("/api/v1/orders/{orderId}", order.at("/data/orderId").asText())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        assertThat(skuRepository.findById(skuId).orElseThrow().getLockedStock()).isEqualTo(2);
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(orderItemRepository.findAll()).hasSize(1);
    }

    @Test
    void cartCheckoutRemovesCartItemsAndDoesNotExposeCancelEndpoint() throws Exception {
        String token = registerUser();
        String skuId = upsertProductAndSku("prod_cart_checkout", "sku_cart_checkout", "SELF_OPERATED", 1500, 4);
        JsonNode cartAdd = postJsonWithToken("/api/v1/cart/items", token, """
                {"skuId":"%s","quantity":3}
                """.formatted(skuId));

        JsonNode created = postJsonWithIdempotency("/api/v1/orders", token, "cart-checkout-key", """
                {
                  "itemIds":["%s"],
                  "addressSnapshot":{
                    "recipient":"Cart Buyer",
                    "phone":"13800002222",
                    "region":"Shanghai",
                    "detail":"No. 2 Cart Road"
                  }
                }
                """.formatted(cartAdd.at("/data/itemId").asText()));
        String orderId = created.at("/data/orderId").asText();

        assertThat(cartItemRepository.findAll()).isEmpty();
        assertThat(skuRepository.findById(skuId).orElseThrow().getLockedStock()).isEqualTo(3);

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        SkuEntity sku = skuRepository.findById(skuId).orElseThrow();
        assertThat(sku.getStock()).isEqualTo(4);
        assertThat(sku.getLockedStock()).isEqualTo(3);
        mockMvc.perform(get("/api/v1/products/prod_cart_checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock", equalTo(1)))
                .andExpect(jsonPath("$.data.skus[0].stock", equalTo(1)));
    }

    @Test
    void orderRejectsMissingManualAddressAndPlayerProductImmediatePurchase() throws Exception {
        String token = registerUser();
        String selfSkuId = upsertProductAndSku("prod_no_address", "sku_no_address", "SELF_OPERATED", 1000, 2);
        String playerSkuId = upsertProductAndSku("prod_player_buy", "sku_player_buy", "PLAYER_SECOND_HAND", 1000, 2);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "missing-address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"skuId":"%s","quantity":1}]}
                                """.formatted(selfSkuId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("INVALID_ARGUMENT")));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "player-direct-buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items":[{"skuId":"%s","quantity":1}],
                                  "addressSnapshot":{
                                    "recipient":"Buyer",
                                    "phone":"13800003333",
                                    "region":"Hangzhou",
                                    "detail":"No. 3 Player Road"
                                  }
                                }
                                """.formatted(playerSkuId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", equalTo("CONFLICT")));
    }

    private String registerUser() throws Exception {
        int userNo = USER_COUNTER.incrementAndGet();
        JsonNode login = postJson("/api/v1/auth/register", """
                {"email":"commerce-purchase-%d@example.com","password":"password123","confirmPassword":"password123","ageGroup":"AGE_18_PLUS","nickname":"buyer-%d"}
                """.formatted(userNo, userNo));
        return login.at("/data/accessToken").asText();
    }

    private String upsertProductAndSku(String productId, String skuId, String type, int priceCent, int stock) {
        Instant now = Instant.now();
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setType(type);
        product.setSellerId("SELF_OPERATED".equals(type) ? null : "seller_fixture");
        product.setTitle("fixture " + productId);
        product.setDescription("fixture product for purchase contract tests");
        product.setImageUrl("https://oss.example.test/commerce/" + productId + ".jpg");
        product.setImageWidth(800);
        product.setImageHeight(600);
        product.setCategoryId("beads");
        product.setCategoryName("豆子");
        product.setStatus("ON_SALE");
        product.setAuditStatus("PASS");
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        productRepository.save(product);

        SkuEntity sku = new SkuEntity();
        sku.setId(skuId);
        sku.setProductId(productId);
        sku.setSpecName("默认规格");
        sku.setPriceCent(priceCent);
        sku.setStock(stock);
        sku.setLockedStock(0);
        sku.setStatus("ON_SALE");
        sku.setCreatedAt(now);
        sku.setUpdatedAt(now);
        skuRepository.save(sku);
        return skuId;
    }

    private JsonNode postJson(String path, String body) throws Exception {
        String content = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode postJsonWithToken(String path, String token, String body) throws Exception {
        String content = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.itemId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode postJsonWithIdempotency(String path, String token, String key, String body) throws Exception {
        String content = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.orderId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }
}
