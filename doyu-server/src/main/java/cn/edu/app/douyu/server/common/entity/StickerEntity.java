package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Sticker 实体：映射 `stickers` 表，保存贴纸。
 */
@Entity
@Table(name = "stickers")
public class StickerEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;
    @Column(name = "pack_id", length = 64, nullable = false)
    private String packId;
    @Column(name = "name", length = 80, nullable = false)
    private String name;
    @Column(name = "emoji_text", length = 80)
    private String emojiText;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StickerEntity() {}

    public StickerEntity(String id, String packId, String name, String emojiText, String imageUrl, int sortOrder, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.packId = packId;
        this.name = name;
        this.emojiText = emojiText;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getPackId() { return packId; }
    public String getName() { return name; }
    public String getEmojiText() { return emojiText; }
    public String getImageUrl() { return imageUrl; }
    public int getSortOrder() { return sortOrder; }
}
