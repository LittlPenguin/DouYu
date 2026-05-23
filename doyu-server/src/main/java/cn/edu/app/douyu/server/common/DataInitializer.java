package cn.edu.app.douyu.server.common;

import cn.edu.app.douyu.server.common.entity.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;
    private final AdminUserRepository adminUserRepository;
    private final DouyuProperties properties;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PostRepository postRepository,
                           ProductRepository productRepository, SkuRepository skuRepository,
                           AdminUserRepository adminUserRepository, DouyuProperties properties,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.adminUserRepository = adminUserRepository;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        Instant now = Instant.now();
        // Seed admin user
        if (adminUserRepository.findByUsername(properties.admin().bootstrapUsername()).isEmpty()) {
            adminUserRepository.save(new AdminUserEntity("admin_bootstrap",
                    properties.admin().bootstrapUsername(),
                    passwordEncoder.encode(properties.admin().bootstrapPassword()),
                    "ACTIVE", now, now));
        }
        // Seed system user
        if (userRepository.findById("system").isEmpty()) {
            userRepository.save(new UserEntity("system", "00000000000", "系统", null, "",
                    "AGE_18_PLUS", false, "VERIFIED", "ACTIVE", now, now));
        }
        // Seed visible post
        if (postRepository.findById("post_seed_1").isEmpty()) {
            postRepository.save(new PostEntity("post_seed_1", "system", "新手拼豆入门", "欢迎来到豆屿拼豆社区",
                    null, null, null, "VISIBLE", 10, 5, 0, true, now, now));
        }
        // Seed products for local QA. Keep these idempotent so existing dev databases
        // receive new QA fixtures without requiring a manual reset.
        seedProduct("prod_bead_red", "SELF_OPERATED", null, "2.6mm 豆子豆沙红",
                "自营常用色拼豆", "beads", "ON_SALE", "PASS", now);
        seedSku("sku_bead_red", "prod_bead_red", "1000颗", 1200, 100, "ON_SALE", now);

        seedProduct("prod_bead_white", "SELF_OPERATED", null, "2.6mm 豆子奶油白",
                "自营常用色拼豆", "beads", "ON_SALE", "PASS", now);
        seedSku("sku_bead_white", "prod_bead_white", "1000颗", 1200, 100, "ON_SALE", now);

        seedProduct("prod_player_second_hand_kit", "PLAYER_SECOND_HAND", "system", "二手拼豆成品套装",
                "玩家寄售的入门成品套装，仅用于本地 QA 验证，不进入标准购物车。", "handmade", "ON_SALE", "PASS", now);
        seedSku("sku_player_second_hand_kit", "prod_player_second_hand_kit", "一套", 3800, 1, "ON_SALE", now);

        seedProduct("prod_player_custom_avatar", "PLAYER_CUSTOM_SERVICE", "system", "宠物头像定制咨询",
                "玩家定制服务样例，仅用于本地 QA 验证；当前阶段只展示咨询边界。", "custom", "ON_SALE", "PASS", now);
        seedSku("sku_player_custom_avatar", "prod_player_custom_avatar", "咨询定金", 0, 1, "ON_SALE", now);
    }

    private void seedProduct(String id, String type, String sellerId, String title, String description,
                             String categoryId, String status, String auditStatus, Instant now) {
        ProductEntity product = productRepository.findById(id).orElseGet(ProductEntity::new);
        if (product.getId() == null) {
            product.setId(id);
            product.setCreatedAt(now);
        }
        product.setType(type);
        product.setSellerId(sellerId);
        product.setTitle(title);
        product.setDescription(description);
        product.setCategoryId(categoryId);
        product.setStatus(status);
        product.setAuditStatus(auditStatus);
        product.setUpdatedAt(now);
        productRepository.save(product);
    }

    private void seedSku(String id, String productId, String specName, int priceCent, int stock,
                         String status, Instant now) {
        SkuEntity sku = skuRepository.findById(id).orElseGet(SkuEntity::new);
        if (sku.getId() == null) {
            sku.setId(id);
            sku.setCreatedAt(now);
        }
        sku.setProductId(productId);
        sku.setSpecName(specName);
        sku.setPriceCent(priceCent);
        sku.setStock(stock);
        sku.setLockedStock(0);
        sku.setStatus(status);
        sku.setUpdatedAt(now);
        skuRepository.save(sku);
    }
}
