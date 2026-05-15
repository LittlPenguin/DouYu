package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, String> {
    List<CommentEntity> findByPostIdAndStatusNotOrderByCreatedAtAsc(String postId, String status);
    long countByPostIdAndStatusNot(String postId, String status);
}
