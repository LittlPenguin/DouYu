package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class PostEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "author_id", length = 64, nullable = false)
    private String authorId;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "content", length = 3000, nullable = false)
    private String content;

    @Column(name = "media_file_ids", length = 1000)
    private String mediaFileIds;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "topic_ids", length = 1000)
    private String topicIds;

    @Column(name = "linked_pattern_id", length = 64)
    private String linkedPatternId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "favorite_count", nullable = false)
    private int favoriteCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "pinned", nullable = false)
    private boolean pinned;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PostEntity() {}

    public PostEntity(String id, String authorId, String title, String content,
                      String mediaFileIds, String topicIds, String linkedPatternId,
                      String status, int likeCount, int favoriteCount, int commentCount,
                      boolean pinned, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.mediaFileIds = mediaFileIds;
        this.topicIds = topicIds;
        this.linkedPatternId = linkedPatternId;
        this.status = status;
        this.likeCount = likeCount;
        this.favoriteCount = favoriteCount;
        this.commentCount = commentCount;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMediaFileIds() { return mediaFileIds; }
    public void setMediaFileIds(String mediaFileIds) { this.mediaFileIds = mediaFileIds; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getTopicIds() { return topicIds; }
    public void setTopicIds(String topicIds) { this.topicIds = topicIds; }
    public String getLinkedPatternId() { return linkedPatternId; }
    public void setLinkedPatternId(String linkedPatternId) { this.linkedPatternId = linkedPatternId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
