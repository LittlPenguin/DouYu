package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 * Topic Repository：访问 `topics` 表，提供社区话题的 JPA 查询。
 */

public interface TopicRepository extends JpaRepository<TopicEntity, String> {
    List<TopicEntity> findByNameContainingIgnoreCaseOrderByPostCountDescNameAsc(String keyword);
    List<TopicEntity> findAllByOrderByPostCountDescNameAsc();
}
