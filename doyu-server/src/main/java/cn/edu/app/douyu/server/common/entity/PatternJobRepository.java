package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatternJobRepository extends JpaRepository<PatternJobEntity, String> {
    List<PatternJobEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
