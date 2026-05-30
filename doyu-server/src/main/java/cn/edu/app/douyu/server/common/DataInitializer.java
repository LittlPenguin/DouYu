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
        if (adminUserRepository.findByUsername(properties.admin().bootstrapUsername()).isEmpty()) {
            adminUserRepository.save(new AdminUserEntity("admin_bootstrap",
                    properties.admin().bootstrapUsername(),
                    passwordEncoder.encode(properties.admin().bootstrapPassword()),
                    "ACTIVE", now, now));
        }
        if (userRepository.findById("system").isEmpty()) {
            userRepository.save(new UserEntity("system", "00000000000", "豆屿编辑部", null,
                    "本地联调与内容示例账号", "AGE_18_PLUS", false, "VERIFIED", "ACTIVE", now, now));
        }

        seedPost("post_seed_1", "新手拼豆入门：从第一块小挂件开始",
                "第一次做拼豆建议先选 2.6mm 或 5mm 基础珠，图案控制在 20x20 以内。先按色号分装，再从轮廓开始摆放，最后用离型纸低温熨烫，等完全冷却后再取下。",
                "topic_beginner,topic_tutorial", seedUrl("/seed/community/newbie-guide.jpg"), 42, 18, 6, true, now);
        seedPost("post_seed_color_palette", "低饱和色卡怎么搭配更耐看",
                "头像和小挂件不要一开始就追求高饱和全色。奶油白、雾蓝、薄荷绿、豆沙粉和深灰能覆盖大多数日常主题，阴影色比主色更能决定作品质感。",
                "topic_color,topic_beginner", seedUrl("/seed/community/color-palette.jpg"), 68, 31, 9, true, now);
        seedPost("post_seed_desk_setup", "桌面收纳：把常用色放在手边",
                "做大图时最容易浪费时间的是找色。可以把本周常用色、透明板、镊子和离型纸放成一个小工作区，剩余颜色按色系收纳，复盘时再补货。",
                "topic_tools,topic_workspace", seedUrl("/seed/community/desk-setup.jpg"), 36, 14, 4, false, now);
        seedPost("post_seed_finished_work", "完成一个小作品后的检查清单",
                "熨烫前拍一张照片检查漏珠和错色；熨烫后看边角是否粘牢；如果要送人，建议补一个背板或钥匙扣配件，作品会更耐用。",
                "topic_showcase,topic_tutorial", seedUrl("/seed/community/finished-work.jpg"), 91, 40, 13, false, now);

        seedProduct("prod_bead_red", "SELF_OPERATED", null, "2.6mm 拼豆豆沙红 1000 颗",
                "自营常用暖色拼豆，适合腮红、花瓣、甜点和小挂件细节。", seedUrl("/seed/commerce/bead-red.jpg"),
                "beads", "ON_SALE", "PASS", now);
        seedSku("sku_bead_red", "prod_bead_red", "1000 颗", 1200, 100, "ON_SALE", now);

        seedProduct("prod_bead_white", "SELF_OPERATED", null, "2.6mm 拼豆奶油白 1000 颗",
                "自营基础高频色，适合高光、背景、边框和浅色角色主体。", seedUrl("/seed/commerce/bead-white.jpg"),
                "beads", "ON_SALE", "PASS", now);
        seedSku("sku_bead_white", "prod_bead_white", "1000 颗", 1200, 100, "ON_SALE", now);

        seedProduct("prod_starter_kit", "SELF_OPERATED", null, "新手拼豆材料包",
                "包含基础色拼豆、透明模板、离型纸和入门说明，适合第一次完成 3 到 5 个小作品。", seedUrl("/seed/commerce/starter-kit.jpg"),
                "cat_beginner", "ON_SALE", "PASS", now);
        seedSku("sku_starter_kit", "prod_starter_kit", "2.6mm 入门套装", 6990, 48, "ON_SALE", now);

        seedProduct("prod_tool_tweezers", "SELF_OPERATED", null, "精细拼豆镊子套装",
                "尖头和弯头两支组合，用于细节摆放、错珠修正和小尺寸图案制作。", seedUrl("/seed/commerce/tool-tweezers.jpg"),
                "cat_tools", "ON_SALE", "PASS", now);
        seedSku("sku_tool_tweezers", "prod_tool_tweezers", "两支装", 1990, 64, "ON_SALE", now);

        seedProduct("prod_player_second_hand_kit", "PLAYER_SECOND_HAND", "system", "玩家二手拼豆成品套装",
                "玩家寄售的入门成品套装，仅用于本地 QA 验证；当前不进入标准购物车。", seedUrl("/seed/commerce/player-second-hand-kit.jpg"),
                "handmade", "ON_SALE", "PASS", now);
        seedSku("sku_player_second_hand_kit", "prod_player_second_hand_kit", "一套", 3800, 1, "ON_SALE", now);

        seedProduct("prod_player_custom_avatar", "PLAYER_CUSTOM_SERVICE", "system", "宠物头像定制咨询",
                "玩家定制服务样例，仅用于本地 QA 验证；当前阶段只展示咨询边界，不接标准购物车。", seedUrl("/seed/commerce/player-custom-service.jpg"),
                "custom", "ON_SALE", "PASS", now);
        seedSku("sku_player_custom_avatar", "prod_player_custom_avatar", "咨询定金", 0, 1, "ON_SALE", now);
    }

    private String seedUrl(String path) {
        if (properties.storage() == null || properties.storage().baseUrl() == null || properties.storage().baseUrl().isBlank()) {
            return path;
        }
        String baseUrl = properties.storage().baseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + path;
    }

    private void seedPost(String id, String title, String content, String topicIds, String coverImageUrl,
                          int likeCount, int favoriteCount, int commentCount, boolean pinned, Instant now) {
        PostEntity post = postRepository.findById(id).orElseGet(PostEntity::new);
        if (post.getId() == null) {
            post.setId(id);
            post.setAuthorId("system");
            post.setCreatedAt(now);
        }
        post.setTitle(title);
        post.setContent(content);
        post.setMediaFileIds(null);
        post.setCoverImageUrl(coverImageUrl);
        post.setTopicIds(topicIds);
        post.setLinkedPatternId(null);
        post.setStatus("VISIBLE");
        post.setLikeCount(likeCount);
        post.setFavoriteCount(favoriteCount);
        post.setCommentCount(commentCount);
        post.setPinned(pinned);
        post.setUpdatedAt(now);
        postRepository.save(post);
    }

    private void seedProduct(String id, String type, String sellerId, String title, String description,
                             String imageUrl, String categoryId, String status, String auditStatus, Instant now) {
        ProductEntity product = productRepository.findById(id).orElseGet(ProductEntity::new);
        if (product.getId() == null) {
            product.setId(id);
            product.setCreatedAt(now);
        }
        product.setType(type);
        product.setSellerId(sellerId);
        product.setTitle(title);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
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
