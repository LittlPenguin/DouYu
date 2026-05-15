package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, String> {
    Optional<IdempotencyRecordEntity> findByUserIdAndIdempotencyKeyAndOperation(String userId, String idempotencyKey, String operation);
}
