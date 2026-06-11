package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * CommentSticker 实体：映射 `comment_stickers` 表，保存评论贴纸关系。
 */
@Entity
@Table(name = "comment_stickers")
public class CommentStickerEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;
    @Column(name = "comment_id", length = 64, nullable = false)
    private String commentId;
    @Column(name = "sticker_id", length = 64, nullable = false)
    private String stickerId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CommentStickerEntity() {}

    public CommentStickerEntity(String id, String commentId, String stickerId, Instant createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.stickerId = stickerId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCommentId() { return commentId; }
    public String getStickerId() { return stickerId; }
    public Instant getCreatedAt() { return createdAt; }
}
