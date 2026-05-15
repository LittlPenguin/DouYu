package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, String> {
    List<PostEntity> findByStatusOrderByPinnedDescCreatedAtDesc(String status);
    List<PostEntity> findByAuthorIdInAndStatusOrderByCreatedAtDesc(List<String> authorIds, String status);
}
