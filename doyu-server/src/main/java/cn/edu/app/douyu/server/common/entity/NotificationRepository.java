package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.readAt = :readAt, n.updatedAt = :readAt WHERE n.userId = :userId AND n.readAt IS NULL")
    void markAllRead(@Param("userId") String userId, @Param("readAt") Instant readAt);
}
