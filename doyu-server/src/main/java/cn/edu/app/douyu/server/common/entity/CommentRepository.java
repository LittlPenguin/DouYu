package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Comment Repository：访问 `comments` 表，提供帖子评论的 JPA 查询。
 */

public interface CommentRepository extends JpaRepository<CommentEntity, String> {
    List<CommentEntity> findByPostIdAndStatusNotOrderByCreatedAtAsc(String postId, String status);
    List<CommentEntity> findByAuthorIdAndStatusNotOrderByCreatedAtDesc(String authorId, String status);
    long countByPostIdAndStatusNot(String postId, String status);
}
