package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * CommentTopic Repository：访问 `comment_topics` 表，提供评论话题关系的 JPA 查询。
 */

public interface CommentTopicRepository extends JpaRepository<CommentTopicEntity, String> {
    List<CommentTopicEntity> findByCommentId(String commentId);
}
