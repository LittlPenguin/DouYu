package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "conversations")
public class ConversationEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_a_id", length = 64, nullable = false) private String userAId;
    @Column(name = "user_b_id", length = 64, nullable = false) private String userBId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public ConversationEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserAId() { return userAId; } public void setUserAId(String userAId) { this.userAId = userAId; }
    public String getUserBId() { return userBId; } public void setUserBId(String userBId) { this.userBId = userBId; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
