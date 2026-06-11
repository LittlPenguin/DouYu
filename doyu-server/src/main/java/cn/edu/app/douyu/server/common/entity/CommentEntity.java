package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Comment 实体：映射 `comments` 表，保存帖子评论。
 */
@Entity
@Table(name = "comments")
public class CommentEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "post_id", length = 64, nullable = false)
    private String postId;

    @Column(name = "author_id", length = 64, nullable = false)
    private String authorId;

    @Column(name = "parent_id", length = 64)
    private String parentId;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @Column(name = "media_file_ids", length = 1200)
    private String mediaFileIds;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CommentEntity() {}

    public CommentEntity(String id, String postId, String authorId, String parentId,
                         String content, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.content = content;
        this.mediaFileIds = null;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMediaFileIds() { return mediaFileIds; }
    public void setMediaFileIds(String mediaFileIds) { this.mediaFileIds = mediaFileIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
