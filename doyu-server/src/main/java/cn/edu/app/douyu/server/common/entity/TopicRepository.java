package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<TopicEntity, String> {
    List<TopicEntity> findByNameContainingIgnoreCaseOrderByPostCountDescNameAsc(String keyword);
    List<TopicEntity> findAllByOrderByPostCountDescNameAsc();
}
