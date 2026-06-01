package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentTopicRepository extends JpaRepository<CommentTopicEntity, String> {
    List<CommentTopicEntity> findByCommentId(String commentId);
}
