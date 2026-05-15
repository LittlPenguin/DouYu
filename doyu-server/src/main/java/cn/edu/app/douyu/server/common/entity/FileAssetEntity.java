package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "file_assets")
public class FileAssetEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "owner_id", length = 64, nullable = false) private String ownerId;
    @Column(name = "usage", length = 32, nullable = false) private String usage;
    @Column(name = "storage_key", length = 255, nullable = false, unique = true) private String storageKey;
    @Column(name = "mime_type", length = 120, nullable = false) private String mimeType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Column(name = "width") private Integer width;
    @Column(name = "height") private Integer height;
    @Column(name = "audit_status", length = 32, nullable = false) private String auditStatus;
    @Column(name = "public_url", length = 500) private String publicUrl;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public FileAssetEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; } public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getUsage() { return usage; } public void setUsage(String usage) { this.usage = usage; }
    public String getStorageKey() { return storageKey; } public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMimeType() { return mimeType; } public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSizeBytes() { return sizeBytes; } public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public Integer getWidth() { return width; } public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; } public void setHeight(Integer height) { this.height = height; }
    public String getAuditStatus() { return auditStatus; } public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getPublicUrl() { return publicUrl; } public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
