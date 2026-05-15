package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLogEntity, String> {
    List<AdminOperationLogEntity> findAllByOrderByCreatedAtDesc();
}
