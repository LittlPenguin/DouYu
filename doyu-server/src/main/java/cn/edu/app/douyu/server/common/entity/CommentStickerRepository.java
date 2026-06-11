package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * CommentSticker Repository：访问 `comment_stickers` 表，提供评论贴纸关系的 JPA 查询。
 */

public interface CommentStickerRepository extends JpaRepository<CommentStickerEntity, String> {
    List<CommentStickerEntity> findByCommentId(String commentId);
}
