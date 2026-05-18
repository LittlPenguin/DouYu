package cn.edu.app.douyu.server.pattern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AiUsageRepository extends JpaRepository<AiUsageEntity, String> {

    @Query("SELECT COUNT(u) FROM AiUsageEntity u WHERE u.userId = :userId AND u.createdAt > :since")
    long countByUserIdSince(@Param("userId") String userId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.costCents), 0) FROM AiUsageEntity u WHERE u.userId = :userId AND u.createdAt > :since")
    long sumCostCentsByUserIdSince(@Param("userId") String userId, @Param("since") Instant since);
}
