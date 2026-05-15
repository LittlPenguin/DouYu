package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reports")
public class ReportEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "reporter_id", length = 64, nullable = false) private String reporterId;
    @Column(name = "target_type", length = 32, nullable = false) private String targetType;
    @Column(name = "target_id", length = 64, nullable = false) private String targetId;
    @Column(name = "reason", length = 120, nullable = false) private String reason;
    @Column(name = "description", length = 1000) private String description;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public ReportEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getReporterId() { return reporterId; } public void setReporterId(String reporterId) { this.reporterId = reporterId; }
    public String getTargetType() { return targetType; } public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; } public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
