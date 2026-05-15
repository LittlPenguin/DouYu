package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "admin_operation_logs")
public class AdminOperationLogEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "admin_id", length = 64, nullable = false) private String adminId;
    @Column(name = "action", length = 80, nullable = false) private String action;
    @Column(name = "target_type", length = 32, nullable = false) private String targetType;
    @Column(name = "target_id", length = 64, nullable = false) private String targetId;
    @Column(name = "before_state", length = 120) private String beforeState;
    @Column(name = "after_state", length = 120) private String afterState;
    @Column(name = "reason", length = 500) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public AdminOperationLogEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getAdminId() { return adminId; } public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; } public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; } public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getBeforeState() { return beforeState; } public void setBeforeState(String beforeState) { this.beforeState = beforeState; }
    public String getAfterState() { return afterState; } public void setAfterState(String afterState) { this.afterState = afterState; }
    public String getReason() { return reason; } public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
