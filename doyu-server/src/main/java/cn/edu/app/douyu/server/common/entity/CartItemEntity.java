package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * CartItem 实体：映射 `cart_items` 表，保存用户购物车条目。
 */
@Entity
@Table(name = "cart_items")
public class CartItemEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_id", length = 64, nullable = false) private String userId;
    @Column(name = "sku_id", length = 64, nullable = false) private String skuId;
    @Column(name = "quantity", nullable = false) private int quantity;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public CartItemEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getSkuId() { return skuId; } public void setSkuId(String skuId) { this.skuId = skuId; }
    public int getQuantity() { return quantity; } public void setQuantity(int quantity) { this.quantity = quantity; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
