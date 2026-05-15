package cn.edu.app.douyu.server.common;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Models {
    private Models() {
    }

    public record User(String id, String phone, String nickname, String avatarFileId, String bio,
                       String ageGroup, boolean isMinor, String realNameStatus, String accountStatus,
                       Instant createdAt, Instant updatedAt) {
    }

    public record RefreshTokenRecord(String id, String userId, String tokenHash, boolean revoked, Instant expiresAt) {
    }

    public record AdminUser(String id, String username, String passwordHash, String status) {
    }

    public record FileAsset(String id, String ownerId, String usage, String storageKey, String mimeType,
                            long sizeBytes, Integer width, Integer height, String auditStatus, String publicUrl,
                            Instant createdAt) {
    }

    public record Post(String id, String authorId, String title, String content,
                       List<String> mediaFileIds, List<String> topicIds, String linkedPatternId,
                       String status, int likeCount, int favoriteCount, int commentCount,
                       boolean pinned, Instant createdAt) {
    }

    public record Comment(String id, String postId, String authorId, String parentId, String content,
                          String status, Instant createdAt) {
    }

    public record PatternJob(String id, String userId, String inputFileId, String beadSize, String targetSize,
                             String difficulty, String paletteId, String style, String status,
                             String failureReason, String patternId, boolean retryable, boolean quotaRefunded,
                             Instant createdAt, Instant updatedAt) {
    }

    public record PatternAsset(String id, String jobId, String ownerId, String previewFileId, String gridFileId,
                               String colorMapFileId, String pdfFileId, String beadSize, int widthCells,
                               int heightCells, int totalBeads, Map<String, Object> materials, String status) {
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

    public record Payment(String id, String orderId, String channel, String status, int amountCent,
                          String channelTradeNo, Instant paidAt) {
    }

    public record Refund(String id, String orderId, String paymentId, int amountCent, String reason, String status) {
    }

    public record Notification(String id, String userId, String type, String title, String content, Instant readAt,
                               Instant createdAt) {
    }

    public record Conversation(String id, String userAId, String userBId, Instant createdAt) {
    }

    public record RewardAccount(String id, String userId, int points, int experience, String levelCode) {
    }

    public record CheckinRecord(String id, String userId, LocalDate checkinDate) {
    }

    public record Report(String id, String reporterId, String targetType, String targetId, String reason,
                         String description, String status, Instant createdAt) {
    }

    public record ModerationRecord(String id, String targetType, String targetId, String result, String reason,
                                   String operatorType, Instant createdAt) {
    }

    public record AdminOperationLog(String id, String adminId, String action, String targetType, String targetId,
                                    String beforeState, String afterState, String reason, Instant createdAt) {
    }

    public static Map<String, Object> materials(int totalBeads) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalBeads", totalBeads);
        data.put("colors", java.util.List.of(
                Map.of("colorCode", "R01", "displayName", "豆沙红", "beadCount", 128, "skuId", "sku_bead_red"),
                Map.of("colorCode", "W01", "displayName", "奶油白", "beadCount", 128, "skuId", "sku_bead_white")
        ));
        return data;
    }
}
