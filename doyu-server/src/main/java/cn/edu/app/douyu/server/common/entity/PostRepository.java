package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, String> {
    List<PostEntity> findByStatusOrderByPinnedDescCreatedAtDesc(String status);
    List<PostEntity> findByAuthorIdInAndStatusOrderByCreatedAtDesc(List<String> authorIds, String status);
    List<PostEntity> findByAuthorIdInOrderByCreatedAtDesc(List<String> authorIds);
    List<PostEntity> findByStatusAndTopicIdsContainingOrderByCreatedAtDesc(String status, String topicId);
    List<PostEntity> findByStatusInOrderByPinnedDescCreatedAtDesc(List<String> statuses);
    List<PostEntity> findByAuthorIdInAndStatusInOrderByCreatedAtDesc(List<String> authorIds, List<String> statuses);
    List<PostEntity> findByStatusInAndTopicIdsContainingOrderByCreatedAtDesc(List<String> statuses, String topicId);
    Page<PostEntity> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
}
