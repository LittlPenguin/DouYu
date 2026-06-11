package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * CommentTopic 实体：映射 `comment_topics` 表，保存评论话题关系。
 */
@Entity
@Table(name = "comment_topics")
public class CommentTopicEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;
    @Column(name = "comment_id", length = 64, nullable = false)
    private String commentId;
    @Column(name = "topic_id", length = 64, nullable = false)
    private String topicId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CommentTopicEntity() {}

    public CommentTopicEntity(String id, String commentId, String topicId, Instant createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.topicId = topicId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCommentId() { return commentId; }
    public String getTopicId() { return topicId; }
    public Instant getCreatedAt() { return createdAt; }
}
