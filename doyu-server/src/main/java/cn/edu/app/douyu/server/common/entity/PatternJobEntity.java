package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pattern_jobs")
public class PatternJobEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_id", length = 64, nullable = false) private String userId;
    @Column(name = "input_file_id", length = 64, nullable = false) private String inputFileId;
    @Column(name = "bead_size", length = 32, nullable = false) private String beadSize;
    @Column(name = "target_size", length = 64, nullable = false) private String targetSize;
    @Column(name = "difficulty", length = 32, nullable = false) private String difficulty;
    @Column(name = "palette_id", length = 64, nullable = false) private String paletteId;
    @Column(name = "style", length = 32, nullable = false) private String style;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "failure_reason", length = 500) private String failureReason;
    @Column(name = "pattern_id", length = 64) private String patternId;
    @Column(name = "retryable") private boolean retryable;
    @Column(name = "quota_refunded") private boolean quotaRefunded;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public PatternJobEntity() {}

    public PatternJobEntity(String id, String userId, String inputFileId, String beadSize, String targetSize,
                            String difficulty, String paletteId, String style, String status, String failureReason,
                            String patternId, boolean retryable, boolean quotaRefunded, Instant createdAt, Instant updatedAt) {
        this.id = id; this.userId = userId; this.inputFileId = inputFileId; this.beadSize = beadSize;
        this.targetSize = targetSize; this.difficulty = difficulty; this.paletteId = paletteId; this.style = style;
        this.status = status; this.failureReason = failureReason; this.patternId = patternId;
        this.retryable = retryable; this.quotaRefunded = quotaRefunded; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getInputFileId() { return inputFileId; } public void setInputFileId(String inputFileId) { this.inputFileId = inputFileId; }
    public String getBeadSize() { return beadSize; } public void setBeadSize(String beadSize) { this.beadSize = beadSize; }
    public String getTargetSize() { return targetSize; } public void setTargetSize(String targetSize) { this.targetSize = targetSize; }
    public String getDifficulty() { return difficulty; } public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getPaletteId() { return paletteId; } public void setPaletteId(String paletteId) { this.paletteId = paletteId; }
    public String getStyle() { return style; } public void setStyle(String style) { this.style = style; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getFailureReason() { return failureReason; } public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getPatternId() { return patternId; } public void setPatternId(String patternId) { this.patternId = patternId; }
    public boolean isRetryable() { return retryable; } public void setRetryable(boolean retryable) { this.retryable = retryable; }
    public boolean isQuotaRefunded() { return quotaRefunded; } public void setQuotaRefunded(boolean quotaRefunded) { this.quotaRefunded = quotaRefunded; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
