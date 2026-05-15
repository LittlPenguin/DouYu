package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "admin_users")
public class AdminUserEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "username", length = 80, nullable = false, unique = true) private String username;
    @Column(name = "password_hash", length = 128, nullable = false) private String passwordHash;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public AdminUserEntity() {}

    public AdminUserEntity(String id, String username, String passwordHash, String status, Instant createdAt, Instant updatedAt) {
        this.id = id; this.username = username; this.passwordHash = passwordHash; this.status = status;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
