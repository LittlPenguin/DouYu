package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "type", length = 32, nullable = false) private String type;
    @Column(name = "seller_id", length = 64) private String sellerId;
    @Column(name = "title", length = 160, nullable = false) private String title;
    @Column(name = "description", length = 3000) private String description;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(name = "category_id", length = 64) private String categoryId;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "audit_status", length = 32, nullable = false) private String auditStatus;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public ProductEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getSellerId() { return sellerId; } public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategoryId() { return categoryId; } public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getAuditStatus() { return auditStatus; } public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
