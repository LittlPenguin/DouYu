package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reward_accounts")
public class RewardAccountEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_id", length = 64, nullable = false, unique = true) private String userId;
    @Column(name = "points", nullable = false) private int points;
    @Column(name = "experience", nullable = false) private int experience;
    @Column(name = "level_code", length = 32, nullable = false) private String levelCode;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public RewardAccountEntity() {}

    public RewardAccountEntity(String id, String userId, int points, int experience, String levelCode, Instant createdAt, Instant updatedAt) {
        this.id = id; this.userId = userId; this.points = points; this.experience = experience;
        this.levelCode = levelCode; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public int getPoints() { return points; } public void setPoints(int points) { this.points = points; }
    public int getExperience() { return experience; } public void setExperience(int experience) { this.experience = experience; }
    public String getLevelCode() { return levelCode; } public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    public int getLevel() {
        try { return Integer.parseInt(levelCode.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 1; }
    }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
