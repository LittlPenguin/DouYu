package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * OrderItem 实体：映射 `order_items` 表，保存订单明细。
 */
@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "order_id", length = 64, nullable = false) private String orderId;
    @Column(name = "sku_id", length = 64, nullable = false) private String skuId;
    @Column(name = "product_id", length = 64, nullable = false) private String productId;
    @Column(name = "quantity", nullable = false) private int quantity;
    @Column(name = "price_cent", nullable = false) private int priceCent;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public OrderItemEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; } public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSkuId() { return skuId; } public void setSkuId(String skuId) { this.skuId = skuId; }
    public String getProductId() { return productId; } public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; } public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getPriceCent() { return priceCent; } public void setPriceCent(int priceCent) { this.priceCent = priceCent; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
