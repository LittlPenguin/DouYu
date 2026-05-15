package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "checkin_records")
public class CheckinRecordEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_id", length = 64, nullable = false) private String userId;
    @Column(name = "checkin_date", nullable = false) private LocalDate checkinDate;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public CheckinRecordEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public LocalDate getCheckinDate() { return checkinDate; } public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
