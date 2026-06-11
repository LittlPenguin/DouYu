package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * IdempotencyRecord 实体：映射 `idempotency_records` 表，保存幂等请求记录。
 */
@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecordEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_id", length = 64) private String userId;
    @Column(name = "idempotency_key", length = 160, nullable = false) private String idempotencyKey;
    @Column(name = "operation", length = 64, nullable = false) private String operation;
    @Column(name = "request_hash", length = 128, nullable = false) private String requestHash;
    @Column(name = "response_body", columnDefinition = "text", nullable = false) private String responseBody;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public IdempotencyRecordEntity() {}

    public IdempotencyRecordEntity(String id, String userId, String idempotencyKey, String operation,
                                   String requestHash, String responseBody, Instant createdAt, Instant updatedAt) {
        this.id = id; this.userId = userId; this.idempotencyKey = idempotencyKey; this.operation = operation;
        this.requestHash = requestHash; this.responseBody = responseBody; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getIdempotencyKey() { return idempotencyKey; } public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getOperation() { return operation; } public void setOperation(String operation) { this.operation = operation; }
    public String getRequestHash() { return requestHash; } public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
    public String getResponseBody() { return responseBody; } public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
