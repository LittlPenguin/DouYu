package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
/**
 * IdempotencyRecord Repository：访问 `idempotency_records` 表，提供幂等请求记录的 JPA 查询。
 */

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, String> {
    Optional<IdempotencyRecordEntity> findByUserIdAndIdempotencyKeyAndOperation(String userId, String idempotencyKey, String operation);
}
