package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMentionEntity, String> {
    List<CommentMentionEntity> findByCommentId(String commentId);
}
