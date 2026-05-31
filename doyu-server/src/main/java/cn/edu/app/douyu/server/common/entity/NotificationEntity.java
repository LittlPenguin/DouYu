package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class NotificationEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "user_id", length = 64, nullable = false) private String userId;
    @Column(name = "conversation_id", length = 64) private String conversationId;
    @Column(name = "sender_id", length = 64) private String senderId;
    @Column(name = "recipient_id", length = 64) private String recipientId;
    @Column(name = "type", length = 32, nullable = false) private String type;
    @Column(name = "title", length = 160, nullable = false) private String title;
    @Column(name = "content", length = 2000, nullable = false) private String content;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public NotificationEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getConversationId() { return conversationId; } public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getSenderId() { return senderId; } public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; } public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public Instant getReadAt() { return readAt; } public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
