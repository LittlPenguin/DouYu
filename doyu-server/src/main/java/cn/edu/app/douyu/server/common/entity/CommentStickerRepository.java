package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentStickerRepository extends JpaRepository<CommentStickerEntity, String> {
    List<CommentStickerEntity> findByCommentId(String commentId);
}
