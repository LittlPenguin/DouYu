package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * CommentMention 实体：映射 `comment_mentions` 表，保存评论提及用户关系。
 */
@Entity
@Table(name = "comment_mentions")
public class CommentMentionEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;
    @Column(name = "comment_id", length = 64, nullable = false)
    private String commentId;
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CommentMentionEntity() {}

    public CommentMentionEntity(String id, String commentId, String userId, Instant createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCommentId() { return commentId; }
    public String getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
}
