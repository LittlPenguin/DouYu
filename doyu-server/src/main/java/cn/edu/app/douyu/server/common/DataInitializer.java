package cn.edu.app.douyu.server.common;

import cn.edu.app.douyu.server.common.entity.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;

    public DataInitializer(UserRepository userRepository, PostRepository postRepository,
                           ProductRepository productRepository, SkuRepository skuRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Instant now = Instant.now();
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
        // Seed products
        if (productRepository.findById("prod_bead_red").isEmpty()) {
            ProductEntity bead = new ProductEntity();
            bead.setId("prod_bead_red"); bead.setType("SELF_OPERATED"); bead.setTitle("2.6mm 豆子豆沙红");
            bead.setDescription("自营常用色拼豆"); bead.setCategoryId("beads"); bead.setStatus("ON_SALE");
            bead.setAuditStatus("PASS"); bead.setCreatedAt(now); bead.setUpdatedAt(now);
            productRepository.save(bead);

            ProductEntity white = new ProductEntity();
            white.setId("prod_bead_white"); white.setType("SELF_OPERATED"); white.setTitle("2.6mm 豆子奶油白");
            white.setDescription("自营常用色拼豆"); white.setCategoryId("beads"); white.setStatus("ON_SALE");
            white.setAuditStatus("PASS"); white.setCreatedAt(now); white.setUpdatedAt(now);
            productRepository.save(white);

            SkuEntity redSku = new SkuEntity();
            redSku.setId("sku_bead_red"); redSku.setProductId("prod_bead_red"); redSku.setSpecName("1000颗");
            redSku.setPriceCent(1200); redSku.setStock(100); redSku.setLockedStock(0); redSku.setStatus("ON_SALE");
            redSku.setCreatedAt(now); redSku.setUpdatedAt(now);
            skuRepository.save(redSku);

            SkuEntity whiteSku = new SkuEntity();
            whiteSku.setId("sku_bead_white"); whiteSku.setProductId("prod_bead_white"); whiteSku.setSpecName("1000颗");
            whiteSku.setPriceCent(1200); whiteSku.setStock(100); whiteSku.setLockedStock(0); whiteSku.setStatus("ON_SALE");
            whiteSku.setCreatedAt(now); whiteSku.setUpdatedAt(now);
            skuRepository.save(whiteSku);
        }
    }
}
