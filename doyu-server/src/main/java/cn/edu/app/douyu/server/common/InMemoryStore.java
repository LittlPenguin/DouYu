package cn.edu.app.douyu.server.common;

import cn.edu.app.douyu.server.common.Models.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStore {
    public final Map<String, User> users = new ConcurrentHashMap<>();
    public final Map<String, String> userIdByPhone = new ConcurrentHashMap<>();
    public final Map<String, RefreshTokenRecord> refreshTokensByHash = new ConcurrentHashMap<>();
    public final Map<String, AdminUser> adminByUsername = new ConcurrentHashMap<>();
    public final Map<String, AdminUser> adminById = new ConcurrentHashMap<>();
    public final Set<String> follows = ConcurrentHashMap.newKeySet();
    public final Map<String, FileAsset> files = new ConcurrentHashMap<>();
    public final Map<String, Post> posts = new ConcurrentHashMap<>();
    public final Map<String, Comment> comments = new ConcurrentHashMap<>();
    public final Set<String> likes = ConcurrentHashMap.newKeySet();
    public final Set<String> favorites = ConcurrentHashMap.newKeySet();
    public final Map<String, PatternJob> patternJobs = new ConcurrentHashMap<>();
    public final Map<String, PatternAsset> patternAssets = new ConcurrentHashMap<>();
    public final Map<String, Product> products = new ConcurrentHashMap<>();
    public final Map<String, Sku> skus = new ConcurrentHashMap<>();
    public final Map<String, CartItem> cartItems = new ConcurrentHashMap<>();
    public final Map<String, Order> orders = new ConcurrentHashMap<>();
    public final Map<String, List<OrderItem>> orderItems = new ConcurrentHashMap<>();
    public final Map<String, Payment> payments = new ConcurrentHashMap<>();
    public final Map<String, Refund> refunds = new ConcurrentHashMap<>();
    public final Map<String, Notification> notifications = new ConcurrentHashMap<>();
    public final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    public final Map<String, RewardAccount> rewards = new ConcurrentHashMap<>();
    public final Set<String> checkins = ConcurrentHashMap.newKeySet();
    public final Map<String, Report> reports = new ConcurrentHashMap<>();
    public final Map<String, ModerationRecord> moderationRecords = new ConcurrentHashMap<>();
    public final Map<String, AdminOperationLog> adminLogs = new ConcurrentHashMap<>();
    public final Map<String, String> idempotencyResponses = new ConcurrentHashMap<>();

    public InMemoryStore(DouyuProperties properties, PasswordEncoder passwordEncoder) {
        String adminId = "admin_bootstrap";
        AdminUser admin = new AdminUser(
                adminId,
                properties.admin().bootstrapUsername(),
                passwordEncoder.encode(properties.admin().bootstrapPassword()),
                "ACTIVE"
        );
        adminByUsername.put(admin.username(), admin);
        adminById.put(admin.id(), admin);
        seedProducts();
        seedVisiblePost();
    }

    public Optional<User> userByPhone(String phone) {
        return Optional.ofNullable(userIdByPhone.get(phone)).map(users::get);
    }

    public List<Product> listedProducts() {
        return products.values().stream()
                .filter(product -> !"DELETED".equals(product.status()))
                .sorted(Comparator.comparing(Product::createdAt).reversed())
                .toList();
    }

    public List<Post> visiblePosts() {
        return posts.values().stream()
                .filter(post -> "VISIBLE".equals(post.status()))
                .sorted(Comparator.comparing(Post::pinned).reversed()
                        .thenComparing(Post::createdAt).reversed()
                        .thenComparing(Post::likeCount).reversed())
                .toList();
    }

    public List<Notification> userNotifications(String userId) {
        return notifications.values().stream()
                .filter(message -> message.userId().equals(userId))
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .toList();
    }

    public List<AdminOperationLog> adminLogs() {
        return adminLogs.values().stream()
                .sorted(Comparator.comparing(AdminOperationLog::createdAt).reversed())
                .toList();
    }

    public List<Report> reports() {
        return reports.values().stream()
                .sorted(Comparator.comparing(Report::createdAt).reversed())
                .toList();
    }

    public List<PatternJob> userPatternJobs(String userId) {
        return patternJobs.values().stream()
                .filter(job -> job.userId().equals(userId))
                .sorted(Comparator.comparing(PatternJob::createdAt).reversed())
                .toList();
    }

    public List<Order> userOrders(String userId) {
        return orders.values().stream()
                .filter(order -> order.buyerId().equals(userId))
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .toList();
    }

    public Map<String, Object> productView(Product product) {
        List<Sku> productSkus = skus.values().stream().filter(sku -> sku.productId().equals(product.id())).toList();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("productId", product.id());
        view.put("type", product.type());
        view.put("sellerId", product.sellerId());
        view.put("title", product.title());
        view.put("description", product.description());
        view.put("categoryId", product.categoryId());
        view.put("categoryName", product.categoryId());
        view.put("status", product.status());
        view.put("auditStatus", product.auditStatus());
        view.put("skus", productSkus.stream().map(this::skuView).toList());
        view.put("swatchColor", 0xFF6B8E7B);
        return view;
    }

    public Map<String, Object> skuView(Sku sku) {
        return Map.of(
                "skuId", sku.id(),
                "productId", sku.productId(),
                "specName", sku.specName(),
                "priceCent", sku.priceCent(),
                "stock", sku.availableStock(),
                "status", sku.status()
        );
    }

    public Map<String, Object> orderView(Order order) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderId", order.id());
        view.put("buyerId", order.buyerId());
        view.put("sellerType", order.sellerType());
        view.put("sellerId", order.sellerId());
        view.put("orderType", order.orderType());
        view.put("status", order.status());
        view.put("totalAmountCent", order.totalAmountCent());
        view.put("payableAmountCent", order.payableAmountCent());
        view.put("addressSnapshot", order.address() != null ? order.address().toString() : "");
        view.put("items", orderItems.getOrDefault(order.id(), List.of()).stream().map(item -> {
            Sku sku = skus.get(item.skuId());
            Product product = products.get(item.productId());
            Map<String, Object> itemView = new LinkedHashMap<>();
            itemView.put("orderItemId", item.id());
            itemView.put("skuId", item.skuId());
            itemView.put("productId", item.productId());
            itemView.put("title", product != null ? product.title() : "");
            itemView.put("specName", sku != null ? sku.specName() : "");
            itemView.put("quantity", item.quantity());
            itemView.put("priceCent", item.priceCent());
            return itemView;
        }).toList());
        return view;
    }

    public synchronized void lockStock(String skuId, int quantity) {
        Sku sku = skus.get(skuId);
        if (sku == null || !"ON_SALE".equals(sku.status())) {
            throw new BizException(ErrorCode.NOT_FOUND, "SKU 不存在");
        }
        if (sku.availableStock() < quantity) {
            throw new BizException(ErrorCode.INVENTORY_NOT_ENOUGH, "库存不足");
        }
        skus.put(skuId, new Sku(sku.id(), sku.productId(), sku.specName(), sku.priceCent(), sku.stock(), sku.lockedStock() + quantity, sku.status()));
    }

    public synchronized void deductLockedStock(String skuId, int quantity) {
        Sku sku = skus.get(skuId);
        if (sku == null) {
            return;
        }
        skus.put(skuId, new Sku(sku.id(), sku.productId(), sku.specName(), sku.priceCent(), sku.stock() - quantity, Math.max(0, sku.lockedStock() - quantity), sku.status()));
    }

    private void seedProducts() {
        Instant now = Instant.now();
        Product bead = new Product("prod_bead_red", "SELF_OPERATED", null, "2.6mm 豆子豆沙红", "自营常用色拼豆", "beads", "ON_SALE", "PASS", now);
        Product white = new Product("prod_bead_white", "SELF_OPERATED", null, "2.6mm 豆子奶油白", "自营常用色拼豆", "beads", "ON_SALE", "PASS", now);
        products.put(bead.id(), bead);
        products.put(white.id(), white);
        skus.put("sku_bead_red", new Sku("sku_bead_red", bead.id(), "1000颗", 1200, 100, 0, "ON_SALE"));
        skus.put("sku_bead_white", new Sku("sku_bead_white", white.id(), "1000颗", 1200, 100, 0, "ON_SALE"));
    }

    private void seedVisiblePost() {
        Instant now = Instant.now();
        posts.put("post_seed_1", new Post("post_seed_1", "system", "新手拼豆入门", "欢迎来到豆屿拼豆社区",
                List.of(), List.of(), null, "VISIBLE", 10, 5, 0, true, now));
    }
}
