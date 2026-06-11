package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * CommentMention Repository：访问 `comment_mentions` 表，提供评论提及用户关系的 JPA 查询。
 */

public interface CommentMentionRepository extends JpaRepository<CommentMentionEntity, String> {
    List<CommentMentionEntity> findByCommentId(String commentId);
}
