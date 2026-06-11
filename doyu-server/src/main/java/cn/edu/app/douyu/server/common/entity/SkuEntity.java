package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Sku 实体：映射 `skus` 表，保存商品 SKU 库存和价格。
 */
@Entity
@Table(name = "skus")
public class SkuEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "product_id", length = 64, nullable = false) private String productId;
    @Column(name = "spec_name", length = 120, nullable = false) private String specName;
    @Column(name = "price_cent", nullable = false) private int priceCent;
    @Column(name = "stock", nullable = false) private int stock;
    @Column(name = "locked_stock", nullable = false) private int lockedStock;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public SkuEntity() {}

    public int getAvailableStock() { return stock - lockedStock; }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; } public void setProductId(String productId) { this.productId = productId; }
    public String getSpecName() { return specName; } public void setSpecName(String specName) { this.specName = specName; }
    public int getPriceCent() { return priceCent; } public void setPriceCent(int priceCent) { this.priceCent = priceCent; }
    public int getStock() { return stock; } public void setStock(int stock) { this.stock = stock; }
    public int getLockedStock() { return lockedStock; } public void setLockedStock(int lockedStock) { this.lockedStock = lockedStock; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
