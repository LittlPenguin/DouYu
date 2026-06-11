package cn.edu.app.douyu.server.common.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
/**
 * Like Repository：访问 `likes` 表，提供通用点赞关系的 JPA 查询。
 */

public interface LikeRepository extends JpaRepository<LikeEntity, String> {
    Optional<LikeEntity> findByUserIdAndTargetTypeAndTargetId(String userId, String targetType, String targetId);
    List<LikeEntity> findByUserIdAndTargetTypeOrderByCreatedAtDesc(String userId, String targetType);
    long countByTargetTypeAndTargetId(String targetType, String targetId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LikeEntity l WHERE l.userId = :userId AND l.targetType = :targetType AND l.targetId = :targetId")
    void deleteByUserIdAndTargetTypeAndTargetId(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);
}
