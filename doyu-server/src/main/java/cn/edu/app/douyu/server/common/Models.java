package cn.edu.app.douyu.server.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;
/**
 * API 模型集合：集中定义后端 Controller 返回给 Android 的轻量 record。
 */

public final class Models {
    private Models() {
    }

    public record User(String id, String phone, String email, String nickname, String avatarFileId, String bio,
                       String region, String ageGroup, boolean isMinor, String realNameStatus, String accountStatus,
                       boolean allowRecommendation, boolean allowFavorites,
                       boolean notifyInteractions, boolean notifyPublish, boolean notifySystem,
                       Instant createdAt, Instant updatedAt) {
    }

    public record FileAsset(String id, String ownerId, String usage, String storageKey, String mimeType,
                            long sizeBytes, Integer width, Integer height, String auditStatus, String publicUrl,
                            Instant createdAt) {
    }

    public record Post(String id, String authorId, String title, String content,
                       List<String> mediaFileIds, List<String> topicIds,
                       String status, int likeCount, int favoriteCount, int commentCount,
                       boolean pinned, Instant createdAt) {
    }

    public record Comment(String id, String postId, String authorId, String parentId, String content,
                          String status, Instant createdAt) {
    }

    public record Product(String id, String type, String sellerId, String title, String description,
                          String categoryId, String status, String auditStatus, Instant createdAt) {
    }

    public record Sku(String id, String productId, String specName, int priceCent, int stock,
                      int lockedStock, String status) {
        public int availableStock() {
            return stock - lockedStock;
        }
    }

    public record CartItem(String id, String userId, String skuId, int quantity) {
    }

    public record Order(String id, String buyerId, String sellerType, String sellerId, String orderType,
                        String status, int totalAmountCent, int payableAmountCent, Map<String, Object> address,
                        Instant expiresAt, Instant createdAt) {
    }

    public record OrderItem(String id, String orderId, String skuId, String productId, int quantity, int priceCent) {
    }

    public record Notification(String id, String userId, String type, String title, String content, Instant readAt,
                               Instant createdAt) {
    }

}
