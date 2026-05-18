package cn.edu.app.douyu.server.pattern;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_usage")
public class AiUsageEntity {
    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Column(name = "job_id", length = 50)
    private String jobId;

    @Column(name = "cost_cents")
    private long costCents;

    @Column(name = "created_at")
    private Instant createdAt;

    public AiUsageEntity() {}

    public AiUsageEntity(String id, String userId, String jobId, long costCents, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.costCents = costCents;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public long getCostCents() { return costCents; }
    public void setCostCents(long costCents) { this.costCents = costCents; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
